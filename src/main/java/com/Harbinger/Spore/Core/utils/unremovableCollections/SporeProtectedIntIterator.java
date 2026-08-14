package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

final class SporeProtectedIntIterator implements ISporeIntIterator {
    private static final Class<? extends ISporeIntIterator> iteratorClass =
            (Class<? extends ISporeIntIterator>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedIntIterator.class,
                    IntIterator.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            iteratorClass,
            SporeProtectedIntIterator.class,
            IntIterator.class
    );

    static ISporeIntIterator newInstance(IntIterator owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                iteratorClass,
                SporeProtectedIntIterator.class,
                IntIterator.class
        );
        if (constructor != null) {
            try {
                return (ISporeIntIterator) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected IntIterator, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedIntIterator(owner);
    }

    private final IntIterator owner;

    private SporeProtectedIntIterator(IntIterator owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasNext() {
        return owner.hasNext();
    }

    @Override
    public int nextInt() {
        return owner.nextInt();
    }

    @Override
    public int skip(int amount) {
        return owner.skip(amount);
    }

    @Override
    public void forEachRemaining(java.util.function.IntConsumer action) {
        owner.forEachRemaining(action);
    }

    @Override
    public void forEachRemaining(it.unimi.dsi.fastutil.ints.IntConsumer action) {
        owner.forEachRemaining(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
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
