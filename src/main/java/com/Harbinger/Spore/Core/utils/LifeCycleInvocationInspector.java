package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import com.Harbinger.Spore.Core.agents.transformers.EntitySource;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodCategory;
import com.Harbinger.Spore.Core.agents.transformers.LifeCycleMethodTarget;
import com.Harbinger.Spore.Core.agents.transformers.SporeDiscoveredLifeCycleMethodRegistry;
import com.Harbinger.Spore.Core.agents.transformers.SporeDiscoveredLifeCycleMethodTransformer;
import com.Harbinger.Spore.Core.agents.transformers.SporeTransformerDebugDump;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import sun.misc.Unsafe;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public final class LifeCycleInvocationInspector
        implements ILifeCycleInvocationInspect, Function<Object, Set<LifeCycleMethod>> {
    private static final long CLASS_KLASS_OFFSET = 16L;
    private static final long KLASS_ACCESS_FLAGS_OFFSET = 164L;
    private static final int JVM_ACC_IS_HIDDEN_CLASS = 0x04000000;
    private static final int JVM_ACC_IS_BEING_REDEFINED = 0x00100000;
    private static final String HEALTH_WRAPPER_SUFFIX = "SporeHealthLifecycleWrapper";
    private static final String DEATH_WRAPPER_SUFFIX = "SporeDeathLifecycleWrapper";
    private static final LifeCycleMethod EXACT_ALIVE_METHOD = new LifeCycleMethod("m_6084_", "()Z");
    private static final LifeCycleMethod EXACT_DEAD_OR_DYING_METHOD = new LifeCycleMethod("m_21224_", "()Z");
    private static final boolean DISABLE_UNSAFE_HIDDEN_RETRANSFORM =
            Boolean.getBoolean("spore.transformer.disableUnsafeHiddenRetransform");
    private static final String[] RETRANSFORM_HOOK_DEPENDENCIES = {
            "com.Harbinger.Spore.Core.asmHooks.IEntityHealth",
            "com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager",
            "net.minecraft.world.entity.LivingEntity",
            "net.minecraft.world.entity.Entity"
    };

    public static final ILifeCycleInvocationInspect INSTANCE;

    private final Map<InvocationKey, InvocationEvidence> observedInvocations = new LinkedHashMap<>();
    private final Map<LifeCycleInspectionKey, Set<LifeCycleMethod>> inspectedLifeCycleMethods = new HashMap<>();
    private final Set<String> pendingOwners = new LinkedHashSet<>();
    private boolean instrumentationTransformerInstalled;
    private boolean jvmtiTransformerInstalled;

    public LifeCycleInvocationInspector() {
    }

    @Override
    public synchronized void inspectAndCacheLifeCycleInvocations(Class<?> livingEntityClass) {
        try {
            inspectAndCacheLifeCycleInvocationsInternal(livingEntityClass);
        } catch (Throwable t) {
            LogUtil.errorf("failed to cache lifecycle invocations for %s, %s",
                    livingEntityClass == null ? "null" : livingEntityClass.getName(),
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }

    private void inspectAndCacheLifeCycleInvocationsInternal(Class<?> livingEntityClass) {
        Class<?> rawClass = getRawOriginalClass(livingEntityClass);
        if (rawClass == null || !LivingEntity.class.isAssignableFrom(rawClass)) {
            return;
        }
        for (Class<?> current = rawClass;
             current != null && LivingEntity.class.isAssignableFrom(current);
             current = current.getSuperclass()) {
            inspectDeclaredLifeCycleMethods(rawClass, current);
        }
    }

    private void inspectDeclaredLifeCycleMethods(Class<?> rootEntityClass, Class<?> declaringLifeCycleClass) {
        ClassNode classNode = readClassNode(declaringLifeCycleClass);
        if (classNode == null) {
            return;
        }
        Set<LifeCycleMethod> inspected = inspectedLifeCycleMethods.computeIfAbsent(
                new LifeCycleInspectionKey(rootEntityClass, declaringLifeCycleClass),
                this
        );
        for (MethodNode method : classNode.methods) {
            if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                    || "<init>".equals(method.name)
                    || "<clinit>".equals(method.name)
                    || method.instructions == null
                    || method.instructions.size() == 0) {
                continue;
            }
            LifeCycleMethod lifeCycleMethod = new LifeCycleMethod(method.name, method.desc);
            LifeCycleKind kind = classify(lifeCycleMethod);
            if (kind != null && inspected.add(lifeCycleMethod)) {
                inspectInvocations(rootEntityClass, classNode.name, method, lifeCycleMethod);
            }
        }
    }

    @Override
    public synchronized void inspectAndRetransformInvocations() {
        try {
            inspectAndRetransformInvocationsInternal();
        } catch (Throwable t) {
            LogUtil.errorf("failed to inspect shared lifecycle invocations, %s", t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }

    private void inspectAndRetransformInvocationsInternal() {
        registerFrequentMethods();
        if (!SporeDiscoveredLifeCycleMethodRegistry.owners().isEmpty()) {
            inspectAndRetransformRegisteredOwners();
        }
    }

    private void registerFrequentMethods() {
        for (DiscoveredMethod method : frequentMethods()) {
            if (isOwnSporeClass(method.owner())) {
                continue;
            }
            if (SporeDiscoveredLifeCycleMethodRegistry.register(
                    method.owner(),
                    method.name(),
                    method.desc(),
                    method.target())) {
                pendingOwners.add(SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(method.owner()));
                LogUtil.logf("Discovered shared lifecycle method %s.%s%s using %s/%s",
                        method.owner(),
                        method.name(),
                        method.desc(),
                        method.target().entitySource(),
                        method.target().category());
            }
        }
    }

    private Set<DiscoveredMethod> frequentMethods() {
        Set<DiscoveredMethod> result = new LinkedHashSet<>();
        for (Map.Entry<InvocationKey, InvocationEvidence> entry : observedInvocations.entrySet()) {
            InvocationEvidence evidence = entry.getValue();
            InvocationKey key = entry.getKey();
            Type returnType;
            try {
                returnType = Type.getReturnType(key.desc());
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (returnType.getSort() == Type.BOOLEAN) {
                addFrequentBooleanMethod(result, key, evidence);
            } else if (returnType.getSort() == Type.FLOAT || returnType.getSort() == Type.DOUBLE) {
                addFrequentHealthMethod(result, key, evidence);
            }
        }
        return result;
    }

    private void addFrequentHealthMethod(Set<DiscoveredMethod> result,
                                         InvocationKey key,
                                         InvocationEvidence evidence) {
        if (key.entitySource() == EntitySource.INSTANCE_THIS) {
            if (evidence.healthInstanceLifeCycleKinds.size() >= 2) {
                result.add(new DiscoveredMethod(
                        key.owner(),
                        key.name(),
                        key.desc(),
                        LifeCycleMethodTarget.instanceThis(LifeCycleMethodCategory.HEALTH)
                ));
            }
            return;
        }
        Set<Integer> frequentArgumentIndexes = new TreeSet<>();
        LifeCycleMethodCategory directCategory = null;
        for (Map.Entry<Integer, EnumSet<LifeCycleMethodCategory>> argumentEvidence
                : evidence.directStaticCategoriesByArgument.entrySet()) {
            for (LifeCycleMethodCategory category : argumentEvidence.getValue()) {
                if (directCategory != null && directCategory != category) {
                    invalidateConflictingStaticCategories(key);
                    return;
                }
                directCategory = category;
                frequentArgumentIndexes.add(argumentEvidence.getKey());
            }
        }
        if (directCategory != null && directCategory != LifeCycleMethodCategory.HEALTH) {
            invalidateConflictingStaticCategories(key);
            return;
        }
        for (Map.Entry<Integer, EnumSet<LifeCycleKind>> argumentEvidence
                : evidence.healthLifeCycleKindsByArgument.entrySet()) {
            if (argumentEvidence.getValue().size() >= 2) {
                frequentArgumentIndexes.add(argumentEvidence.getKey());
            }
        }
        if (!frequentArgumentIndexes.isEmpty()) {
            result.add(new DiscoveredMethod(
                    key.owner(),
                    key.name(),
                    key.desc(),
                    LifeCycleMethodTarget.staticArguments(
                            LifeCycleMethodCategory.HEALTH,
                            toIntArray(frequentArgumentIndexes)
                    )
            ));
        }
    }

    private void addFrequentBooleanMethod(Set<DiscoveredMethod> result,
                                          InvocationKey key,
                                          InvocationEvidence evidence) {
        if (key.entitySource() == EntitySource.INSTANCE_THIS) {
            LifeCycleMethodCategory category = booleanCategory(evidence.instanceBooleanPolarities);
            if (category != null) {
                result.add(new DiscoveredMethod(
                        key.owner(),
                        key.name(),
                        key.desc(),
                        LifeCycleMethodTarget.instanceThis(category)
                ));
            }
            return;
        }

        LifeCycleMethodCategory commonCategory = null;
        Set<Integer> entityArgumentIndexes = new TreeSet<>();
        for (Map.Entry<Integer, EnumSet<LifeCycleMethodCategory>> argumentEvidence
                : evidence.directStaticCategoriesByArgument.entrySet()) {
            for (LifeCycleMethodCategory category : argumentEvidence.getValue()) {
                if (commonCategory != null && commonCategory != category) {
                    invalidateConflictingStaticCategories(key);
                    return;
                }
                commonCategory = category;
                entityArgumentIndexes.add(argumentEvidence.getKey());
            }
        }
        for (Map.Entry<Integer, EnumMap<BooleanLifeCycleMethod, BooleanPolarity>> argumentEvidence
                : evidence.booleanPolaritiesByArgument.entrySet()) {
            LifeCycleMethodCategory category = booleanCategory(argumentEvidence.getValue());
            if (category == null) {
                continue;
            }
            if (commonCategory != null && commonCategory != category) {
                invalidateConflictingStaticCategories(key);
                return;
            }
            commonCategory = category;
            entityArgumentIndexes.add(argumentEvidence.getKey());
        }
        if (commonCategory != null && !entityArgumentIndexes.isEmpty()) {
            result.add(new DiscoveredMethod(
                    key.owner(),
                    key.name(),
                    key.desc(),
                    LifeCycleMethodTarget.staticArguments(commonCategory, toIntArray(entityArgumentIndexes))
            ));
        }
    }

    private void invalidateConflictingStaticCategories(InvocationKey key) {
        LogUtil.logf("Skip lifecycle method %s.%s%s: entity arguments imply conflicting categories",
                key.owner(), key.name(), key.desc());
        if (SporeDiscoveredLifeCycleMethodRegistry.invalidate(
                key.owner(),
                key.name(),
                key.desc(),
                "entity arguments imply conflicting lifecycle categories"
        )) {
            pendingOwners.add(SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(key.owner()));
        }
    }

    private LifeCycleMethodCategory booleanCategory(
            EnumMap<BooleanLifeCycleMethod, BooleanPolarity> polarities) {
        BooleanPolarity alive = polarities.get(BooleanLifeCycleMethod.ALIVE);
        BooleanPolarity deadOrDying = polarities.get(BooleanLifeCycleMethod.DEAD_OR_DYING);
        if (alive == BooleanPolarity.DIRECT && deadOrDying == BooleanPolarity.NEGATED) {
            return LifeCycleMethodCategory.ALIVE;
        }
        if (alive == BooleanPolarity.NEGATED && deadOrDying == BooleanPolarity.DIRECT) {
            return LifeCycleMethodCategory.DEAD_OR_DYING;
        }
        return null;
    }

    private int[] toIntArray(Set<Integer> indexes) {
        int[] result = new int[indexes.size()];
        int targetIndex = 0;
        for (int index : indexes) {
            result[targetIndex++] = index;
        }
        return result;
    }

    private ClassNode readClassNode(Class<?> entityClass) {
        Class<?> rawSourceClass = getRawOriginalClass(entityClass);
        if (rawSourceClass == null) {
            return null;
        }
        ClassNode rawNode = readClassNodeBytes(rawSourceClass, false);
        if (rawNode != null) {
            return rawNode;
        }
        Class<?> resourceClass = ClassLoaderUtil.INSTANCE.tryAvoidHiddenClass(rawSourceClass);
        if (resourceClass == null || resourceClass == rawSourceClass) {
            LogUtil.errorf("failed to inspect lifecycle bytecode for %s", rawSourceClass.getName());
            return null;
        }
        ClassNode resourceNode = readClassNodeBytes(resourceClass, true);
        if (resourceNode == null) {
            LogUtil.errorf("failed to inspect lifecycle bytecode for %s", rawSourceClass.getName());
        }
        return resourceNode;
    }

    private ClassNode readClassNodeBytes(Class<?> sourceClass, boolean logFailure) {
        byte[] bytes;
        try {
            bytes = BytecodeUtil.loadClassBytesResourceStreamFirst(sourceClass);
        } catch (Throwable firstFailure) {
            try {
                bytes = BytecodeUtil.loadClassBytes(sourceClass);
            } catch (Throwable secondFailure) {
                if (logFailure) {
                    LogUtil.errorf("failed to inspect lifecycle bytecode for %s, %s",
                            sourceClass.getName(),
                            secondFailure.getMessage());
                }
                return null;
            }
        }
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            return classNode;
        } catch (Throwable t) {
            if (logFailure) {
                LogUtil.errorf("failed to parse lifecycle bytecode for %s, %s",
                        sourceClass.getName(),
                        t.getMessage());
            }
            return null;
        }
    }

    private LifeCycleKind classify(LifeCycleMethod method) {
        Type returnType;
        try {
            returnType = Type.getReturnType(method.desc());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        if (returnType.getSort() == Type.FLOAT || returnType.getSort() == Type.DOUBLE) {
            if (nameLooksLikeMaxHealth(method.name())) {
                return LifeCycleKind.MAX_HEALTH;
            }
            if (nameLooksLikeHealth(method.name())) {
                return LifeCycleKind.HEALTH;
            }
            return null;
        }
        if (returnType.getSort() == Type.BOOLEAN) {
            if (nameLooksLikeIsDeadOrDying(method.name())) {
                return LifeCycleKind.DEAD_OR_DYING;
            }
            if (nameLooksLikeIsAlive(method.name())) {
                return LifeCycleKind.ALIVE;
            }
        }
        return null;
    }

    private Class<?> getRawOriginalClass(Class<?> wrapperClass) {
        Class<?> current = wrapperClass;
        while (current != null && (current.getName().contains(HEALTH_WRAPPER_SUFFIX)
                || current.getName().contains(DEATH_WRAPPER_SUFFIX))) {
            Class<?> parent = current.getSuperclass();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current;
    }

    private boolean nameLooksLikeHealth(String name) {
        if ("haveDiexv".equals(name)) {
            return true;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return (normalized.contains("heal") && !normalized.contains("max"))
                || "m_21223_".equals(name);
    }

    private boolean nameLooksLikeMaxHealth(String name) {
        if ("haveBigDiexv".equals(name)) {
            return true;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return (normalized.contains("max") && normalized.contains("heal"))
                || "m_21233_".equals(name);
    }

    private boolean nameLooksLikeIsDeadOrDying(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("dead")
                || normalized.contains("die")
                || normalized.contains("death")
                || normalized.contains("away")
                || normalized.contains("died")
                || (normalized.contains("kill") && !normalized.contains("skill"))
                || normalized.contains("weak")
                || (normalized.contains("end")
                && !normalized.contains("render")
                && !normalized.contains("legend"))
                || "m_21224_".equals(name);
    }

    private boolean nameLooksLikeIsAlive(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("alive")
                || normalized.contains("living")
                || "m_6084_".equals(name);
    }

    private LifeCycleMethodCategory directStaticCategory(LifeCycleKind lifeCycleKind,
                                                         String targetName,
                                                         Type returnType) {
        int returnSort = returnType.getSort();
        if ((returnSort == Type.FLOAT || returnSort == Type.DOUBLE)
                && (lifeCycleKind == LifeCycleKind.HEALTH || lifeCycleKind == LifeCycleKind.MAX_HEALTH)
                && (nameLooksLikeHealth(targetName) || nameLooksLikeMaxHealth(targetName))) {
            return LifeCycleMethodCategory.HEALTH;
        }
        if (returnSort != Type.BOOLEAN) {
            return null;
        }
        if (lifeCycleKind == LifeCycleKind.ALIVE && nameLooksLikeIsAlive(targetName)) {
            return LifeCycleMethodCategory.ALIVE;
        }
        if (lifeCycleKind == LifeCycleKind.DEAD_OR_DYING && nameLooksLikeIsDeadOrDying(targetName)) {
            return LifeCycleMethodCategory.DEAD_OR_DYING;
        }
        return null;
    }

    private void inspectInvocations(Class<?> rootEntityClass,
                                    String className,
                                    MethodNode method,
                                    LifeCycleMethod lifeCycleMethod) {
        LifeCycleKind lifeCycleKind = classify(lifeCycleMethod);
        if (lifeCycleKind == null) {
            return;
        }
        BooleanLifeCycleMethod booleanLifeCycleMethod = exactBooleanLifeCycleMethod(lifeCycleMethod);
        Frame<BasicValue>[] frames;
        try {
            Analyzer<BasicValue> analyzer = new Analyzer<>(new ThisTrackingInterpreter());
            frames = analyzer.analyze(className, method);
        } catch (AnalyzerException | RuntimeException e) {
            LogUtil.errorf("failed to analyze lifecycle method %s.%s%s, %s",
                    className,
                    method.name,
                    method.desc,
                    e.getMessage());
            return;
        }

        int instructionIndex = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext(), instructionIndex++) {
            if (!(instruction instanceof MethodInsnNode call)) {
                continue;
            }
            boolean staticCall = call.getOpcode() == Opcodes.INVOKESTATIC;
            boolean instanceCall = call.getOpcode() == Opcodes.INVOKEVIRTUAL
                    || call.getOpcode() == Opcodes.INVOKEINTERFACE;
            if (!staticCall && !instanceCall) {
                continue;
            }
            Type returnType;
            Type[] argumentTypes;
            try {
                returnType = Type.getReturnType(call.desc);
                argumentTypes = Type.getArgumentTypes(call.desc);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            boolean booleanReturn = returnType.getSort() == Type.BOOLEAN;
            if (!booleanReturn
                    && returnType.getSort() != Type.FLOAT
                    && returnType.getSort() != Type.DOUBLE) {
                continue;
            }
            LifeCycleMethodCategory directStaticCategory = staticCall
                    ? directStaticCategory(lifeCycleKind, call.name, returnType)
                    : null;
            if (booleanReturn
                    && booleanLifeCycleMethod == null
                    && directStaticCategory == null) {
                continue;
            }
            Frame<BasicValue> frame = instructionIndex < frames.length ? frames[instructionIndex] : null;
            InvocationStackLayout stackLayout = invocationStackLayout(frame, argumentTypes, instanceCall);
            if (stackLayout == null) {
                continue;
            }

            BooleanPolarity polarity = booleanReturn && directStaticCategory == null
                    ? BooleanPolarityAnalyzer.analyze(method, call, frame)
                    : null;
            if (staticCall) {
                inspectStaticInvocation(
                        call,
                        argumentTypes,
                        frame,
                        stackLayout,
                        lifeCycleKind,
                        directStaticCategory,
                        booleanLifeCycleMethod,
                        polarity,
                        booleanReturn
                );
            } else {
                inspectInstanceInvocation(
                        rootEntityClass,
                        call,
                        frame,
                        stackLayout,
                        lifeCycleKind,
                        booleanLifeCycleMethod,
                        polarity,
                        booleanReturn
                );
            }
        }
    }

    private void inspectStaticInvocation(MethodInsnNode call,
                                         Type[] argumentTypes,
                                         Frame<BasicValue> frame,
                                         InvocationStackLayout stackLayout,
                                         LifeCycleKind lifeCycleKind,
                                         LifeCycleMethodCategory directStaticCategory,
                                         BooleanLifeCycleMethod booleanLifeCycleMethod,
                                         BooleanPolarity polarity,
                                         boolean booleanReturn) {
        if (argumentTypes.length == 0) {
            return;
        }
        InvocationKey key = new InvocationKey(
                SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(call.owner),
                call.name,
                call.desc,
                EntitySource.STATIC_ARGUMENTS
        );
        for (int argumentIndex = 0; argumentIndex < argumentTypes.length; argumentIndex++) {
            if (!isReferenceType(argumentTypes[argumentIndex])
                    || !ThisTrackingInterpreter.isThis(
                    frame.getStack(stackLayout.argumentStackIndexes()[argumentIndex]))) {
                continue;
            }
            InvocationEvidence evidence = observedInvocations.get(key);
            if (evidence == null) {
                evidence = new InvocationEvidence();
                observedInvocations.put(key, evidence);
            }
            if (directStaticCategory != null) {
                EnumSet<LifeCycleMethodCategory> categories =
                        evidence.directStaticCategoriesByArgument.get(argumentIndex);
                if (categories == null) {
                    categories = EnumSet.noneOf(LifeCycleMethodCategory.class);
                    evidence.directStaticCategoriesByArgument.put(argumentIndex, categories);
                }
                categories.add(directStaticCategory);
            }
            if (booleanReturn) {
                if (directStaticCategory != null || booleanLifeCycleMethod == null) {
                    continue;
                }
                EnumMap<BooleanLifeCycleMethod, BooleanPolarity> polarities =
                        evidence.booleanPolaritiesByArgument.get(argumentIndex);
                if (polarities == null) {
                    polarities = new EnumMap<>(BooleanLifeCycleMethod.class);
                    evidence.booleanPolaritiesByArgument.put(argumentIndex, polarities);
                }
                mergeBooleanPolarity(key, polarities, booleanLifeCycleMethod, polarity);
            } else {
                EnumSet<LifeCycleKind> kinds = evidence.healthLifeCycleKindsByArgument.get(argumentIndex);
                if (kinds == null) {
                    kinds = EnumSet.noneOf(LifeCycleKind.class);
                    evidence.healthLifeCycleKindsByArgument.put(argumentIndex, kinds);
                }
                kinds.add(lifeCycleKind);
            }
        }
    }

    private void inspectInstanceInvocation(Class<?> rootEntityClass,
                                           MethodInsnNode call,
                                           Frame<BasicValue> frame,
                                           InvocationStackLayout stackLayout,
                                           LifeCycleKind lifeCycleKind,
                                           BooleanLifeCycleMethod booleanLifeCycleMethod,
                                           BooleanPolarity polarity,
                                           boolean booleanReturn) {
        if (stackLayout.receiverStackIndex() < 0
                || !ThisTrackingInterpreter.isThis(frame.getStack(stackLayout.receiverStackIndex()))) {
            return;
        }
        if (booleanReturn) {
            if (nameLooksLikeIsAlive(call.name) || nameLooksLikeIsDeadOrDying(call.name)) {
                return;
            }
        } else if (nameLooksLikeHealth(call.name) || nameLooksLikeMaxHealth(call.name)) {
            return;
        }
        ResolvedInstanceMethod implementation = resolveInstanceImplementation(
                rootEntityClass,
                call.name,
                call.desc
        );
        if (implementation == null) {
            LogUtil.logf("Could not resolve concrete lifecycle invocation %s.%s%s from root %s",
                    call.owner,
                    call.name,
                    call.desc,
                    rootEntityClass == null ? "null" : rootEntityClass.getName());
            return;
        }
        InvocationKey key = new InvocationKey(
                implementation.owner(),
                call.name,
                call.desc,
                EntitySource.INSTANCE_THIS
        );
        InvocationEvidence evidence = observedInvocations.get(key);
        if (evidence == null) {
            evidence = new InvocationEvidence();
            observedInvocations.put(key, evidence);
        }
        if (booleanReturn) {
            if (booleanLifeCycleMethod == null) {
                return;
            }
            mergeBooleanPolarity(key, evidence.instanceBooleanPolarities, booleanLifeCycleMethod, polarity);
        } else {
            evidence.healthInstanceLifeCycleKinds.add(lifeCycleKind);
        }
    }

    private void mergeBooleanPolarity(InvocationKey key,
                                      EnumMap<BooleanLifeCycleMethod, BooleanPolarity> polarities,
                                      BooleanLifeCycleMethod lifeCycleMethod,
                                      BooleanPolarity polarity) {
        BooleanPolarity observed = polarity == null ? BooleanPolarity.UNKNOWN : polarity;
        BooleanPolarity existing = polarities.get(lifeCycleMethod);
        if (existing == null) {
            polarities.put(lifeCycleMethod, observed);
        } else if (existing != observed) {
            polarities.put(lifeCycleMethod, BooleanPolarity.UNKNOWN);
            if (SporeDiscoveredLifeCycleMethodRegistry.invalidate(
                    key.owner(),
                    key.name(),
                    key.desc(),
                    "mixed boolean polarity evidence"
            )) {
                pendingOwners.add(SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(key.owner()));
            }
        }
    }

    private BooleanLifeCycleMethod exactBooleanLifeCycleMethod(LifeCycleMethod method) {
        if (EXACT_ALIVE_METHOD.equals(method)) {
            return BooleanLifeCycleMethod.ALIVE;
        }
        if (EXACT_DEAD_OR_DYING_METHOD.equals(method)) {
            return BooleanLifeCycleMethod.DEAD_OR_DYING;
        }
        return null;
    }

    private InvocationStackLayout invocationStackLayout(Frame<BasicValue> frame,
                                                        Type[] argumentTypes,
                                                        boolean hasReceiver) {
        if (frame == null || frame.getStackSize() < argumentTypes.length + (hasReceiver ? 1 : 0)) {
            return null;
        }
        int[] argumentStackIndexes = new int[argumentTypes.length];
        int stackIndex = frame.getStackSize() - 1;
        for (int argumentIndex = argumentTypes.length - 1; argumentIndex >= 0; argumentIndex--) {
            BasicValue value = frame.getStack(stackIndex);
            if (value == null || value.getSize() != argumentTypes[argumentIndex].getSize()) {
                return null;
            }
            argumentStackIndexes[argumentIndex] = stackIndex--;
        }
        int receiverStackIndex = hasReceiver ? stackIndex : -1;
        if (hasReceiver && receiverStackIndex < 0) {
            return null;
        }
        return new InvocationStackLayout(receiverStackIndex, argumentStackIndexes);
    }

    private boolean isReferenceType(Type type) {
        return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
    }

    private ResolvedInstanceMethod resolveInstanceImplementation(Class<?> rootEntityClass,
                                                                 String name,
                                                                 String desc) {
        ResolvedInstanceMethod runtimeImplementation = resolveRuntimeInstanceImplementation(
                rootEntityClass,
                name,
                desc
        );
        return runtimeImplementation != null
                ? runtimeImplementation
                : resolveBytecodeInstanceImplementation(rootEntityClass, name, desc);
    }

    private ResolvedInstanceMethod resolveRuntimeInstanceImplementation(Class<?> rootEntityClass,
                                                                        String name,
                                                                        String desc) {
        for (Class<?> current = rootEntityClass; current != null; ) {
            Class<?> parent = null;
            try {
                parent = current.getSuperclass();
            } catch (Throwable ignored) {
            }

            Method[] methods = null;
            try {
                methods = ClassReflectionUtil.getDeclaredMethods(current);
            } catch (Throwable ignored) {
            }
            if (methods != null) {
                for (Method method : methods) {
                    if (method == null) {
                        continue;
                    }
                    try {
                        int modifiers = method.getModifiers();
                        if (!method.getName().equals(name)
                                || !Type.getMethodDescriptor(method).equals(desc)
                                || Modifier.isStatic(modifiers)
                                || Modifier.isAbstract(modifiers)
                                || Modifier.isNative(modifiers)
                                || method.isBridge()
                                || method.isSynthetic()) {
                            continue;
                        }
                        return new ResolvedInstanceMethod(
                                SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(
                                        method.getDeclaringClass().getName()
                                )
                        );
                    } catch (Throwable ignored) {
                    }
                }
            }
            current = parent;
        }
        return null;
    }

    private ResolvedInstanceMethod resolveBytecodeInstanceImplementation(Class<?> rootEntityClass,
                                                                         String name,
                                                                         String desc) {
        for (Class<?> current = rootEntityClass; current != null; current = current.getSuperclass()) {
            ClassNode classNode = readClassNode(current);
            if (classNode == null) {
                continue;
            }
            for (MethodNode candidate : classNode.methods) {
                if (!name.equals(candidate.name)
                        || !desc.equals(candidate.desc)
                        || (candidate.access & (Opcodes.ACC_STATIC
                        | Opcodes.ACC_ABSTRACT
                        | Opcodes.ACC_NATIVE)) != 0
                        || "<init>".equals(candidate.name)
                        || "<clinit>".equals(candidate.name)
                        || candidate.instructions == null
                        || candidate.instructions.size() == 0) {
                    continue;
                }
                return new ResolvedInstanceMethod(
                        SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(classNode.name)
                );
            }
        }
        return null;
    }

    private void inspectAndRetransformRegisteredOwners() {
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation != null) {
            installInstrumentationTransformer(instrumentation);
        }

        IJVNTIPointer jvmti = null;
        Class<?>[] loadedClasses = safeLoadedClasses(instrumentation);
        if (loadedClasses.length == 0 && instrumentation == null) {
            jvmti = JVMTIPointerUtil.newInstance();
            if (jvmti != null) {
                installJvmtiTransformer(jvmti);
                loadedClasses = safeLoadedClasses(jvmti);
            }
        }

        List<Class<?>> matchingClasses = matchingLoadedClasses(loadedClasses);
        if (matchingClasses.isEmpty()) {
            return;
        }
        Map<Class<?>, KlassAndAccessFlags> hiddenFlags = new IdentityHashMap<>();
        Set<Class<?>> transformed = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            List<Class<?>> prepared = prepareTargets(matchingClasses, hiddenFlags);
            if (prepared.isEmpty()) {
                return;
            }

            List<Class<?>> remaining = new ArrayList<>(prepared);
            if (instrumentation != null && instrumentation.isRetransformClassesSupported()) {
                List<Class<?>> instrumentationTargets = modifiableTargets(instrumentation, remaining);
                retransformBisected(instrumentation, instrumentationTargets, transformed);
                remaining.removeAll(transformed);
            }

            if (!remaining.isEmpty()) {
                if (jvmti == null) {
                    jvmti = JVMTIPointerUtil.newInstance();
                }
                if (jvmti != null && installJvmtiTransformer(jvmti)
                        && jvmti.isRetransformClassesSupported()
                        && jvmti.isTransformerHookInstalled()) {
                    List<Class<?>> jvmtiTargets = modifiableTargets(jvmti, remaining);
                    retransformBisected(jvmti, jvmtiTargets, transformed);
                }
            }
            clearCompletedOwners(matchingClasses, transformed);
        } catch (Throwable t) {
            LogUtil.errorf("failed to retransform discovered lifecycle methods, %s", t.getMessage());
            LogUtil.printStackTrace(t);
        } finally {
            for (Map.Entry<Class<?>, KlassAndAccessFlags> entry : hiddenFlags.entrySet()) {
                resetToHidden(entry.getKey(), entry.getValue());
            }
        }
    }

    private void installInstrumentationTransformer(IInstrumentations instrumentation) {
        if (instrumentationTransformerInstalled) {
            return;
        }
        ClassFileTransformer transformer = SporeDiscoveredLifeCycleMethodTransformer.newInstance();
        instrumentation.addTransformer(transformer);
        instrumentationTransformerInstalled = true;
    }

    private boolean installJvmtiTransformer(IJVNTIPointer jvmti) {
        if (jvmtiTransformerInstalled) {
            return true;
        }
        jvmti.addTransformer(SporeDiscoveredLifeCycleMethodTransformer.newSelfTransformer());
        jvmtiTransformerInstalled = jvmti.isTransformerHookInstalled();
        return jvmtiTransformerInstalled;
    }

    private Class<?>[] safeLoadedClasses(IInstrumentations instrumentation) {
        if (instrumentation == null) {
            return new Class<?>[0];
        }
        try {
            Class<?>[] classes = instrumentation.getAllLoadedClasses();
            return classes == null ? new Class<?>[0] : classes;
        } catch (Throwable t) {
            LogUtil.errorf("failed to enumerate loaded classes via Instrumentation, %s", t.getMessage());
            return new Class<?>[0];
        }
    }

    private Class<?>[] safeLoadedClasses(IJVNTIPointer jvmti) {
        if (jvmti == null) {
            return new Class<?>[0];
        }
        try {
            Class<?>[] classes = jvmti.getAllLoadedClasses();
            return classes == null ? new Class<?>[0] : classes;
        } catch (Throwable t) {
            LogUtil.errorf("failed to enumerate loaded classes via JVMTI, %s", t.getMessage());
            return new Class<?>[0];
        }
    }

    private List<Class<?>> matchingLoadedClasses(Class<?>[] loadedClasses) {
        List<Class<?>> result = new ArrayList<>();
        Set<Class<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Class<?> loadedClass : loadedClasses) {
            if (loadedClass == null || loadedClass.isArray() || loadedClass.isPrimitive()) {
                continue;
            }
            String owner = SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(loadedClass.getName());
            if (pendingOwners.contains(owner)
                    && !isOwnSporeClass(loadedClass.getName())
                    && seen.add(loadedClass)) {
                result.add(loadedClass);
            }
        }
        return result;
    }

    private List<Class<?>> prepareTargets(List<Class<?>> classes,
                                          Map<Class<?>, KlassAndAccessFlags> hiddenFlags) {
        List<Class<?>> targets = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (isHiddenLikeClass(clazz) && DISABLE_UNSAFE_HIDDEN_RETRANSFORM) {
                LogUtil.logf("Skip hidden-like discovered lifecycle class %s during post-definition retransform.",
                        clazz.getName());
                continue;
            }
            prepareRetransformDependencies(clazz);
            if (clazz.isHidden()) {
                KlassAndAccessFlags flags = modifyClassAccessFlags(clazz);
                if (flags == null) {
                    continue;
                }
                hiddenFlags.put(clazz, flags);
            }
            targets.add(clazz);
        }
        return targets;
    }

    private List<Class<?>> modifiableTargets(IInstrumentations instrumentation, Collection<Class<?>> classes) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> clazz : classes) {
            try {
                if (instrumentation.isModifiableClass(clazz)) {
                    result.add(clazz);
                }
            } catch (Throwable t) {
                LogUtil.errorf("discovered lifecycle class %s is not modifiable via Instrumentation, %s",
                        clazz.getName(),
                        t.getMessage());
            }
        }
        return result;
    }

    private List<Class<?>> modifiableTargets(IJVNTIPointer jvmti, Collection<Class<?>> classes) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> clazz : classes) {
            try {
                if (jvmti.isModifiableClass(clazz)) {
                    result.add(clazz);
                }
            } catch (Throwable t) {
                LogUtil.errorf("discovered lifecycle class %s is not modifiable via JVMTI, %s",
                        clazz.getName(),
                        t.getMessage());
            }
        }
        return result;
    }

    private void retransformBisected(IInstrumentations instrumentation,
                                      List<Class<?>> targets,
                                      Set<Class<?>> transformed) {
        if (targets.isEmpty()) {
            return;
        }
        try {
            retransformWithContextLoader(instrumentation, targets);
            transformed.addAll(targets);
        } catch (Throwable t) {
            if (targets.size() == 1) {
                Class<?> target = targets.get(0);
                LogUtil.errorf("Skipped discovered lifecycle class %s during Instrumentation retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("instrumentation-discovered-lifecycle", target, t);
                return;
            }
            int middle = targets.size() / 2;
            retransformBisected(instrumentation, targets.subList(0, middle), transformed);
            retransformBisected(instrumentation, targets.subList(middle, targets.size()), transformed);
        }
    }

    private void retransformBisected(IJVNTIPointer jvmti,
                                      List<Class<?>> targets,
                                      Set<Class<?>> transformed) {
        if (targets.isEmpty()) {
            return;
        }
        try {
            retransformWithContextLoader(jvmti, targets);
            transformed.addAll(targets);
        } catch (Throwable t) {
            if (targets.size() == 1) {
                Class<?> target = targets.get(0);
                LogUtil.errorf("Skipped discovered lifecycle class %s during JVMTI retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("jvmti-discovered-lifecycle", target, t);
                return;
            }
            int middle = targets.size() / 2;
            retransformBisected(jvmti, targets.subList(0, middle), transformed);
            retransformBisected(jvmti, targets.subList(middle, targets.size()), transformed);
        }
    }

    private void retransformWithContextLoader(IInstrumentations instrumentation,
                                              List<Class<?>> targets) throws Throwable {
        Thread thread = Thread.currentThread();
        ClassLoader originalLoader = thread.getContextClassLoader();
        ClassLoader targetLoader = commonTargetClassLoader(targets);
        try {
            if (targetLoader != null && targetLoader != originalLoader) {
                thread.setContextClassLoader(targetLoader);
            }
            instrumentation.retransformClasses(targets.toArray(new Class<?>[0]));
        } finally {
            if (targetLoader != null && targetLoader != originalLoader) {
                thread.setContextClassLoader(originalLoader);
            }
        }
    }

    private void retransformWithContextLoader(IJVNTIPointer jvmti,
                                              List<Class<?>> targets) throws Throwable {
        Thread thread = Thread.currentThread();
        ClassLoader originalLoader = thread.getContextClassLoader();
        ClassLoader targetLoader = commonTargetClassLoader(targets);
        try {
            if (targetLoader != null && targetLoader != originalLoader) {
                thread.setContextClassLoader(targetLoader);
            }
            jvmti.retransformClasses(targets.toArray(new Class<?>[0]));
        } finally {
            if (targetLoader != null && targetLoader != originalLoader) {
                thread.setContextClassLoader(originalLoader);
            }
        }
    }

    private ClassLoader commonTargetClassLoader(List<Class<?>> targets) {
        ClassLoader result = null;
        boolean initialized = false;
        for (Class<?> target : targets) {
            if (!initialized) {
                result = target.getClassLoader();
                initialized = true;
            } else if (result != target.getClassLoader()) {
                return null;
            }
        }
        return result;
    }

    private void clearCompletedOwners(List<Class<?>> matchingClasses, Set<Class<?>> transformed) {
        Map<String, Boolean> completedByOwner = new HashMap<>();
        for (Class<?> matchingClass : matchingClasses) {
            String owner = SporeDiscoveredLifeCycleMethodRegistry.normalizeOwner(matchingClass.getName());
            boolean completed = transformed.contains(matchingClass);
            Boolean previous = completedByOwner.get(owner);
            completedByOwner.put(owner, previous == null ? completed : previous && completed);
        }
        for (Map.Entry<String, Boolean> entry : completedByOwner.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                pendingOwners.remove(entry.getKey());
            }
        }
    }

    private KlassAndAccessFlags modifyClassAccessFlags(Class<?> clazz) {
        Unsafe unsafe = ClassUtil.getUnsafe();
        if (unsafe == null) {
            LogUtil.error("Unsafe is unavailable, skip hidden discovered lifecycle class retransform.");
            return null;
        }
        long klass = unsafe.getLong(clazz, CLASS_KLASS_OFFSET);
        if (klass == 0L) {
            return null;
        }
        int accessFlags = unsafe.getInt(klass + KLASS_ACCESS_FLAGS_OFFSET);
        if ((accessFlags & JVM_ACC_IS_HIDDEN_CLASS) == 0) {
            LogUtil.errorf("Hidden discovered lifecycle class %s does not expose expected hidden access flag.",
                    clazz.getName());
            return null;
        }
        unsafe.putInt(
                klass + KLASS_ACCESS_FLAGS_OFFSET,
                accessFlags & ~JVM_ACC_IS_HIDDEN_CLASS & ~JVM_ACC_IS_BEING_REDEFINED
        );
        return new KlassAndAccessFlags(klass, accessFlags);
    }

    private void resetToHidden(Class<?> clazz, KlassAndAccessFlags flags) {
        if (flags == null) {
            return;
        }
        Unsafe unsafe = ClassUtil.getUnsafe();
        if (unsafe == null) {
            LogUtil.errorf("Unsafe is unavailable, cannot restore hidden flag for %s.",
                    clazz == null ? "null" : clazz.getName());
            return;
        }
        unsafe.putInt(flags.klass() + KLASS_ACCESS_FLAGS_OFFSET, flags.accessFlags());
    }

    private void prepareRetransformDependencies(Class<?> target) {
        ClassLoader loader = target == null ? null : target.getClassLoader();
        for (String dependency : RETRANSFORM_HOOK_DEPENDENCIES) {
            try {
                Class.forName(dependency, false, loader);
            } catch (Throwable ignored) {
                try {
                    Class.forName(dependency, false, LifeCycleInvocationInspector.class.getClassLoader());
                } catch (Throwable ignoredAgain) {
                }
            }
        }
    }

    private boolean isHiddenLikeClass(Class<?> clazz) {
        String name = clazz == null ? null : clazz.getName();
        return name != null && (name.contains("/0x") || name.contains("+0x"));
    }

    private boolean isOwnSporeClass(String name) {
        return name != null && name.replace('.', '/').startsWith("com/Harbinger/Spore/");
    }

    @Override
    public Set<LifeCycleMethod> apply(Object ignored) {
        return new HashSet<>();
    }

    private enum LifeCycleKind {
        HEALTH,
        MAX_HEALTH,
        DEAD_OR_DYING,
        ALIVE
    }

    private enum BooleanLifeCycleMethod {
        ALIVE,
        DEAD_OR_DYING
    }

    private record InvocationKey(String owner,
                                 String name,
                                 String desc,
                                 EntitySource entitySource) {
    }

    private record DiscoveredMethod(String owner,
                                    String name,
                                    String desc,
                                    LifeCycleMethodTarget target) {
    }

    private static final class InvocationEvidence {
        private final Map<Integer, EnumSet<LifeCycleKind>> healthLifeCycleKindsByArgument = new HashMap<>();
        private final EnumSet<LifeCycleKind> healthInstanceLifeCycleKinds = EnumSet.noneOf(LifeCycleKind.class);
        private final Map<Integer, EnumSet<LifeCycleMethodCategory>> directStaticCategoriesByArgument =
                new HashMap<>();
        private final Map<Integer, EnumMap<BooleanLifeCycleMethod, BooleanPolarity>>
                booleanPolaritiesByArgument = new HashMap<>();
        private final EnumMap<BooleanLifeCycleMethod, BooleanPolarity> instanceBooleanPolarities =
                new EnumMap<>(BooleanLifeCycleMethod.class);
    }

    private record LifeCycleInspectionKey(Class<?> rootEntityClass, Class<?> declaringLifeCycleClass) {
    }

    private record InvocationStackLayout(int receiverStackIndex, int[] argumentStackIndexes) {
    }

    private record ResolvedInstanceMethod(String owner) {
    }

    private static final class ThisTrackingValue extends BasicValue {
        private final boolean thisValue;

        private ThisTrackingValue(Type type, boolean thisValue) {
            super(type);
            this.thisValue = thisValue;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ThisTrackingValue value
                    && thisValue == value.thisValue
                    && super.equals(other);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Boolean.hashCode(thisValue);
        }
    }

    private static final class ThisTrackingInterpreter extends BasicInterpreter {
        private ThisTrackingInterpreter() {
            super(Opcodes.ASM9);
        }

        @Override
        public BasicValue newParameterValue(boolean isInstanceMethod, int local, Type type) {
            if (isInstanceMethod && local == 0) {
                return new ThisTrackingValue(type, true);
            }
            return super.newParameterValue(isInstanceMethod, local, type);
        }

        @Override
        public BasicValue unaryOperation(AbstractInsnNode instruction, BasicValue value)
                throws AnalyzerException {
            BasicValue result = super.unaryOperation(instruction, value);
            if (instruction.getOpcode() == Opcodes.CHECKCAST && isThis(value) && result != null) {
                return new ThisTrackingValue(result.getType(), true);
            }
            return result;
        }

        @Override
        public BasicValue merge(BasicValue first, BasicValue second) {
            BasicValue merged = super.merge(first, second);
            if (isThis(first) && isThis(second) && merged != null) {
                return new ThisTrackingValue(merged.getType(), true);
            }
            return merged;
        }

        private static boolean isThis(BasicValue value) {
            return value instanceof ThisTrackingValue tracked && tracked.thisValue;
        }
    }

    private record KlassAndAccessFlags(long klass, int accessFlags) {
    }

    static {
        Class<? extends ILifeCycleInvocationInspect>[] managerClass = new Class[1];
        INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
                managerClass,
                ILifeCycleInvocationInspect.class,
                LifeCycleInvocationInspector.class,
                new Class<?>[0]
        );
        if (managerClass[0] != null) {
            ClassReflectionUtil.removeCachedReflectionData(managerClass[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(LifeCycleInvocationInspector.class);
    }
}
