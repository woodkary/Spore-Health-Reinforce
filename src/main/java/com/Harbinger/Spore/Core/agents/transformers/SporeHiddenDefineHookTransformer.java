package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.asmHooks.HiddenDefineHook;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;

public final class SporeHiddenDefineHookTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String LOOKUP_OWNER = "java/lang/invoke/MethodHandles$Lookup";
    private static final String LOOKUP_DESC = "Ljava/lang/invoke/MethodHandles$Lookup;";
    private static final String CLASS_OPTION_ARRAY_DESC = "[Ljava/lang/invoke/MethodHandles$Lookup$ClassOption;";
    private static final String METHOD_HANDLE_DESC = "Ljava/lang/invoke/MethodHandle;";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/HiddenDefineHook";
    private static final String DEFINE_HIDDEN_CLASS_DESC =
            "([BZ" + CLASS_OPTION_ARRAY_DESC + ")Ljava/lang/invoke/MethodHandles$Lookup;";
    private static final String FIND_STATIC_DESC =
            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;";
    private static final String DEFINE_HIDDEN_HOOK_DESC =
            "(" + LOOKUP_DESC + "[B)[B";
    private static final String FIND_STATIC_HOOK_DESC =
            "(" + METHOD_HANDLE_DESC + ")" + METHOD_HANDLE_DESC;
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeHiddenDefineHookTransformer.class
            );
    private static MethodHandle constructor;

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeHiddenDefineHookTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer res = newInstance();
        if (res instanceof SelfTransformer selfTransformer) {
            return selfTransformer;
        }
        return new SporeHiddenDefineHookTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeHiddenDefineHookTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeHiddenDefineHookTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeHiddenDefineHookTransformer();
    }

    public SporeHiddenDefineHookTransformer() {
    }

    @Override
    protected byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            if (classNode.name == null || shouldSkipClass(classNode.name)) {
                return null;
            }
            String effectiveClassName = className == null ? classNode.name : className;
            if (transformLookupCalls(classNode)) {
                return toBytes(loader, effectiveClassName, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform hidden define call sites of %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private boolean shouldSkipClass(String internalName) {
        return HOOK_OWNER.equals(internalName)
                || internalName.equals("com/Harbinger/Spore/Core/agents/transformers/SporeHiddenDefineHookTransformer");
    }

    private boolean transformLookupCalls(ClassNode classNode) {
        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (!canPatch(method) || alreadyCallsHook(method)) {
                continue;
            }
            if (patchLookupCalls(method)) {
                modified = true;
                LogUtil.logf("Transformed hidden define lookup calls %s.%s%s",
                        classNode.name,
                        method.name,
                        method.desc);
            }
        }
        return modified;
    }

    private boolean canPatch(MethodNode method) {
        return method != null
                && method.instructions != null
                && (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0;
    }

    private boolean alreadyCallsHook(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode methodInsn && HOOK_OWNER.equals(methodInsn.owner)) {
                return true;
            }
        }
        return false;
    }

    private boolean patchLookupCalls(MethodNode method) {
        boolean modified = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn instanceof MethodInsnNode methodInsn && LOOKUP_OWNER.equals(methodInsn.owner)) {
                if (isDefineHiddenClassCall(methodInsn)) {
                    patchDefineHiddenClassCall(method, methodInsn);
                    modified = true;
                } else if (isFindStaticCall(methodInsn)) {
                    patchFindStaticCall(method, methodInsn);
                    modified = true;
                }
            }
            insn = next;
        }
        return modified;
    }

    private boolean isDefineHiddenClassCall(MethodInsnNode methodInsn) {
        return "defineHiddenClass".equals(methodInsn.name)
                && DEFINE_HIDDEN_CLASS_DESC.equals(methodInsn.desc);
    }

    private boolean isFindStaticCall(MethodInsnNode methodInsn) {
        return "findStatic".equals(methodInsn.name)
                && FIND_STATIC_DESC.equals(methodInsn.desc);
    }

    private void patchDefineHiddenClassCall(MethodNode method, MethodInsnNode methodInsn) {
        int classOptionsLocal = allocateTempLocal(method, Type.getType(CLASS_OPTION_ARRAY_DESC));
        int initializeLocal = allocateTempLocal(method, Type.BOOLEAN_TYPE);
        int bytesLocal = allocateTempLocal(method, Type.getType("[B"));
        int lookupLocal = allocateTempLocal(method, Type.getObjectType(LOOKUP_OWNER));

        InsnList inject = new InsnList();
        inject.add(new VarInsnNode(Opcodes.ASTORE, classOptionsLocal));
        inject.add(new VarInsnNode(Opcodes.ISTORE, initializeLocal));
        inject.add(new VarInsnNode(Opcodes.ASTORE, bytesLocal));
        inject.add(new VarInsnNode(Opcodes.ASTORE, lookupLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, lookupLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, bytesLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                HOOK_OWNER,
                "lookupDefineHiddenClassHook",
                DEFINE_HIDDEN_HOOK_DESC,
                false
        ));
        inject.add(new VarInsnNode(Opcodes.ASTORE, bytesLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, lookupLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, bytesLocal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, initializeLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, classOptionsLocal));
        method.instructions.insertBefore(methodInsn, inject);
    }

    private void patchFindStaticCall(MethodNode method, MethodInsnNode methodInsn) {
        InsnList inject = new InsnList();
        inject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                HOOK_OWNER,
                "lookupFindDefineClass0StaticHook",
                FIND_STATIC_HOOK_DESC,
                false
        ));
        method.instructions.insert(methodInsn, inject);
    }

    private int allocateTempLocal(MethodNode method, Type type) {
        int index = Math.max(method.maxLocals, minLocalsFromDesc(method));
        method.maxLocals = index + type.getSize();
        return index;
    }

    private int minLocalsFromDesc(MethodNode method) {
        int locals = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        Type[] args = Type.getArgumentTypes(method.desc);
        for (Type arg : args) {
            locals += arg.getSize();
        }
        return locals;
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

    @Override
    public byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer) {
        return transformInternal(loader, className, classfileBuffer);
    }
}
