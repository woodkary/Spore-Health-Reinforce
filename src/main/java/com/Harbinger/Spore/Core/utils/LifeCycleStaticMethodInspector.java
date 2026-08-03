package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import com.Harbinger.Spore.Core.agents.transformers.SporeStaticHealthMethodRegistry;
import com.Harbinger.Spore.Core.agents.transformers.SporeStaticHealthMethodTransformer;
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
import java.util.*;

public final class LifeCycleStaticMethodInspector implements ILifeCycleStaticMethodInspect {
    private static final long CLASS_KLASS_OFFSET = 16L;
    private static final long KLASS_ACCESS_FLAGS_OFFSET = 164L;
    private static final int JVM_ACC_IS_HIDDEN_CLASS = 0x04000000;
    private static final int JVM_ACC_IS_BEING_REDEFINED = 0x00100000;
    private static final String HEALTH_WRAPPER_SUFFIX = "SporeHealthLifecycleWrapper";
    private static final String DEATH_WRAPPER_SUFFIX = "SporeDeathLifecycleWrapper";
    private static final boolean DISABLE_UNSAFE_HIDDEN_RETRANSFORM =
            Boolean.getBoolean("spore.transformer.disableUnsafeHiddenRetransform");
    private static final String[] RETRANSFORM_HOOK_DEPENDENCIES = {
            "com.Harbinger.Spore.Core.asmHooks.IEntityHealth",
            "com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager",
            "net.minecraft.world.entity.LivingEntity",
            "net.minecraft.world.entity.Entity"
    };

    public static final ILifeCycleStaticMethodInspect INSTANCE;

    private final Map<StaticMethodKey, StaticMethodEvidence> observedStaticMethods = new LinkedHashMap<>();
    private final Map<Class<?>, Set<LifeCycleMethod>> inspectedLifeCycleMethods = new IdentityHashMap<>();
    private final Set<String> pendingOwners = new LinkedHashSet<>();
    private boolean instrumentationTransformerInstalled;
    private boolean jvmtiTransformerInstalled;

    public LifeCycleStaticMethodInspector() {
    }

    @Override
    public synchronized void inspectAndCacheLifeCycleStaticMethods(Class<?> livingEntityClass) {
        try {
            inspectAndCacheLifeCycleStaticMethodsInternal(livingEntityClass);
        } catch (Throwable t) {
            LogUtil.errorf("failed to cache lifecycle static methods for %s, %s",
                    livingEntityClass == null ? "null" : livingEntityClass.getName(),
                    t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }

    private void inspectAndCacheLifeCycleStaticMethodsInternal(Class<?> livingEntityClass) {
        Class<?> rawClass = getRawOriginalClass(livingEntityClass);
        if (rawClass == null || !LivingEntity.class.isAssignableFrom(rawClass)) {
            return;
        }
        for (Class<?> current = rawClass;
             current != null && LivingEntity.class.isAssignableFrom(current);
             current = current.getSuperclass()) {
            inspectDeclaredLifeCycleMethods(current);
        }
    }

    private void inspectDeclaredLifeCycleMethods(Class<?> entityClass) {
        ClassNode classNode = readClassNode(entityClass);
        if (classNode == null) {
            return;
        }
        Set<LifeCycleMethod> inspected = inspectedLifeCycleMethods.computeIfAbsent(
                entityClass,
                ignored -> new HashSet<>()
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
                inspectStaticCalls(classNode.name, method, kind);
            }
        }
    }

    @Override
    public synchronized void inspectAndRetransformStatic() {
        try {
            inspectAndRetransformStaticInternal();
        } catch (Throwable t) {
            LogUtil.errorf("failed to inspect shared static lifecycle health methods, %s", t.getMessage());
            LogUtil.printStackTrace(t);
        }
    }

    private void inspectAndRetransformStaticInternal() {
        for (StaticMethod method : frequentMethods()) {
            if (!StackTraceUtil.isBadModName(method.owner())) {
                continue;
            }
            if (SporeStaticHealthMethodRegistry.register(
                    method.owner(),
                    method.name(),
                    method.desc(),
                    method.entityArgumentIndexes())) {
                pendingOwners.add(SporeStaticHealthMethodRegistry.normalizeOwner(method.owner()));
                LogUtil.logf("Discovered shared static lifecycle health method %s.%s%s",
                        method.owner(),
                        method.name(),
                        method.desc());
            }
        }
        if (!SporeStaticHealthMethodRegistry.owners().isEmpty()) {
            inspectAndRetransformRegisteredOwners();
        }
    }

    private Set<StaticMethod> frequentMethods() {
        Set<StaticMethod> result = new LinkedHashSet<>();
        for (Map.Entry<StaticMethodKey, StaticMethodEvidence> entry : observedStaticMethods.entrySet()) {
            StaticMethodEvidence evidence = entry.getValue();
            Set<Integer> frequentArgumentIndexes = new TreeSet<>();
            for (Map.Entry<Integer, EnumSet<LifeCycleKind>> argumentEvidence
                    : evidence.lifeCycleKindsByArgument.entrySet()) {
                if (argumentEvidence.getValue().size() >= 2) {
                    frequentArgumentIndexes.add(argumentEvidence.getKey());
                }
            }
            if (frequentArgumentIndexes.isEmpty()) {
                continue;
            }
            StaticMethodKey key = entry.getKey();
            result.add(new StaticMethod(
                    key.owner(),
                    key.name(),
                    key.desc(),
                    Collections.unmodifiableSet(frequentArgumentIndexes)
            ));
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

    private void inspectStaticCalls(String className, MethodNode method, LifeCycleKind lifeCycleKind) {
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
            if (!(instruction instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESTATIC) {
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
            if ((returnType.getSort() != Type.FLOAT && returnType.getSort() != Type.DOUBLE)
                    || argumentTypes.length == 0) {
                continue;
            }
            Frame<BasicValue> frame = instructionIndex < frames.length ? frames[instructionIndex] : null;
            if (frame == null || frame.getStackSize() < argumentTypes.length) {
                continue;
            }
            int firstArgument = frame.getStackSize() - argumentTypes.length;
            StaticMethodKey key = new StaticMethodKey(
                    SporeStaticHealthMethodRegistry.normalizeOwner(call.owner),
                    call.name,
                    call.desc
            );
            for (int argumentIndex = 0; argumentIndex < argumentTypes.length; argumentIndex++) {
                if (argumentTypes[argumentIndex].getSort() != Type.OBJECT
                        || !ThisTrackingInterpreter.isThis(frame.getStack(firstArgument + argumentIndex))) {
                    continue;
                }
                StaticMethodEvidence evidence = observedStaticMethods.get(key);
                if (evidence == null) {
                    evidence = new StaticMethodEvidence();
                    observedStaticMethods.put(key, evidence);
                }
                EnumSet<LifeCycleKind> kinds = evidence.lifeCycleKindsByArgument.get(argumentIndex);
                if (kinds == null) {
                    kinds = EnumSet.noneOf(LifeCycleKind.class);
                    evidence.lifeCycleKindsByArgument.put(argumentIndex, kinds);
                }
                kinds.add(lifeCycleKind);
            }
        }
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
            LogUtil.errorf("failed to retransform shared static lifecycle health methods, %s", t.getMessage());
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
        ClassFileTransformer transformer = SporeStaticHealthMethodTransformer.newInstance();
        instrumentation.addTransformer(transformer);
        instrumentationTransformerInstalled = true;
    }

    private boolean installJvmtiTransformer(IJVNTIPointer jvmti) {
        if (jvmtiTransformerInstalled) {
            return true;
        }
        jvmti.addTransformer(SporeStaticHealthMethodTransformer.newSelfTransformer());
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
            String owner = SporeStaticHealthMethodRegistry.normalizeOwner(loadedClass.getName());
            if (pendingOwners.contains(owner)
                    && StackTraceUtil.isBadModName(loadedClass.getName())
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
                LogUtil.logf("Skip hidden-like static lifecycle class %s during post-definition retransform.",
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
                LogUtil.errorf("static lifecycle class %s is not modifiable via Instrumentation, %s",
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
                LogUtil.errorf("static lifecycle class %s is not modifiable via JVMTI, %s",
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
                LogUtil.errorf("Skipped static lifecycle class %s during Instrumentation retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("instrumentation-static-health", target, t);
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
                LogUtil.errorf("Skipped static lifecycle class %s during JVMTI retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("jvmti-static-health", target, t);
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
            String owner = SporeStaticHealthMethodRegistry.normalizeOwner(matchingClass.getName());
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
            LogUtil.error("Unsafe is unavailable, skip hidden static lifecycle class retransform.");
            return null;
        }
        long klass = unsafe.getLong(clazz, CLASS_KLASS_OFFSET);
        if (klass == 0L) {
            return null;
        }
        int accessFlags = unsafe.getInt(klass + KLASS_ACCESS_FLAGS_OFFSET);
        if ((accessFlags & JVM_ACC_IS_HIDDEN_CLASS) == 0) {
            LogUtil.errorf("Hidden static lifecycle class %s does not expose expected hidden access flag.",
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
                    Class.forName(dependency, false, LifeCycleStaticMethodInspector.class.getClassLoader());
                } catch (Throwable ignoredAgain) {
                }
            }
        }
    }

    private boolean isHiddenLikeClass(Class<?> clazz) {
        String name = clazz == null ? null : clazz.getName();
        return name != null && (name.contains("/0x") || name.contains("+0x"));
    }

    private enum LifeCycleKind {
        HEALTH,
        MAX_HEALTH,
        DEAD_OR_DYING,
        ALIVE
    }

    private record StaticMethodKey(String owner, String name, String desc) {
    }

    private record StaticMethod(String owner,
                                String name,
                                String desc,
                                Set<Integer> entityArgumentIndexes) {
    }

    private static final class StaticMethodEvidence {
        private final Map<Integer, EnumSet<LifeCycleKind>> lifeCycleKindsByArgument = new HashMap<>();
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
        Class<? extends ILifeCycleStaticMethodInspect>[] managerClass = new Class[1];
        INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
                managerClass,
                ILifeCycleStaticMethodInspect.class,
                LifeCycleStaticMethodInspector.class,
                new Class<?>[0]
        );
        if (managerClass[0] != null) {
            ClassReflectionUtil.removeCachedReflectionData(managerClass[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(LifeCycleStaticMethodInspector.class);
    }
}
