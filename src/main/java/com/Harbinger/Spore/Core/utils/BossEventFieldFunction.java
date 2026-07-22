package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.BossEvent;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

final class BossEventFieldFunction implements Function<Class<?>, Optional<Field>> {
    @Override
    public Optional<Field> apply(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (BossEvent.class.isAssignableFrom(field.getType())) {
                    return Optional.of(field);
                }
            }
        }
        return Optional.empty();
    }
}
