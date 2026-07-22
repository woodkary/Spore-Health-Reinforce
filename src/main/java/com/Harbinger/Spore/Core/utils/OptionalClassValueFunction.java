package com.Harbinger.Spore.Core.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class OptionalClassValueFunction implements Function<Class<?>, Optional<Class<?>>> {
    private final IOptionalClassValueLoader loader;

    OptionalClassValueFunction(IOptionalClassValueLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    @Override
    public Optional<Class<?>> apply(Class<?> type) {
        return loader.loadClassValue(type);
    }
}
