package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.wrappedMethod.IWrappedMethod;

import java.util.List;

final class HeasdalthDeathMethodsFunction extends HeasdalthClassValueFunction<List<IWrappedMethod>> {
    HeasdalthDeathMethodsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<IWrappedMethod> apply(Class<?> type) {
        return loader.scanDeathMethods(type);
    }
}
