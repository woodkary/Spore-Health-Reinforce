package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Consumer;

final class SporeProtectedEntryIterator<E>
        implements ISporeObjectIterator<Int2ObjectMap.Entry<E>> {
    private static final Class<? extends ISporeObjectIterator<?>> iteratorClass =
            (Class<? extends ISporeObjectIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedEntryIterator.class,
                    ObjectIterator.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            iteratorClass,
            SporeProtectedEntryIterator.class,
            ObjectIterator.class
    );

    static <E> ISporeObjectIterator<Int2ObjectMap.Entry<E>> newInstance(
            ObjectIterator<Int2ObjectMap.Entry<E>> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                iteratorClass,
                SporeProtectedEntryIterator.class,
                ObjectIterator.class
        );
        if (constructor != null) {
            try {
                return (ISporeObjectIterator<Int2ObjectMap.Entry<E>>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected entry iterator, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedEntryIterator<>(owner);
    }

    private final ObjectIterator<Int2ObjectMap.Entry<E>> owner;

    private SporeProtectedEntryIterator(ObjectIterator<Int2ObjectMap.Entry<E>> owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasNext() {
        return owner.hasNext();
    }

    @Override
    public Int2ObjectMap.Entry<E> next() {
        return SporeProtectedInt2ObjectEntry.newInstance(owner.next());
    }

    @Override
    public int skip(int amount) {
        int skipped = 0;
        while (skipped < amount && hasNext()) {
            next();
            skipped++;
        }
        return skipped;
    }

    @Override
    public void forEachRemaining(Consumer<? super Int2ObjectMap.Entry<E>> action) {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.accept(next());
        }
    }

    @Override
    public void remove() {
    }

    @Override
    public void actualRemove() {
        owner.remove();
    }
}
