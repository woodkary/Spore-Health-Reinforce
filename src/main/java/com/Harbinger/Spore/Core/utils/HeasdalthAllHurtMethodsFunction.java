package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.wrappedMethod.IWrappedMethod;

import java.util.List;

final class HeasdalthAllHurtMethodsFunction extends HeasdalthClassValueFunction<List<IWrappedMethod>> {
    HeasdalthAllHurtMethodsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<IWrappedMethod> apply(Class<?> type) {
        return loader.scanAllHurtMethods(type);
    }
}
