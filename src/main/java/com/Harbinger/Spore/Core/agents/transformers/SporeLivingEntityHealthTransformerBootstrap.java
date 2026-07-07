package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import net.minecraft.world.entity.LivingEntity;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

public final class SporeLivingEntityHealthTransformerBootstrap implements ICommonBootStrap  {
    public static final ICommonBootStrap INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            ICommonBootStrap.class,
            SporeLivingEntityHealthTransformerBootstrap.class
    );
    private volatile boolean installed;
    public synchronized void installAndRetransform() {
        if (installed) {
            return;
        }
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation == null) {
            LogUtil.error("Instrumentation is unavailable, skip LivingEntity health transformer.");
            return;
        }
        ClassFileTransformer healthTransformer = SporeLivingEntityHealthTransformer.newInstance();
        ClassFileTransformer effectApplicationTransformer = SporeLivingEntityEffectApplicationTransformer.newInstance();
        instrumentation.addTransformer(healthTransformer);
        instrumentation.addTransformer(effectApplicationTransformer);
        installed = true;
        retransformLoadedLivingEntities(instrumentation);
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
}
