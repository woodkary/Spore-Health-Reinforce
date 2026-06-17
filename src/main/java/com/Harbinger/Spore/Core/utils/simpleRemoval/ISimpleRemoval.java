package com.Harbinger.Spore.Core.utils.simpleRemoval;

import net.minecraft.world.entity.Entity;

public interface ISimpleRemoval {
    boolean remove(Entity entity, Entity.RemovalReason removalReason);
    Entity removeLocal(Entity entity, Entity.RemovalReason removalReason);
}
