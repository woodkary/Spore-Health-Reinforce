package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class SporeInt2ObjectMapProxy<V> implements ISporeInt2ObjectMap<V> {
    private static final Class<? extends ISporeInt2ObjectMap<?>> mapClass =
            (Class<? extends ISporeInt2ObjectMap<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeInt2ObjectMapProxy.class,
                    Int2ObjectMap.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeInt2ObjectMapProxy.class,
            Int2ObjectMap.class
    );

    public static <V> ISporeInt2ObjectMap<V> newInstance(Int2ObjectMap<V> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                mapClass,
                SporeInt2ObjectMapProxy.class,
                Int2ObjectMap.class
        );
        if (constructor != null) {
            try {
                return (ISporeInt2ObjectMap<V>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new Int2ObjectMap proxy, %s", throwable.getMessage());
            }
        }
        return new SporeInt2ObjectMapProxy<>(owner);
    }

    private final Int2ObjectMap<V> owner;
    private ISporeIntSet keySet;
    private ISporeObjectCollection<V> values;
    private ISporeFastEntrySet<V> entries;

    public SporeInt2ObjectMapProxy(Int2ObjectMap<V> owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    @Override
    public int size() {
        return owner.size();
    }

    @Override
    public boolean isEmpty() {
        return owner.isEmpty();
    }

    @Override
    public boolean containsKey(int key) {
        return owner.containsKey(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return owner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return owner.containsValue(value);
    }

    @Override
    public V get(int key) {
        return owner.get(key);
    }

    @Override
    public V get(Object key) {
        return owner.get(key);
    }

    @Override
    public V getOrDefault(int key, V defaultValue) {
        return owner.getOrDefault(key, defaultValue);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return owner.getOrDefault(key, defaultValue);
    }

    @Override
    public V defaultReturnValue() {
        return owner.defaultReturnValue();
    }

    @Override
    public void defaultReturnValue(V value) {
    }

    @Override
    public void forEach(BiConsumer<? super Integer, ? super V> consumer) {
        owner.forEach(consumer);
    }

    @Override
    public V put(int key, V value) {
        return value;
    }

    @Override
    public V put(Integer key, V value) {
        return value;
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends V> map) {
    }

    @Override
    public V remove(int key) {
        return owner.get(key);
    }

    @Override
    public V remove(Object key) {
        return owner.get(key);
    }

    @Override
    public void clear() {
    }

    @Override
    public V putIfAbsent(int key, V value) {
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        return current != defaultValue || owner.containsKey(key) ? current : value;
    }

    @Override
    public V putIfAbsent(Integer key, V value) {
        V current = owner.get(key);
        return current != null || owner.containsKey(key) ? current : value;
    }

    @Override
    public boolean remove(int key, Object value) {
        return false;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(int key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public boolean replace(Integer key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(int key, V value) {
        return owner.containsKey(key) ? value : owner.defaultReturnValue();
    }

    @Override
    public V replace(Integer key, V value) {
        return owner.containsKey(key) ? value : null;
    }

    @Override
    public void replaceAll(@NotNull BiFunction<? super Integer, ? super V, ? extends V> function) {
    }

    @Override
    public V computeIfAbsent(int key, @NotNull IntFunction<? extends V> mappingFunction) {
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        return current != defaultValue || owner.containsKey(key)
                ? current
                : mappingFunction.apply(key);
    }

    @Override
    public V computeIfAbsent(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction) {
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        if (current != defaultValue || owner.containsKey(key)) {
            return current;
        }
        return mappingFunction.containsKey(key) ? mappingFunction.get(key) : defaultValue;
    }

    @Deprecated
    @Override
    public V computeIfAbsentPartial(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(int key,
                              @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        if (current == defaultValue && !owner.containsKey(key)) {
            return defaultValue;
        }
        V result = remappingFunction.apply(key, current);
        return result == null ? defaultValue : result;
    }

    @Override
    public V compute(int key, @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        boolean present = current != defaultValue || owner.containsKey(key);
        V result = remappingFunction.apply(key, present ? current : null);
        return result == null ? defaultValue : result;
    }

    @Override
    public V merge(int key, V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        V current = owner.get(key);
        V defaultValue = owner.defaultReturnValue();
        V result = current != defaultValue || owner.containsKey(key)
                ? remappingFunction.apply(current, value)
                : value;
        return result == null ? defaultValue : result;
    }

    @Override
    public V computeIfAbsent(Integer key, @NotNull Function<? super Integer, ? extends V> mappingFunction) {
        V current = owner.get(key);
        return current != null ? current : mappingFunction.apply(key);
    }

    @Override
    public V computeIfPresent(Integer key,
                              @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        V current = owner.get(key);
        if (current == null) {
            return null;
        }
        return remappingFunction.apply(key, current);
    }

    @Override
    public V compute(Integer key, @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        return remappingFunction.apply(key, owner.get(key));
    }

    @Override
    public V merge(Integer key, V value,
                   @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        V current = owner.get(key);
        return current == null ? value : remappingFunction.apply(current, value);
    }

    @Override
    public ISporeIntSet keySet() {
        ISporeIntSet result = keySet;
        if (result == null) {
            result = SporeProtectedIntSet.newInstance(owner.keySet());
            keySet = result;
        }
        return result;
    }

    @Override
    public ISporeObjectCollection<V> values() {
        ISporeObjectCollection<V> result = values;
        if (result == null) {
            result = SporeProtectedObjectCollection.newInstance(owner.values());
            values = result;
        }
        return result;
    }

    @Override
    public ISporeFastEntrySet<V> int2ObjectEntrySet() {
        ISporeFastEntrySet<V> result = entries;
        if (result == null) {
            result = SporeProtectedFastEntrySet.newInstance(owner.int2ObjectEntrySet());
            entries = result;
        }
        return result;
    }

    @Deprecated
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ObjectSet<Map.Entry<Integer, V>> entrySet() {
        return (ObjectSet) int2ObjectEntrySet();
    }

    @Override
    public void actualDefaultReturnValue(V value) {
        owner.defaultReturnValue(value);
    }

    @Override
    public V actualPut(int key, V value) {
        return owner.put(key, value);
    }

    @Override
    public V actualPut(Integer key, V value) {
        return owner.put(key, value);
    }

    @Override
    public void actualPutAll(@NotNull Map<? extends Integer, ? extends V> map) {
        owner.putAll(map);
    }

    @Override
    public V actualRemove(int key) {
        return owner.remove(key);
    }

    @Override
    public V actualRemove(Object key) {
        return owner.remove(key);
    }

    @Override
    public V actualPutIfAbsent(int key, V value) {
        return owner.putIfAbsent(key, value);
    }

    @Override
    public V actualPutIfAbsent(Integer key, V value) {
        return owner.putIfAbsent(key, value);
    }

    @Override
    public boolean actualRemove(int key, Object value) {
        return owner.remove(key, value);
    }

    @Override
    public boolean actualRemove(Object key, Object value) {
        return owner.remove(key, value);
    }

    @Override
    public boolean actualReplace(int key, V oldValue, V newValue) {
        return owner.replace(key, oldValue, newValue);
    }

    @Override
    public boolean actualReplace(Integer key, V oldValue, V newValue) {
        return owner.replace(key, oldValue, newValue);
    }

    @Override
    public V actualReplace(int key, V value) {
        return owner.replace(key, value);
    }

    @Override
    public V actualReplace(Integer key, V value) {
        return owner.replace(key, value);
    }

    @Override
    public void actualReplaceAll(@NotNull BiFunction<? super Integer, ? super V, ? extends V> function) {
        owner.replaceAll(function);
    }

    @Override
    public V actualComputeIfAbsent(int key, @NotNull IntFunction<? extends V> mappingFunction) {
        return owner.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V actualComputeIfAbsent(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction) {
        return owner.computeIfAbsent(key, mappingFunction);
    }

    @Deprecated
    @Override
    public V actualComputeIfAbsentPartial(int key, @NotNull Int2ObjectFunction<? extends V> mappingFunction) {
        return owner.computeIfAbsentPartial(key, mappingFunction);
    }

    @Override
    public V actualComputeIfPresent(int key,
                                    @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        return owner.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V actualCompute(int key,
                           @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        return owner.compute(key, remappingFunction);
    }

    @Override
    public V actualMerge(int key, V value,
                         @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return owner.merge(key, value, remappingFunction);
    }

    @Override
    public V actualComputeIfAbsent(Integer key, @NotNull Function<? super Integer, ? extends V> mappingFunction) {
        return owner.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V actualComputeIfPresent(Integer key,
                                    @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        return owner.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V actualCompute(Integer key,
                           @NotNull BiFunction<? super Integer, ? super V, ? extends V> remappingFunction) {
        return owner.compute(key, remappingFunction);
    }

    @Override
    public V actualMerge(Integer key, @NotNull V value,
                         @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return owner.merge(key, value, remappingFunction);
    }

    @Override
    public void actualClear() {
        owner.clear();
    }

    @Override
    public boolean equals(Object object) {
        return object == this || owner.equals(object);
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    @Override
    public String toString() {
        return owner.toString();
    }
}
