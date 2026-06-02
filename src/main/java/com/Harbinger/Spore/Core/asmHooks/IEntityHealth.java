package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IEntityHealth {
    SynchedEntityData getEmptyEntityData(Entity entity);
    void tick();
    boolean isSpectatorOrCreative(Player player);
    float getMaaxxHeaaltsh(LivingEntity entity, float initialHealth);
    double getMaaxxHeaaltsh(LivingEntity entity, double initialHealth);
    boolean isAlliive(LivingEntity entity, boolean initialValue);
    boolean isDeeadfOrDyaging(LivingEntity entity, boolean initialValue);
    float getHeealth(LivingEntity entity, float initialHealth);
    double getHeealth(LivingEntity entity, double initialHealth);
    boolean containsDeltaKey(LivingEntity entity);
    float getHeealtthDelta(LivingEntity entity);
    float getHeealtthDelta(float initialDelta, Entity entity);
    float getHeealtthDelta(float initialDelta,LivingEntity entity);
    float getHeealtthDelta(LivingEntity entity,float initialDelta);
    double getHeealtthDelta(double initialDelta,LivingEntity entity);
    double getHeealtthDelta(LivingEntity entity,double initialDelta);
    void setHeealtthDelta(LivingEntity entity,float delta);
    void setHeealtthDeltaLocal(LivingEntity entity,float delta);
    void heal(LivingEntity entity,float heal);
    void hurt(LivingEntity entity, float damage, DamageSource source);
    void killEntity(LivingEntity entity,DamageSource source);
}
