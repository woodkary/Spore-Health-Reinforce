package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.function.Predicate;

final class SporeProtectedIntSet implements ISporeIntSet {
    private static final Class<? extends ISporeIntSet> setClass =
            (Class<? extends ISporeIntSet>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedIntSet.class,
                    IntSet.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            setClass,
            SporeProtectedIntSet.class,
            IntSet.class
    );

    static ISporeIntSet newInstance(IntSet owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                setClass,
                SporeProtectedIntSet.class,
                IntSet.class
        );
        if (constructor != null) {
            try {
                return (ISporeIntSet) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected IntSet, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedIntSet(owner);
    }

    private final IntSet owner;

    private SporeProtectedIntSet(IntSet owner) {
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
    public boolean contains(int key) {
        return owner.contains(key);
    }

    @Override
    public boolean contains(Object key) {
        return owner.contains(key);
    }

    @Override
    public ISporeIntIterator iterator() {
        return SporeProtectedIntIterator.newInstance(owner.iterator());
    }

    @Override
    public Object[] toArray() {
        return owner.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return owner.toArray(array);
    }

    @Override
    public int[] toIntArray() {
        return owner.toIntArray();
    }

    @Override
    public int[] toArray(int[] array) {
        return owner.toArray(array);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return owner.containsAll(collection);
    }

    @Override
    public boolean containsAll(@NotNull IntCollection collection) {
        return owner.containsAll(collection);
    }

    @Override
    public boolean add(int key) {
        return false;
    }

    @Override
    public boolean add(Integer key) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Integer> collection) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull IntCollection collection) {
        return false;
    }

    @Override
    public boolean remove(int key) {
        return false;
    }

    @Override
    public boolean rem(int key) {
        return false;
    }

    @Override
    public boolean remove(Object key) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull IntCollection collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull IntCollection collection) {
        return false;
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super Integer> filter) {
        return false;
    }

    @Override
    public boolean removeIf(@NotNull java.util.function.IntPredicate filter) {
        return false;
    }

    @Override
    public boolean removeIf(@NotNull it.unimi.dsi.fastutil.ints.IntPredicate filter) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean actualAdd(Integer key) {
        return owner.add(key);
    }

    @Override
    public boolean actualAdd(int key) {
        return owner.add(key);
    }

    @Override
    public boolean actualAddAll(@NotNull Collection<? extends Integer> collection) {
        return owner.addAll(collection);
    }

    @Override
    public boolean actualAddAll(@NotNull IntCollection collection) {
        return owner.addAll(collection);
    }

    @Override
    public boolean actualRemove(Object key) {
        return owner.remove(key);
    }

    @Override
    public boolean actualRemove(int key) {
        return owner.remove(key);
    }

    @Override
    public boolean actualRem(int key) {
        return owner.rem(key);
    }

    @Override
    public boolean actualRemoveAll(@NotNull Collection<?> collection) {
        return owner.removeAll(collection);
    }

    @Override
    public boolean actualRemoveAll(@NotNull IntCollection collection) {
        return owner.removeAll(collection);
    }

    @Override
    public boolean actualRetainAll(@NotNull Collection<?> collection) {
        return owner.retainAll(collection);
    }

    @Override
    public boolean actualRetainAll(@NotNull IntCollection collection) {
        return owner.retainAll(collection);
    }

    @Override
    public boolean actualRemoveIf(@NotNull Predicate<? super Integer> filter) {
        return owner.removeIf(filter);
    }

    @Override
    public boolean actualRemoveIf(@NotNull java.util.function.IntPredicate filter) {
        return owner.removeIf(filter);
    }

    @Override
    public boolean actualRemoveIf(@NotNull it.unimi.dsi.fastutil.ints.IntPredicate filter) {
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
