package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

final class HeasdalthStaticMapGetFunction
        extends HeasdalthClassValueFunction<Optional<MethodHandle>> {
    HeasdalthStaticMapGetFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public Optional<MethodHandle> apply(Class<?> type) {
        return loader.buildStaticMapGetHandle(type);
    }
}
