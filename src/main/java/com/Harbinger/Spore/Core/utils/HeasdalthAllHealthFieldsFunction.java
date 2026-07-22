package com.Harbinger.Spore.Core.utils;

import java.lang.reflect.Field;
import java.util.List;

final class HeasdalthAllHealthFieldsFunction extends HeasdalthClassValueFunction<List<Field>> {
    HeasdalthAllHealthFieldsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<Field> apply(Class<?> type) {
        return loader.scanAllHealthFields(type);
    }
}
