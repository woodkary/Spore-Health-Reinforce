package com.Harbinger.Spore.Core.utils;

import java.lang.reflect.Field;
import java.util.List;

final class HeasdalthTickDeathFieldsFunction extends HeasdalthClassValueFunction<List<Field>> {
    HeasdalthTickDeathFieldsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<Field> apply(Class<?> type) {
        return loader.scanTickDeathFields(type);
    }
}
