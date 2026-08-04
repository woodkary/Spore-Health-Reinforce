package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.agents.transformers.EntitySource;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodCategory;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodTarget;
import com.Harbinger.Spore.Core.agents.transformers.SporeDiscoveredLifeCycleMethodRegistry;
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
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifeCycleInvocationInspectorTest {
    private static final String STATIC_OWNER = "test/UnlabelledEntityUtil";
    private static final String STATIC_NAME = "calculate";
    private static final String STATIC_DESC = "(Ljava/lang/Object;)F";
    private static final String BOOLEAN_STATIC_OWNER = "test/UnlabelledBooleanEntityUtil";
    private static final String BOOLEAN_STATIC_NAME = "calculateFlag";
    private static final String BOOLEAN_STATIC_DESC = "(Ljava/lang/Object;)Z";

    @Test
    void discoversSameThisArgumentAcrossTwoLifecycleKinds() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        Method inspect = declaredMethod("inspectInvocations");
        inspect.setAccessible(true);
        MethodNode health = healthMethod();
        MethodNode dead = deadMethod();

        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", health,
                lifeCycleMethod(health));
        assertEquals(0, frequentMethods(inspector).size());

        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", dead,
                lifeCycleMethod(dead));
        Object target = frequentMethods(inspector).iterator().next();
        assertEquals(STATIC_OWNER, invokeAccessor(target, "owner"));
        assertEquals(STATIC_NAME, invokeAccessor(target, "name"));
        assertEquals(STATIC_DESC, invokeAccessor(target, "desc"));
        LifeCycleMethodTarget healthTarget = (LifeCycleMethodTarget) invokeAccessor(target, "target");
        assertEquals(EntitySource.STATIC_ARGUMENTS, healthTarget.entitySource());
        assertEquals(LifeCycleMethodCategory.HEALTH, healthTarget.category());
        assertArrayEquals(new int[]{0}, healthTarget.entityArgumentIndexes());
    }

    @Test
    void doesNotCountRepeatedCallsFromOneLifecycleKindAsTwoKinds() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        Method inspect = declaredMethod("inspectInvocations");
        inspect.setAccessible(true);
        MethodNode health = healthMethod();

        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", health,
                lifeCycleMethod(health));
        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", health,
                lifeCycleMethod(health));

        assertEquals(0, frequentMethods(inspector).size());
    }

    @Test
    void doesNotTreatMaybeThisAsDefiniteThis() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        Method inspect = declaredMethod("inspectInvocations");
        inspect.setAccessible(true);
        MethodNode maybeHealth = maybeThisMethod();

        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", maybeHealth,
                new LifeCycleMethod("getHealth", "()F"));
        inspect.invoke(inspector, FinalLifecycleEntity.class, "test/EntityFixture", maybeHealth,
                new LifeCycleMethod("isDeadOrDying", "()Z"));

        assertEquals(0, frequentMethods(inspector).size());
    }

    @Test
    void scansFinalClassAndFinalLifecycleMethodsWithoutBuildingWrapper() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspector.inspectAndCacheLifeCycleInvocations(FinalLifecycleEntity.class);

        String owner = internalName(FinalLifecycleStaticUtil.class);
        assertTrue(hasDiscoveredTarget(inspector, owner, "calculate", EntitySource.STATIC_ARGUMENTS));
    }

    @Test
    @SuppressWarnings("unchecked")
    void hiddenSingletonUsesItsExplicitFunctionForInspectionCache() throws Exception {
        Object hiddenInspector = LifeCycleInvocationInspector.INSTANCE;
        assertTrue(hiddenInspector instanceof Function<?, ?>);
        Set<LifeCycleMethod> freshSet =
                ((Function<Object, Set<LifeCycleMethod>>) hiddenInspector).apply(new Object());
        assertTrue(freshSet.isEmpty());

        LifeCycleInvocationInspector.INSTANCE.inspectAndCacheLifeCycleInvocations(FinalLifecycleEntity.class);

        assertTrue(hasDiscoveredTarget(
                hiddenInspector,
                internalName(FinalLifecycleStaticUtil.class),
                "calculate",
                EntitySource.STATIC_ARGUMENTS
        ));

        Method inspect = declaredMethod(hiddenInspector.getClass(), "inspectInvocations");
        inspect.setAccessible(true);
        MethodNode alive = booleanStaticMethod(
                "m_6084_",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.DIRECT
        );
        MethodNode dead = booleanStaticMethod(
                "m_21224_",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.NEGATED
        );
        inspect.invoke(hiddenInspector, FinalLifecycleEntity.class, "test/HiddenBooleanFixture",
                alive, lifeCycleMethod(alive));
        inspect.invoke(hiddenInspector, FinalLifecycleEntity.class, "test/HiddenBooleanFixture",
                dead, lifeCycleMethod(dead));
        assertTrue(hasDiscoveredTarget(
                hiddenInspector,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        ));
    }

    @Test
    void discoversInstanceReceiverThisAcrossWideArgumentsAndExcludesNamedHealthMethods() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspector.inspectAndCacheLifeCycleInvocations(InstanceLifecycleEntity.class);

        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(InstanceLifecycleEntity.class),
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
        assertFalse(hasDiscoveredTarget(
                inspector,
                internalName(InstanceLifecycleEntity.class),
                "calculateHealthState",
                EntitySource.INSTANCE_THIS
        ));
    }

    @Test
    void discoversInvokeInterfaceReceiverThisAcrossWideArguments() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        Method inspect = declaredMethod("inspectInvocations");
        inspect.setAccessible(true);
        String className = internalName(InterfaceInvocationEntity.class);
        MethodNode health = interfaceHealthMethod();
        MethodNode dead = interfaceDeadMethod();

        inspect.invoke(inspector, InterfaceInvocationEntity.class, className, health,
                lifeCycleMethod(health));
        inspect.invoke(inspector, InterfaceInvocationEntity.class, className, dead,
                lifeCycleMethod(dead));

        assertTrue(hasDiscoveredTarget(
                inspector,
                className,
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
    }

    @Test
    void resolvesInheritedMethodToClassThatContainsTheBody() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspector.inspectAndCacheLifeCycleInvocations(InheritedLifecycleChild.class);

        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(InheritedLifecycleParent.class),
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
        assertFalse(hasDiscoveredTarget(
                inspector,
                internalName(InheritedLifecycleChild.class),
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
    }

    @Test
    void inspectsSharedParentLifecycleSeparatelyForEachRootOverride() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspector.inspectAndCacheLifeCycleInvocations(RootCacheLifecycleChildA.class);
        inspector.inspectAndCacheLifeCycleInvocations(RootCacheLifecycleChildB.class);

        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(RootCacheLifecycleChildA.class),
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(RootCacheLifecycleChildB.class),
                "calculateState",
                EntitySource.INSTANCE_THIS
        ));
    }

    @Test
    void classifiesStaticBooleanDirectAliveNegatedDeadAsAlive() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.DIRECT));
        assertEquals(0, frequentMethods(inspector).size());
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));

        assertTrue(hasDiscoveredTarget(
                inspector,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        ));
    }

    @Test
    void classifiesInstanceBooleanNegatedAliveDirectDeadAsDeadOrDying() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_6084_", BooleanPolarity.NEGATED));
        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_21224_", BooleanPolarity.DIRECT));

        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(BooleanInstanceEntity.class),
                "calculateFlag",
                EntitySource.INSTANCE_THIS,
                LifeCycleMethodCategory.DEAD_OR_DYING
        ));
    }

    @Test
    void excludesBooleanInstanceHelpersWhoseNamesAlreadyDescribeLifecycle() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_6084_", "calculateAliveFlag", BooleanPolarity.DIRECT));
        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_21224_", "calculateAliveFlag", BooleanPolarity.NEGATED));
        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_6084_", "calculateDeadFlag", BooleanPolarity.DIRECT));
        inspectMethod(inspector, BooleanInstanceEntity.class,
                booleanInstanceMethod("m_21224_", "calculateDeadFlag", BooleanPolarity.NEGATED));

        assertEquals(0, frequentMethods(inspector).size());
    }

    @Test
    void rejectsSameUnknownAndNonExactBooleanEvidence() throws Exception {
        LifeCycleInvocationInspector direct = new LifeCycleInvocationInspector();
        inspectMethod(direct, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.DIRECT));
        inspectMethod(direct, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.DIRECT));
        assertEquals(0, frequentMethods(direct).size());

        LifeCycleInvocationInspector negated = new LifeCycleInvocationInspector();
        inspectMethod(negated, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));
        inspectMethod(negated, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));
        assertEquals(0, frequentMethods(negated).size());

        LifeCycleInvocationInspector unknown = new LifeCycleInvocationInspector();
        inspectMethod(unknown, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.UNKNOWN));
        inspectMethod(unknown, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));
        assertEquals(0, frequentMethods(unknown).size());

        LifeCycleInvocationInspector nonExact = new LifeCycleInvocationInspector();
        inspectMethod(nonExact, FinalLifecycleEntity.class,
                booleanStaticMethod("isAliveCustom", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.DIRECT));
        inspectMethod(nonExact, FinalLifecycleEntity.class,
                booleanStaticMethod("isDeadCustom", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));
        assertEquals(0, frequentMethods(nonExact).size());
    }

    @Test
    void invalidatesPreviouslyRegisteredBooleanTargetWhenLaterEvidenceHasMixedPolarity() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.DIRECT));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));
        Object discovered = frequentMethods(inspector).iterator().next();
        LifeCycleMethodTarget target = (LifeCycleMethodTarget) invokeAccessor(discovered, "target");
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.register(
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                BOOLEAN_STATIC_DESC,
                target
        ));

        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", BOOLEAN_STATIC_DESC, 0, BooleanPolarity.NEGATED));

        assertEquals(0, frequentMethods(inspector).size());
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.isInvalid(
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                BOOLEAN_STATIC_DESC
        ));
        assertFalse(SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(BOOLEAN_STATIC_OWNER)
                .containsKey(BOOLEAN_STATIC_NAME + BOOLEAN_STATIC_DESC));
    }

    @Test
    void rejectsBooleanStaticMethodWhenEntityArgumentsImplyConflictingCategories() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String descriptor = "(Ljava/lang/Object;Ljava/lang/Object;)Z";

        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", descriptor, 0, BooleanPolarity.DIRECT));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", descriptor, 0, BooleanPolarity.NEGATED));
        Object initiallyDiscovered = frequentMethods(inspector).iterator().next();
        LifeCycleMethodTarget initialTarget = (LifeCycleMethodTarget) invokeAccessor(initiallyDiscovered, "target");
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.register(
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                descriptor,
                initialTarget
        ));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_6084_", descriptor, 1, BooleanPolarity.NEGATED));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                booleanStaticMethod("m_21224_", descriptor, 1, BooleanPolarity.DIRECT));

        assertEquals(0, frequentMethods(inspector).size());
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.isInvalid(
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                descriptor
        ));
        assertFalse(SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(BOOLEAN_STATIC_OWNER)
                .containsKey(BOOLEAN_STATIC_NAME + descriptor));
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
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "maybeHealth", "(ZLjava/lang/Object;)F", null, null);
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

    private MethodNode booleanStaticMethod(String lifecycleName,
                                           String helperDescriptor,
                                           int thisArgumentIndex,
                                           BooleanPolarity polarity) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, lifecycleName, "()Z", null, null);
        method.visitCode();
        int argumentCount = org.objectweb.asm.Type.getArgumentTypes(helperDescriptor).length;
        for (int argumentIndex = 0; argumentIndex < argumentCount; argumentIndex++) {
            if (argumentIndex == thisArgumentIndex) {
                method.visitVarInsn(Opcodes.ALOAD, 0);
            } else {
                method.visitInsn(Opcodes.ACONST_NULL);
            }
        }
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                helperDescriptor,
                false
        );
        appendPolarity(method, polarity);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(Math.max(2, argumentCount), 1);
        method.visitEnd();
        return method;
    }

    private MethodNode booleanInstanceMethod(String lifecycleName, BooleanPolarity polarity) {
        return booleanInstanceMethod(lifecycleName, "calculateFlag", polarity);
    }

    private MethodNode booleanInstanceMethod(String lifecycleName,
                                             String helperName,
                                             BooleanPolarity polarity) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, lifecycleName, "()Z", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                internalName(BooleanInstanceEntity.class),
                helperName,
                "()Z",
                false
        );
        appendPolarity(method, polarity);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 1);
        method.visitEnd();
        return method;
    }

    private void appendPolarity(MethodNode method, BooleanPolarity polarity) {
        if (polarity == BooleanPolarity.NEGATED) {
            method.visitInsn(Opcodes.ICONST_1);
            method.visitInsn(Opcodes.IXOR);
        } else if (polarity == BooleanPolarity.UNKNOWN) {
            method.visitInsn(Opcodes.ICONST_1);
            method.visitInsn(Opcodes.IAND);
        }
    }

    private MethodNode interfaceHealthMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getHealth", "()F", null, null);
        method.visitCode();
        appendInterfaceStateCall(method);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(5, 1);
        method.visitEnd();
        return method;
    }

    private MethodNode interfaceDeadMethod() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "isDeadOrDying", "()Z", null, null);
        method.visitCode();
        appendInterfaceStateCall(method);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPG);
        Label alive = new Label();
        method.visitJumpInsn(Opcodes.IFGT, alive);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitLabel(alive);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(5, 1);
        method.visitEnd();
        return method;
    }

    private void appendInterfaceStateCall(MethodNode method) {
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.LCONST_1);
        method.visitInsn(Opcodes.DCONST_1);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                internalName(InstanceValueContract.class),
                "calculateState",
                "(JD)F",
                true
        );
    }

    private boolean hasDiscoveredTarget(Object inspector,
                                        String owner,
                                        String name,
                                        EntitySource source) throws Exception {
        return hasDiscoveredTarget(
                inspector,
                owner,
                name,
                source,
                LifeCycleMethodCategory.HEALTH
        );
    }

    private boolean hasDiscoveredTarget(Object inspector,
                                        String owner,
                                        String name,
                                        EntitySource source,
                                        LifeCycleMethodCategory category) throws Exception {
        for (Object method : frequentMethods(inspector)) {
            LifeCycleMethodTarget target = (LifeCycleMethodTarget) invokeAccessor(method, "target");
            if (owner.equals(invokeAccessor(method, "owner"))
                    && name.equals(invokeAccessor(method, "name"))
                    && source == target.entitySource()
                    && target.category() == category) {
                return true;
            }
        }
        return false;
    }

    private Set<?> frequentMethods(Object inspector) throws Exception {
        Method frequentMethods = declaredMethod(inspector.getClass(), "frequentMethods");
        frequentMethods.setAccessible(true);
        return (Set<?>) frequentMethods.invoke(inspector);
    }

    private Method declaredMethod(String name) {
        return declaredMethod(LifeCycleInvocationInspector.class, name);
    }

    private Method declaredMethod(Class<?> type, String name) {
        for (Method method : ClassReflectionUtil.getDeclaredMethods(type)) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        throw new IllegalStateException("Missing method " + name);
    }

    private LifeCycleMethod lifeCycleMethod(MethodNode method) {
        return new LifeCycleMethod(method.name, method.desc);
    }

    private void inspectMethod(LifeCycleInvocationInspector inspector,
                               Class<?> rootEntityClass,
                               MethodNode method) throws Exception {
        Method inspect = declaredMethod("inspectInvocations");
        inspect.setAccessible(true);
        inspect.invoke(inspector, rootEntityClass, internalName(rootEntityClass), method, lifeCycleMethod(method));
    }

    private Object invokeAccessor(Object target, String accessor) throws Exception {
        Method method = target.getClass().getDeclaredMethod(accessor);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private String internalName(Class<?> type) {
        return type.getName().replace('.', '/');
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

    private static class InstanceLifecycleEntity extends ArmorStand {
        private InstanceLifecycleEntity(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public float getCalculatedHealth() {
            return calculateState(1L, 2.0D);
        }

        public boolean isCalculatedDead() {
            return calculateState(1L, 2.0D) <= 0.0F;
        }

        public float getNamedHealth() {
            return calculateHealthState();
        }

        public boolean isNamedDead() {
            return calculateHealthState() <= 0.0F;
        }

        protected float calculateState(long seed, double modifier) {
            return (float) (seed + modifier);
        }

        protected float calculateHealthState() {
            return 1.0F;
        }
    }

    private interface InstanceValueContract {
        float calculateState(long seed, double modifier);
    }

    private static final class InterfaceInvocationEntity extends ArmorStand implements InstanceValueContract {
        private InterfaceInvocationEntity(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        @Override
        public float calculateState(long seed, double modifier) {
            return (float) (seed + modifier);
        }
    }

    private static final class BooleanInstanceEntity extends ArmorStand {
        private BooleanInstanceEntity(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public boolean calculateFlag() {
            return true;
        }

        public boolean calculateAliveFlag() {
            return true;
        }

        public boolean calculateDeadFlag() {
            return false;
        }
    }

    private static class InheritedLifecycleParent extends ArmorStand {
        private InheritedLifecycleParent(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public float getCalculatedHealth() {
            return calculateState();
        }

        public boolean isCalculatedDead() {
            return calculateState() <= 0.0F;
        }

        protected float calculateState() {
            return 1.0F;
        }
    }

    private static final class InheritedLifecycleChild extends InheritedLifecycleParent {
        private InheritedLifecycleChild(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }
    }

    private static class RootCacheLifecycleParent extends ArmorStand {
        private RootCacheLifecycleParent(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public float getCalculatedHealth() {
            return calculateState();
        }

        public boolean isCalculatedDead() {
            return calculateState() <= 0.0F;
        }

        protected float calculateState() {
            return 1.0F;
        }
    }

    private static final class RootCacheLifecycleChildA extends RootCacheLifecycleParent {
        private RootCacheLifecycleChildA(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        @Override
        protected float calculateState() {
            return 2.0F;
        }
    }

    private static final class RootCacheLifecycleChildB extends RootCacheLifecycleParent {
        private RootCacheLifecycleChildB(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        @Override
        protected float calculateState() {
            return 3.0F;
        }
    }
}
