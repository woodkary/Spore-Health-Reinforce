package com.Harbinger.Spore.Core.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class OptionalClassFunction implements Function<Class<?>, Optional<Class<?>>> {
    private final Function<Class<?>, Class<?>> loader;

    OptionalClassFunction(Function<Class<?>, Class<?>> loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    @Override
    public Optional<Class<?>> apply(Class<?> type) {
        return Optional.ofNullable(loader.apply(type));
    }
}
