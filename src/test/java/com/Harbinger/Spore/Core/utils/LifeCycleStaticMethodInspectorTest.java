package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifeCycleStaticMethodInspectorTest {
    private static final String STATIC_OWNER = "test/UnlabelledEntityUtil";
    private static final String STATIC_NAME = "calculate";
    private static final String STATIC_DESC = "(Ljava/lang/Object;)F";

    @Test
    void discoversSameThisArgumentAcrossTwoLifecycleKinds() throws Exception {
        LifeCycleStaticMethodInspector inspector = new LifeCycleStaticMethodInspector();
        Method inspect = declaredMethod("inspectStaticCalls");
        inspect.setAccessible(true);
        Class<?> kindClass = inspect.getParameterTypes()[2];

        inspect.invoke(inspector, "test/EntityFixture", healthMethod(), enumValue(kindClass, "HEALTH"));
        assertEquals(0, frequentMethods(inspector).size());

        inspect.invoke(inspector, "test/EntityFixture", deadMethod(), enumValue(kindClass, "DEAD_OR_DYING"));
        Set<?> frequent = frequentMethods(inspector);
        assertEquals(1, frequent.size());

        Object target = frequent.iterator().next();
        assertEquals(STATIC_OWNER, invokeAccessor(target, "owner"));
        assertEquals(STATIC_NAME, invokeAccessor(target, "name"));
        assertEquals(STATIC_DESC, invokeAccessor(target, "desc"));
        assertEquals(Set.of(0), invokeAccessor(target, "entityArgumentIndexes"));
    }

    @Test
    void doesNotTreatMaybeThisAsDefiniteThis() throws Exception {
        LifeCycleStaticMethodInspector inspector = new LifeCycleStaticMethodInspector();
        Method inspect = declaredMethod("inspectStaticCalls");
        inspect.setAccessible(true);
        Class<?> kindClass = inspect.getParameterTypes()[2];

        inspect.invoke(inspector, "test/EntityFixture", maybeThisMethod(), enumValue(kindClass, "HEALTH"));
        inspect.invoke(inspector, "test/EntityFixture", maybeThisMethod(), enumValue(kindClass, "DEAD_OR_DYING"));

        assertEquals(0, frequentMethods(inspector).size());
    }

    @Test
    void scansFinalClassAndFinalLifecycleMethodsWithoutBuildingWrapper() throws Exception {
        LifeCycleStaticMethodInspector inspector = new LifeCycleStaticMethodInspector();

        inspector.inspectAndCacheLifeCycleStaticMethods(FinalLifecycleEntity.class);

        String owner = FinalLifecycleStaticUtil.class.getName().replace('.', '/');
        assertTrue(frequentMethods(inspector).stream().anyMatch(method -> {
            try {
                return owner.equals(invokeAccessor(method, "owner"))
                        && "calculate".equals(invokeAccessor(method, "name"))
                        && Set.of(0).equals(invokeAccessor(method, "entityArgumentIndexes"));
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }));
    }

    private MethodNode healthMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getHealth", "()F", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, STATIC_OWNER, STATIC_NAME, STATIC_DESC, false);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 2);
        method.visitEnd();
        return method;
    }

    private MethodNode deadMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "isDeadOrDying", "()Z", null, null);
        MethodVisitor visitor = method;
        visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, STATIC_OWNER, STATIC_NAME, STATIC_DESC, false);
        visitor.visitInsn(Opcodes.FCONST_0);
        visitor.visitInsn(Opcodes.FCMPG);
        Label alive = new Label();
        visitor.visitJumpInsn(Opcodes.IFGT, alive);
        visitor.visitInsn(Opcodes.ICONST_1);
        visitor.visitInsn(Opcodes.IRETURN);
        visitor.visitLabel(alive);
        visitor.visitInsn(Opcodes.ICONST_0);
        visitor.visitInsn(Opcodes.IRETURN);
        visitor.visitMaxs(2, 1);
        visitor.visitEnd();
        return method;
    }

    private MethodNode maybeThisMethod() {
        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "maybeHealth",
                "(ZLjava/lang/Object;)F",
                null,
                null
        );
        method.visitCode();
        Label useOther = new Label();
        Label merged = new Label();
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitJumpInsn(Opcodes.IFEQ, useOther);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ASTORE, 3);
        method.visitJumpInsn(Opcodes.GOTO, merged);
        method.visitLabel(useOther);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitVarInsn(Opcodes.ASTORE, 3);
        method.visitLabel(merged);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, STATIC_OWNER, STATIC_NAME, STATIC_DESC, false);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 4);
        method.visitEnd();
        return method;
    }

    private Set<?> frequentMethods(LifeCycleStaticMethodInspector inspector) throws Exception {
        Method frequentMethods = declaredMethod("frequentMethods");
        frequentMethods.setAccessible(true);
        return (Set<?>) frequentMethods.invoke(inspector);
    }

    private Method declaredMethod(String name) {
        for (Method method : ClassReflectionUtil.getDeclaredMethods(LifeCycleStaticMethodInspector.class)) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        throw new IllegalStateException("Missing method " + name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object enumValue(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass, name);
    }

    private Object invokeAccessor(Object target, String accessor) throws Exception {
        Method method = target.getClass().getDeclaredMethod(accessor);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static final class FinalLifecycleEntity extends ArmorStand {
        private FinalLifecycleEntity(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public final float getFinalHealth() {
            return FinalLifecycleStaticUtil.calculate(this);
        }

        public final boolean isFinalDead() {
            return FinalLifecycleStaticUtil.calculate(this) <= 0.0F;
        }
    }

    private static final class FinalLifecycleStaticUtil {
        private static float calculate(FinalLifecycleEntity entity) {
            return entity == null ? 0.0F : 1.0F;
        }
    }
}
