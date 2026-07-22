package com.Harbinger.Spore.Core.utils;

import net.minecraft.network.syncher.EntityDataAccessor;

import java.util.Map;

final class HeasdalthAccessorNamesFunction
        extends HeasdalthClassValueFunction<Map<EntityDataAccessor<?>, String>> {
    HeasdalthAccessorNamesFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public Map<EntityDataAccessor<?>, String> apply(Class<?> type) {
        return loader.buildAccessorNameMap(type);
    }
}
