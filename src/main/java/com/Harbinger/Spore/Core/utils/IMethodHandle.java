package com.Harbinger.Spore.Core.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;

public interface IMethodHandle {
    MethodHandles.Lookup getLookup();
    <V> Object fastRemove(Int2ObjectMap<V> map, int key);
    <V> Object fastPut(Int2ObjectMap<V> map, int key, V value);
    void fastClear(Int2ObjectMap<?> map);
    <K, V> Object javaMapRemove(Map<K, V> map, Object key);
    <K, V> Object javaMapPut(Map<K, V> map, K key, V value);
    void javaMapClear(Map<?, ?> map);
    <T> boolean javaCollectionRemove(Collection<T> collection, Object element);

    MethodHandle ensureConstructor(MethodHandle constructor,
                                   Class<?> hiddenClass,
                                   Class<?> originalClass,
                                   Class<?>... ctorTypes);
}
