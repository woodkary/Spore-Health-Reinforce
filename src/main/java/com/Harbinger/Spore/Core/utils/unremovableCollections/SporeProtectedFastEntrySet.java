package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class SporeProtectedFastEntrySet<E> implements ISporeFastEntrySet<E> {
    private static final Class<? extends ISporeFastEntrySet<?>> entrySetClass =
            (Class<? extends ISporeFastEntrySet<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedFastEntrySet.class,
                    ObjectSet.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            entrySetClass,
            SporeProtectedFastEntrySet.class,
            ObjectSet.class
    );

    static <E> ISporeFastEntrySet<E> newInstance(ObjectSet<Int2ObjectMap.Entry<E>> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entrySetClass,
                SporeProtectedFastEntrySet.class,
                ObjectSet.class
        );
        if (constructor != null) {
            try {
                return (ISporeFastEntrySet<E>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected FastEntrySet, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedFastEntrySet<>(owner);
    }

    private final ObjectSet<Int2ObjectMap.Entry<E>> owner;

    private SporeProtectedFastEntrySet(ObjectSet<Int2ObjectMap.Entry<E>> owner) {
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
    public boolean contains(Object object) {
        return owner.contains(object);
    }

    @Override
    public ISporeObjectIterator<Int2ObjectMap.Entry<E>> iterator() {
        return SporeProtectedEntryIterator.newInstance(owner.iterator());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ISporeObjectIterator<Int2ObjectMap.Entry<E>> fastIterator() {
        ObjectIterator<Int2ObjectMap.Entry<E>> iterator;
        if (owner instanceof Int2ObjectMap.FastEntrySet fastEntrySet) {
            iterator = ((Int2ObjectMap.FastEntrySet<E>) fastEntrySet).fastIterator();
        } else {
            iterator = owner.iterator();
        }
        return SporeProtectedEntryIterator.newInstance(iterator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        Object[] result = owner.toArray();
        for (int index = 0; index < result.length; index++) {
            if (result[index] instanceof Int2ObjectMap.Entry<?> entry) {
                result[index] = SporeProtectedInt2ObjectEntry.newInstance((Int2ObjectMap.Entry<E>) entry);
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        Object[] values = toArray();
        if (array.length < values.length) {
            return (T[]) Arrays.copyOf(values, values.length, array.getClass());
        }
        System.arraycopy(values, 0, array, 0, values.length);
        if (array.length > values.length) {
            array[values.length] = null;
        }
        return array;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return owner.containsAll(collection);
    }

    @Override
    public void forEach(Consumer<? super Int2ObjectMap.Entry<E>> action) {
        Objects.requireNonNull(action);
        ISporeObjectIterator<Int2ObjectMap.Entry<E>> iterator = iterator();
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    @Override
    public void fastForEach(Consumer<? super Int2ObjectMap.Entry<E>> action) {
        Objects.requireNonNull(action);
        ISporeObjectIterator<Int2ObjectMap.Entry<E>> iterator = fastIterator();
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actualFastForEach(@NotNull Consumer<? super Int2ObjectMap.Entry<E>> action) {
        if (owner instanceof Int2ObjectMap.FastEntrySet fastEntrySet) {
            ((Int2ObjectMap.FastEntrySet<E>) fastEntrySet).fastForEach(action);
        } else {
            owner.forEach(action);
        }
    }

    @Override
    public boolean add(Int2ObjectMap.Entry<E> entry) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Int2ObjectMap.Entry<E>> collection) {
        return false;
    }

    @Override
    public boolean remove(Object object) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super Int2ObjectMap.Entry<E>> filter) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean actualAdd(Int2ObjectMap.Entry<E> entry) {
        return owner.add(entry);
    }

    @Override
    public boolean actualAddAll(@NotNull Collection<? extends Int2ObjectMap.Entry<E>> collection) {
        return owner.addAll(collection);
    }

    @Override
    public boolean actualRemove(Object object) {
        return owner.remove(object);
    }

    @Override
    public boolean actualRemoveAll(@NotNull Collection<?> collection) {
        return owner.removeAll(collection);
    }

    @Override
    public boolean actualRetainAll(@NotNull Collection<?> collection) {
        return owner.retainAll(collection);
    }

    @Override
    public boolean actualRemoveIf(@NotNull Predicate<? super Int2ObjectMap.Entry<E>> filter) {
        return owner.removeIf(filter);
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
