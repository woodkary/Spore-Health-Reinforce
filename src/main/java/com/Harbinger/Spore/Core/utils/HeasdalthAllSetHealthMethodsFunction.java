package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.utils.wrappedMethod.IWrappedMethod;

import java.util.List;

final class HeasdalthAllSetHealthMethodsFunction extends HeasdalthClassValueFunction<List<IWrappedMethod>> {
    HeasdalthAllSetHealthMethodsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<IWrappedMethod> apply(Class<?> type) {
        return loader.scanAllSetHealthMethods(type);
    }
}
