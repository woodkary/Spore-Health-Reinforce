package com.Harbinger.Spore.Core.utils;

import java.lang.reflect.Field;
import java.util.List;

final class HeasdalthDeathFieldsFunction extends HeasdalthClassValueFunction<List<Field>> {
    HeasdalthDeathFieldsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<Field> apply(Class<?> type) {
        return loader.scanDeathFields(type);
    }
}
