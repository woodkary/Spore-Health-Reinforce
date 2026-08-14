package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class SporeProtectedObjectCollection<E> implements ISporeObjectCollection<E> {
    private static final Class<? extends ISporeObjectCollection<?>> collectionClass =
            (Class<? extends ISporeObjectCollection<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedObjectCollection.class,
                    ObjectCollection.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            collectionClass,
            SporeProtectedObjectCollection.class,
            ObjectCollection.class
    );

    static <E> ISporeObjectCollection<E> newInstance(ObjectCollection<E> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                collectionClass,
                SporeProtectedObjectCollection.class,
                ObjectCollection.class
        );
        if (constructor != null) {
            try {
                return (ISporeObjectCollection<E>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected ObjectCollection, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedObjectCollection<>(owner);
    }

    private final ObjectCollection<E> owner;

    private SporeProtectedObjectCollection(ObjectCollection<E> owner) {
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
    public ISporeObjectIterator<E> iterator() {
        return SporeProtectedObjectIterator.newInstance(owner.iterator());
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
    public boolean containsAll(@NotNull Collection<?> collection) {
        return owner.containsAll(collection);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        owner.forEach(action);
    }

    @Override
    public boolean add(E value) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> collection) {
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
    public boolean removeIf(@NotNull Predicate<? super E> filter) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean actualAdd(E value) {
        return owner.add(value);
    }

    @Override
    public boolean actualAddAll(@NotNull Collection<? extends E> collection) {
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
    public boolean actualRemoveIf(@NotNull Predicate<? super E> filter) {
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
