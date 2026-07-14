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

import java.lang.instrument.Instrumentation;

public final class InstrumentationImplTransformUtil extends SporeClassFileTransformer0 implements IInstrumentationImplTransformer {
    private static final String INSTRUMENTATION_IMPL_INTERNAL = "sun/instrument/InstrumentationImpl";
    private static final String TARGET_METHOD_NAME = "transform";
    private static final String TARGET_METHOD_DESC = "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B";
    private static final String SPORE_INTERNAL_PREFIX = "com/Harbinger/Spore/Core";
    private static final String SELF_INTERNAL = "com/Harbinger/Spore/Core/agents/transformers/InstrumentationImplTransformUtil";
    private static final String GET_REAL_BYTE_DESC = "(Ljava/lang/instrument/Instrumentation;Ljava/lang/String;[B)[B";
    public static final IInstrumentationImplTransformer INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IInstrumentationImplTransformer.class,
            InstrumentationImplTransformUtil.class
    );
    private volatile boolean instInstalled = false;
    private volatile boolean jvmtiInstalled = false;
    @Override
    public void inspectInstrumentationImpl() {
        //同时安装Instrumentation和jvmti监听InstrumentationImpl加载，但不重转换
        IJVNTIPointer jvmtiUtil= JVMTIPointerUtil.newInstance();
        if(jvmtiUtil!=null && !jvmtiInstalled) {
            //只安装Transformer，不进行retransform
            jvmtiUtil.addTransformer(this);
            jvmtiInstalled=jvmtiUtil.isTransformerHookInstalled();
        }
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        boolean instrumentationReady = instrumentation != null;
        if(instrumentationReady&&!instInstalled) {
            //只安装Transformer，不进行retransform
            instrumentation.addTransformer(this);
            instInstalled = true;
        }
    }
    public static byte[] getRealByte(Instrumentation inst, String className,byte[] original){
        return shouldSkipTransform(inst,className)?null:original;
    }
    public static boolean shouldSkipTransform(Instrumentation inst, String className){
        if (className == null || !className.startsWith(SPORE_INTERNAL_PREFIX)) {
            return false;
        }
        Instrumentation current = InstrumentationUtil.inst;
        return current!=null && inst != current;
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
            for(MethodNode mn : classNode.methods) {
                if (!TARGET_METHOD_NAME.equals(mn.name)||!TARGET_METHOD_DESC.equals(mn.desc)) {
                    continue;
                }
                // Replace every original return value with getRealByte(this, className, original).
                if (patchTransformMethod(mn)) {
                    modified=true;
                    LogUtil.log("Transformed sun.instrument.InstrumentationImpl transform return values for Spore classes.");
                }
            }
            return modified ? toBytes(loader, effectiveClassName, classfileBuffer, classNode) : null;
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
                        SELF_INTERNAL,
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
                    && SELF_INTERNAL.equals(methodInsn.owner)
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
