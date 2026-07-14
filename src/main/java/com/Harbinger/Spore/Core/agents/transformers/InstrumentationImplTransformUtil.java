package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.UnmodifiableClassException;

public final class InstrumentationImplTransformUtil extends SporeClassFileTransformer0 implements IInstrumentationImplTransformer {
    private static final String INSTRUMENTATION_IMPL_INTERNAL = "sun/instrument/InstrumentationImpl";
    private static final String TARGET_METHOD_NAME = "transform";
    private static final String TARGET_METHOD_DESC = "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B";
    private static final String AGENT_BRIDGE_INTERNAL = "SporeAgent";
    private static final String GET_REAL_BYTE_DESC = "(Ljava/lang/instrument/Instrumentation;Ljava/lang/String;[B)[B";
    public static final IInstrumentationImplTransformer INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IInstrumentationImplTransformer.class,
            InstrumentationImplTransformUtil.class
    );
    private volatile boolean instInstalled = false;
    private volatile boolean instRetransformed = false;
    private volatile boolean jvmtiInstalled = false;
    private volatile boolean jvmtiRetransformed = false;
    private volatile int successfulTransformGeneration = 0;
    private final Class<?> instImplClass;
    public InstrumentationImplTransformUtil() {
        Class<?> impl=null;
        try {
            // Load the exact bootstrap class without triggering its initialization.
            impl = Class.forName("sun.instrument.InstrumentationImpl", false, null);
        }catch (Throwable e) {
            LogUtil.errorf("Cannot load sun.instrument.InstrumentationImpl: %s", e.getMessage());
        }
        instImplClass = impl;
    }

    @Override
    public synchronized void inspectInstrumentationImpl() {
        // The agent must finish attaching before InstrumentationImpl can reference
        // the bootstrap-visible SporeAgent bridge.
        if (jvmtiRetransformed || instRetransformed) {
            return;
        }

        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation == null) {
            LogUtil.error("Instrumentation is unavailable; skip InstrumentationImpl transformation because the bootstrap bridge is not ready.");
            return;
        }
        if (!isAgentBridgeResolvable()) {
            LogUtil.error("Bootstrap SporeAgent.getRealByte bridge is unavailable; skip InstrumentationImpl transformation.");
            return;
        }

        // Prefer JVMTI after the bridge is ready. If this backend cannot retransform
        // the class, keep the installed hook and fall back to Instrumentation below.
        IJVNTIPointer jvmtiUtil = JVMTIPointerUtil.newInstance();
        if (jvmtiUtil != null && !jvmtiInstalled) {
            jvmtiUtil.addTransformer(this);
            jvmtiInstalled = jvmtiUtil.isTransformerHookInstalled();
        }
        if (canRetransform(jvmtiUtil) && !jvmtiRetransformed) {
            int generation = successfulTransformGeneration;
            try {
                jvmtiUtil.retransformClasses(new Class<?>[]{instImplClass});
                if (successfulTransformGeneration != generation) {
                    jvmtiRetransformed = true;
                    return;
                }
                LogUtil.error("JVMTI retransform completed without applying the InstrumentationImpl hook; falling back to Instrumentation.");
            } catch (Throwable t) {
                LogUtil.errorf("Cannot retransform sun.instrument.InstrumentationImpl via JVMTI: %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }

        boolean instrumentationReady = canRetransform(instrumentation);
        if (instrumentationReady && !instInstalled) {
            instrumentation.addTransformer(this);
            instInstalled = true;
        }
        if (instrumentationReady && instInstalled && !instRetransformed) {
            int generation = successfulTransformGeneration;
            try {
                instrumentation.retransformClasses(new Class<?>[]{instImplClass});
                if (successfulTransformGeneration != generation) {
                    instRetransformed = true;
                } else {
                    LogUtil.error("Instrumentation retransform completed without applying the InstrumentationImpl hook.");
                }
            } catch (UnmodifiableClassException e) {
                LogUtil.errorf("Cannot retransform sun.instrument.InstrumentationImpl via instrumentation: %s", e.getMessage());
            } catch (Throwable t) {
                LogUtil.errorf("Unexpected failure retransformed sun.instrument.InstrumentationImpl via instrumentation: %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
    }

    private boolean isAgentBridgeResolvable() {
        try {
            Class<?> bridge = Class.forName("SporeAgent", false, null);
            bridge.getDeclaredMethod(
                    "getRealByte",
                    java.lang.instrument.Instrumentation.class,
                    String.class,
                    byte[].class
            );
            return instImplClass != null && instImplClass.getModule().canRead(bridge.getModule());
        } catch (Throwable t) {
            LogUtil.errorf("Cannot resolve bootstrap SporeAgent bridge: %s", t.getMessage());
            return false;
        }
    }

    private boolean canRetransform(IJVNTIPointer jvmtiUtil) {
        if (jvmtiUtil == null || !jvmtiInstalled || instImplClass == null) {
            return false;
        }
        try {
            return jvmtiUtil.isRetransformClassesSupported()
                    && jvmtiUtil.isModifiableClass(instImplClass);
        } catch (Throwable t) {
            LogUtil.errorf("JVMTI cannot retransform sun.instrument.InstrumentationImpl: %s", t.getMessage());
            return false;
        }
    }

    private boolean canRetransform(IInstrumentations instrumentation) {
        if (instrumentation == null || instImplClass == null) {
            return false;
        }
        try {
            return instrumentation.isRetransformClassesSupported()
                    && instrumentation.isModifiableClass(instImplClass);
        } catch (Throwable t) {
            LogUtil.errorf("Instrumentation cannot retransform sun.instrument.InstrumentationImpl: %s", t.getMessage());
            return false;
        }
    }

    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        return transformInternal(loader, className, classfileBuffer);
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        if(className!=null&&!className.equals(INSTRUMENTATION_IMPL_INTERNAL)) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            if (classNode.name == null || classNode.superName == null) {
                return null;
            }
            String effectiveClassName = className == null ? classNode.name : className;
            if(!INSTRUMENTATION_IMPL_INTERNAL.equals(effectiveClassName.replace('.', '/'))
                    && !INSTRUMENTATION_IMPL_INTERNAL.equals(classNode.name)) {
                return null;
            }
            boolean modified=false;
            boolean hookPresent=false;
            for(MethodNode mn : classNode.methods) {
                if (!TARGET_METHOD_NAME.equals(mn.name)||!TARGET_METHOD_DESC.equals(mn.desc)) {
                    continue;
                }
                if (alreadyWrapsReturnValue(mn)) {
                    hookPresent = true;
                    continue;
                }
                // Replace every original return value with getRealByte(this, className, original).
                if (patchTransformMethod(mn)) {
                    modified=true;
                    hookPresent=true;
                    LogUtil.log("Transformed sun.instrument.InstrumentationImpl transform return values for Spore classes.");
                }
            }
            if (!hookPresent) {
                return null;
            }
            byte[] transformed = modified ? toBytes(loader, effectiveClassName, classfileBuffer, classNode) : null;
            successfulTransformGeneration++;
            return transformed;
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform sun.instrument.InstrumentationImpl, %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private boolean patchTransformMethod(MethodNode method) {
        if (method == null || method.instructions == null || alreadyWrapsReturnValue(method)) {
            return false;
        }
        int originalLocal = method.maxLocals;
        method.maxLocals = originalLocal + 1;
        boolean modified = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn.getOpcode() == Opcodes.ARETURN) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(Opcodes.ASTORE, originalLocal));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                inject.add(new VarInsnNode(Opcodes.ALOAD, 3));
                inject.add(new VarInsnNode(Opcodes.ALOAD, originalLocal));
                inject.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        AGENT_BRIDGE_INTERNAL,
                        "getRealByte",
                        GET_REAL_BYTE_DESC,
                        false
                ));
                method.instructions.insertBefore(insn, inject);
                modified = true;
            }
            insn = next;
        }
        return modified;
    }

    private boolean alreadyWrapsReturnValue(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn
                    && AGENT_BRIDGE_INTERNAL.equals(methodInsn.owner)
                    && "getRealByte".equals(methodInsn.name)
                    && GET_REAL_BYTE_DESC.equals(methodInsn.desc)) {
                return true;
            }
        }
        return false;
    }
    private byte[] toBytes(ClassLoader loader, String className, byte[] inputBytes, ClassNode classNode) {
        ClassWriter writer = new SporeFrameClassWriter(loader, classNode, ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] transformed = writer.toByteArray();
        SporeTransformerDebugDump.rememberTransformed(
                getClass().getName(),
                className,
                classNode.name,
                inputBytes,
                transformed
        );
        return transformed;
    }
}
