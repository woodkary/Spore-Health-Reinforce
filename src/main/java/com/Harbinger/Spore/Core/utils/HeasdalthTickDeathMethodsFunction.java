package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.wrappedMethod.IWrappedMethod;

import java.util.List;

final class HeasdalthTickDeathMethodsFunction extends HeasdalthClassValueFunction<List<IWrappedMethod>> {
    HeasdalthTickDeathMethodsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<IWrappedMethod> apply(Class<?> type) {
        return loader.scanTickDeathMethods(type);
    }
}
