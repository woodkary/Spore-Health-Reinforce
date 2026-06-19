package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author karywoodOyo
 */
public final class AllReturnUtil implements IAllReturn {
    public static final IAllReturn INSTANCE = BytecodeUtil.createHiddenSingletonInstance(IAllReturn.class, AllReturnUtil.class);
    private static final String WRAPPER_SUFFIX = "SporeAllReturnWrapper";
    private static final String HIDDEN_NAME_SEGMENT = "/0x";

    public AllReturnUtil() {
    }

    @Override
    public void ttranssansformNode(ClassNode target, Class<?> superClass) {
        String superName = Type.getInternalName(superClass);
        String wrapperName = buildWrapperInternalName(superClass);

        // public class SuperClassRemovedWrapper extends SuperClass
        target.visit(Opcodes.V17, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, wrapperName, null, superName, null);
        target.visitSource(".dynamic", null);

        Set<String> visited = new HashSet<>();

        do {
            for (Method method : superClass.getDeclaredMethods()) {
                String name = method.getName();
                String desc = Type.getMethodDescriptor(method);
                String sig = name + desc;
                if (visited.contains(sig)) {
                    continue;
                }
                int mod = method.getModifiers();
                if(Modifier.isFinal(mod)){
                    visited.add(sig);
                    continue;
                }
                if (Modifier.isStatic(mod) || Modifier.isPrivate(mod)||Modifier.isInterface(mod))
                    continue;

                if (!(Modifier.isPublic(mod) || Modifier.isProtected(mod)))
                    continue;

                visited.add(sig);

                generateWrappedMethod(target, superClass, method, name, desc);
            }

            superClass = superClass.getSuperclass();

        } while (superClass != null && superClass != Object.class);

        target.visitEnd();
    }
    private String buildWrapperInternalName(Class<?> superClass) {
        String stableBinaryName = toStableBinaryName(superClass);
        int lastDot = stableBinaryName.lastIndexOf('.');
        String pkg = lastDot >= 0 ? stableBinaryName.substring(0, lastDot) : "";
        String simple = lastDot >= 0 ? stableBinaryName.substring(lastDot + 1) : stableBinaryName;
        if (simple.isEmpty()) {
            simple = "SporeDynamicHost";
        }
        if (pkg.isEmpty()) {
            return simple + WRAPPER_SUFFIX;
        }
        return pkg.replace('.', '/') + "/" + simple + WRAPPER_SUFFIX;
    }
    private String toStableBinaryName(Class<?> clazz) {
        String name = clazz.getName();
        int hidden = name.indexOf(HIDDEN_NAME_SEGMENT);
        if (hidden > 0) {
            return name.substring(0, hidden);
        }
        return name;
    }
    private void generateWrappedMethod(ClassNode target,
                                              Class<?> owner,
                                              Method method,
                                              String name,
                                              String desc) {

        int access = method.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
        MethodVisitor mv = target.visitMethod(access, name, desc, null, null);
        mv.visitCode();

        Type ret = Type.getReturnType(desc);
        if (isGetStackTraceMethod(name, ret)) {
            // Thread#getStackTrace 特化：返回空数组，避免默认数组元素为 null 导致后续 NPE
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/StackTraceElement");
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (isObfGetUuidMethod(name, desc)) {
            // 实体 UUID 特化：始终返回随机 UUID
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/util/UUID",
                    "randomUUID",
                    "()Ljava/util/UUID;",
                    false
            );
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (isObfGetBlockPosMethod(name, desc)) {
            // BlockEntity#getBlockPos 特化：返回统一无效坐标，阻断后续位置访问链
            mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/core/BlockPos");
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitIntInsn(Opcodes.SIPUSH, -2048);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/core/BlockPos", "<init>", "(III)V", false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (isObfGetBlockStateMethod(name, desc)) {
            // BlockEntity#getBlockState 特化：强制返回空气方块状态
            // 等价于 Blocks.f_50016_.m_49966_() (Blocks.AIR.defaultBlockState())
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "net/minecraft/world/level/block/Blocks",
                    "f_50016_",
                    "Lnet/minecraft/world/level/block/Block;"
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/world/level/block/Block",
                    "m_49966_",
                    "()Lnet/minecraft/world/level/block/state/BlockState;",
                    false
            );
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (isObfGetEntityDataMethod(owner, name, desc)) {
            // Entity#getEntityData(m_20088_) 特化：返回全局维护的空实体数据
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "com/Harbinger/Spore/Core/asmHooks/EntityHeealuthManager",
                    "INSTANCE",
                    "Lcom/Harbinger/Spore/Core/asmHooks/IEntityHealth;"
            );
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "com/Harbinger/Spore/Core/asmHooks/IEntityHealth",
                    "getEmptyEntityData",
                    "(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/syncher/SynchedEntityData;",
                    true
            );
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (shouldForceSuperForLootDropMethods(owner, name, desc)) {
            // 生物掉落逻辑特化：这些 void 方法不能被短路为空实现，否则会丢失正常掉落
            loadAllArguments(mv, method);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    owner.getName().replace('.', '/'),
                    name,
                    desc,
                    false
            );
            mv.visitInsn(getReturnOpcode(ret));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        if (canUseEmitDefaultReturnValue(ret)) {
            // 可被 emitDefaultReturnValue 覆盖的返回类型：直接默认返回
            emitDefaultReturnValue(mv, name, ret);
        } else {
            // 复杂对象类型：回退到 super.xxx(...)
            String invokeOwnerInternal = resolveComplexObjectInvokeOwner(owner, method, desc);
            loadAllArguments(mv, method);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    invokeOwnerInternal,
                    name,
                    desc,
                    false
            );
            mv.visitInsn(getReturnOpcode(ret));
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    private void emitDefaultReturnValue(MethodVisitor mv,String name, Type type) {
        switch (type.getSort()) {

            case Type.VOID:
                mv.visitInsn(Opcodes.RETURN);
                return;

            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                if(name.equals("m_213877_")||
                        name.equals("m_58901_")||
                        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.nameLooksLikeIsDeadOrDying(name)){
                    mv.visitInsn(Opcodes.ICONST_1);
                }else{
                    mv.visitInsn(Opcodes.ICONST_0);
                }
                mv.visitInsn(Opcodes.IRETURN);
                return;

            case Type.FLOAT:
                mv.visitInsn(Opcodes.FCONST_0);
                mv.visitInsn(Opcodes.FRETURN);
                return;

            case Type.LONG:
                mv.visitInsn(Opcodes.LCONST_0);
                mv.visitInsn(Opcodes.LRETURN);
                return;

            case Type.DOUBLE:
                if(name.equals("m_20185_")||
                        name.equals("m_20165_")||
                        name.equals("m_20186_")||
                        name.equals("m_20227_")||
                        name.equals("m_20189_")||
                        name.equals("m_20246_")){
                    //返回NaN
                    mv.visitLdcInsn(Double.doubleToRawLongBits(Double.NaN));
                    mv.visitInsn(Opcodes.L2D);
                }else {
                    mv.visitInsn(Opcodes.DCONST_0);
                }
                mv.visitInsn(Opcodes.DRETURN);
                return;

            case Type.OBJECT:
                String internalName = type.getInternalName();

                // 判断是否是数字/布尔/字符包装类
                switch (internalName) {
                    case "java/lang/Integer":
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Integer",
                                "valueOf",
                                "(I)Ljava/lang/Integer;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Long":
                        mv.visitInsn(Opcodes.LCONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Long",
                                "valueOf",
                                "(J)Ljava/lang/Long;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Float":
                        mv.visitInsn(Opcodes.FCONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Float",
                                "valueOf",
                                "(F)Ljava/lang/Float;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Double":
                        mv.visitInsn(Opcodes.DCONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Double",
                                "valueOf",
                                "(D)Ljava/lang/Double;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Short":
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Short",
                                "valueOf",
                                "(S)Ljava/lang/Short;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Byte":
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Byte",
                                "valueOf",
                                "(B)Ljava/lang/Byte;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Boolean":
                        mv.visitInsn(Opcodes.ICONST_0); // false
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Boolean",
                                "valueOf",
                                "(Z)Ljava/lang/Boolean;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "java/lang/Character":
                        mv.visitInsn(Opcodes.ICONST_0); // '\0'
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "java/lang/Character",
                                "valueOf",
                                "(C)Ljava/lang/Character;",
                                false);
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    case "net/minecraft/world/phys/Vec3":
                        mv.visitFieldInsn(
                                Opcodes.GETSTATIC,
                                "com/Harbinger/Spore/Core/utils/simpleRemoval/SimpleRemoveUtil",
                                "INSTANCE",
                                "Lcom/Harbinger/Spore/Core/utils/simpleRemoval/ISimpleRemoval;"
                        );
                        mv.visitMethodInsn(
                                Opcodes.INVOKEINTERFACE,
                                "com/Harbinger/Spore/Core/utils/simpleRemoval/ISimpleRemoval",
                                "getNaNPosition",
                                "()Lnet/minecraft/world/phys/Vec3;",
                                true
                        );
                        mv.visitInsn(Opcodes.ARETURN);
                        return;

                    default:
                        // 不是包装类 → 不注入，保留原方法
                        return;
                }


            case Type.ARRAY:
                // 数组返回一个长度为 50 的数组
                Type element = type.getElementType();
                int sort = element.getSort();

                mv.visitIntInsn(Opcodes.BIPUSH, 50); // push length=50

                if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                    // primitive array (NEWARRAY)
                    mv.visitIntInsn(
                            Opcodes.NEWARRAY,
                            getOpcode(sort, 10) // 你已有的 getOpcode
                    );
                } else {
                    // object array (ANEWARRAY)
                    mv.visitTypeInsn(Opcodes.ANEWARRAY, element.getInternalName());
                }

                mv.visitInsn(Opcodes.ARETURN);
                return;


            default:
        }
    }
    private int getOpcode(int sort, int opcode) {
        return switch (sort) {
            case Type.BOOLEAN -> 4;
            case Type.CHAR -> 5;
            case Type.BYTE -> 8;
            case Type.SHORT -> 9;
            case Type.INT -> 10;
            case Type.FLOAT -> 6;
            case Type.LONG -> 11;
            case Type.DOUBLE -> 7;
            default -> throw new UnsupportedOperationException();
        };

    }
    private void loadAllArguments(MethodVisitor mv, Method method) {
        int index = 0;

        // this
        mv.visitVarInsn(Opcodes.ALOAD, index++);

        for (Class<?> param : method.getParameterTypes()) {
            Type t = Type.getType(param);
            mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), index);
            index += t.getSize();
        }
    }
    private int getReturnOpcode(Type ret) {
        int sort = ret.getSort();
        return switch (sort) {
            case Type.VOID -> Opcodes.RETURN;
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> Opcodes.IRETURN;
            case Type.FLOAT -> Opcodes.FRETURN;
            case Type.LONG -> Opcodes.LRETURN;
            case Type.DOUBLE -> Opcodes.DRETURN;
            case Type.ARRAY, Type.OBJECT -> Opcodes.ARETURN;
            default -> throw new IllegalArgumentException("Unsupported return type sort: " + sort + " (" + ret + ")");
        };
    }
    private boolean isGetStackTraceMethod(String name, Type ret) {
        if (!"getStackTrace".equals(name)) {
            return false;
        }
        if (ret.getSort() != Type.ARRAY) {
            return false;
        }
        Type element = ret.getElementType();
        return element != null
                && element.getSort() == Type.OBJECT
                && "java/lang/StackTraceElement".equals(element.getInternalName());
    }
    private boolean isObfGetUuidMethod(String name, String desc) {
        return "m_20148_".equals(name) && "()Ljava/util/UUID;".equals(desc);
    }
    private boolean isObfGetBlockPosMethod(String name, String desc) {
        return "m_58899_".equals(name) && "()Lnet/minecraft/core/BlockPos;".equals(desc);
    }
    private boolean isObfGetBlockStateMethod(String name, String desc) {
        return "m_58900_".equals(name) && "()Lnet/minecraft/world/level/block/state/BlockState;".equals(desc);
    }
    private boolean isObfGetEntityDataMethod(Class<?> owner, String name, String desc) {
        return Entity.class.isAssignableFrom(owner)
                && "m_20088_".equals(name)
                && "()Lnet/minecraft/network/syncher/SynchedEntityData;".equals(desc);
    }
    private boolean shouldForceSuperForLootDropMethods(Class<?> owner, String name, String desc) {
        if (!LivingEntity.class.isAssignableFrom(owner)) {
            return false;
        }
        if ("(Lnet/minecraft/world/damagesource/DamageSource;IZ)V".equals(desc)) {
            return "dropCustomDeathLoot".equals(name) || "m_7472_".equals(name);
        }
        if ("()V".equals(desc)) {
            return "dropEquipment".equals(name) || "m_5907_".equals(name);
        }
        if ("()I".equals(desc)) {
            return "getExperienceReward".equals(name) || "m_213860_".equals(name);
        }
        return false;
    }

    private boolean canUseEmitDefaultReturnValue(Type type) {
        int sort = type.getSort();
        if (sort == Type.VOID
                || sort == Type.BOOLEAN
                || sort == Type.BYTE
                || sort == Type.CHAR
                || sort == Type.SHORT
                || sort == Type.INT
                || sort == Type.FLOAT
                || sort == Type.LONG
                || sort == Type.DOUBLE
                || sort == Type.ARRAY) {
            return true;
        }
        if (sort != Type.OBJECT) {
            return false;
        }
        String internalName = type.getInternalName();
        return "java/lang/Integer".equals(internalName)
                || "java/lang/Long".equals(internalName)
                || "java/lang/Float".equals(internalName)
                || "java/lang/Double".equals(internalName)
                || "java/lang/Short".equals(internalName)
                || "java/lang/Byte".equals(internalName)
                || "java/lang/Boolean".equals(internalName)
                || "java/lang/Character".equals(internalName)
                || "net/minecraft/world/phys/Vec3".equals(internalName);
    }

    private String resolveComplexObjectInvokeOwner(Class<?> owner, Method method, String desc) {
        String ownerInternal = owner.getName().replace('.', '/');
        Class<?> cursor = owner.getSuperclass();
        String targetName = method.getName();
        while (cursor != null) {
            try {
                Method[] declaredMethods = cursor.getDeclaredMethods();
                for (Method candidate : declaredMethods) {
                    if (!targetName.equals(candidate.getName())) {
                        continue;
                    }
                    if (!desc.equals(Type.getMethodDescriptor(candidate))) {
                        continue;
                    }
                    if (isInvokableSuperTarget(candidate, desc)) {
                        // 从 owner 父类开始，命中第一个可调用同签名方法就直接使用
                        return cursor.getName().replace('.', '/');
                    }
                }
            } catch (Throwable ignored) {
            }
            cursor = cursor.getSuperclass();
        }
        return ownerInternal;
    }

    private boolean isInvokableSuperTarget(Method method, String desc) {
        if (method == null) {
            return false;
        }
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) {
            return false;
        }
        // abstract/native 视为“无可用 Java 方法体”，不作为 invokespecial 目标
        if (Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return false;
        }
        return desc.equals(Type.getMethodDescriptor(method));
    }

}
