package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.IJVNTIPointer;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.agents.JVMTIPointerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import net.minecraft.world.entity.LivingEntity;
import sun.misc.Unsafe;

import java.lang.instrument.ClassFileTransformer;
import java.util.*;

public final class SporeLivingEntityHealthTransformerBootstrap implements ICommonBootStrap  {
    private static final long CLASS_KLASS_OFFSET = 16L;
    private static final long KLASS_ACCESS_FLAGS_OFFSET = 164L;
    private static final int JVM_ACC_IS_HIDDEN_CLASS = 0x04000000;
    public static final ICommonBootStrap INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            ICommonBootStrap.class,
            SporeLivingEntityHealthTransformerBootstrap.class
    );
    private volatile boolean installed;
    private volatile boolean jvmtiTransInstalled;
    private volatile boolean hiddenRetransformInstalled;
    private volatile boolean hiddenJvmtiRetransformInstalled;
    private volatile boolean loadedLivingEntitiesRetransformed;

    private IJVNTIPointer ensureJVMTIUtil(IJVNTIPointer jvmtiUtil){
        if(jvmtiUtil == null){
            return JVMTIPointerUtil.newInstance();
        }
        return jvmtiUtil;
    }
    @Override
    public synchronized void retransformMaybeHiddenClasses(Class<?>... classes){
        if (classes == null || classes.length == 0) {
            return;
        }
        Map<Class<?>,KlassAndAccessFlags> hiddenAddresses=new HashMap<>();
        try {
            IInstrumentations instrumentation = InstrumentationUtil.getInstance();
            boolean instrumentationReady = instrumentation != null
                    && instrumentation.isRetransformClassesSupported()
                    && installTransformersForHiddenRetransform(instrumentation);
            IJVNTIPointer jvmUtil = null;
            boolean jvmtiReady = false;
            if (!instrumentationReady) {
                jvmUtil = ensureJVMTIUtil(null);
                jvmtiReady = isJvmtiReadyForHiddenRetransform(jvmUtil);
            }
            if (!instrumentationReady && !jvmtiReady) {
                LogUtil.error("No usable transformer backend for hidden LivingEntity classes.");
                return;
            }
            List<Class<?>> targets=collectMaybeHiddenRetransformTargets(
                    classes,
                    hiddenAddresses,
                    instrumentation,
                    jvmUtil,
                    instrumentationReady,
                    jvmtiReady
            );
            if (targets.isEmpty()) {
                return;
            }
            if (instrumentationReady) {
                Collection<Class<?>> visited=new ArrayList<>();
                int transformed=retransformBisected(instrumentation, targets,visited);
                if(transformed>=targets.size()) {
                    return;
                }
                Collection<Class<?>> remaining=new HashSet<>(targets);
                for (Class<?> vis : visited) {
                    remaining.remove(vis);
                }
                List<Class<?>> toRetransform=new ArrayList<>(remaining);
                jvmUtil = ensureJVMTIUtil(jvmUtil);
                if (isJvmtiReadyForHiddenRetransform(jvmUtil)) {
                    retransformBisected(jvmUtil,toRetransform);
                }
                return;
            }
            retransformBisected(jvmUtil, targets);
        }finally {
            for (Map.Entry<Class<?>, KlassAndAccessFlags> entry : hiddenAddresses.entrySet()) {
                resetToHidden(entry.getKey(), entry.getValue());
            }
        }
    }
    @Override
    public synchronized void retransformMaybeHiddenClassesInstOnly(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return;
        }
        Map<Class<?>,KlassAndAccessFlags> hiddenAddresses=new HashMap<>();
        try {
            IInstrumentations instrumentation = InstrumentationUtil.getInstance();
            boolean instrumentationReady = instrumentation != null
                    && instrumentation.isRetransformClassesSupported()
                    && installTransformersForHiddenRetransform(instrumentation);
            if (!instrumentationReady) {
                LogUtil.error("Instrumentation backend is unavailable for maybe-hidden LivingEntity classes.");
                return;
            }
            List<Class<?>> targets=collectMaybeHiddenRetransformTargets(
                    classes,
                    hiddenAddresses,
                    instrumentation,
                    null,
                    true,
                    false
            );
            if (!targets.isEmpty()) {
                retransformBisected(instrumentation, targets, new ArrayList<>());
            }
        } finally {
            for (Map.Entry<Class<?>, KlassAndAccessFlags> entry : hiddenAddresses.entrySet()) {
                resetToHidden(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public synchronized void retransformMaybeHiddenClassesJVMTIOnly(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return;
        }
        Map<Class<?>,KlassAndAccessFlags> hiddenAddresses=new HashMap<>();
        try {
            IJVNTIPointer jvmUtil = ensureJVMTIUtil(null);
            if (!isJvmtiReadyForHiddenRetransform(jvmUtil)) {
                LogUtil.error("JVMTI backend is unavailable for maybe-hidden LivingEntity classes.");
                return;
            }
            List<Class<?>> targets=collectMaybeHiddenRetransformTargets(
                    classes,
                    hiddenAddresses,
                    null,
                    jvmUtil,
                    false,
                    true
            );
            if (!targets.isEmpty()) {
                retransformBisected(jvmUtil, targets);
            }
        } finally {
            for (Map.Entry<Class<?>, KlassAndAccessFlags> entry : hiddenAddresses.entrySet()) {
                resetToHidden(entry.getKey(), entry.getValue());
            }
        }
    }

    private List<Class<?>> collectMaybeHiddenRetransformTargets(Class<?>[] classes,
                                                               Map<Class<?>,KlassAndAccessFlags> hiddenAddresses,
                                                               IInstrumentations instrumentation,
                                                               IJVNTIPointer jvmtiUtil,
                                                               boolean useInstrumentation,
                                                               boolean useJvmti) {
        List<Class<?>> targets=new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz == null) {
                continue;
            }
            boolean isHidden=clazz.isHidden();
            KlassAndAccessFlags flags=null;
            if (isHidden) {
                flags=modifyClassesAccessFlags(clazz);
                if (flags == null) {
                    continue;
                }
                hiddenAddresses.put(clazz, flags);
            }
            boolean shouldRetransform = false;
            if (useInstrumentation) {
                shouldRetransform = shouldRetransformHiddenCandidate(instrumentation, clazz);
            }
            if (!shouldRetransform && useJvmti) {
                shouldRetransform = shouldRetransformHiddenCandidate(jvmtiUtil, clazz);
            }
            if (shouldRetransform) {
                targets.add(clazz);
            } else if (isHidden) {
                resetToHidden(clazz, flags);
                hiddenAddresses.remove(clazz);
            }
        }
        return targets;
    }

    private KlassAndAccessFlags modifyClassesAccessFlags(Class<?> clazz){
        if(clazz == null || !clazz.isHidden()){
            return null;
        }
        Unsafe uns= ClassUtil.getUnsafe();
        if (uns == null) {
            LogUtil.error("Unsafe is unavailable, skip hidden class retransform.");
            return null;
        }
        long klass=uns.getLong(clazz, CLASS_KLASS_OFFSET);
        if (klass == 0L) {
            return null;
        }
        int accessFlags=uns.getInt(klass + KLASS_ACCESS_FLAGS_OFFSET);
        if ((accessFlags & JVM_ACC_IS_HIDDEN_CLASS) == 0) {
            LogUtil.errorf("Hidden class %s does not expose expected hidden access flag, skip unsafe retransform.", clazz.getName());
            return null;
        }
        uns.putInt(klass + KLASS_ACCESS_FLAGS_OFFSET, accessFlags & ~JVM_ACC_IS_HIDDEN_CLASS);
        return new KlassAndAccessFlags(klass, accessFlags);
    }
    private void resetToHidden(Class<?> clazz, KlassAndAccessFlags klassAndAccessFlags){
        if(klassAndAccessFlags==null){
            return;
        }
        Unsafe uns= ClassUtil.getUnsafe();
        if (uns == null) {
            LogUtil.errorf("Unsafe is unavailable, cannot restore hidden class flag for %s.", clazz == null ? "null" : clazz.getName());
            return;
        }
        uns.putInt(klassAndAccessFlags.klass() + KLASS_ACCESS_FLAGS_OFFSET, klassAndAccessFlags.accessFlags());
    }

    @Override
    public synchronized void installAndRetransform() {
        if (loadedLivingEntitiesRetransformed) {
            return;
        }
        Collection<Class<?>> remaining = null;
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation != null&&installTransformers(instrumentation)) {
            Collection<Class<?>> visited=new ArrayList<>();
            remaining=retransformLivingEntitiesViaInst(instrumentation,visited);
        }
        if(remaining!=null&&remaining.isEmpty()){
            //全部转换，结束
            loadedLivingEntitiesRetransformed = true;
            return;
        }
        LogUtil.log("Instrumentation can't transform all, start JVMTI.");
        //用jvmti transform剩余的类
        IJVNTIPointer jvmtiUtil=JVMTIPointerUtil.newInstance();
        if (isJvmtiReady(jvmtiUtil)) {
            Collection<Class<?>> jvmtiTargets = remaining == null ? collectJvmtiLivingEntityTargets(jvmtiUtil) : remaining;
            int transformed = retransformLivingEntitiesViaJVMTI(jvmtiUtil,jvmtiTargets);
            if (transformed >= jvmtiTargets.size() && (remaining != null || !jvmtiTargets.isEmpty())) {
                loadedLivingEntitiesRetransformed = true;
            } else if (jvmtiTargets.isEmpty()) {
                LogUtil.error("JVMTI fallback found no loaded LivingEntity classes; keep transformer bootstrap retryable.");
            } else {
                LogUtil.errorf("JVMTI fallback transformed %d/%d loaded LivingEntity classes.", transformed, jvmtiTargets.size());
            }
        } else if (remaining != null && !remaining.isEmpty()) {
            LogUtil.errorf("JVMTI fallback is unavailable; %d loaded LivingEntity classes remain untransformed.", remaining.size());
        }
    }

    private boolean isJvmtiReady(IJVNTIPointer jvmtiUtil) {
        if (jvmtiUtil == null) {
            return false;
        }
        try {
            return jvmtiUtil.isRetransformClassesSupported()
                    && installTransformers(jvmtiUtil)
                    && jvmtiUtil.isTransformerHookInstalled();
        } catch (Throwable t) {
            LogUtil.errorf("JVMTI transformer backend is unavailable: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }
    private boolean isJvmtiReadyForHiddenRetransform(IJVNTIPointer jvmtiUtil) {
        if (jvmtiUtil == null) {
            return false;
        }
        try {
            return jvmtiUtil.isRetransformClassesSupported()
                    && installTransformersForHiddenRetransform(jvmtiUtil)
                    && jvmtiUtil.isTransformerHookInstalled();
        } catch (Throwable t) {
            LogUtil.errorf("JVMTI hidden transformer backend is unavailable: %s", t.getMessage());
            LogUtil.printStackTrace(t);
            return false;
        }
    }

    private boolean installTransformers(IJVNTIPointer jvmtiUtil) {
        if (jvmtiUtil == null) {
            return false;
        }
        if(jvmtiTransInstalled){
            return true;
        }
        SelfTransformer healthTransformer = SporeLivingEntityHealthTransformer.newSelfTransformer();
        SelfTransformer effectApplicationTransformer = SporeLivingEntityEffectApplicationTransformer.newSelfTransformer();
        jvmtiUtil.addTransformer(healthTransformer);
        jvmtiUtil.addTransformer(effectApplicationTransformer);
        jvmtiTransInstalled=jvmtiUtil.isTransformerHookInstalled();
        return true;
    }
    private boolean installTransformersForHiddenRetransform(IJVNTIPointer jvmtiUtil) {
        if (jvmtiUtil == null) {
            return false;
        }
        if(hiddenJvmtiRetransformInstalled){
            return true;
        }
        SelfTransformer healthTransformer = SporeLivingEntityHealthTransformer.newSelfTransformer();
        SelfTransformer effectApplicationTransformer = SporeLivingEntityEffectApplicationTransformer.newSelfTransformer();
        jvmtiUtil.addTransformer(healthTransformer);
        jvmtiUtil.addTransformer(effectApplicationTransformer);
        hiddenJvmtiRetransformInstalled=true;
        return true;
    }
    private boolean installTransformers(IInstrumentations instrumentation) {
        if (installed) {
            return true;
        }
        ClassFileTransformer healthTransformer = SporeLivingEntityHealthTransformer.newInstance();
        ClassFileTransformer effectApplicationTransformer = SporeLivingEntityEffectApplicationTransformer.newInstance();
        instrumentation.addTransformer(healthTransformer);
        instrumentation.addTransformer(effectApplicationTransformer);
        installed = true;
        return true;
    }
    private boolean installTransformersForHiddenRetransform(IInstrumentations instrumentation) {
        if (instrumentation == null) {
            return false;
        }
        if (hiddenRetransformInstalled) {
            return true;
        }
        ClassFileTransformer healthTransformer = SporeLivingEntityHealthTransformer.newInstance();
        ClassFileTransformer effectApplicationTransformer = SporeLivingEntityEffectApplicationTransformer.newInstance();
        instrumentation.addTransformer(healthTransformer);
        instrumentation.addTransformer(effectApplicationTransformer);
        hiddenRetransformInstalled = true;
        return true;
    }
    private int retransformLivingEntitiesViaJVMTI(IJVNTIPointer jvmtiUtil, Collection<Class<?>> targets){
        if (jvmtiUtil == null) {
            return 0;
        }
        if(targets==null||targets.isEmpty()){
            targets = collectJvmtiLivingEntityTargets(jvmtiUtil);
        }
        if (targets.isEmpty()) {
            LogUtil.log("No loaded LivingEntity classes need retransform.");
            return 0;
        }
        if (!jvmtiUtil.isRetransformClassesSupported()) {
            LogUtil.error("RetransformClasses is not supported by current Instrumentation.");
            return 0;
        }
        List<Class<?>> toRetransform = targets instanceof List<Class<?>> l ? l : new ArrayList<>(targets);
        //用jvmti转换target
        int transformed = retransformBisected(jvmtiUtil, toRetransform);
        LogUtil.logf("Retransformed %d loaded LivingEntity classes.", transformed);
        return transformed;
    }

    private Collection<Class<?>> collectJvmtiLivingEntityTargets(IJVNTIPointer jvmtiUtil) {
        List<Class<?>> targets = new ArrayList<>();
        if (jvmtiUtil == null) {
            return targets;
        }
        Class<?>[] allLoaded = jvmtiUtil.getAllLoadedClasses();
        for (Class<?> clazz : allLoaded) {
            if (shouldRetransform(jvmtiUtil, clazz)) {
                targets.add(clazz);
            }
        }
        return targets;
    }

    //返回本次transform剩余的类
    private Collection<Class<?>> retransformLivingEntitiesViaInst(IInstrumentations instrumentation, Collection<Class<?>> visited) {
        Class<?>[] allLoaded = instrumentation.getAllLoadedClasses();
        List<Class<?>> targets = new ArrayList<>();
        for (Class<?> clazz : allLoaded) {
            if (shouldRetransform(instrumentation, clazz)) {
                targets.add(clazz);
            }
        }
        if (targets.isEmpty()) {
            LogUtil.log("No loaded LivingEntity classes need retransform.");
            return Collections.emptyList();
        }
        if (!instrumentation.isRetransformClassesSupported()) {
            LogUtil.error("RetransformClasses is not supported by current Instrumentation.");
            return targets;
        }
        int transformed = retransformBisected(instrumentation, targets,visited);
        LogUtil.logf("Retransformed %d loaded LivingEntity classes.", transformed);
        if(transformed >= targets.size()){
            return Collections.emptyList();
        }
        Collection<Class<?>> remaining = new HashSet<>(targets);
        for (Class<?> vis : visited) {
            remaining.remove(vis);
        }
        return remaining;
    }
    private int retransformBisected(IJVNTIPointer jvmtiUtil, List<Class<?>> targets) {
        if (targets.isEmpty()) {
            return 0;
        }
        try {
            jvmtiUtil.retransformClasses(targets.toArray(new Class<?>[0]));
            return targets.size();
        } catch (Throwable t) {
            if (targets.size() == 1) {
                Class<?> target = targets.get(0);
                LogUtil.errorf("Skipped LivingEntity class %s during retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("jvmti", target, t);
                return 0;
            }
            LogUtil.errorf("Failed to retransform batch of %d loaded LivingEntity classes, split and retry: %s",
                    targets.size(),
                    t.getMessage());
            LogUtil.printStackTrace(t);
            int middle = targets.size() / 2;
            return retransformBisected(jvmtiUtil, targets.subList(0, middle))
                    + retransformBisected(jvmtiUtil, targets.subList(middle, targets.size()));
        }
    }
    private int retransformBisected(IInstrumentations instrumentation, List<Class<?>> targets,Collection<Class<?>> visited) {
        if (targets.isEmpty()) {
            return 0;
        }
        try {
            instrumentation.retransformClasses(targets.toArray(new Class<?>[0]));
            visited.addAll(targets);
            return targets.size();
        } catch (Throwable t) {
            if (targets.size() == 1) {
                Class<?> target = targets.get(0);
                LogUtil.errorf("Skipped LivingEntity class %s during retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                SporeTransformerDebugDump.dumpFailedTransform("instrumentation", target, t);
                return 0;
            }
            LogUtil.errorf("Failed to retransform batch of %d loaded LivingEntity classes, split and retry: %s",
                    targets.size(),
                    t.getMessage());
            LogUtil.printStackTrace(t);
            int middle = targets.size() / 2;
            return retransformBisected(instrumentation, targets.subList(0, middle), visited)
                    + retransformBisected(instrumentation, targets.subList(middle, targets.size()), visited);
        }
    }

    private boolean shouldRetransform(IInstrumentations instrumentation, Class<?> clazz) {
        if (clazz == null
                || clazz.isHidden()
                || clazz.isArray()
                || clazz.isPrimitive()
                || clazz.isInterface()) {
            return false;
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return false;
        }
        return instrumentation.isModifiableClass(clazz);
    }
    private boolean shouldRetransform(IJVNTIPointer jvmtiUtil, Class<?> clazz) {
        if (clazz == null
                || jvmtiUtil == null
                || clazz.isHidden()
                || clazz.isArray()
                || clazz.isPrimitive()
                || clazz.isInterface()) {
            return false;
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return false;
        }
        return jvmtiUtil.isModifiableClass(clazz);
    }

    private boolean shouldRetransformHiddenCandidate(IInstrumentations instrumentation, Class<?> clazz) {
        if (clazz == null
                || instrumentation == null
                || clazz.isArray()
                || clazz.isPrimitive()
                || clazz.isInterface()) {
            return false;
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return false;
        }
        try {
            return instrumentation.isModifiableClass(clazz);
        } catch (Throwable t) {
            LogUtil.errorf("Hidden LivingEntity class %s is not safely modifiable: %s", clazz.getName(), t.getMessage());
            return false;
        }
    }
    private boolean shouldRetransformHiddenCandidate(IJVNTIPointer jvmtiUtil, Class<?> clazz) {
        if (clazz == null
                || jvmtiUtil == null
                || clazz.isArray()
                || clazz.isPrimitive()
                || clazz.isInterface()) {
            return false;
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return false;
        }
        try {
            return jvmtiUtil.isModifiableClass(clazz);
        } catch (Throwable t) {
            LogUtil.errorf("Hidden LivingEntity class %s is not safely modifiable: %s", clazz.getName(), t.getMessage());
            return false;
        }
    }

    public record KlassAndAccessFlags(long klass,int accessFlags){
    }
}
