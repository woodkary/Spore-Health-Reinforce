package com.Harbinger.Spore.Core.utils;

import java.util.function.Function;

final class BuildWrapperClassFunction implements Function<Class<?>,Class<?>> {
    @Override
    public Class<?> apply(Class<?> aClass) {
        return LivingEntityHealthLifecycleWrapperUtil.INSTANCE.buildWrapperClass(aClass);
    }
}