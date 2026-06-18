package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.level.ChunkMap;

import java.util.Map;

abstract class ProtectedTrackedEntityMapBase extends Int2ObjectOpenHashMap<ChunkMap.TrackedEntity> {
    public ProtectedTrackedEntityMapBase() {
    }

    public ProtectedTrackedEntityMapBase(Map<? extends Integer, ? extends ChunkMap.TrackedEntity> m) {
        super(m);
    }

    public ProtectedTrackedEntityMapBase(Int2ObjectMap<ChunkMap.TrackedEntity> m) {
        super(m);
    }

    protected boolean shouldExposeValue(ChunkMap.TrackedEntity value) {
        return value != null && !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(value);
    }

    protected ObjectIterator<Entry<ChunkMap.TrackedEntity>> superEntryIterator() {
        return super.int2ObjectEntrySet().iterator();
    }
}
