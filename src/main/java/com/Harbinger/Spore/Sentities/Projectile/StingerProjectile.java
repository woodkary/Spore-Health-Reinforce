package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class StingerProjectile extends AbstractArrow {
   private static final EntityDataAccessor DAMAGE;

   public StingerProjectile(Level level) {
      super((EntityType)Sentities.STINGER.get(), level);
   }

   public StingerProjectile(Level level, LivingEntity living, float damage) {
      super((EntityType)Sentities.STINGER.get(), level);
      this.setOwner(living);
      this.setDamage(damage);
   }

   public Float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   public void setDamage(Float value) {
      this.entityData.set(DAMAGE, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE, 0.0F);
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
   }

   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      Entity var3 = result.getEntity();
      if (var3 instanceof LivingEntity living) {
         this.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity)this.getOwner()), this.getDamage());
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 0));
         living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
         living.setArrowCount(living.getArrowCount() - 1);
      }

      this.discard();
   }

   protected void onHitBlock(BlockHitResult p_36755_) {
      super.onHitBlock(p_36755_);
      this.discard();
   }

   protected boolean canHitEntity(Entity entity) {
      return !(entity instanceof UtilityEntity) && !(entity instanceof Infected) && super.canHitEntity(entity);
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 200 == 0) {
         this.discard();
      }

   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   static {
      DAMAGE = SynchedEntityData.defineId(StingerProjectile.class, EntityDataSerializers.FLOAT);
   }
}
