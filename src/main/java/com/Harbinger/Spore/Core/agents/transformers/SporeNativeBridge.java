package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;

import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class SporeNativeBridge implements INativeBridge {
    public static final INativeBridge INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            INativeBridge.class,
            SporeNativeBridge.class
    );
    private static final String DLL_RESOURCE = "/sporeTransformerBridge.dll";
    private volatile boolean loaded;
    private MethodHandle systemLoad;
    private MethodHandle systemLoad2;
    private SporeNativeBridge() {
    }
    private void systemLoad(String filePath,int x,float y,double z){
        try {
            MethodHandles.Lookup lookup1 = ClassUtil.getLookup();
            if (systemLoad == null) {
                try {
                    systemLoad = lookup1.findStatic(ClassLoader.class,
                            "loadLibrary",
                            MethodType.methodType(Class.forName("jdk.internal.loader.NativeLibrary"), Class.class, File.class));
                }catch (Throwable var4) {
                    LogUtil.errorf("Failed to load Native Library", var4);
                }
            }
            if(systemLoad==null&&systemLoad2==null) {
                systemLoad2=lookup1.findStatic(System.class,
                        "load",
                        MethodType.methodType(void.class, String.class));
            }
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                throw new UnsatisfiedLinkError(
                        "Expecting an absolute path of the library: " + filePath);
            }
            if(systemLoad!=null) {
                systemLoad.invoke(ClassUtil.class, file);
                LogUtil.log("called ClassLoader.loadLibrary");
                return;
            }else if(systemLoad2!=null) {
                systemLoad2.invoke(filePath);
                return;
            }

        }catch (Throwable throwable){
            LogUtil.errorf("Failed to load file.%s,%s",filePath,throwable);
            LogUtil.flush();
        }
        systemLoad(filePath,x,y);
    }

    private void systemLoad(String filePath,int x,float y){
        System.load(filePath);
    }
    @Override
    public synchronized void load() {
        if (loaded) {
            return;
        }
        try (InputStream input = SporeNativeBridge.class.getResourceAsStream(DLL_RESOURCE)) {
            if (input == null) {
                LogUtil.error("Transformer native bridge resource is missing: " + DLL_RESOURCE);
                return;
            }
            Path file = Files.createTempFile("spore-transformer-", ".dll");
            Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            systemLoad(file.toAbsolutePath().toString(),0,0.0f,0.0);
            loaded = true;
            LogUtil.log("Loaded transformer native bridge: " + file.toAbsolutePath());
        } catch (Throwable t) {
            LogUtil.errorf("Failed to load transformer native bridge: %s", t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }
}
