package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.Utility.WaveEntity;
import com.Harbinger.Spore.Sentities.Variants.UmarmerVariants;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Umarmer extends Organoid implements VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor TIMER;
   public static final EntityDataAccessor ATTACKING;
   public static final EntityDataAccessor HARD_ATTACK;
   public static final EntityDataAccessor PINNED;
   public static final EntityDataAccessor SHIELDING;
   public AnimationState attackAnimationState = new AnimationState();
   public AnimationState pin_start = new AnimationState();
   public AnimationState pin_end = new AnimationState();
   public AnimationState pin_idle = new AnimationState();
   public AnimationState shield_idle = new AnimationState();
   public AnimationState shield_start = new AnimationState();
   public AnimationState shield_end = new AnimationState();
   public AnimationState squeeze_idle = new AnimationState();
   private int attackAnimationTimeout = 0;
   private int SlamAttackAnimationTimeout = 0;
   private int SlamAttackAnimationEndTimeout = 0;
   private int idlePinTimeout = 0;
   private int idleShieldTimeout = 0;
   private int startShieldTimeout = 0;
   private int endShieldTimeout = 0;
   private int squeezeTimeout = 0;
   private int chargeWave = 0;
   private boolean start_shield = false;
   private boolean end_shield = false;
   private boolean end_pin = false;

   public Umarmer(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.umarmer_loot.get();
   }

   public int getNumberOfParticles() {
      return 6;
   }

   public int getEmerge_tick() {
      return 80;
   }

   public int getBorrow_tick() {
      return 100;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.umarmed_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.umarmed_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.umarmed_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)20.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putBoolean("pinned", (Boolean)this.entityData.get(PINNED));
      tag.putBoolean("shielded", (Boolean)this.entityData.get(SHIELDING));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(PINNED, tag.getBoolean("pinned"));
      this.entityData.set(SHIELDING, tag.getBoolean("shielded"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public int getTimer() {
      return (Integer)this.entityData.get(TIMER);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.setupAnimationStates();
      }

      if (!this.level().isClientSide) {
         if (this.getTarget() != null && this.chargeWave < 61) {
            ++this.chargeWave;
         }

         if (this.getTarget() == null && (Integer)this.entityData.get(TIMER) < 2400) {
            this.entityData.set(TIMER, (Integer)this.entityData.get(TIMER) + 1);
         } else if ((Integer)this.entityData.get(TIMER) >= 2400) {
            this.tickBurrowing();
         }
      }

      if (!this.isVehicle() && this.isPinned()) {
         this.setPinned(false);
      }

   }

   private void setupAnimationStates() {
      if (this.isPinned() && this.idlePinTimeout <= 0) {
         this.idlePinTimeout = 40;
         this.pin_idle.start(this.tickCount);
      } else {
         --this.idlePinTimeout;
      }

      if (this.start_shield && this.startShieldTimeout <= 0) {
         this.startShieldTimeout = 30;
         this.shield_start.start(this.tickCount);
      } else {
         --this.startShieldTimeout;
         this.start_shield = false;
      }

      if (this.end_pin && this.SlamAttackAnimationEndTimeout <= 0) {
         this.SlamAttackAnimationEndTimeout = 40;
         this.pin_end.start(this.tickCount);
      } else {
         --this.SlamAttackAnimationEndTimeout;
         this.end_pin = false;
      }

      if (this.end_shield && this.endShieldTimeout <= 0) {
         this.endShieldTimeout = 30;
         this.shield_end.start(this.tickCount);
      } else {
         --this.endShieldTimeout;
         this.end_shield = false;
      }

      if (this.isVehicle() && !this.isPinned() && this.squeezeTimeout <= 0) {
         this.squeezeTimeout = 40;
         this.squeeze_idle.start(this.tickCount);
      } else {
         --this.squeezeTimeout;
      }

      if (this.isShielding() && this.idleShieldTimeout <= 0) {
         this.idleShieldTimeout = 30;
         this.shield_idle.start(this.tickCount);
      } else {
         --this.idleShieldTimeout;
      }

      if (this.startShieldTimeout <= 0) {
         this.shield_start.stop();
      }

      if (this.endShieldTimeout <= 0) {
         this.shield_end.stop();
      }

      if (this.IsAttacking() && this.attackAnimationTimeout <= 0) {
         this.attackAnimationTimeout = 20;
         this.attackAnimationState.start(this.tickCount);
      } else {
         --this.attackAnimationTimeout;
      }

      if (this.IsHardAttacking() && this.SlamAttackAnimationTimeout <= 0) {
         this.SlamAttackAnimationTimeout = 60;
         this.pin_start.start(this.tickCount);
      } else {
         --this.SlamAttackAnimationTimeout;
      }

      if (!this.IsHardAttacking()) {
         this.pin_start.stop();
      }

      if (!this.IsAttacking()) {
         this.attackAnimationState.stop();
      }

      if (!this.isPinned()) {
         this.pin_idle.stop();
      }

      if (!this.isVehicle()) {
         this.squeeze_idle.stop();
      }

      if (!this.isShielding()) {
         this.shield_idle.stop();
      }

   }

   public boolean isNoAi() {
      return this.isBurrowing() || this.isEmerging();
   }

   public boolean hurt(DamageSource source, float value) {
      if (source.is(DamageTypeTags.IS_PROJECTILE)) {
         if (this.isShielding()) {
            return super.hurt(source, value / 4.0F);
         }

         this.setShielding(true);
      }

      return this.isEmerging() ? false : super.hurt(source, value);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new GrabTarget(this));
      this.goalSelector.addGoal(4, new UmarmedMeleeAttack(this, (double)0.0F, false, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(4, new PinAttack(this, (double)0.0F, false));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this) {
         public boolean canUse() {
            return super.canUse() && !Umarmer.this.isVehicle();
         }
      });
      super.registerGoals();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ATTACKING, false);
      this.entityData.define(HARD_ATTACK, false);
      this.entityData.define(PINNED, false);
      this.entityData.define(SHIELDING, false);
      this.entityData.define(TIMER, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void SetAttacking(boolean value) {
      this.entityData.set(ATTACKING, value);
   }

   public boolean IsAttacking() {
      return (Boolean)this.entityData.get(ATTACKING);
   }

   public void setPinned(boolean value) {
      this.entityData.set(PINNED, value);
   }

   public boolean isPinned() {
      return (Boolean)this.entityData.get(PINNED);
   }

   public void SetHardAttacking(boolean value) {
      this.entityData.set(HARD_ATTACK, value);
   }

   public boolean IsHardAttacking() {
      return (Boolean)this.entityData.get(HARD_ATTACK);
   }

   public void setShielding(boolean value) {
      this.entityData.set(SHIELDING, value);
   }

   public boolean isShielding() {
      return (Boolean)this.entityData.get(SHIELDING);
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         this.discard();
         burrowing = -1;
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public boolean shouldRiderSit() {
      return false;
   }

   public boolean isVehicle() {
      if (this.getFirstPassenger() != null) {
         Entity entity = this.getFirstPassenger();
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (this.isPinned()) {
               this.getLookControl().setLookAt(entity.getX(), entity.getEyeY(), entity.getZ());
               livingEntity.setPose(Pose.SWIMMING);
            }

            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 2), this);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1), this);
         }
      }

      return super.isVehicle();
   }

   protected void positionRider(Entity entity, MoveFunction p_19958_) {
      super.positionRider(entity, p_19958_);
      Vec3 vec3;
      double y;
      if (this.isPinned()) {
         vec3 = (new Vec3((double)2.0F, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         y = -0.1;
      } else {
         vec3 = (new Vec3(0.4, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         y = 1.2;
      }

      entity.setPos(this.getX() + vec3.x, this.getY() + y, this.getZ() + vec3.z);
   }

   public boolean fireImmune() {
      return this.getVariant() == UmarmerVariants.CHARRED;
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (this.getVariant() == UmarmerVariants.CHARRED) {
            livingEntity.setSecondsOnFire(10);
         } else if (this.getVariant() == UmarmerVariants.BILE) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
         } else if (this.getVariant() == UmarmerVariants.CORROSIVE) {
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 600, 1));
         }

         livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 1));
         livingEntity.knockback((double)1.2F, (double)(-Mth.sin(this.getYRot() * ((float)Math.PI / 180F))), (double)Mth.cos(this.getYRot() * ((float)Math.PI / 180F)));
      }

      return super.doHurtTarget(entity);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.UMARMER_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      UmarmerVariants variant = (UmarmerVariants)Util.getRandom(UmarmerVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public UmarmerVariants getVariant() {
      return UmarmerVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= UmarmerVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return UmarmerVariants.values().length;
   }

   private void setVariant(UmarmerVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (PINNED.equals(dataAccessor)) {
         if (!this.isPinned()) {
            this.end_pin = true;
         }

         this.refreshDimensions();
      }

      if (SHIELDING.equals(dataAccessor)) {
         if (this.isShielding()) {
            this.start_shield = true;
         } else {
            this.end_shield = true;
         }
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isPinned() ? super.getDimensions(pose).scale(2.75F, 0.35F) : super.getDimensions(pose);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public boolean isCloseCombatant() {
      return true;
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.INT);
      TIMER = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.INT);
      ATTACKING = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.BOOLEAN);
      HARD_ATTACK = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.BOOLEAN);
      PINNED = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.BOOLEAN);
      SHIELDING = SynchedEntityData.defineId(Umarmer.class, EntityDataSerializers.BOOLEAN);
   }

   static class UmarmedMeleeAttack extends AOEMeleeAttackGoal {
      private final Umarmer mob;
      private final int attackDelay = 10;
      private int ticksUntilNextAttack = 10;
      private boolean shouldCountTillNextAttack = false;

      public UmarmedMeleeAttack(Umarmer umarmer, double p_25553_, boolean p_25554_, Predicate<LivingEntity> livingEntityPredicate) {
         super(umarmer, p_25553_, p_25554_, (double)2.0F, 1.0F, livingEntityPredicate);
         this.mob = umarmer;
      }

      public boolean canUse() {
         LivingEntity target = this.mob.getTarget();
         if (this.mob.isPinned()) {
            return false;
         } else if (this.mob.isVehicle()) {
            return false;
         } else {
            return target != null && this.mob.distanceToSqr(target) > this.getAttackReachSqr(target) ? false : super.canUse();
         }
      }

      protected double getAttackReachSqr(LivingEntity entity) {
         return (double)16.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
      }

      protected void checkAndPerformAttack(LivingEntity entity, double at) {
         if (this.isEnemyWithinAttackDistance(entity, at)) {
            this.shouldCountTillNextAttack = true;
            if (this.isTimeToStartAttackAnimation()) {
               this.mob.SetAttacking(true);
               this.mob.setShielding(false);
            }

            if (this.isTimeToAttack()) {
               this.mob.getLookControl().setLookAt(entity.getX(), entity.getEyeY(), entity.getZ());
               this.performAttack(entity);
            }
         } else {
            this.resetAttackCooldown();
            this.shouldCountTillNextAttack = false;
            this.mob.SetAttacking(false);
            this.mob.attackAnimationTimeout = 0;
         }

      }

      private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
         return pDistToEnemySqr <= this.getAttackReachSqr(pEnemy);
      }

      protected void resetAttackCooldown() {
         this.ticksUntilNextAttack = this.adjustedTickDelay(20);
      }

      protected boolean isTimeToAttack() {
         return this.ticksUntilNextAttack <= 0;
      }

      protected boolean isTimeToStartAttackAnimation() {
         return this.ticksUntilNextAttack <= 10;
      }

      protected int getTicksUntilNextAttack() {
         return this.ticksUntilNextAttack;
      }

      protected void performAttack(LivingEntity pEnemy) {
         this.resetAttackCooldown();
         this.mob.swing(InteractionHand.MAIN_HAND);
         this.mob.doHurtTarget(pEnemy);
         AABB hitbox = pEnemy.getBoundingBox().inflate(this.box);

         for(LivingEntity en : pEnemy.level().getEntitiesOfClass(LivingEntity.class, hitbox, this.victims)) {
            this.mob.doHurtTarget(en);
         }

      }

      public void tick() {
         super.tick();
         if (this.shouldCountTillNextAttack) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
         }

      }

      public void stop() {
         this.mob.SetAttacking(false);
         super.stop();
      }
   }

   static class PinAttack extends CustomMeleeAttackGoal {
      private final Umarmer mob;
      private final int attackDelay = 55;
      private int ticksUntilNextAttack = 110;
      private boolean shouldCountTillNextAttack = false;

      public PinAttack(Umarmer umarmer, double p_25553_, boolean p_25554_) {
         super(umarmer, p_25553_, p_25554_);
         this.mob = umarmer;
      }

      public boolean canUse() {
         if (this.mob.isPinned()) {
            return false;
         } else if (this.mob.isVehicle()) {
            return false;
         } else {
            return this.mob.chargeWave >= 60 ? true : super.canUse();
         }
      }

      protected double getAttackReachSqr(LivingEntity entity) {
         return (double)8.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
      }

      protected void checkAndPerformAttack(LivingEntity entity, double at) {
         if (this.isEnemyWithinAttackDistance(entity, at)) {
            this.shouldCountTillNextAttack = true;
            if (this.isTimeToStartAttackAnimation()) {
               this.mob.SetHardAttacking(true);
               this.mob.setShielding(false);
            }

            if (this.isTimeToAttack()) {
               this.mob.getLookControl().setLookAt(entity.getX(), entity.getEyeY(), entity.getZ());
               this.performAttack(entity);
            }
         } else if (!this.isEnemyWithinAttackDistance(entity, at) && this.mob.chargeWave >= 60) {
            this.shouldCountTillNextAttack = true;
            if (this.isTimeToStartAttackAnimation()) {
               this.mob.SetHardAttacking(true);
               this.mob.setShielding(false);
               if (this.isTimeToAttack()) {
                  WaveEntity waveEntity = new WaveEntity(this.mob.level(), this.mob);
                  this.mob.level().addFreshEntity(waveEntity);
                  this.mob.chargeWave = 0;
               }
            }
         } else {
            this.resetAttackCooldown();
            this.shouldCountTillNextAttack = false;
            this.mob.SetHardAttacking(false);
            this.mob.attackAnimationTimeout = 0;
         }

      }

      public boolean canContinueToUse() {
         return this.mob.isPinned() ? false : super.canContinueToUse();
      }

      private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
         return pDistToEnemySqr <= this.getAttackReachSqr(pEnemy);
      }

      protected void resetAttackCooldown() {
         this.ticksUntilNextAttack = this.adjustedTickDelay(110);
      }

      protected boolean isTimeToAttack() {
         return this.ticksUntilNextAttack <= 0;
      }

      protected boolean isTimeToStartAttackAnimation() {
         return this.ticksUntilNextAttack <= 55;
      }

      protected int getTicksUntilNextAttack() {
         return this.ticksUntilNextAttack;
      }

      protected void performAttack(LivingEntity pEnemy) {
         this.resetAttackCooldown();
         this.mob.swing(InteractionHand.MAIN_HAND);
         this.mob.setPinned(true);
         pEnemy.startRiding(this.mob);
         this.mob.doHurtTarget(pEnemy);
      }

      public void tick() {
         super.tick();
         if (this.shouldCountTillNextAttack) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
         }

      }

      public void stop() {
         this.mob.SetHardAttacking(false);
         super.stop();
      }
   }

   static class GrabTarget extends Goal {
      Umarmer umarmer;
      int damage;

      public GrabTarget(Umarmer umarmer1) {
         this.umarmer = umarmer1;
      }

      public boolean canUse() {
         if (this.umarmer.isVehicle() && !this.umarmer.isPinned()) {
            return true;
         } else {
            return this.umarmer.getTarget() != null && this.umarmer.random.nextInt(15) == 0;
         }
      }

      public void start() {
         super.start();
         if (this.umarmer.getTarget() != null && !this.umarmer.isVehicle() && this.umarmer.distanceToSqr(this.umarmer.getTarget()) < (double)5.0F) {
            this.umarmer.getTarget().startRiding(this.umarmer);
         }

      }

      public void tick() {
         super.tick();
         Entity target = this.umarmer.getTarget();
         if (target != null && this.umarmer.getFirstPassenger() == target) {
            if (this.damage >= 10) {
               this.umarmer.doHurtTarget(target);
               this.damage = 0;
            } else {
               ++this.damage;
            }
         }

      }
   }
}
