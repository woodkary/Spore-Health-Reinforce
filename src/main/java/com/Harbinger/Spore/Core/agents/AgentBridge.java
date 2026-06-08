package com.Harbinger.Spore.Core.agents;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class AgentBridge implements IAgentBridge{
    private static final String AGENT_RESOURCE = "/sporeAgent.jar";
    private static final String AGENT_CLASS_ENTRY = "SporeAgent.class";

    public static final IAgentBridge INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IAgentBridge.class,
            AgentBridge.class
    );

    private MethodHandle methodHandleGetProperty1;
    private MethodHandle methodHandleGetProperty2;

    @Override
    public void loadAgent() {
        String pid = getPid();
        if (pid == null || pid.isEmpty()) {
            LogUtil.error("Failed to resolve current JVM pid for attach.");
            return;
        }

        VirtualMachine vm = null;

        try {
            allowAttach();
            String agentJarPath = resolveCompatibleAgentJarPathForAttach();
            if (agentJarPath == null || agentJarPath.isEmpty()) {
                LogUtil.error("Failed to resolve compatible agent jar for attach.");
                return;
            }
            LogUtil.log("Attaching agent to running JVM...");
            vm = VirtualMachine.attach(pid);
            // Always pass agentArgs as jar path for compatibility with agents
            // that expect their own jar path in agentmain/premain arg.
            vm.loadAgent(agentJarPath, agentJarPath);
            if (InstrumentationUtil.INSTANCE == null) {
                probeInstrumentationWithoutAttach();
            }
            LogUtil.log("Attached agent to running JVM.");
            if (InstrumentationUtil.INSTANCE != null) {
                LogUtil.log("Instrumentation enabled.");
            } else {
                LogUtil.error("Instrumentation wasn't set by agent.");
            }
        } catch (Exception var8) {
            LogUtil.errorf("Failed to attach agent to running JVM.",var8);
            LogUtil.printStackTrace(var8);
        }finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (Throwable ignored) {
                }
            }
            LogUtil.log("Agent attach finished.");
            LogUtil.flush();
        }
    }

    private String getPid() {
        try {
            String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
            int p = nameOfRunningVM.indexOf("@");
            return p > 0 ? nameOfRunningVM.substring(0, p) : nameOfRunningVM;
        } catch (Throwable t) {
            LogUtil.errorf("Failed to get JVM pid: %s", t.getMessage());
            return null;
        }
    }

    private void allowAttach() {
        LogUtil.log("Starting self agent attach...");
        try {
            Field f = Class.forName("sun.tools.attach.HotSpotVirtualMachine").getDeclaredField("ALLOW_ATTACH_SELF");
            ClassUtil.setFieldValue(f, (Object)null, true);
            LogUtil.log("Self agent attach allowed.");
        } catch (Throwable t) {
            LogUtil.error("Failed to allow self agent attach.");
            LogUtil.printStackTrace(t);
        } finally {
            LogUtil.log("Self agent attach finished.");
            LogUtil.flush();
        }
    }

    private String resolveCompatibleAgentJarPathForAttach() {
        try {
            File agent = createBundledAttachAgentJar();
            if (agent != null) {
                return agent.getAbsolutePath();
            }
        } catch (Throwable t) {
            LogUtil.errorf("Failed to prepare bundled attach agent jar: %s", t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private File createBundledAttachAgentJar() {
        InputStream is = AgentBridge.class.getResourceAsStream(AGENT_RESOURCE);
        if (is == null) {
            LogUtil.error("Attach agent resource " + AGENT_RESOURCE + " is missing.");
            return null;
        }
        try (InputStream closeable = is) {
            Path dir = Paths.get(systemGetProperty("java.io.tmpdir"), "spore_attach");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path file = dir.resolve("sporeAgent_" + System.currentTimeMillis() + ".jar");
            Files.copy(closeable, file, StandardCopyOption.REPLACE_EXISTING);

            int runtimeMajor = getCurrentRuntimeClassMajor();
            int agentMajor = readClassMajorFromJar(file.toFile(), AGENT_CLASS_ENTRY);
            if (agentMajor > 0 && runtimeMajor > 0 && agentMajor > runtimeMajor) {
                LogUtil.errorf("Bundled attach agent class version is too new for current JVM. runtimeMajor=%d, agentMajor=%d.",
                        runtimeMajor, agentMajor);
                try {
                    Files.deleteIfExists(file);
                } catch (Throwable ignored) {
                }
                return null;
            }
            return file.toFile();
        } catch (Throwable t) {
            LogUtil.errorf("Failed to copy bundled attach agent jar: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private int readClassMajorFromJar(File jarFile, String classEntryName) {
        if (jarFile == null || classEntryName == null) {
            return -1;
        }
        try (JarFile jf = new JarFile(jarFile)) {
            JarEntry entry = jf.getJarEntry(classEntryName);
            if (entry == null) {
                return -1;
            }
            try (InputStream is = jf.getInputStream(entry)) {
                byte[] header = is.readNBytes(8);
                if (header.length < 8) {
                    return -1;
                }
                int magic = ((header[0] & 0xFF) << 24)
                        | ((header[1] & 0xFF) << 16)
                        | ((header[2] & 0xFF) << 8)
                        | (header[3] & 0xFF);
                if (magic != 0xCAFEBABE) {
                    return -1;
                }
                return ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
            }
        } catch (Throwable t) {
            LogUtil.errorf("Failed to read class major version from jar %s: %s", jarFile, t.getMessage());
            return -1;
        }
    }

    private int getCurrentRuntimeClassMajor() {
        try {
            int feature = Runtime.version().feature();
            if (feature > 0) {
                return feature + 44;
            }
        } catch (Throwable ignored) {
        }
        try {
            String classVersion = systemGetProperty("java.class.version");
            if (classVersion != null) {
                int dot = classVersion.indexOf('.');
                String major = dot >= 0 ? classVersion.substring(0, dot) : classVersion;
                return Integer.parseInt(major);
            }
        } catch (Throwable ignored) {
        }
        return -1;
    }

    private void probeInstrumentationWithoutAttach() {
        if (InstrumentationUtil.INSTANCE != null) {
            return;
        }
        IInstrumentations instrumentation = getInstrumentationFromClassLoader(Thread.currentThread().getContextClassLoader(), "context");
        if (instrumentation != null) {
            InstrumentationUtil.INSTANCE = instrumentation;
            return;
        }
        instrumentation = getInstrumentationFromClassLoader(ClassLoader.getSystemClassLoader(), "system");
        if (instrumentation != null) {
            InstrumentationUtil.INSTANCE = instrumentation;
        }
    }

    private IInstrumentations getInstrumentationFromClassLoader(ClassLoader loader, String source) {
        if (loader == null) {
            return null;
        }
        try {
            Class<?> instrumentationUtilClass = Class.forName(
                    "com.Harbinger.Spore.Core.agents.InstrumentationUtil",
                    false,
                    loader);
            Field instanceField = instrumentationUtilClass.getDeclaredField("INSTANCE");
            Object res = ClassUtil.getFieldValue(instanceField, (Object)null);
            if (res == null) {
                return null;
            }
            if (res instanceof IInstrumentations instrumentation) {
                LogUtil.log("found InstrumentationUtil from source " + source);
                return instrumentation;
            }
            Instrumentation instrumentation = extractInstrumentation(res);
            if (instrumentation != null) {
                LogUtil.log("found cross-loader InstrumentationUtil from source " + source + ", rebuilding local wrapper");
                InstrumentationUtil.setInstrumentationForAgentBridge(instrumentation);
                return InstrumentationUtil.INSTANCE;
            }
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            LogUtil.errorf("failed to find InstrumentationUtil in loader %s,%s", loader, e.getMessage());
        } catch (Throwable t) {
            LogUtil.errorf("failed to read cross-loader InstrumentationUtil in loader %s,%s", loader, t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private Instrumentation extractInstrumentation(Object wrapperObject) {
        if (wrapperObject == null) {
            return null;
        }
        try {
            Method unpack = wrapperObject.getClass().getDeclaredMethod("unpack");
            Object result = unpack.invoke(wrapperObject);
            if (result instanceof Instrumentation instrumentation) {
                return instrumentation;
            }
        } catch (Throwable ignored) {
        }
        try {
            Object result = ClassUtil.getFieldValueFromHierarchy(wrapperObject, "instrumentation");
            if (result instanceof Instrumentation instrumentation) {
                return instrumentation;
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to extract Instrumentation from wrapper %s,%s",
                    wrapperObject.getClass().getName(), t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private String systemGetProperty(Object property) {
        try {
            return getProperties().getProperty(property.toString());
        } catch (Throwable t) {
            LogUtil.errorf("Failed to get system property: %s", t.getMessage());
        }
        try {
            if (methodHandleGetProperty1 == null) {
                methodHandleGetProperty1 = ClassUtil.getLookup().findStatic(System.class,
                        "getProperty",
                        MethodType.methodType(String.class, String.class)
                );
            }
            Object value = methodHandleGetProperty1.invoke(property.toString());
            return value == null ? systemGetProperty0(property.toString()) : value.toString();
        } catch (Throwable t) {
            return systemGetProperty0(property.toString());
        }
    }

    private Properties getProperties() {
        try {
            Field props = System.class.getDeclaredField("props");
            Object value = ClassUtil.getFieldValue(props, (Object)null);
            if (value instanceof Properties properties) {
                return properties;
            }
        } catch (NoSuchFieldException e) {
            LogUtil.errorf("Could not find System property %s", e.getMessage());
        }
        return getProperties0();
    }

    private Properties getProperties0() {
        return System.getProperties();
    }

    private String systemGetProperty0(String property) {
        return System.getProperty(property);
    }

    private String systemGetProperty(Object key, Object def) {
        try {
            return getProperties().getProperty(key.toString(), def.toString());
        } catch (Throwable t) {
            LogUtil.errorf("Failed to get system property: %s", t.getMessage());
        }
        try {
            if (methodHandleGetProperty2 == null) {
                methodHandleGetProperty2 = ClassUtil.getLookup().findStatic(System.class,
                        "getProperty",
                        MethodType.methodType(String.class, String.class, String.class)
                );
            }
            Object value = methodHandleGetProperty2.invoke(key.toString(), def.toString());
            return value == null ? null : value.toString();
        } catch (Throwable t) {
            return systemGetProperty0(key.toString(), def.toString());
        }
    }

    private String systemGetProperty0(String key, String def) {
        return System.getProperty(key, def);
    }
}
