package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

final class HeasdalthStaticMapPutFunction
        extends HeasdalthClassValueFunction<Optional<MethodHandle>> {
    HeasdalthStaticMapPutFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public Optional<MethodHandle> apply(Class<?> type) {
        return loader.buildStaticMapPutHandle(type);
    }
}
