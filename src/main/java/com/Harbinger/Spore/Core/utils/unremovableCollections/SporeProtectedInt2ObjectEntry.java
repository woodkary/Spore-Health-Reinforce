package com.Harbinger.Spore.Core.utils.unremovableCollections;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.lang.invoke.MethodHandle;

final class SporeProtectedInt2ObjectEntry<E> implements ISporeInt2ObjectEntry<E> {
    private static final Class<? extends ISporeInt2ObjectEntry<?>> entryClass =
            (Class<? extends ISporeInt2ObjectEntry<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeProtectedInt2ObjectEntry.class,
                    Int2ObjectMap.Entry.class
            );
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            entryClass,
            SporeProtectedInt2ObjectEntry.class,
            Int2ObjectMap.Entry.class
    );

    static <E> ISporeInt2ObjectEntry<E> newInstance(Int2ObjectMap.Entry<E> owner) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entryClass,
                SporeProtectedInt2ObjectEntry.class,
                Int2ObjectMap.Entry.class
        );
        if (constructor != null) {
            try {
                return (ISporeInt2ObjectEntry<E>) constructor.invoke(owner);
            } catch (Throwable throwable) {
                LogUtil.errorf("failed to new protected Int2ObjectMap entry, %s", throwable.getMessage());
            }
        }
        return new SporeProtectedInt2ObjectEntry<>(owner);
    }

    private final Int2ObjectMap.Entry<E> owner;

    private SporeProtectedInt2ObjectEntry(Int2ObjectMap.Entry<E> owner) {
        this.owner = owner;
    }

    @Override
    public int getIntKey() {
        return owner.getIntKey();
    }

    @Override
    public Integer getKey() {
        return owner.getKey();
    }

    @Override
    public E getValue() {
        return owner.getValue();
    }

    @Override
    public E setValue(E value) {
        return value;
    }

    @Override
    public E actualSetValue(E value) {
        return owner.setValue(value);
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
