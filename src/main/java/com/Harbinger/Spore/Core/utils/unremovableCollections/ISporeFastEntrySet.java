package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ISporeFastEntrySet<V> extends Int2ObjectMap.FastEntrySet<V>, ISporeObjectSet<Int2ObjectMap.Entry<V>> {
    @Override
    ISporeObjectIterator<Int2ObjectMap.Entry<V>> iterator();

    @Override
    ISporeObjectIterator<Int2ObjectMap.Entry<V>> fastIterator();

    void actualFastForEach(@NotNull Consumer<? super Int2ObjectMap.Entry<V>> consumer);
}
