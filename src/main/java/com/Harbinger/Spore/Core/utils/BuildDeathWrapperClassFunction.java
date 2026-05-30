package com.Harbinger.Spore.Core.utils;

import java.util.function.Function;

final class BuildDeathWrapperClassFunction implements Function<Class<?>,Class<?>> {
    @Override
    public Class<?> apply(Class<?> aClass) {
        return LivingEntityHealthLifecycleWrapperUtil.INSTANCE.buildDeathWrapperClass(aClass);
    }
}
