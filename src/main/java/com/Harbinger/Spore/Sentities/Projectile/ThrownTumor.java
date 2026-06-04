package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Fluids.BileLiquid;
import java.util.List;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PlayMessages;

public class ThrownTumor extends ThrowableItemProjectile {
   private static final EntityDataAccessor TYPE;
   public MobEffect mobEffect;
   public int duration = 600;
   public int potion_level = 1;
   public ExplosionInteraction explode;

   public ThrownTumor(EntityType type, Level level) {
      super(type, level);
      this.explode = ExplosionInteraction.NONE;
   }

   public ThrownTumor(Level level, LivingEntity entity) {
      super((EntityType)Sentities.THROWN_TUMOR.get(), entity, level);
      this.explode = ExplosionInteraction.NONE;
   }

   public ThrownTumor(PlayMessages.SpawnEntity spawnEntity, Level level) {
      super((EntityType)Sentities.THROWN_TUMOR.get(), level);
      this.explode = ExplosionInteraction.NONE;
   }

   protected Item getDefaultItem() {
      Item var10000;
      switch ((Integer)this.entityData.get(TYPE)) {
         case 1 -> var10000 = (Item)Sitems.SICKEN_TUMOR.get();
         case 2 -> var10000 = (Item)Sitems.CALCIFIED_TUMOR.get();
         case 3 -> var10000 = (Item)Sitems.FROZEN_TUMOR.get();
         case 4 -> var10000 = (Item)Sitems.BILE_TUMOR.get();
         default -> var10000 = (Item)Sitems.TUMOR.get();
      }

      return var10000;
   }

   public void handleEntityEvent(byte value) {
      if (value == 3) {
         for(int i = 0; i < 8; ++i) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - (double)0.5F) * 0.08, ((double)this.random.nextFloat() - (double)0.5F) * 0.08, ((double)this.random.nextFloat() - (double)0.5F) * 0.08);
         }
      }

   }

   public void setMobEffect(MobEffect effect) {
      this.mobEffect = effect;
   }

   public void setExplode(ExplosionInteraction value) {
      this.explode = value;
   }

   protected void onHit(HitResult hitResult) {
      super.onHit(hitResult);
      if (!this.level().isClientSide) {
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)(Integer)SConfig.SERVER.tumor_explosion.get(), this.explode);
         AABB aabb = this.getBoundingBox().inflate((double)3.0F);
         List<Entity> entities = this.level().getEntities(this, aabb);
         switch ((Integer)this.entityData.get(TYPE)) {
            case 0 -> this.summonPotionEffects();
            case 1 -> this.poisonTargets(entities);
            case 2 -> this.damageTargets(entities);
            case 3 -> this.freezeTargets(entities);
            case 4 -> this.bileTargets(entities);
         }

         this.discard();
      }

   }

   public void summonPotionEffects() {
      AreaEffectCloud cloud = (AreaEffectCloud)EntityType.AREA_EFFECT_CLOUD.create(this.level());
      if (this.mobEffect != null && cloud != null) {
         cloud.addEffect(new MobEffectInstance(this.mobEffect, this.duration, this.potion_level));
         cloud.setDuration(160);
         cloud.setRadius(2.0F);
         cloud.moveTo(this.getX(), this.getY(), this.getZ());
         this.level().addFreshEntity(cloud);
      }

   }

   public void poisonTargets(List<Entity> entityList) {
      for(Entity entity : entityList) {
         if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
         }
      }

   }

   public void bileTargets(List<Entity> entityList) {
      for(Entity entity : entityList) {
         if (entity instanceof LivingEntity livingEntity) {
            for(MobEffectInstance instance : BileLiquid.bileEffects()) {
               livingEntity.addEffect(instance);
            }
         }
      }

   }

   public void freezeTargets(List<Entity> entityList) {
      for(Entity entity : entityList) {
         if (entity instanceof LivingEntity target) {
            MobEffectInstance instance = target.getEffect((MobEffect)Seffects.FROSTBITE.get());
            int intensity = instance == null ? 0 : instance.getAmplifier() + 1;
            target.addEffect(new MobEffectInstance((MobEffect)Seffects.FROSTBITE.get(), 600, intensity));
            if(SporeJudge.isSporeEntity(target)) {
               SporeAttackUtil.INSTANCE.dealDamage(target,this.getOwner() instanceof LivingEntity liv?liv:null,target.damageSources().freeze(),5.0f);
            }
         }
      }

   }

   public void damageTargets(List<Entity> entityList) {
      for(Entity entity : entityList) {
         if (entity instanceof LivingEntity target) {
            Entity var6 = this.getOwner();
            boolean isTargetOwner=target.equals(var6);
            if (var6 instanceof LivingEntity owner) {
               DamageSource source = this.level().damageSources().mobProjectile(this, owner);
               if(isTargetOwner) {
                  target.hurt(source, 10.0F);
               } else {
                  target.hurtTime = 0;
                  target.invulnerableTime = 0;
                  SporeAttackUtil.INSTANCE.dealDamage(target, owner, source, 10.0F);
               }
            }
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TYPE, 0);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TYPE, tag.getInt("type"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("type", (Integer)this.entityData.get(TYPE));
   }

   public void setType(int value) {
      this.entityData.set(TYPE, value);
   }

   static {
      TYPE = SynchedEntityData.defineId(ThrownTumor.class, EntityDataSerializers.INT);
   }
}
