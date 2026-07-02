package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ASMHurtArrowUtil implements IASMHurtArrow, Function<Class<?>, Class<?>> {
    public static final IASMHurtArrow INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IASMHurtArrow.class,
            ASMHurtArrowUtil.class
    );
    private static final String WRAPPER_SUFFIX="SporeEnhancedArrowWrapper";
    private static final String ON_HIT_ENTITY_NAME = "m_5790_";
    private static final String ENTITY_HIT_RESULT_DESC = "(Lnet/minecraft/world/phys/EntityHitResult;)V";
    private static final String ABSTRACT_ARROW_INTERNAL = "net/minecraft/world/entity/projectile/AbstractArrow";
    private static final String HOOK_OWNER = "com/Harbinger/Spore/Core/utils/ASMHurtArrowUtil";
    private static final String HOOK_INTERFACE_INTERNAL = "com/Harbinger/Spore/Core/utils/IASMHurtArrow";
    private static final String HOOK_METHOD_NAME = "onHitEntityHook";
    private static final String HOOK_METHOD_DESC = "(Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/phys/EntityHitResult;)V";
    private static final String HIDDEN_NAME_SEGMENT = "/0x";
    private final Map<Class<?>,Class<?>> WRAPPER_CACHE=new ConcurrentHashMap<>();
    private final Map<Class<?>,Class<?>> WRAPPER_TO_ORIGINAL_CACHE=new ConcurrentHashMap<>();
    @Override
    public void wrap(Object arrow){
        Class<?> wrapper=getWrapper(arrow.getClass());
        if(wrapper!=null){
            KlassPointerUtil.INSTANCE.replaceClass(arrow,wrapper,"",0,0.0f);
        }
    }
    @Override
    public Class<?> getOrginalClass(Class<?> wrapperValue){
        return WRAPPER_TO_ORIGINAL_CACHE.getOrDefault(wrapperValue,wrapperValue);
    }
    private Class<?> getWrapper(Class<?> original) {
        original=getOrginalClass(original);
        if(original==null||!Projectile.class.isAssignableFrom(original)||Modifier.isFinal(original.getModifiers())) {
            return null;
        }
        if(original.getName().contains(WRAPPER_SUFFIX)) {
            return original;
        }
        Class<?> wrapper=WRAPPER_CACHE.computeIfAbsent(original, this);
        if(wrapper!=null){
            WRAPPER_TO_ORIGINAL_CACHE.putIfAbsent(wrapper,original);
        }
        return wrapper;
    }
    private Class<?> buildWrapperClass(Class<?> original) {
        try {
            if (original == null || !AbstractArrow.class.isAssignableFrom(original) || Modifier.isFinal(original.getModifiers())) {
                return null;
            }
            if (original.getName().contains(WRAPPER_SUFFIX)) {
                return original;
            }
            if (!canOverrideOnHitEntity(original)) {
                return null;
            }

            ClassNode node = new ClassNode();
            String superName = Type.getInternalName(original);
            String wrapperName = buildWrapperInternalName(original);

            node.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, wrapperName, null, superName, null);
            node.visitSource(".dynamic", null);

            if (!emitConstructors(node, original, superName)) {
                return null;
            }
            emitOnHitEntityWrapper(node, superName);
            node.visitEnd();

            return ClassLoaderUtil.INSTANCE.deffineneHiddenClazz(node, original);
        } catch (Throwable throwable) {
            LogUtil.errorf("failed to build arrow wrapper class %s", throwable.getMessage());
            LogUtil.printStackTrace(throwable);
            return null;
        }
    }

    @Override
    public Class<?> apply(Class<?> original) {
        return buildWrapperClass(original);
    }
    @Override
    public void onHitEntityHook(Projectile projectile, EntityHitResult result) {
        if (projectile.level().isClientSide) {
            return;
        }
        Entity target = ParentUtil.INSTANCE.getUltimateParent(result.getEntity());
        if(!(target instanceof LivingEntity liv)||(target instanceof Player)){
            return;
        }
        Entity owner = projectile.getOwner();
        if(!(owner instanceof LivingEntity own)){
            return;
        }
        DamageSource source=projectile.level().damageSources().mobProjectile(projectile, own);
        SporeAttackUtil.INSTANCE.dealDamage(liv,own,source, projectile instanceof AbstractArrow arrow?(float)arrow.getBaseDamage():1.0f);
        liv.invulnerableTime=0;
    }

    private String buildWrapperInternalName(Class<?> original) {
        String stableBinaryName = toStableBinaryName(original);
        int lastDot = stableBinaryName.lastIndexOf('.');
        String pkg = lastDot >= 0 ? stableBinaryName.substring(0, lastDot) : "";
        String simple = lastDot >= 0 ? stableBinaryName.substring(lastDot + 1) : stableBinaryName;
        if (simple.isEmpty()) {
            simple = "SporeDynamicArrow";
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

    private boolean emitConstructors(ClassNode node, Class<?> original, String superName) {
        boolean added = false;
        for (Constructor<?> ctor : original.getDeclaredConstructors()) {
            int modifiers = ctor.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                continue;
            }

            int access = 0;
            if (Modifier.isPublic(modifiers)) {
                access |= Opcodes.ACC_PUBLIC;
            } else if (Modifier.isProtected(modifiers)) {
                access |= Opcodes.ACC_PROTECTED;
            }

            String desc = Type.getConstructorDescriptor(ctor);
            MethodVisitor mv = node.visitMethod(access, "<init>", desc, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);

            Type[] args = Type.getArgumentTypes(desc);
            int slot = 1;
            for (Type arg : args) {
                mv.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), slot);
                slot += arg.getSize();
            }

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", desc, false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            added = true;
        }
        return added;
    }

    private void emitOnHitEntityWrapper(ClassNode node, String superName) {
        MethodVisitor mv = node.visitMethod(Opcodes.ACC_PUBLIC, ON_HIT_ENTITY_NAME, ENTITY_HIT_RESULT_DESC, null, null);
        mv.visitCode();
        mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                HOOK_OWNER,
                "INSTANCE",
                "L" + HOOK_INTERFACE_INTERNAL + ";"
        );
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                HOOK_INTERFACE_INTERNAL,
                HOOK_METHOD_NAME,
                HOOK_METHOD_DESC,
                true
        );

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, ON_HIT_ENTITY_NAME, ENTITY_HIT_RESULT_DESC, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private boolean canOverrideOnHitEntity(Class<?> original) {
        for (Class<?> cursor = original; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            try {
                for (java.lang.reflect.Method method : cursor.getDeclaredMethods()) {
                    if (!ON_HIT_ENTITY_NAME.equals(method.getName())) {
                        continue;
                    }
                    if (!ENTITY_HIT_RESULT_DESC.equals(Type.getMethodDescriptor(method))) {
                        continue;
                    }
                    int modifiers = method.getModifiers();
                    return !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers) && !Modifier.isFinal(modifiers);
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            java.lang.reflect.Method method = AbstractArrow.class.getDeclaredMethod(ON_HIT_ENTITY_NAME, EntityHitResult.class);
            int modifiers = method.getModifiers();
            return !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers) && !Modifier.isFinal(modifiers);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
