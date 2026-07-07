package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public final class SporeSetProxy<T> implements ISporeSet<T> {
    private static final Class<? extends ISporeSet<?>> setClass = (Class<? extends ISporeSet<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeSetProxy.class,
            Set.class
    );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            setClass,
            SporeSetProxy.class,
            Set.class
    );

    public static <T> ISporeSet<T> newInstance(Set<T> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                setClass,
                SporeSetProxy.class,
                Set.class
        );
        if (constructor != null) {
            try {
                return (ISporeSet<T>) constructor.invoke(owner);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new ProxySet instance, %s", e.getMessage());
            }
        }
        return new SporeSetProxy<>(owner);
    }

    private final Set<T> owner;

    public SporeSetProxy(Set<T> owner) {
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
    public @NotNull Iterator<T> iterator() {
        return ProtectedIterator.newInstance(owner.iterator());
    }

    @Override
    public @NotNull Object[] toArray() {
        return owner.toArray();
    }

    @Override
    public @NotNull <T1> T1[] toArray(@NotNull T1[] a) {
        return owner.toArray(a);
    }

    @Override
    public boolean add(T t) {
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
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean actualAdd(T t) {
        return owner.add(t);
    }

    @Override
    public boolean actualAddAll(@NotNull Collection<? extends T> c) {
        return owner.addAll(c);
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
    public boolean removeIf(@NotNull Predicate<? super T> filter) {
        return false;
    }

    @Override
    public void clear() {
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
    public boolean actualRemoveIf(@NotNull Predicate<? super T> filter) {
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
                    LogUtil.errorf("failed to new ProtectedSetIterator instance, %s", e.getMessage());
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
