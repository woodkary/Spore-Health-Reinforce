package com.Harbinger.Spore.Core.utils;

import java.util.Objects;
import java.util.function.Function;

final class LoadingClassValue<T> extends ClassValue<T> {
    private final Function<Class<?>, ? extends T> loader;

    LoadingClassValue(Function<Class<?>, ? extends T> loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    @Override
    protected T computeValue(Class<?> type) {
        return Objects.requireNonNull(loader.apply(type));
    }
}
