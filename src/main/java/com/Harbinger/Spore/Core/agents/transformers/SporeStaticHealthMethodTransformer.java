package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SporeStaticHealthMethodTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private static final String HOOK_METHOD = "getHeealth";
    private static final String FLOAT_OBJECT_HOOK_DESC = "(FLjava/lang/Object;)F";
    private static final String DOUBLE_OBJECT_HOOK_DESC = "(DLjava/lang/Object;)D";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS =
            resolveTransformerClass();
    private static MethodHandle constructor;

    private static Class<? extends ClassFileTransformer> resolveTransformerClass() {
        SporeStaticHealthMethodRegistry.owners();
        return (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                SporeStaticHealthMethodTransformer.class
        );
    }

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeStaticHealthMethodTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        if (transformer instanceof SelfTransformer selfTransformer) {
            return selfTransformer;
        }
        return new SporeStaticHealthMethodTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeStaticHealthMethodTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeStaticHealthMethodTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeStaticHealthMethodTransformer();
    }

    public SporeStaticHealthMethodTransformer() {
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
            if (classNode.name == null) {
                return null;
            }
            Map<String, int[]> targets = SporeStaticHealthMethodRegistry.targetsForOwner(classNode.name);
            if (targets.isEmpty()) {
                return null;
            }

            boolean modified = false;
            for (MethodNode method : classNode.methods) {
                int[] entityArgumentIndexes = targets.get(
                        SporeStaticHealthMethodRegistry.methodKey(method.name, method.desc)
                );
                if (entityArgumentIndexes == null
                        || (method.access & Opcodes.ACC_STATIC) == 0
                        || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                        || method.instructions == null
                        || alreadyCallsHook(method)) {
                    continue;
                }
                if (patchReturns(method, entityArgumentIndexes)) {
                    modified = true;
                    LogUtil.logf("Transformed static lifecycle health method %s.%s%s",
                            classNode.name,
                            method.name,
                            method.desc);
                }
            }
            if (modified) {
                return toBytes(loader, className == null ? classNode.name : className, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform static lifecycle health class %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private boolean patchReturns(MethodNode method, int[] entityArgumentIndexes) {
        Type returnType = Type.getReturnType(method.desc);
        boolean isFloat = returnType.getSort() == Type.FLOAT;
        if (!isFloat && returnType.getSort() != Type.DOUBLE) {
            return false;
        }

        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        int[] argumentSlots = argumentSlots(argumentTypes);
        int[] usableIndexes = usableEntityArgumentIndexes(entityArgumentIndexes, argumentTypes);
        if (usableIndexes.length == 0) {
            return false;
        }

        int resultLocal = Math.max(method.maxLocals, minimumLocals(argumentTypes));
        method.maxLocals = resultLocal + returnType.getSize();
        int returnOpcode = isFloat ? Opcodes.FRETURN : Opcodes.DRETURN;
        int storeOpcode = isFloat ? Opcodes.FSTORE : Opcodes.DSTORE;
        int loadOpcode = isFloat ? Opcodes.FLOAD : Opcodes.DLOAD;
        String hookDescriptor = isFloat ? FLOAT_OBJECT_HOOK_DESC : DOUBLE_OBJECT_HOOK_DESC;
        boolean modified = false;

        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == returnOpcode) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(storeOpcode, resultLocal));
                for (int argumentIndex : usableIndexes) {
                    int argumentSlot = argumentSlots[argumentIndex];
                    inject.add(new FieldInsnNode(
                            Opcodes.GETSTATIC,
                            HOOK_OWNER,
                            "INSTANCE",
                            "L" + HOOK_INTERFACE + ";"
                    ));
                    inject.add(new VarInsnNode(loadOpcode, resultLocal));
                    inject.add(new VarInsnNode(Opcodes.ALOAD, argumentSlot));
                    inject.add(new MethodInsnNode(
                            Opcodes.INVOKEINTERFACE,
                            HOOK_INTERFACE,
                            HOOK_METHOD,
                            hookDescriptor,
                            true
                    ));
                    inject.add(new VarInsnNode(storeOpcode, resultLocal));
                }
                inject.add(new VarInsnNode(loadOpcode, resultLocal));
                method.instructions.insertBefore(instruction, inject);
                modified = true;
            }
            instruction = next;
        }
        return modified;
    }

    private int[] usableEntityArgumentIndexes(int[] requestedIndexes, Type[] argumentTypes) {
        List<Integer> usable = new ArrayList<>();
        for (int index : requestedIndexes) {
            if (index >= 0
                    && index < argumentTypes.length
                    && isReferenceType(argumentTypes[index])) {
                usable.add(index);
            }
        }
        int[] result = new int[usable.size()];
        for (int i = 0; i < usable.size(); i++) {
            result[i] = usable.get(i);
        }
        return result;
    }

    private boolean isReferenceType(Type type) {
        int sort = type.getSort();
        return sort == Type.OBJECT || sort == Type.ARRAY;
    }

    private int[] argumentSlots(Type[] argumentTypes) {
        int[] slots = new int[argumentTypes.length];
        int slot = 0;
        for (int i = 0; i < argumentTypes.length; i++) {
            slots[i] = slot;
            slot += argumentTypes[i].getSize();
        }
        return slots;
    }

    private int minimumLocals(Type[] argumentTypes) {
        int locals = 0;
        for (Type argumentType : argumentTypes) {
            locals += argumentType.getSize();
        }
        return locals;
    }

    private boolean alreadyCallsHook(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode call)
                    || !HOOK_INTERFACE.equals(call.owner)
                    || !HOOK_METHOD.equals(call.name)) {
                continue;
            }
            if (FLOAT_OBJECT_HOOK_DESC.equals(call.desc)
                    || DOUBLE_OBJECT_HOOK_DESC.equals(call.desc)) {
                return true;
            }
        }
        return false;
    }

    private byte[] toBytes(ClassLoader loader,
                           String className,
                           byte[] inputBytes,
                           ClassNode classNode) {
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
