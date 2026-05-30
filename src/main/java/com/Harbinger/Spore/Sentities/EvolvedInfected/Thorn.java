package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Variants.ThornVariants;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Thorn extends EvolvedInfected implements VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;

   public Thorn(EntityType type, Level level) {
      super(type, level);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.thorn_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.thorn_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.thorn_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public boolean hurt(DamageSource source, float amount) {
      Entity var4 = source.getDirectEntity();
      if (var4 instanceof LivingEntity livingEntity) {
         if (livingEntity.distanceToSqr(this) < (double)100.0F && !source.is(DamageTypes.THORNS)) {
            if (this.getVariant() == ThornVariants.TOXIC) {
               livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
               livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 0));
            }

            livingEntity.hurt(this.level().damageSources().thorns(this), amount * 0.4F);
         }
      }

      return super.hurt(source, amount);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
      }

      return super.doHurtTarget(entity);
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      ThornVariants variant = (ThornVariants)Util.getRandom(ThornVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HUSK_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.thorn_loot.get();
   }

   private void setVariant(ThornVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public ThornVariants getVariant() {
      return ThornVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= ThornVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return ThornVariants.values().length;
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Thorn.class, EntityDataSerializers.INT);
   }
}
