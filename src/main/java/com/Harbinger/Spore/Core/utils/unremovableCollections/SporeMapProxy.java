package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SporeMapProxy<K, V> implements ISporeMap<K, V> {
    public static final Class<? extends ISporeMap<?, ?>> mapClass = (Class<? extends ISporeMap<?, ?>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeMapProxy.class,
            Map.class
    );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeMapProxy.class,
            Map.class
    );

    public static <K, V> ISporeMap<K, V> newInstance(Map<K, V> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                mapClass,
                SporeMapProxy.class,
                Map.class
        );
        if (constructor != null) {
            try {
                return (ISporeMap<K, V>) constructor.invoke(owner);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new ProxyMap instance, %s", e.getMessage());
            }
        }
        return new SporeMapProxy<>(owner);
    }

    private final Map<K, V> owner;
    private Set<K> keySet;
    private Collection<V> values;
    private Set<Entry<K, V>> entrySet;

    public SporeMapProxy(Map<K, V> owner) {
        this.owner = owner;
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
    public boolean containsKey(Object key) {
        return owner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return owner.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return owner.get(key);
    }

    @Override
    public @Nullable V put(K key, V value) {
        return value;
    }

    @Override
    public V remove(Object key) {
        return owner.get(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
    }

    @Override
    public void clear() {
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return value;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return value;
    }

    @Override
    public void replaceAll(@NotNull BiFunction<? super K, ? super V, ? extends V> function) {
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        V value = owner.get(key);
        return value != null || owner.containsKey(key) ? value : mappingFunction.apply(key);
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V value = owner.get(key);
        if (value == null && !owner.containsKey(key)) {
            return null;
        }
        return remappingFunction.apply(key, value);
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return remappingFunction.apply(key, owner.get(key));
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V oldValue = owner.get(key);
        return oldValue == null && !owner.containsKey(key) ? value : remappingFunction.apply(oldValue, value);
    }

    @Override
    public @NotNull Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = SporeSetProxy.newInstance(owner.keySet());
            keySet = ks;
        }
        return ks;
    }

    @Override
    public @NotNull Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = ProtectedCollectionView.newInstance(owner.values());
            values = vs;
        }
        return vs;
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        if (es == null) {
            es = ProtectedEntrySet.newInstance(owner.entrySet());
            entrySet = es;
        }
        return es;
    }

    @Override
    public V actualPut(K key, V value) {
        return owner.put(key, value);
    }

    @Override
    public void actualPutAll(@NotNull Map<? extends K, ? extends V> m) {
        owner.putAll(m);
    }

    @Override
    public V actualPutIfAbsent(K key, V value) {
        return owner.putIfAbsent(key, value);
    }

    @Override
    public V actualRemove(Object key) {
        return owner.remove(key);
    }

    @Override
    public boolean actualRemove(Object key, Object value) {
        return owner.remove(key, value);
    }

    @Override
    public V actualReplace(K key, V value) {
        return owner.replace(key, value);
    }

    @Override
    public boolean actualReplace(K key, V oldValue, V newValue) {
        return owner.replace(key, oldValue, newValue);
    }

    @Override
    public void actualReplaceAll(@NotNull BiFunction<? super K, ? super V, ? extends V> function) {
        owner.replaceAll(function);
    }

    @Override
    public V actualComputeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return owner.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V actualComputeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return owner.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V actualCompute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return owner.compute(key, remappingFunction);
    }

    @Override
    public V actualMerge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return owner.merge(key, value, remappingFunction);
    }

    @Override
    public void actualClear() {
        owner.clear();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || owner.equals(o);
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    @Override
    public String toString() {
        return owner.toString();
    }

    private static final class ProtectedCollectionView<E> implements ISporeCollection<E> {
        private static final Class<? extends ISporeCollection<?>> collectionClass = (Class<? extends ISporeCollection<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedCollectionView.class,
                Collection.class
        );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                collectionClass,
                ProtectedCollectionView.class,
                Collection.class
        );

        public static <E> ISporeCollection<E> newInstance(Collection<E> owner) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    collectionClass,
                    ProtectedCollectionView.class,
                    Collection.class
            );
            if (constructor != null) {
                try {
                    return (ISporeCollection<E>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new ProtectedCollectionView instance, %s", e.getMessage());
                }
            }
            return new ProtectedCollectionView<>(owner);
        }

        private final Collection<E> owner;

        private ProtectedCollectionView(Collection<E> owner) {
            this.owner = owner;
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
        public boolean contains(Object o) {
            return owner.contains(o);
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return ProtectedIterator.newInstance(owner.iterator());
        }

        @Override
        public @NotNull Object[] toArray() {
            return owner.toArray();
        }

        @Override
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            return owner.toArray(a);
        }

        @Override
        public boolean add(E e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return owner.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super E> filter) {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean actualAdd(E e) {
            return owner.add(e);
        }

        @Override
        public boolean actualAddAll(@NotNull Collection<? extends E> c) {
            return owner.addAll(c);
        }

        @Override
        public boolean actualRemove(Object o) {
            return owner.remove(o);
        }

        @Override
        public boolean actualRemoveAll(@NotNull Collection<?> c) {
            return owner.removeAll(c);
        }

        @Override
        public boolean actualRetainAll(@NotNull Collection<?> c) {
            return owner.retainAll(c);
        }

        @Override
        public boolean actualRemoveIf(@NotNull Predicate<? super E> filter) {
            return owner.removeIf(filter);
        }

        @Override
        public void actualClear() {
            owner.clear();
        }

        @Override
        public boolean equals(Object o) {
            return o == this || owner.equals(o);
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

    private static final class ProtectedEntrySet<K, V> implements ISporeSet<Entry<K, V>> {
        private static final Class<? extends ISporeSet<?>> entrySetClass = (Class<? extends ISporeSet<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedEntrySet.class,
                Set.class
        );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                entrySetClass,
                ProtectedEntrySet.class,
                Set.class
        );

        public static <K, V> ISporeSet<Entry<K, V>> newInstance(Set<Entry<K, V>> owner) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entrySetClass,
                    ProtectedEntrySet.class,
                    Set.class
            );
            if (constructor != null) {
                try {
                    return (ISporeSet<Entry<K, V>>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new ProtectedEntrySet instance, %s", e.getMessage());
                }
            }
            return new ProtectedEntrySet<>(owner);
        }

        private final Set<Entry<K, V>> owner;

        private ProtectedEntrySet(Set<Entry<K, V>> owner) {
            this.owner = owner;
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
        public boolean contains(Object o) {
            return owner.contains(o);
        }

        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return ProtectedEntryIterator.newInstance(owner.iterator());
        }

        @Override
        public @NotNull Object[] toArray() {
            Object[] raw = owner.toArray();
            for (int i = 0; i < raw.length; i++) {
                if (raw[i] instanceof Entry<?, ?> entry) {
                    raw[i] = ProtectedEntry.newInstance((Entry<K, V>) entry);
                }
            }
            return raw;
        }

        @Override
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            Object[] raw = toArray();
            if (a.length < raw.length) {
                return (T[]) java.util.Arrays.copyOf(raw, raw.length, a.getClass());
            }
            System.arraycopy(raw, 0, a, 0, raw.length);
            if (a.length > raw.length) {
                a[raw.length] = null;
            }
            return a;
        }

        @Override
        public boolean add(Entry<K, V> kvEntry) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return owner.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Entry<K, V>> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> filter) {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean actualAdd(Entry<K, V> kvEntry) {
            return owner.add(kvEntry);
        }

        @Override
        public boolean actualAddAll(@NotNull Collection<? extends Entry<K, V>> c) {
            return owner.addAll(c);
        }

        @Override
        public boolean actualRemove(Object o) {
            return owner.remove(o);
        }

        @Override
        public boolean actualRemoveAll(@NotNull Collection<?> c) {
            return owner.removeAll(c);
        }

        @Override
        public boolean actualRetainAll(@NotNull Collection<?> c) {
            return owner.retainAll(c);
        }

        @Override
        public boolean actualRemoveIf(@NotNull Predicate<? super Entry<K, V>> filter) {
            return owner.removeIf(filter);
        }

        @Override
        public void actualClear() {
            owner.clear();
        }

        @Override
        public boolean equals(Object o) {
            return o == this || owner.equals(o);
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

    private static final class ProtectedEntryIterator<K, V> implements ISporeIterator<Entry<K, V>> {
        private static final Class<? extends ISporeIterator<?>> iteratorClass = (Class<? extends ISporeIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedEntryIterator.class,
                Iterator.class
        );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedEntryIterator.class,
                Iterator.class
        );

        public static <K, V> ISporeIterator<Entry<K, V>> newInstance(Iterator<Entry<K, V>> owner) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedEntryIterator.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (ISporeIterator<Entry<K, V>>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new ProtectedEntryIterator instance, %s", e.getMessage());
                }
            }
            return new ProtectedEntryIterator<>(owner);
        }

        private final Iterator<Entry<K, V>> owner;

        private ProtectedEntryIterator(Iterator<Entry<K, V>> owner) {
            this.owner = owner;
        }

        @Override
        public boolean hasNext() {
            return owner.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return ProtectedEntry.newInstance(owner.next());
        }

        @Override
        public void remove() {
        }

        @Override
        public void actualRemove() {
            owner.remove();
        }
    }

    private static final class ProtectedEntry<K, V> implements ISporeEntry<K, V> {
        private static final Class<? extends ISporeEntry<?, ?>> entryClass = (Class<? extends ISporeEntry<?, ?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedEntry.class,
                Entry.class
        );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                entryClass,
                ProtectedEntry.class,
                Entry.class
        );

        public static <K, V> ISporeEntry<K, V> newInstance(Entry<K, V> owner) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entryClass,
                    ProtectedEntry.class,
                    Entry.class
            );
            if (constructor != null) {
                try {
                    return (ISporeEntry<K, V>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new ProtectedEntry instance, %s", e.getMessage());
                }
            }
            return new ProtectedEntry<>(owner);
        }

        private final Entry<K, V> owner;

        private ProtectedEntry(Entry<K, V> owner) {
            this.owner = owner;
        }

        @Override
        public K getKey() {
            return owner.getKey();
        }

        @Override
        public V getValue() {
            return owner.getValue();
        }

        @Override
        public V setValue(V value) {
            return value;
        }

        @Override
        public V actualSetValue(V value) {
            return owner.setValue(value);
        }

        @Override
        public boolean equals(Object o) {
            return o == this || owner.equals(o);
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

    private static final class ProtectedIterator<E> implements ISporeIterator<E> {
        private static final Class<? extends ISporeIterator<?>> iteratorClass = (Class<? extends ISporeIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedIterator.class,
                Iterator.class
        );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedIterator.class,
                Iterator.class
        );

        public static <E> ISporeIterator<E> newInstance(Iterator<E> owner) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedIterator.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (ISporeIterator<E>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new ProtectedMapIterator instance, %s", e.getMessage());
                }
            }
            return new ProtectedIterator<>(owner);
        }

        private final Iterator<E> owner;

        private ProtectedIterator(Iterator<E> owner) {
            this.owner = owner;
        }

        @Override
        public boolean hasNext() {
            return owner.hasNext();
        }

        @Override
        public E next() {
            return owner.next();
        }

        @Override
        public void remove() {
        }

        @Override
        public void actualRemove() {
            owner.remove();
        }

        @Override
        public void forEachRemaining(java.util.function.Consumer<? super E> action) {
            owner.forEachRemaining(action);
        }
    }
}
