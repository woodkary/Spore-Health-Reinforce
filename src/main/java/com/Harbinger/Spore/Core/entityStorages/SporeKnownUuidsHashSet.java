package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Predicate;

public final class SporeKnownUuidsHashSet extends HashSet<UUID> implements ISporeEntityStorage {
    private static final Class<? extends HashSet<UUID>> setClass =
            (Class<? extends HashSet<UUID>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeKnownUuidsHashSet.class
            );
    private static MethodHandle noArg = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            setClass,
            SporeKnownUuidsHashSet.class
    );
    private static MethodHandle collectionArg = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            setClass,
            SporeKnownUuidsHashSet.class,
            Collection.class
    );

    public static HashSet<UUID> newInstance() {
        noArg = MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                setClass,
                SporeKnownUuidsHashSet.class
        );
        if (noArg != null) {
            try {
                return (HashSet<UUID>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeKnownUuidsHashSet instance", t.getMessage());
            }
        }
        return new SporeKnownUuidsHashSet();
    }

    public static HashSet<UUID> newInstance(Collection<? extends UUID> collection) {
        collectionArg = MethodHandleUtil.INSTANCE.ensureConstructor(
                collectionArg,
                setClass,
                SporeKnownUuidsHashSet.class,
                Collection.class
        );
        if (collectionArg != null) {
            try {
                return (HashSet<UUID>) collectionArg.invoke(collection);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeKnownUuidsHashSet instance", t.getMessage());
            }
        }
        return new SporeKnownUuidsHashSet(collection);
    }

    public SporeKnownUuidsHashSet() {
    }

    public SporeKnownUuidsHashSet(Collection<? extends UUID> collection) {
        addAll(collection);
    }

    @Override
    public boolean add(UUID uuid) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(uuid)) {
            return false;
        }
        return super.add(uuid);
    }

    @Override
    public boolean addAll(Collection<? extends UUID> collection) {
        boolean changed = false;
        for (UUID uuid : collection) {
            changed |= add(uuid);
        }
        return changed;
    }

    @Override
    public boolean contains(Object o) {
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(o) && super.contains(o);
    }

    @Override
    public Iterator<UUID> iterator() {
        return ProtectedUuidIterator.newInstance(super.iterator());
    }

    @Override
    public int size() {
        int count = 0;
        Iterator<UUID> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public boolean removeIf(Predicate<? super UUID> filter) {
        boolean changed = false;
        Iterator<UUID> iterator = iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (filter.test(uuid)) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    private static final class ProtectedUuidIterator implements Iterator<UUID> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedUuidIterator.class,
                        Iterator.class
                );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedUuidIterator.class,
                Iterator.class
        );

        public static Iterator<UUID> newInstance(Iterator<UUID> delegate) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedUuidIterator.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (Iterator<UUID>) constructor.invoke(delegate);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedUuidIterator, %s", t.getMessage());
                }
            }
            return new ProtectedUuidIterator(delegate);
        }

        private final Iterator<UUID> delegate;
        private UUID lastReturned;
        private UUID nextCandidate;
        private boolean nextReady;

        private ProtectedUuidIterator(Iterator<UUID> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            if (nextReady) {
                return true;
            }
            while (delegate.hasNext()) {
                UUID uuid = delegate.next();
                if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(uuid)) {
                    nextCandidate = uuid;
                    nextReady = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public UUID next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = nextCandidate;
            nextCandidate = null;
            nextReady = false;
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            delegate.remove();
            lastReturned = null;
        }
    }
}
