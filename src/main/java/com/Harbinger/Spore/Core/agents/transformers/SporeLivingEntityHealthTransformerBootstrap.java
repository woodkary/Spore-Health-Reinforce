package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
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
    private volatile boolean loadedLivingEntitiesRetransformed;

    @Override
    public synchronized void tryRetransformHiddenClasses(Class<?>... classes){
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation == null) {
            LogUtil.error("Instrumentation is unavailable, skip LivingEntity health transformer.");
            return;
        }
        if (!instrumentation.isRetransformClassesSupported()) {
            LogUtil.error("RetransformClasses is not supported by current Instrumentation.");
            return;
        }
        if (!installTransformers(instrumentation)) {
            return;
        }
        List<Class<?>> hiddenClasses=new ArrayList<>();
        Map<Class<?>,KlassAndAccessFlags> hiddenAddresses=new HashMap<>();
        try {
            for (Class<?> clazz : classes) {
                KlassAndAccessFlags flags = modifyClassesAccessFlags(clazz);
                if (flags == null) {
                    continue;
                }
                hiddenAddresses.put(clazz,flags);
                if (shouldRetransformHiddenCandidate(instrumentation, clazz)) {
                    hiddenClasses.add(clazz);
                } else {
                    resetToHidden(clazz, flags);
                    hiddenAddresses.remove(clazz);
                }
            }
            if (hiddenClasses.isEmpty()) {
                return;
            }
            retransformBisected(instrumentation, hiddenClasses);
        }finally {
            for (Map.Entry<Class<?>, KlassAndAccessFlags> entry : hiddenAddresses.entrySet()) {
                resetToHidden(entry.getKey(), entry.getValue());
            }
        }
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

    private void resetToHidden(Class<?> clazz,KlassAndAccessFlags klassAndAccessFlags){
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
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation == null) {
            LogUtil.error("Instrumentation is unavailable, skip LivingEntity health transformer.");
            return;
        }
        if (installTransformers(instrumentation) && !loadedLivingEntitiesRetransformed) {
            loadedLivingEntitiesRetransformed = true;
            retransformLoadedLivingEntities(instrumentation);
        }
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

    private void retransformLoadedLivingEntities(IInstrumentations instrumentation) {
        if (!instrumentation.isRetransformClassesSupported()) {
            LogUtil.error("RetransformClasses is not supported by current Instrumentation.");
            return;
        }
        Class<?>[] allLoaded = instrumentation.getAllLoadedClasses();
        List<Class<?>> targets = new ArrayList<>();
        for (Class<?> clazz : allLoaded) {
            if (shouldRetransform(instrumentation, clazz)) {
                targets.add(clazz);
            }
        }
        if (targets.isEmpty()) {
            LogUtil.log("No loaded LivingEntity classes need retransform.");
            return;
        }
        int transformed = retransformBisected(instrumentation, targets);
        LogUtil.logf("Retransformed %d loaded LivingEntity classes.", transformed);
    }

    private int retransformBisected(IInstrumentations instrumentation, List<Class<?>> targets) {
        if (targets.isEmpty()) {
            return 0;
        }
        try {
            instrumentation.retransformClasses(targets.toArray(new Class<?>[0]));
            return targets.size();
        } catch (Throwable t) {
            if (targets.size() == 1) {
                Class<?> target = targets.get(0);
                LogUtil.errorf("Skipped LivingEntity class %s during retransform: %s",
                        target.getName(),
                        t.getMessage());
                LogUtil.printStackTrace(t);
                return 0;
            }
            LogUtil.errorf("Failed to retransform batch of %d loaded LivingEntity classes, split and retry: %s",
                    targets.size(),
                    t.getMessage());
            LogUtil.printStackTrace(t);
            int middle = targets.size() / 2;
            return retransformBisected(instrumentation, targets.subList(0, middle))
                    + retransformBisected(instrumentation, targets.subList(middle, targets.size()));
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

    private boolean shouldRetransformHiddenCandidate(IInstrumentations instrumentation, Class<?> clazz) {
        if (clazz == null
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

    public record KlassAndAccessFlags(long klass,int accessFlags){
    }
}
