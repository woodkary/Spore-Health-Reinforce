package com.Harbinger.Spore.Core.utils;

import java.util.Objects;
import java.util.function.Function;

abstract class HeasdalthClassValueFunction<T> implements Function<Class<?>, T> {
    protected final IHeasdalthClassValueLoader loader;

    HeasdalthClassValueFunction(IHeasdalthClassValueLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }
}
