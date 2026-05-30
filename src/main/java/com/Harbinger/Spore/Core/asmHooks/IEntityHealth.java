package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IEntityHealth {
    SynchedEntityData getEmptyEntityData(Entity entity);

    float getHeealtthDelta(float initialDelta, Entity entity);
    float getHeealtthDelta(float initialDelta,LivingEntity entity);
    float getHeealtthDelta(LivingEntity entity,float initialDelta);
    double getHeealtthDelta(double initialDelta,LivingEntity entity);
    double getHeealtthDelta(LivingEntity entity,double initialDelta);
    void setHeealtthDelta(LivingEntity entity,float delta);
    void setHeealtthDeltaLocal(LivingEntity entity,float delta);
}
