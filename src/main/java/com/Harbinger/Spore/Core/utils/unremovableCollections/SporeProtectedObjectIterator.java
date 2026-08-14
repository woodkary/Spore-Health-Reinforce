package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

final class SporeProtectedObjectIterator<E> implements ISporeObjectIterator<E> {
    private static final Class<? extends ISporeObjectIterator<?>> iteratorClass =
            (Class<? extends ISporeObjectIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedObjectIterator.class,
                    ObjectIterator.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            iteratorClass,
            SporeProtectedObjectIterator.class,
            ObjectIterator.class
    );

    static <E> ISporeObjectIterator<E> newInstance(ObjectIterator<E> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                iteratorClass,
                SporeProtectedObjectIterator.class,
                ObjectIterator.class
        );
        if (constructor != null) {
            try {
                return (ISporeObjectIterator<E>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected ObjectIterator, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedObjectIterator<>(owner);
    }

    private final ObjectIterator<E> owner;

    private SporeProtectedObjectIterator(ObjectIterator<E> owner) {
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
    public int skip(int amount) {
        return owner.skip(amount);
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        owner.forEachRemaining(action);
    }

    @Override
    public void remove() {
    }

    @Override
    public void actualRemove() {
        owner.remove();
    }
}
