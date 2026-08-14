package com.Harbinger.Spore.Core.utils.unremovableCollections;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public interface ISporeInt2ObjectEntry<V> extends Int2ObjectMap.Entry<V>, ISporeEntry<Integer, V> {
    @Override
    V actualSetValue(V value);
}
