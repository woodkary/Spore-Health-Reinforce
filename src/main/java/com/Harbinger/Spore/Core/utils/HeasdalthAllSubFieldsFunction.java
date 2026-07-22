package com.Harbinger.Spore.Core.utils;

import java.lang.reflect.Field;
import java.util.List;

final class HeasdalthAllSubFieldsFunction extends HeasdalthClassValueFunction<List<Field>> {
    HeasdalthAllSubFieldsFunction(IHeasdalthClassValueLoader loader) {
        super(loader);
    }

    @Override
    public List<Field> apply(Class<?> type) {
        return loader.scanAllSubFields(type);
    }
}
