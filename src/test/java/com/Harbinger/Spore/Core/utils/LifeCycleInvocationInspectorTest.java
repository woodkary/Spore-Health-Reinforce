package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.agents.transformers.EntitySource;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodCategory;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodTarget;
import com.Harbinger.Spore.Core.agents.transformers.SporeDiscoveredLifeCycleMethodRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifeCycleInvocationInspectorTest {
    private static final String STATIC_OWNER = "test/UnlabelledEntityUtil";
    private static final String STATIC_NAME = "calculate";
    private static final String STATIC_DESC = "(Ljava/lang/Object;)F";
    private static final String BOOLEAN_STATIC_OWNER = "test/UnlabelledBooleanEntityUtil";
    private static final String BOOLEAN_STATIC_NAME = "calculateFlag";
    private static final String BOOLEAN_STATIC_DESC = "(Ljava/lang/Object;)Z";
    private static final String EXACT_FLOAT_STATIC_OWNER = "test/ExactBooleanFloatUtil";
    private static final String EXACT_FLOAT_STATIC_NAME = "calculateFloat";

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
    void resolvesRuntimeParentMethodMissingFromOriginalClassResource() throws Exception {
        String contractName = "test.mixin.RuntimeLifecycleContract";
        String parentName = "test.mixin.RuntimeMixinParent";
        String childName = "test.mixin.RuntimeMixinChild";
        String methodName = "runtimeMixinValue";
        String descriptor = "()Z";
        Map<String, byte[]> runtimeClasses = new HashMap<>();
        Map<String, byte[]> resourceClasses = new HashMap<>();
        runtimeClasses.put(contractName, interfaceFixture(contractName, methodName, descriptor));
        runtimeClasses.put(parentName, classFixture(
                parentName,
                "java/lang/Object",
                new String[]{internalName(contractName)},
                methodName,
                descriptor,
                true,
                false
        ));
        runtimeClasses.put(childName, classFixture(
                childName,
                internalName(parentName),
                null,
                null,
                null,
                false,
                false
        ));
        resourceClasses.put(contractName, runtimeClasses.get(contractName));
        resourceClasses.put(parentName, classFixture(
                parentName,
                "java/lang/Object",
                new String[]{internalName(contractName)},
                null,
                null,
                false,
                false
        ));
        resourceClasses.put(childName, runtimeClasses.get(childName));

        ResourceMismatchClassLoader loader = new ResourceMismatchClassLoader(
                getClass().getClassLoader(),
                runtimeClasses,
                resourceClasses
        );
        loader.define(contractName);
        loader.define(parentName);
        Class<?> child = loader.define(childName);

        assertFalse(classBytesDeclareMethod(resourceClasses.get(parentName), methodName, descriptor));
        assertEquals(
                internalName(parentName),
                resolveInstanceOwner(new LifeCycleInvocationInspector(), child, methodName, descriptor)
        );
    }

    @Test
    void ignoresAbstractInterfaceMethodAsConcreteImplementation() throws Exception {
        assertNull(resolveInstanceOwner(
                new LifeCycleInvocationInspector(),
                InstanceValueContract.class,
                "calculateState",
                "(JD)F"
        ));
    }

    @Test
    void fallsBackToOriginalClassNodeWhenRuntimeReflectionCannotResolveMethods() throws Exception {
        String className = "test.mixin.BytecodeFallbackTarget";
        String methodName = "fallbackValue";
        String descriptor = "()F";
        Map<String, byte[]> runtimeClasses = new HashMap<>();
        Map<String, byte[]> resourceClasses = new HashMap<>();
        runtimeClasses.put(className, classFixture(
                className,
                "java/lang/Object",
                null,
                null,
                null,
                false,
                true
        ));
        resourceClasses.put(className, classFixture(
                className,
                "java/lang/Object",
                null,
                methodName,
                descriptor,
                true,
                true
        ));
        ResourceMismatchClassLoader loader = new ResourceMismatchClassLoader(
                getClass().getClassLoader(),
                runtimeClasses,
                resourceClasses
        );
        Class<?> runtimeClass = loader.define(className);

        boolean reflectionFailed = false;
        try {
            ClassReflectionUtil.getDeclaredMethods(runtimeClass);
        } catch (Throwable expected) {
            reflectionFailed = true;
        }
        assertTrue(reflectionFailed);
        assertTrue(classBytesDeclareMethod(resourceClasses.get(className), methodName, descriptor));
        assertEquals(
                internalName(className),
                resolveInstanceOwner(new LifeCycleInvocationInspector(), runtimeClass, methodName, descriptor)
        );
    }

    @Test
    void allowsMinecraftOwnerRegistrationAndLoadedClassMatching() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = internalName(LivingEntity.class);
        String methodName = "sporeTestMixinLifecycleValue";
        inspectStaticHealthEvidence(inspector, owner, methodName);

        invokeNoArg(inspector, "registerFrequentMethods");

        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(owner)
                .containsKey(methodName + STATIC_DESC));
        List<?> matches = matchingLoadedClasses(inspector, new Class<?>[]{LivingEntity.class});
        assertEquals(1, matches.size());
        assertEquals(LivingEntity.class, matches.get(0));
    }

    @Test
    void excludesOwnSporeOwnersInBothNameFormsAndFilteringEntrypoints() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String ownOwner = "com/Harbinger/Spore/testing/OwnLifecycleTarget";
        String methodName = "sporeTestOwnLifecycleValue";
        inspectStaticHealthEvidence(inspector, ownOwner, methodName);

        assertTrue(isOwnSporeClass(inspector, "com.Harbinger.Spore.testing.Target"));
        assertTrue(isOwnSporeClass(inspector, "com/Harbinger/Spore/testing/Target"));
        assertTrue(isOwnSporeClass(inspector, "com/Harbinger/Spore/testing/Target/0x1234"));
        assertFalse(isOwnSporeClass(inspector, null));
        assertFalse(isOwnSporeClass(inspector, "net.minecraft.world.entity.LivingEntity"));

        invokeNoArg(inspector, "registerFrequentMethods");
        assertFalse(SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(ownOwner)
                .containsKey(methodName + STATIC_DESC));

        pendingOwners(inspector).add(internalName(LifeCycleInvocationInspectorTest.class));
        assertTrue(matchingLoadedClasses(
                inspector,
                new Class<?>[]{LifeCycleInvocationInspectorTest.class}
        ).isEmpty());
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
    void directlyDiscoversSingleNamedStaticBooleanHelpersWithoutPolarity() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/NamedBooleanLifecycleUtil";

        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "m_6084_",
                owner,
                "isAliveState",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.UNKNOWN
        ));
        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "m_21224_",
                owner,
                "isDeadOrDyingState",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.UNKNOWN
        ));

        assertTrue(hasDiscoveredTarget(
                inspector,
                owner,
                "isAliveState",
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        ));
        assertTrue(hasDiscoveredTarget(
                inspector,
                owner,
                "isDeadOrDyingState",
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.DEAD_OR_DYING
        ));
    }

    @Test
    void directlyDiscoversNamedStaticBooleanHelpersFromCustomLifecycleMethods() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/CustomNamedBooleanLifecycleUtil";

        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "isAliveCustom",
                owner,
                "hasLivingState",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.UNKNOWN
        ));
        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "isDeadCustom",
                owner,
                "hasDiedState",
                BOOLEAN_STATIC_DESC,
                0,
                BooleanPolarity.UNKNOWN
        ));

        assertTrue(hasDiscoveredTarget(
                inspector,
                owner,
                "hasLivingState",
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        ));
        assertTrue(hasDiscoveredTarget(
                inspector,
                owner,
                "hasDiedState",
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.DEAD_OR_DYING
        ));
    }

    @Test
    void directlyDiscoversHealthAndMaxHealthStaticNamesAcrossBothOuterKinds() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/NamedHealthLifecycleUtil";

        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getHealth", "()F", owner, "readHealth", "(Ljava/lang/Object;)F", 0));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getHealth", "()D", owner, "readMaxHealth", "(Ljava/lang/Object;)D", 0));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getMaxHealth", "()F", owner, "currentHealth", "(Ljava/lang/Object;)F", 0));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getMaxHealth", "()D", owner, "currentMaxHealth", "(Ljava/lang/Object;)D", 0));

        assertTrue(hasDiscoveredTarget(inspector, owner, "readHealth",
                EntitySource.STATIC_ARGUMENTS, LifeCycleMethodCategory.HEALTH));
        assertTrue(hasDiscoveredTarget(inspector, owner, "readMaxHealth",
                EntitySource.STATIC_ARGUMENTS, LifeCycleMethodCategory.HEALTH));
        assertTrue(hasDiscoveredTarget(inspector, owner, "currentHealth",
                EntitySource.STATIC_ARGUMENTS, LifeCycleMethodCategory.HEALTH));
        assertTrue(hasDiscoveredTarget(inspector, owner, "currentMaxHealth",
                EntitySource.STATIC_ARGUMENTS, LifeCycleMethodCategory.HEALTH));
    }

    @Test
    void unionsDirectHealthArgumentsWithFrequencyBasedArguments() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/CombinedNamedHealthLifecycleUtil";
        String name = "readHealth";
        String descriptor = "(Ljava/lang/Object;Ljava/lang/Object;)F";

        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getHealth", "()F", owner, name, descriptor, 0));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                exactBooleanStaticFloatMethod("m_6084_", owner, name, descriptor, 1));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                exactBooleanStaticFloatMethod("m_21224_", owner, name, descriptor, 1));

        LifeCycleMethodTarget target = discoveredTarget(
                inspector,
                owner,
                name,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.HEALTH
        );
        assertTrue(target != null);
        assertArrayEquals(new int[]{0, 1}, target.entityArgumentIndexes());
    }

    @Test
    void directStaticDiscoveryStillRequiresNameTypeAndDefiniteThis() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/RejectedNamedLifecycleUtil";

        inspectMethod(inspector, FinalLifecycleEntity.class,
                numericStaticMethod("getHealth", "()F", owner, "calculateValue", STATIC_DESC, 0));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                maybeThisMethod(owner, "readHealth"));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                healthMethodCallingBoolean(owner, "isAliveState"));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                aliveMethodCallingFloat(owner, "readHealth"));

        assertEquals(0, frequentMethods(inspector).size());
    }

    @Test
    void invalidatesConflictingDirectStaticCategories() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/ConflictingNamedBooleanLifecycleUtil";
        String name = "isAliveOrDeadState";
        String descriptor = "(Ljava/lang/Object;Ljava/lang/Object;)Z";

        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "isAliveCustom", owner, name, descriptor, 0, BooleanPolarity.UNKNOWN));
        LifeCycleMethodTarget initialTarget = discoveredTarget(
                inspector,
                owner,
                name,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        );
        assertTrue(initialTarget != null);
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.register(
                owner, name, descriptor, initialTarget));

        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "isDeadCustom", owner, name, descriptor, 1, BooleanPolarity.UNKNOWN));

        assertEquals(0, frequentMethods(inspector).size());
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.isInvalid(
                owner, name, descriptor));
        assertFalse(SporeDiscoveredLifeCycleMethodRegistry.targetsForOwner(owner)
                .containsKey(name + descriptor));
    }

    @Test
    void invalidatesDirectCategoryThatConflictsWithPolarityCategory() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String owner = "test/DirectPolarityConflictUtil";
        String name = "calculateConflict";

        addDirectAndPolarityConflictEvidence(inspector, owner, name, BOOLEAN_STATIC_DESC);

        assertEquals(0, frequentMethods(inspector).size());
        assertTrue(SporeDiscoveredLifeCycleMethodRegistry.isInvalid(
                owner, name, BOOLEAN_STATIC_DESC));
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

    @Test
    void discoversStaticFloatHelperAcrossExactBooleanLifecycleMethods() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspectMethod(inspector, FinalLifecycleEntity.class,
                exactBooleanStaticFloatMethod("m_6084_", EXACT_FLOAT_STATIC_OWNER, EXACT_FLOAT_STATIC_NAME));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                exactBooleanStaticFloatMethod("m_21224_", EXACT_FLOAT_STATIC_OWNER, EXACT_FLOAT_STATIC_NAME));

        assertTrue(hasDiscoveredTarget(
                inspector,
                EXACT_FLOAT_STATIC_OWNER,
                EXACT_FLOAT_STATIC_NAME,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.HEALTH
        ));
    }

    @Test
    void discoversInstanceFloatHelperAcrossExactBooleanLifecycleMethods() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();

        inspectMethod(inspector, ExactBooleanFloatInstanceEntity.class,
                exactBooleanInstanceFloatMethod("m_6084_"));
        inspectMethod(inspector, ExactBooleanFloatInstanceEntity.class,
                exactBooleanInstanceFloatMethod("m_21224_"));

        assertTrue(hasDiscoveredTarget(
                inspector,
                internalName(ExactBooleanFloatInstanceEntity.class),
                "calculateValue",
                EntitySource.INSTANCE_THIS,
                LifeCycleMethodCategory.HEALTH
        ));
    }

    @Test
    void discoversFloatAndBooleanHelpersFromSameExactLifecycleMethods() throws Exception {
        LifeCycleInvocationInspector inspector = new LifeCycleInvocationInspector();
        String floatOwner = "test/CombinedFloatUtil";
        String floatName = "calculateCombinedFloat";

        inspectMethod(inspector, FinalLifecycleEntity.class,
                combinedExactBooleanMethod("m_6084_", floatOwner, floatName, BooleanPolarity.DIRECT));
        inspectMethod(inspector, FinalLifecycleEntity.class,
                combinedExactBooleanMethod("m_21224_", floatOwner, floatName, BooleanPolarity.NEGATED));

        assertTrue(hasDiscoveredTarget(
                inspector,
                floatOwner,
                floatName,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.HEALTH
        ));
        assertTrue(hasDiscoveredTarget(
                inspector,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                EntitySource.STATIC_ARGUMENTS,
                LifeCycleMethodCategory.ALIVE
        ));
    }

    private MethodNode healthMethod() {
        return healthMethod(STATIC_OWNER, STATIC_NAME);
    }

    private MethodNode healthMethod(String owner, String methodName) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getHealth", "()F", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ASTORE, 1);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, owner, methodName, STATIC_DESC, false);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 2);
        method.visitEnd();
        return method;
    }

    private MethodNode deadMethod() {
        return deadMethod(STATIC_OWNER, STATIC_NAME);
    }

    private MethodNode deadMethod(String owner, String methodName) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "isDeadOrDying", "()Z", null, null);
        MethodVisitor visitor = method;
        visitor.visitCode();
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, methodName, STATIC_DESC, false);
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
        return maybeThisMethod(STATIC_OWNER, STATIC_NAME);
    }

    private MethodNode maybeThisMethod(String owner, String methodName) {
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
        method.visitMethodInsn(Opcodes.INVOKESTATIC, owner, methodName, STATIC_DESC, false);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 4);
        method.visitEnd();
        return method;
    }

    private MethodNode booleanStaticMethod(String lifecycleName,
                                           String helperDescriptor,
                                           int thisArgumentIndex,
                                           BooleanPolarity polarity) {
        return booleanStaticMethod(
                lifecycleName,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                helperDescriptor,
                thisArgumentIndex,
                polarity
        );
    }

    private MethodNode booleanStaticMethod(String lifecycleName,
                                           String helperOwner,
                                           String helperName,
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
                helperOwner,
                helperName,
                helperDescriptor,
                false
        );
        appendPolarity(method, polarity);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(Math.max(2, argumentCount), 1);
        method.visitEnd();
        return method;
    }

    private MethodNode exactBooleanStaticFloatMethod(String lifecycleName,
                                                     String helperOwner,
                                                     String helperName) {
        return exactBooleanStaticFloatMethod(
                lifecycleName,
                helperOwner,
                helperName,
                STATIC_DESC,
                0
        );
    }

    private MethodNode exactBooleanStaticFloatMethod(String lifecycleName,
                                                     String helperOwner,
                                                     String helperName,
                                                     String helperDescriptor,
                                                     int thisArgumentIndex) {
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
        method.visitMethodInsn(Opcodes.INVOKESTATIC, helperOwner, helperName, helperDescriptor, false);
        appendFloatLifecycleResult(method, lifecycleName);
        method.visitMaxs(Math.max(2, argumentCount), 1);
        method.visitEnd();
        return method;
    }

    private MethodNode numericStaticMethod(String lifecycleName,
                                           String lifecycleDescriptor,
                                           String helperOwner,
                                           String helperName,
                                           String helperDescriptor,
                                           int thisArgumentIndex) {
        MethodNode method = new MethodNode(
                Opcodes.ACC_PUBLIC,
                lifecycleName,
                lifecycleDescriptor,
                null,
                null
        );
        method.visitCode();
        org.objectweb.asm.Type[] argumentTypes =
                org.objectweb.asm.Type.getArgumentTypes(helperDescriptor);
        for (int argumentIndex = 0; argumentIndex < argumentTypes.length; argumentIndex++) {
            if (argumentIndex == thisArgumentIndex) {
                method.visitVarInsn(Opcodes.ALOAD, 0);
            } else {
                method.visitInsn(Opcodes.ACONST_NULL);
            }
        }
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                helperOwner,
                helperName,
                helperDescriptor,
                false
        );
        method.visitInsn(org.objectweb.asm.Type.getReturnType(lifecycleDescriptor).getOpcode(Opcodes.IRETURN));
        method.visitMaxs(Math.max(2, argumentTypes.length), 1);
        method.visitEnd();
        return method;
    }

    private MethodNode healthMethodCallingBoolean(String helperOwner, String helperName) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getHealth", "()F", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                helperOwner,
                helperName,
                BOOLEAN_STATIC_DESC,
                false
        );
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FRETURN);
        method.visitMaxs(1, 1);
        method.visitEnd();
        return method;
    }

    private MethodNode aliveMethodCallingFloat(String helperOwner, String helperName) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "isAliveCustom", "()Z", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                helperOwner,
                helperName,
                STATIC_DESC,
                false
        );
        method.visitInsn(Opcodes.POP);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(1, 1);
        method.visitEnd();
        return method;
    }

    private MethodNode exactBooleanInstanceFloatMethod(String lifecycleName) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, lifecycleName, "()Z", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                internalName(ExactBooleanFloatInstanceEntity.class),
                "calculateValue",
                "()F",
                false
        );
        appendFloatLifecycleResult(method, lifecycleName);
        method.visitMaxs(2, 1);
        method.visitEnd();
        return method;
    }

    private MethodNode combinedExactBooleanMethod(String lifecycleName,
                                                  String floatOwner,
                                                  String floatName,
                                                  BooleanPolarity booleanPolarity) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, lifecycleName, "()Z", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, floatOwner, floatName, STATIC_DESC, false);
        method.visitInsn(Opcodes.POP);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                BOOLEAN_STATIC_OWNER,
                BOOLEAN_STATIC_NAME,
                BOOLEAN_STATIC_DESC,
                false
        );
        appendPolarity(method, booleanPolarity);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(2, 1);
        method.visitEnd();
        return method;
    }

    private void appendFloatLifecycleResult(MethodNode method, String lifecycleName) {
        method.visitInsn(Opcodes.FCONST_0);
        method.visitInsn(Opcodes.FCMPG);
        Label falseResult = new Label();
        Label done = new Label();
        method.visitJumpInsn("m_6084_".equals(lifecycleName) ? Opcodes.IFLE : Opcodes.IFGT, falseResult);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitJumpInsn(Opcodes.GOTO, done);
        method.visitLabel(falseResult);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitLabel(done);
        method.visitInsn(Opcodes.IRETURN);
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
        return discoveredTarget(inspector, owner, name, source, category) != null;
    }

    private LifeCycleMethodTarget discoveredTarget(Object inspector,
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
                return target;
            }
        }
        return null;
    }

    private Set<?> frequentMethods(Object inspector) throws Exception {
        Method frequentMethods = declaredMethod(inspector.getClass(), "frequentMethods");
        frequentMethods.setAccessible(true);
        return (Set<?>) frequentMethods.invoke(inspector);
    }

    private String resolveInstanceOwner(LifeCycleInvocationInspector inspector,
                                        Class<?> rootClass,
                                        String name,
                                        String descriptor) throws Exception {
        Method resolve = declaredMethod("resolveInstanceImplementation");
        resolve.setAccessible(true);
        Object resolved = resolve.invoke(inspector, rootClass, name, descriptor);
        return resolved == null ? null : (String) invokeAccessor(resolved, "owner");
    }

    private void inspectStaticHealthEvidence(LifeCycleInvocationInspector inspector,
                                             String owner,
                                             String methodName) throws Exception {
        inspectMethod(inspector, FinalLifecycleEntity.class, healthMethod(owner, methodName));
        inspectMethod(inspector, FinalLifecycleEntity.class, deadMethod(owner, methodName));
    }

    @SuppressWarnings("unchecked")
    private void addDirectAndPolarityConflictEvidence(LifeCycleInvocationInspector inspector,
                                                      String owner,
                                                      String methodName,
                                                      String descriptor) throws Exception {
        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "m_6084_", owner, methodName, descriptor, 0, BooleanPolarity.NEGATED));
        inspectMethod(inspector, FinalLifecycleEntity.class, booleanStaticMethod(
                "m_21224_", owner, methodName, descriptor, 0, BooleanPolarity.DIRECT));

        Field observedInvocationsField = declaredField(
                LifeCycleInvocationInspector.class,
                "observedInvocations"
        );
        observedInvocationsField.setAccessible(true);
        Map<Object, Object> observedInvocations =
                (Map<Object, Object>) observedInvocationsField.get(inspector);
        for (Map.Entry<Object, Object> entry : observedInvocations.entrySet()) {
            Object key = entry.getKey();
            if (!owner.equals(invokeAccessor(key, "owner"))
                    || !methodName.equals(invokeAccessor(key, "name"))
                    || !descriptor.equals(invokeAccessor(key, "desc"))
                    || invokeAccessor(key, "entitySource") != EntitySource.STATIC_ARGUMENTS) {
                continue;
            }
            Object evidence = entry.getValue();
            Field directCategoriesField = declaredField(
                    evidence.getClass(),
                    "directStaticCategoriesByArgument"
            );
            directCategoriesField.setAccessible(true);
            Map<Integer, EnumSet<LifeCycleMethodCategory>> directCategories =
                    (Map<Integer, EnumSet<LifeCycleMethodCategory>>) directCategoriesField.get(evidence);
            directCategories.put(0, EnumSet.of(LifeCycleMethodCategory.ALIVE));
            return;
        }
        throw new IllegalStateException("Missing boolean polarity evidence for " + owner + "." + methodName);
    }

    private void invokeNoArg(LifeCycleInvocationInspector inspector, String methodName) throws Exception {
        Method method = declaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(inspector);
    }

    @SuppressWarnings("unchecked")
    private Set<String> pendingOwners(LifeCycleInvocationInspector inspector) throws Exception {
        for (Field field : ClassReflectionUtil.getDeclaredFields(LifeCycleInvocationInspector.class)) {
            if ("pendingOwners".equals(field.getName())) {
                field.setAccessible(true);
                return (Set<String>) field.get(inspector);
            }
        }
        throw new IllegalStateException("Missing field pendingOwners");
    }

    private List<?> matchingLoadedClasses(LifeCycleInvocationInspector inspector,
                                          Class<?>[] classes) throws Exception {
        Method method = declaredMethod("matchingLoadedClasses");
        method.setAccessible(true);
        return (List<?>) method.invoke(inspector, (Object) classes);
    }

    private boolean isOwnSporeClass(LifeCycleInvocationInspector inspector, String name) throws Exception {
        Method method = declaredMethod("isOwnSporeClass");
        method.setAccessible(true);
        return (boolean) method.invoke(inspector, name);
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

    private Field declaredField(Class<?> type, String name) {
        for (Field field : ClassReflectionUtil.getDeclaredFields(type)) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        throw new IllegalStateException("Missing field " + name);
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

    private String internalName(String name) {
        return name.replace('.', '/');
    }

    private byte[] interfaceFixture(String className, String methodName, String descriptor) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                internalName(className),
                null,
                "java/lang/Object",
                null
        );
        writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                methodName,
                descriptor,
                null,
                null
        ).visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private byte[] classFixture(String className,
                                String superName,
                                String[] interfaces,
                                String methodName,
                                String descriptor,
                                boolean includeTargetMethod,
                                boolean includeMissingTypeMethod) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, internalName(className), null, superName, interfaces);
        MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
        if (includeTargetMethod) {
            MethodVisitor target = writer.visitMethod(Opcodes.ACC_PUBLIC, methodName, descriptor, null, null);
            target.visitCode();
            if ("()Z".equals(descriptor)) {
                target.visitInsn(Opcodes.ICONST_1);
                target.visitInsn(Opcodes.IRETURN);
            } else {
                target.visitInsn(Opcodes.FCONST_1);
                target.visitInsn(Opcodes.FRETURN);
            }
            target.visitMaxs(0, 0);
            target.visitEnd();
        }
        if (includeMissingTypeMethod) {
            MethodVisitor poison = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "unresolvableSignature",
                    "(Ltest/mixin/MissingDependency;)V",
                    null,
                    null
            );
            poison.visitCode();
            poison.visitInsn(Opcodes.RETURN);
            poison.visitMaxs(0, 0);
            poison.visitEnd();
        }
        writer.visitEnd();
        return writer.toByteArray();
    }

    private boolean classBytesDeclareMethod(byte[] bytes, String name, String descriptor) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (MethodNode method : node.methods) {
            if (name.equals(method.name) && descriptor.equals(method.desc)) {
                return true;
            }
        }
        return false;
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

    private static final class ExactBooleanFloatInstanceEntity extends ArmorStand {
        private ExactBooleanFloatInstanceEntity(EntityType<? extends ArmorStand> type, Level level) {
            super(type, level);
        }

        public float calculateValue() {
            return 1.0F;
        }
    }

    private static final class ResourceMismatchClassLoader extends ClassLoader {
        private final Map<String, byte[]> runtimeClasses;
        private final Map<String, byte[]> resourceClasses;

        private ResourceMismatchClassLoader(ClassLoader parent,
                                            Map<String, byte[]> runtimeClasses,
                                            Map<String, byte[]> resourceClasses) {
            super(parent);
            this.runtimeClasses = runtimeClasses;
            this.resourceClasses = resourceClasses;
        }

        private Class<?> define(String name) {
            byte[] bytes = runtimeClasses.get(name);
            return defineClass(name, bytes, 0, bytes.length);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            String className = name;
            if (className.startsWith("/")) {
                className = className.substring(1);
            }
            if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - 6).replace('/', '.');
            }
            byte[] bytes = resourceClasses.get(className);
            return bytes == null ? super.getResourceAsStream(name) : new ByteArrayInputStream(bytes);
        }
    }
}
