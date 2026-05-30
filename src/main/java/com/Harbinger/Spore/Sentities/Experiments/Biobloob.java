package com.Harbinger.Spore.Sentities.Experiments;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.BileProjectile;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Biobloob extends Experiment implements RangedAttackMob {
   public static final EntityDataAccessor SCALE;
   private final float maxScale = 2.0F;
   private final float minScale = 0.5F;
   private static double health;
   private static double damage;
   private static double armor;

   public Biobloob(EntityType type, Level level) {
      super(type, level);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("scale", (Float)this.entityData.get(SCALE));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(SCALE, tag.getFloat("scale"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SCALE, 1.0F);
   }

   public float getScale() {
      return (Float)this.entityData.get(SCALE);
   }

   public void setScale(float value) {
      this.entityData.set(SCALE, value);
   }

   public boolean hurt(DamageSource source, float amount) {
      boolean result = super.hurt(source, amount);
      if (result) {
         this.shrink(amount);
      }

      return result;
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      super.awardKillScore(entity, i, damageSource);
      this.grow();
   }

   private void grow() {
      if (this.getScale() < 2.0F) {
         this.setScale(this.getScale() + 0.1F);
         this.applyScaleEffects();
      }

   }

   private void shrink(float damageTaken) {
      this.setScale(this.getScale() - damageTaken * 0.01F);
      if (this.getScale() < 0.5F) {
         this.setScale(0.5F);
      }

      this.applyScaleEffects();
   }

   private void applyScaleEffects() {
      Vec3 position = this.position();
      this.setPos(position);
      this.computeAttribute(Attributes.MAX_HEALTH, health * (double)this.getScale());
      this.computeAttribute(Attributes.ATTACK_DAMAGE, damage * (double)this.getScale());
      this.computeAttribute(Attributes.ARMOR, armor * (double)this.getScale());
      this.computeAttribute(Attributes.MOVEMENT_SPEED, 0.3 * (double)(1.0F / this.getScale()));
      if (this.getHealth() > this.getMaxHealth()) {
         this.setHealth(this.getMaxHealth());
      }

   }

   public @NotNull EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(this.getScale() == 1.0F ? 1.0F : this.getScale() * 0.8F);
   }

   public void onSyncedDataUpdated(EntityDataAccessor accessor) {
      super.onSyncedDataUpdated(accessor);
      if (SCALE.equals(accessor)) {
         this.refreshDimensions();
      }

   }

   private void computeAttribute(Attribute attributes, double value) {
      AttributeInstance instance = this.getAttribute(attributes);
      if (instance != null) {
         instance.setBaseValue(value);
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FLYING_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, damage).add(Attributes.ARMOR, armor).add(Attributes.FOLLOW_RANGE, (double)30.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.bioblob_loot.get();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.BIOBLOB.get();
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity livingEntity) {
            return (double)(livingEntity.getBbWidth() + 13.0F * Biobloob.this.getScale());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      if (!this.level().isClientSide) {
         if (Math.random() <= (double)0.5F) {
            BileProjectile bileProjectile = new BileProjectile(this.level(), this, this.TARGET_SELECTOR);
            double dx = livingEntity.getX() - this.getX();
            double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.0F;
            double dz = livingEntity.getZ() - this.getZ();
            bileProjectile.setDamage((float)((Double)SConfig.SERVER.biobloob__ranged_damage.get() * (Double)SConfig.SERVER.global_damage.get()));
            bileProjectile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
            bileProjectile.shoot(dx, dy - bileProjectile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
            this.level().addFreshEntity(bileProjectile);
         } else {
            AcidBall.shoot(this, livingEntity, (float)((Double)SConfig.SERVER.biobloob__ranged_damage.get() * (Double)SConfig.SERVER.global_damage.get()));
            this.playSound(SoundEvents.SLIME_JUMP, 1.0F, 0.5F);
         }
      }

   }

   public void tick() {
      super.tick();
      if (this.tickCount % 60 == 0) {
         LivingEntity living = this.getTarget();
         if (living != null) {
            this.performRangedAttack(living, 0.0F);
         }
      }

   }

   static {
      SCALE = SynchedEntityData.defineId(Biobloob.class, EntityDataSerializers.FLOAT);
      health = (Double)SConfig.SERVER.biobloob_hp.get() * (Double)SConfig.SERVER.global_health.get();
      damage = (Double)SConfig.SERVER.biobloob_damage.get() * (Double)SConfig.SERVER.global_damage.get();
      armor = (Double)SConfig.SERVER.biobloob_armor.get() * (Double)SConfig.SERVER.global_armor.get();
   }
}
