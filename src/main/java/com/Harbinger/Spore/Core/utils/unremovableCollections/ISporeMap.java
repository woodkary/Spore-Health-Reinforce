package com.Harbinger.Spore.Core.utils.unremovableCollections;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ISporeMap<K,V> extends Map<K,V> {
    V actualPut(K key, V value);
    void actualPutAll(@NotNull Map<? extends K, ? extends V> m);
    V actualPutIfAbsent(K key, V value);
    V actualRemove(Object key);
    boolean actualRemove(Object key, Object value);
    V actualReplace(K key, V value);
    boolean actualReplace(K key, V oldValue, V newValue);
    void actualReplaceAll(@NotNull BiFunction<? super K, ? super V, ? extends V> function);
    V actualComputeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction);
    V actualComputeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    V actualCompute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction);
    V actualMerge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction);
    void actualClear();
}
