package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.world.level.entity.EntityAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class ProtectedUUIDHashMapBase<V extends EntityAccess> extends HashMap<UUID, V> {
    protected boolean shouldExposeValue(V value) {
        return !SimpleRemoveUtil.INSTANCE.isRemoved(value);
    }

    public ProtectedUUIDHashMapBase() {
    }

    public ProtectedUUIDHashMapBase(Map<? extends UUID, ? extends V> m) {
        super(m);
    }
}
