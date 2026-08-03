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

public final class SporeDiscoveredHealthMethodTransformer extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private static final String HOOK_METHOD = "getHeealth";
    private static final String FLOAT_OBJECT_HOOK_DESC = "(FLjava/lang/Object;)F";
    private static final String DOUBLE_OBJECT_HOOK_DESC = "(DLjava/lang/Object;)D";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS = resolveTransformerClass();
    private static MethodHandle constructor;

    private static Class<? extends ClassFileTransformer> resolveTransformerClass() {
        SporeDiscoveredHealthMethodRegistry.owners();
        return (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                SporeDiscoveredHealthMethodTransformer.class
        );
    }

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeDiscoveredHealthMethodTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        return transformer instanceof SelfTransformer selfTransformer
                ? selfTransformer
                : new SporeDiscoveredHealthMethodTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeDiscoveredHealthMethodTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeDiscoveredHealthMethodTransformer, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeDiscoveredHealthMethodTransformer();
    }

    public SporeDiscoveredHealthMethodTransformer() {
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
            Map<String, HealthMethodTarget> targets =
                    SporeDiscoveredHealthMethodRegistry.targetsForOwner(classNode.name);
            if (targets.isEmpty()) {
                return null;
            }

            boolean modified = false;
            for (MethodNode method : classNode.methods) {
                HealthMethodTarget target = targets.get(
                        SporeDiscoveredHealthMethodRegistry.methodKey(method.name, method.desc)
                );
                if (target == null
                        || "<init>".equals(method.name)
                        || "<clinit>".equals(method.name)
                        || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                        || method.instructions == null
                        || method.instructions.size() == 0
                        || alreadyCallsHook(method)) {
                    continue;
                }
                boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
                if ((target.entitySource() == EntitySource.STATIC_ARGUMENTS) != isStatic) {
                    LogUtil.errorf("Skip discovered health method %s.%s%s: target mode %s conflicts with access %s",
                            classNode.name,
                            method.name,
                            method.desc,
                            target.entitySource(),
                            isStatic ? "static" : "instance");
                    continue;
                }
                if (patchReturns(method, target)) {
                    modified = true;
                    LogUtil.logf("Transformed discovered lifecycle health method %s.%s%s using %s",
                            classNode.name,
                            method.name,
                            method.desc,
                            target.entitySource());
                }
            }
            if (modified) {
                return toBytes(loader, className == null ? classNode.name : className, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform discovered lifecycle health class %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private boolean patchReturns(MethodNode method, HealthMethodTarget target) {
        Type returnType = Type.getReturnType(method.desc);
        boolean isFloat = returnType.getSort() == Type.FLOAT;
        if (!isFloat && returnType.getSort() != Type.DOUBLE) {
            return false;
        }

        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
        int[] usableIndexes = target.entitySource() == EntitySource.STATIC_ARGUMENTS
                ? usableEntityArgumentIndexes(target.entityArgumentIndexes(), argumentTypes)
                : new int[0];
        if (target.entitySource() == EntitySource.STATIC_ARGUMENTS && usableIndexes.length == 0) {
            return false;
        }

        int[] argumentSlots = argumentSlots(argumentTypes, isStatic);
        int resultLocal = Math.max(method.maxLocals, minimumLocals(argumentTypes, isStatic));
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
                if (target.entitySource() == EntitySource.INSTANCE_THIS) {
                    appendHook(inject, loadOpcode, storeOpcode, resultLocal, 0, hookDescriptor);
                } else {
                    for (int argumentIndex : usableIndexes) {
                        appendHook(
                                inject,
                                loadOpcode,
                                storeOpcode,
                                resultLocal,
                                argumentSlots[argumentIndex],
                                hookDescriptor
                        );
                    }
                }
                inject.add(new VarInsnNode(loadOpcode, resultLocal));
                method.instructions.insertBefore(instruction, inject);
                modified = true;
            }
            instruction = next;
        }
        return modified;
    }

    private void appendHook(InsnList inject,
                            int loadOpcode,
                            int storeOpcode,
                            int resultLocal,
                            int entityLocal,
                            String hookDescriptor) {
        inject.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                "L" + HOOK_INTERFACE + ";"
        ));
        inject.add(new VarInsnNode(loadOpcode, resultLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, entityLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                HOOK_METHOD,
                hookDescriptor,
                true
        ));
        inject.add(new VarInsnNode(storeOpcode, resultLocal));
    }

    private int[] usableEntityArgumentIndexes(int[] requestedIndexes, Type[] argumentTypes) {
        List<Integer> usable = new ArrayList<>();
        for (int index : requestedIndexes) {
            if (index >= 0 && index < argumentTypes.length && isReferenceType(argumentTypes[index])) {
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
        return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
    }

    private int[] argumentSlots(Type[] argumentTypes, boolean isStatic) {
        int[] slots = new int[argumentTypes.length];
        int slot = isStatic ? 0 : 1;
        for (int i = 0; i < argumentTypes.length; i++) {
            slots[i] = slot;
            slot += argumentTypes[i].getSize();
        }
        return slots;
    }

    private int minimumLocals(Type[] argumentTypes, boolean isStatic) {
        int locals = isStatic ? 0 : 1;
        for (Type argumentType : argumentTypes) {
            locals += argumentType.getSize();
        }
        return locals;
    }

    private boolean alreadyCallsHook(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode call
                    && HOOK_INTERFACE.equals(call.owner)
                    && HOOK_METHOD.equals(call.name)
                    && (FLOAT_OBJECT_HOOK_DESC.equals(call.desc)
                    || DOUBLE_OBJECT_HOOK_DESC.equals(call.desc))) {
                return true;
            }
        }
        return false;
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
