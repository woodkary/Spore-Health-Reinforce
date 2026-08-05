package com.Harbinger.Spore.Core.entityStorages;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.syncher.SynchedEntityData;

public interface ICustomEntityData {
    Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById();
}
