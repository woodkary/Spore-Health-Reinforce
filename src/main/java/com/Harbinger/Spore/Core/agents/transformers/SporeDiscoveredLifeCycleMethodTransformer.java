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

public final class SporeDiscoveredLifeCycleMethodTransformer
        extends SporeClassFileTransformer0 implements SelfTransformer {
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager";
    private static final String HOOK_INTERFACE = "com/Harbinger/Spore/Core/asmHooks/IEntityHealth";
    private static final String FLOAT_OBJECT_HEALTH_DESC = "(FLjava/lang/Object;)F";
    private static final String DOUBLE_OBJECT_HEALTH_DESC = "(DLjava/lang/Object;)D";
    private static final String BOOLEAN_OBJECT_DESC = "(ZLjava/lang/Object;)Z";
    private static final Class<? extends ClassFileTransformer> TRANSFORM_CLASS = resolveTransformerClass();
    private static MethodHandle constructor;

    private static Class<? extends ClassFileTransformer> resolveTransformerClass() {
        SporeDiscoveredLifeCycleMethodRegistry.owners();
        return (Class<? extends ClassFileTransformer>) BytecodeUtil.resolveHiddenClassOrSelf(
                SporeDiscoveredLifeCycleMethodTransformer.class
        );
    }

    static {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeDiscoveredLifeCycleMethodTransformer.class
        );
    }

    public static SelfTransformer newSelfTransformer() {
        ClassFileTransformer transformer = newInstance();
        return transformer instanceof SelfTransformer selfTransformer
                ? selfTransformer
                : new SporeDiscoveredLifeCycleMethodTransformer();
    }

    public static ClassFileTransformer newInstance() {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                TRANSFORM_CLASS,
                SporeDiscoveredLifeCycleMethodTransformer.class
        );
        if (constructor != null) {
            try {
                return (ClassFileTransformer) constructor.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to init hidden SporeDiscoveredLifeCycleMethodTransformer, %s",
                        t.getMessage());
                LogUtil.printStackTrace(t);
            }
        }
        return new SporeDiscoveredLifeCycleMethodTransformer();
    }

    public SporeDiscoveredLifeCycleMethodTransformer() {
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
            Map<String, LifeCycleMethodTarget> targets =
                    SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(classNode.name);
            if (targets.isEmpty()) {
                return null;
            }

            boolean modified = false;
            for (MethodNode method : classNode.methods) {
                LifeCycleMethodTarget target = targets.get(
                        SporeDiscoveredLifeCycleMethodRegistry.methodKey(method.name, method.desc)
                );
                if (target == null
                        || "<init>".equals(method.name)
                        || "<clinit>".equals(method.name)
                        || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                        || method.instructions == null
                        || method.instructions.size() == 0) {
                    continue;
                }
                boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
                if ((target.entitySource() == EntitySource.STATIC_ARGUMENTS) != isStatic) {
                    LogUtil.errorf(
                            "Skip discovered lifecycle method %s.%s%s: target mode %s conflicts with access %s",
                            classNode.name,
                            method.name,
                            method.desc,
                            target.entitySource(),
                            isStatic ? "static" : "instance"
                    );
                    continue;
                }
                HookSpec hookSpec = resolveHookSpec(method, target);
                if (hookSpec == null || alreadyCallsHook(method, hookSpec)) {
                    continue;
                }
                if (patchReturns(method, target, hookSpec)) {
                    modified = true;
                    LogUtil.logf("Transformed discovered lifecycle method %s.%s%s using %s/%s",
                            classNode.name,
                            method.name,
                            method.desc,
                            target.entitySource(),
                            target.category());
                }
            }
            if (modified) {
                return toBytes(loader, className == null ? classNode.name : className, classfileBuffer, classNode);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to transform discovered lifecycle class %s, %s",
                    className == null ? "<hidden-or-anonymous>" : className,
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private HookSpec resolveHookSpec(MethodNode method, LifeCycleMethodTarget target) {
        Type returnType;
        try {
            returnType = Type.getReturnType(method.desc);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (target.category() == LifeCycleMethodCategory.HEALTH) {
            if (returnType.getSort() == Type.FLOAT) {
                return new HookSpec("getHeealth", FLOAT_OBJECT_HEALTH_DESC,
                        Opcodes.FRETURN, Opcodes.FSTORE, Opcodes.FLOAD);
            }
            if (returnType.getSort() == Type.DOUBLE) {
                return new HookSpec("getHeealth", DOUBLE_OBJECT_HEALTH_DESC,
                        Opcodes.DRETURN, Opcodes.DSTORE, Opcodes.DLOAD);
            }
        } else if (returnType.getSort() == Type.BOOLEAN) {
            if (target.category() == LifeCycleMethodCategory.ALIVE) {
                return new HookSpec("isAlliive", BOOLEAN_OBJECT_DESC,
                        Opcodes.IRETURN, Opcodes.ISTORE, Opcodes.ILOAD);
            }
            if (target.category() == LifeCycleMethodCategory.DEAD_OR_DYING) {
                return new HookSpec("isDeeadfOrDyaging", BOOLEAN_OBJECT_DESC,
                        Opcodes.IRETURN, Opcodes.ISTORE, Opcodes.ILOAD);
            }
        }
        LogUtil.errorf("Skip discovered lifecycle method %s%s: category %s is incompatible with return %s",
                method.name,
                method.desc,
                target.category(),
                returnType.getDescriptor());
        return null;
    }

    private boolean patchReturns(MethodNode method,
                                 LifeCycleMethodTarget target,
                                 HookSpec hookSpec) {
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
        int[] usableIndexes = target.entitySource() == EntitySource.STATIC_ARGUMENTS
                ? usableEntityArgumentIndexes(target.entityArgumentIndexes(), argumentTypes)
                : new int[0];
        if (target.entitySource() == EntitySource.STATIC_ARGUMENTS && usableIndexes.length == 0) {
            return false;
        }

        Type returnType = Type.getReturnType(method.desc);
        int[] argumentSlots = argumentSlots(argumentTypes, isStatic);
        int resultLocal = Math.max(method.maxLocals, minimumLocals(argumentTypes, isStatic));
        method.maxLocals = resultLocal + returnType.getSize();
        boolean modified = false;

        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == hookSpec.returnOpcode()) {
                InsnList inject = new InsnList();
                inject.add(new VarInsnNode(hookSpec.storeOpcode(), resultLocal));
                if (target.entitySource() == EntitySource.INSTANCE_THIS) {
                    appendHook(inject, hookSpec, resultLocal, 0);
                } else {
                    for (int argumentIndex : usableIndexes) {
                        appendHook(inject, hookSpec, resultLocal, argumentSlots[argumentIndex]);
                    }
                }
                inject.add(new VarInsnNode(hookSpec.loadOpcode(), resultLocal));
                method.instructions.insertBefore(instruction, inject);
                modified = true;
            }
            instruction = next;
        }
        return modified;
    }

    private void appendHook(InsnList inject, HookSpec hookSpec, int resultLocal, int entityLocal) {
        inject.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                "L" + HOOK_INTERFACE + ";"
        ));
        inject.add(new VarInsnNode(hookSpec.loadOpcode(), resultLocal));
        inject.add(new VarInsnNode(Opcodes.ALOAD, entityLocal));
        inject.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE,
                hookSpec.methodName(),
                hookSpec.descriptor(),
                true
        ));
        inject.add(new VarInsnNode(hookSpec.storeOpcode(), resultLocal));
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

    private boolean alreadyCallsHook(MethodNode method, HookSpec hookSpec) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode call
                    && HOOK_INTERFACE.equals(call.owner)
                    && hookSpec.methodName().equals(call.name)
                    && hookSpec.descriptor().equals(call.desc)) {
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

    private record HookSpec(String methodName,
                            String descriptor,
                            int returnOpcode,
                            int storeOpcode,
                            int loadOpcode) {
    }
}
