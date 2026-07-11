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
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.Instrumentation;

public final class InstrumentationImplTransformUtil extends SporeClassFileTransformer0 implements IInstrumentationImplTransformer {
    private static final String INSTRUMENTATION_IMPL_INTERNAL = "sun/instrument/InstrumentationImpl";
    private static final String TARGET_METHOD_NAME = "transform";
    private static final String TARGET_METHOD_DESC = "(Ljava/lang/Module;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[BZ)[B";
    private static final String SPORE_INTERNAL_PREFIX = "com/Harbinger/Spore";
    private static final String SELF_INTERNAL = "com/Harbinger/Spore/Core/agents/transformers/InstrumentationImplTransformUtil";
    private static final String SHOULD_SKIP_TRANSFORM_DESC = "(Ljava/lang/instrument/Instrumentation;Ljava/lang/String;)Z";
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
//        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
//        boolean instrumentationReady = instrumentation != null;
//        if(instrumentationReady&&!instInstalled) {
//            //只安装Transformer，不进行retransform
//            instrumentation.addTransformer(this);
//            instInstalled = true;
//        }
    }
    public static boolean shouldSkipTransform(Instrumentation inst, String className){
        if (className == null || !className.startsWith(SPORE_INTERNAL_PREFIX)) {
            return false;
        }
        Instrumentation current = InstrumentationUtil.inst;
        return inst != current;
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
                //以下是对private byte[]
                //    transform(  Module              module,
                //                ClassLoader         loader,
                //                String              classname,
                //                Class<?>            classBeingRedefined,
                //                ProtectionDomain    protectionDomain,
                //                byte[]              classfileBuffer,
                //                boolean             isRetransformer)
                //的修改：开头插入指令：if(InstrumentationImplTransformUtil.shouldSkipTransform(this, classname)) return null;
                //即非本mod安装的Instrumentation不能转换我自己的类（包括可能的隐藏类）。
                if (patchTransformMethod(classNode.name, mn)) {
                    modified=true;
                    LogUtil.log("Transformed sun.instrument.InstrumentationImpl transform method to skip Spore classes.");
                }
            }
            return modified ? toBytes(loader, effectiveClassName, classfileBuffer, classNode) : null;
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform sun.instrument.InstrumentationImpl, %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }

    private boolean patchTransformMethod(String ownerInternalName, MethodNode method) {
        if (method == null || method.instructions == null || alreadySkipsSporeClasses(method)) {
            return false;
        }
        AbstractInsnNode firstRealInsn = findFirstRealInstruction(method);
        if (firstRealInsn == null) {
            return false;
        }
        LabelNode continueLabel = new LabelNode();
        InsnList inject = new InsnList();
        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
        inject.add(new VarInsnNode(Opcodes.ALOAD, 3));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SELF_INTERNAL,
                "shouldSkipTransform",
                SHOULD_SKIP_TRANSFORM_DESC,
                false
        ));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        inject.add(new InsnNode(Opcodes.ACONST_NULL));
        inject.add(new InsnNode(Opcodes.ARETURN));
        inject.add(continueLabel);
        inject.add(new FrameNode(
                Opcodes.F_NEW,
                8,
                new Object[]{
                        ownerInternalName,
                        "java/lang/Module",
                        "java/lang/ClassLoader",
                        "java/lang/String",
                        "java/lang/Class",
                        "java/security/ProtectionDomain",
                        "[B",
                        Opcodes.INTEGER
                },
                0,
                null
        ));
        method.instructions.insertBefore(firstRealInsn, inject);
        return true;
    }

    private boolean alreadySkipsSporeClasses(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn
                    && SELF_INTERNAL.equals(methodInsn.owner)
                    && "shouldSkipTransform".equals(methodInsn.name)
                    && SHOULD_SKIP_TRANSFORM_DESC.equals(methodInsn.desc)) {
                return true;
            }
        }
        return false;
    }

    private AbstractInsnNode findFirstRealInstruction(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode || insn instanceof LineNumberNode || insn.getType() == AbstractInsnNode.FRAME) {
                continue;
            }
            return insn;
        }
        return null;
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
