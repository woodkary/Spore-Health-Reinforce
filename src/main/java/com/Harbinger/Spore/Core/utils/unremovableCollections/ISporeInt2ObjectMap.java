package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.IntFunction;

public interface ISporeInt2ObjectMap<V> extends Int2ObjectMap<V>, ISporeMap<Integer, V> {
    @Override
    ISporeFastEntrySet<V> int2ObjectEntrySet();

    @Override
    ISporeIntSet keySet();

    @Override
    ISporeObjectCollection<V> values();

    void actualDefaultReturnValue(V value);

    V actualPut(int key, V value);

    V actualRemove(int key);

    V actualPutIfAbsent(int key, V value);

    boolean actualRemove(int key, Object value);

    boolean actualReplace(int key, V oldValue, V newValue);

    V actualReplace(int key, V value);

    V actualComputeIfAbsent(int key, @NotNull IntFunction<? extends V> mappingFunction);

    V actualComputeIfAbsent(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction);

    V actualComputeIfAbsentPartial(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction);

    V actualComputeIfPresent(int key, @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction);

    V actualCompute(int key, @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction);

    V actualMerge(int key, V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction);
}
