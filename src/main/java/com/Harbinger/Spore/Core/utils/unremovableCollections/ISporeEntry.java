package com.Harbinger.Spore.Core.utils.unremovableCollections;

import java.util.Map;

public interface ISporeEntry<K, V> extends Map.Entry<K, V> {
    V actualSetValue(V value);
}
