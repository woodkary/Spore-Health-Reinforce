package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import net.minecraft.world.entity.LivingEntity;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

public final class SporeLivingEntityHealthTransformerBootstrap {
    private static volatile boolean installed;

    private SporeLivingEntityHealthTransformerBootstrap() {
    }

    public static synchronized void installAndRetransform() {
        if (installed) {
            return;
        }
        IInstrumentations instrumentation = InstrumentationUtil.getInstance();
        if (instrumentation == null) {
            LogUtil.error("Instrumentation is unavailable, skip LivingEntity health transformer.");
            return;
        }
        ClassFileTransformer transformer = SporeLivingEntityHealthTransformer.newInstance();
        instrumentation.addTransformer(transformer);
        installed = true;
        retransformLoadedLivingEntities(instrumentation);
    }

    private static void retransformLoadedLivingEntities(IInstrumentations instrumentation) {
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
        try {
            instrumentation.retransformClasses(targets.toArray(new Class<?>[0]));
            LogUtil.logf("Retransformed %d loaded LivingEntity classes.", targets.size());
        } catch (Throwable t) {
            LogUtil.errorf("Failed to retransform loaded LivingEntity classes as one batch: %s", t.getMessage());
            retransformIndividually(instrumentation, targets);
        }
    }

    private static void retransformIndividually(IInstrumentations instrumentation, List<Class<?>> targets) {
        int transformed = 0;
        for (Class<?> target : targets) {
            try {
                instrumentation.retransformClasses(new Class<?>[]{target});
                transformed++;
            } catch (Throwable t) {
                LogUtil.errorf("Skipped LivingEntity class %s during retransform: %s",
                        target.getName(),
                        t.getMessage());
            }
        }
        LogUtil.logf("Retransformed %d loaded LivingEntity classes individually.", transformed);
    }

    private static boolean shouldRetransform(IInstrumentations instrumentation, Class<?> clazz) {
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
