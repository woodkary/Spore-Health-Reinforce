package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.asmHooks.HiddenDefineHook;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;

public final class SporeHiddenDefineHookTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String LOOKUP_OWNER = "java/lang/invoke/MethodHandles$Lookup";
    private static final String LOOKUP_DESC = "Ljava/lang/invoke/MethodHandles$Lookup;";
    private static final String CLASS_OPTION_ARRAY_DESC = "[Ljava/lang/invoke/MethodHandles$Lookup$ClassOption;";
    private static final String METHOD_HANDLE_DESC = "Ljava/lang/invoke/MethodHandle;";
    private static final String REFLECT_METHOD_OWNER = "java/lang/reflect/Method";
    private static final String OBJECT_ARRAY_DESC = "[Ljava/lang/Object;";
    private static final String CLASS_ARRAY_DESC = "[Ljava/lang/Class;";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/HiddenDefineHook";
    private static final String DEFINE_HIDDEN_CLASS_DESC =
            "([BZ" + CLASS_OPTION_ARRAY_DESC + ")Ljava/lang/invoke/MethodHandles$Lookup;";
    private static final String FIND_STATIC_DESC =
            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;";
    private static final String METHOD_INVOKE_DESC =
            "(Ljava/lang/Object;" + OBJECT_ARRAY_DESC + ")Ljava/lang/Object;";
    private static final String DEFINE_HIDDEN_HOOK_DESC =
            "(" + LOOKUP_DESC + "[B)[B";
    private static final String RECORD_HIDDEN_LOOKUP_DESC =
            "(" + LOOKUP_DESC + ")" + LOOKUP_DESC;
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
        if(className!=null&&(shouldSkipClass(className)||!StackTraceUtil.isBadModName(className)||className.equals("SporeAgent"))) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            if (classNode.name == null || shouldSkipClass(classNode.name) || !StackTraceUtil.isBadModName(classNode.name)||classNode.name.equals("SporeAgent")) {
                return null;
            }
            String effectiveClassName = className == null ? classNode.name : className;
            if (transformHiddenDefineCalls(classNode)) {
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

    private boolean transformHiddenDefineCalls(ClassNode classNode) {
        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            if (!canPatch(method)) {
                continue;
            }
            if (patchHiddenDefineCalls(method)) {
                modified = true;
                LogUtil.logf("Transformed hidden define call sites %s.%s%s",
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

    private boolean patchHiddenDefineCalls(MethodNode method) {
        boolean modified = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode next = insn.getNext();
            if (insn instanceof MethodInsnNode methodInsn) {
                if (isDefineHiddenClassCall(methodInsn)) {
                    if (!hasHookBefore(methodInsn, "lookupDefineHiddenClassHook", 8)) {
                        patchDefineHiddenClassCall(method, methodInsn);
                        modified = true;
                    }
                    if (!hasHookAfter(methodInsn, "recordHiddenLookup", 1)) {
                        patchRecordHiddenLookup(method, methodInsn);
                        modified = true;
                    }
                } else if (isFindStaticCall(methodInsn)
                        && !hasHookAfter(methodInsn, "lookupFindDefineClass0StaticHook", 1)) {
                    patchFindStaticCall(method, methodInsn);
                    modified = true;
                } else if (isReflectiveMethodInvoke(methodInsn)
                        && !hasHookBefore(methodInsn, "lookupDefineHiddenClassHook", 8)) {
                    patchReflectiveMethodInvoke(method, methodInsn);
                    modified = true;
                }
            }
            insn = next;
        }
        return modified;
    }

    private boolean isDefineHiddenClassCall(MethodInsnNode methodInsn) {
        return LOOKUP_OWNER.equals(methodInsn.owner)
                && "defineHiddenClass".equals(methodInsn.name)
                && DEFINE_HIDDEN_CLASS_DESC.equals(methodInsn.desc);
    }

    private boolean isFindStaticCall(MethodInsnNode methodInsn) {
        return LOOKUP_OWNER.equals(methodInsn.owner)
                && "findStatic".equals(methodInsn.name)
                && FIND_STATIC_DESC.equals(methodInsn.desc);
    }

    private boolean isReflectiveMethodInvoke(MethodInsnNode methodInsn) {
        return REFLECT_METHOD_OWNER.equals(methodInsn.owner)
                && "invoke".equals(methodInsn.name)
                && METHOD_INVOKE_DESC.equals(methodInsn.desc);
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

    private void patchRecordHiddenLookup(MethodNode method, MethodInsnNode methodInsn) {
        InsnList inject = new InsnList();
        inject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                HOOK_OWNER,
                "recordHiddenLookup",
                RECORD_HIDDEN_LOOKUP_DESC,
                false
        ));
        method.instructions.insert(methodInsn, inject);
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

    private void patchReflectiveMethodInvoke(MethodNode method, MethodInsnNode methodInsn) {
        int argumentsLocal = allocateTempLocal(method, Type.getType(OBJECT_ARRAY_DESC));
        int receiverLocal = allocateTempLocal(method, Type.getType(Object.class));
        int reflectMethodLocal = allocateTempLocal(method, Type.getObjectType(REFLECT_METHOD_OWNER));
        int methodNameLocal = allocateTempLocal(method, Type.getType(String.class));
        int parameterTypesLocal = allocateTempLocal(method, Type.getType(CLASS_ARRAY_DESC));
        int parameterIndexLocal = allocateTempLocal(method, Type.INT_TYPE);

        LabelNode nameMatched = new LabelNode();
        LabelNode parameterLoop = new LabelNode();
        LabelNode nextParameter = new LabelNode();
        LabelNode invokeOriginal = new LabelNode();

        InsnList inject = new InsnList();
        inject.add(new VarInsnNode(Opcodes.ASTORE, argumentsLocal));
        inject.add(new VarInsnNode(Opcodes.ASTORE, receiverLocal));
        inject.add(new VarInsnNode(Opcodes.ASTORE, reflectMethodLocal));

        inject.add(new VarInsnNode(Opcodes.ALOAD, reflectMethodLocal));
        inject.add(new JumpInsnNode(Opcodes.IFNULL, invokeOriginal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, receiverLocal));
        inject.add(new TypeInsnNode(Opcodes.INSTANCEOF, LOOKUP_OWNER));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, invokeOriginal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, reflectMethodLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                REFLECT_METHOD_OWNER,
                "getDeclaringClass",
                "()Ljava/lang/Class;",
                false
        ));
        inject.add(new LdcInsnNode(Type.getObjectType(LOOKUP_OWNER)));
        inject.add(new JumpInsnNode(Opcodes.IF_ACMPNE, invokeOriginal));

        inject.add(new VarInsnNode(Opcodes.ALOAD, reflectMethodLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                REFLECT_METHOD_OWNER,
                "getName",
                "()Ljava/lang/String;",
                false
        ));
        inject.add(new VarInsnNode(Opcodes.ASTORE, methodNameLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, methodNameLocal));
        inject.add(new LdcInsnNode("defineHiddenClass"));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "equals",
                "(Ljava/lang/Object;)Z",
                false
        ));
        inject.add(new JumpInsnNode(Opcodes.IFNE, nameMatched));
        inject.add(new VarInsnNode(Opcodes.ALOAD, methodNameLocal));
        inject.add(new LdcInsnNode("makeHiddenClassDefiner"));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "equals",
                "(Ljava/lang/Object;)Z",
                false
        ));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, invokeOriginal));

        inject.add(nameMatched);
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new JumpInsnNode(Opcodes.IFNULL, invokeOriginal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, reflectMethodLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                REFLECT_METHOD_OWNER,
                "getParameterTypes",
                "()" + CLASS_ARRAY_DESC,
                false
        ));
        inject.add(new VarInsnNode(Opcodes.ASTORE, parameterTypesLocal));
        inject.add(new InsnNode(Opcodes.ICONST_0));
        inject.add(new VarInsnNode(Opcodes.ISTORE, parameterIndexLocal));

        inject.add(parameterLoop);
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, parameterTypesLocal));
        inject.add(new InsnNode(Opcodes.ARRAYLENGTH));
        inject.add(new JumpInsnNode(Opcodes.IF_ICMPGE, invokeOriginal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new InsnNode(Opcodes.ARRAYLENGTH));
        inject.add(new JumpInsnNode(Opcodes.IF_ICMPGE, invokeOriginal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, parameterTypesLocal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new InsnNode(Opcodes.AALOAD));
        inject.add(new LdcInsnNode(Type.getType("[B")));
        inject.add(new JumpInsnNode(Opcodes.IF_ACMPNE, nextParameter));
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new InsnNode(Opcodes.AALOAD));
        inject.add(new TypeInsnNode(Opcodes.INSTANCEOF, "[B"));
        inject.add(new JumpInsnNode(Opcodes.IFEQ, nextParameter));

        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                OBJECT_ARRAY_DESC,
                "clone",
                "()Ljava/lang/Object;",
                false
        ));
        inject.add(new TypeInsnNode(Opcodes.CHECKCAST, OBJECT_ARRAY_DESC));
        inject.add(new VarInsnNode(Opcodes.ASTORE, argumentsLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, receiverLocal));
        inject.add(new TypeInsnNode(Opcodes.CHECKCAST, LOOKUP_OWNER));
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        inject.add(new VarInsnNode(Opcodes.ILOAD, parameterIndexLocal));
        inject.add(new InsnNode(Opcodes.AALOAD));
        inject.add(new TypeInsnNode(Opcodes.CHECKCAST, "[B"));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                HOOK_OWNER,
                "lookupDefineHiddenClassHook",
                DEFINE_HIDDEN_HOOK_DESC,
                false
        ));
        inject.add(new InsnNode(Opcodes.AASTORE));
        inject.add(new JumpInsnNode(Opcodes.GOTO, invokeOriginal));

        inject.add(nextParameter);
        inject.add(new IincInsnNode(parameterIndexLocal, 1));
        inject.add(new JumpInsnNode(Opcodes.GOTO, parameterLoop));

        inject.add(invokeOriginal);
        inject.add(new VarInsnNode(Opcodes.ALOAD, reflectMethodLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, receiverLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, argumentsLocal));
        method.instructions.insertBefore(methodInsn, inject);
    }

    private boolean hasHookBefore(AbstractInsnNode start, String hookName, int maxInstructions) {
        return hasHook(start.getPrevious(), hookName, maxInstructions, false);
    }

    private boolean hasHookAfter(AbstractInsnNode start, String hookName, int maxInstructions) {
        return hasHook(start.getNext(), hookName, maxInstructions, true);
    }

    private boolean hasHook(AbstractInsnNode insn, String hookName, int maxInstructions, boolean forward) {
        int inspected = 0;
        while (insn != null && inspected < maxInstructions) {
            if (insn.getOpcode() >= 0) {
                inspected++;
                if (insn instanceof MethodInsnNode methodInsn
                        && HOOK_OWNER.equals(methodInsn.owner)
                        && hookName.equals(methodInsn.name)) {
                    return true;
                }
            }
            insn = forward ? insn.getNext() : insn.getPrevious();
        }
        return false;
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
        ClassWriter writer = new SporeFrameClassWriter(
                loader,
                classNode,
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
        );
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
