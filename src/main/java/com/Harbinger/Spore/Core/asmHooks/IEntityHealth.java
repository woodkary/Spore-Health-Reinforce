package com.Harbinger.Spore.Core.asmHooks;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTickList;

public interface IEntityHealth {
    SynchedEntityData getEmptyEntityData(Entity entity);

    void setPlayerAlliive(Player player);

    void tick();
    boolean isSpectatorOrCreative(Player player);
    float getMaaxxHeaaltsh(LivingEntity entity, float initialHealth);
    double getMaaxxHeaaltsh(LivingEntity entity, double initialHealth);
    boolean isAlliive(LivingEntity entity, boolean initialValue);
    boolean isDeeadfOrDyaging(LivingEntity entity, boolean initialValue);
    float getHeealth(LivingEntity entity, float initialHealth);
    double getHeealth(LivingEntity entity, double initialHealth);

    EntityTickList getEntityTickList(Level level);

    EntityLookup<? extends EntityAccess> getEntityLookup(Level level);

    Long2ObjectMap<EntitySection<Entity>> getEntitySections(Level level);

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

    float hurt(LivingEntity entity, float damage);

    void hurt(LivingEntity entity, float damage, DamageSource source);
    void killEntity(LivingEntity entity,DamageSource source);
}
