package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

import java.util.Map;

abstract class ProtectedEntityMapBase<V> extends Int2ObjectLinkedOpenHashMap<V> {
    public ProtectedEntityMapBase(Map<? extends Integer, ? extends V> m) {
        super(m);
    }

    public ProtectedEntityMapBase() {
        super();
    }

    public ProtectedEntityMapBase(Int2ObjectMap<V> m, float f) {
        super(m, f);
    }

    protected boolean shouldExposeValue(V value) {
        return !SimpleRemoveUtil.INSTANCE.isRemoved(value);
    }

    protected ObjectBidirectionalIterator<Entry<V>> superEntryIterator() {
        return super.int2ObjectEntrySet().iterator();
    }
}
