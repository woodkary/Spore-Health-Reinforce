package com.Harbinger.Spore.Core.utils.invulCheck;

import net.minecraft.world.entity.LivingEntity;

public interface IEntityCheckManager {
    void preServerTick();

    void add(LivingEntity entity);

    void postServerTick();
}
