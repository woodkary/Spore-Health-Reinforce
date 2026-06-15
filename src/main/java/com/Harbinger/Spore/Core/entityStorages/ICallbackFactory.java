package com.Harbinger.Spore.Core.entityStorages;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityInLevelCallback;

public interface ICallbackFactory {
    EntityInLevelCallback newInstance(LivingEntity entity, EntityInLevelCallback callback);
}
