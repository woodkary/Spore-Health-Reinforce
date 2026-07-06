package com.Harbinger.Spore.Core.utils.unremovableCollections;

import java.util.Map;

public interface ISporeMap<K,V> extends Map<K,V> {
    V actualRemove(Object key);
    boolean actualRemove(Object key, Object value);
    void actualClear();
}
