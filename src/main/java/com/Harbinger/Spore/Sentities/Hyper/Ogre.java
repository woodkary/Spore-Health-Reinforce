package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBlockProjectile;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ogre extends Hyper implements RangedAttackMob, ArmorPersentageBypass {
   public static final EntityDataAccessor HAS_IMPALED_BODY;
   private int attackAnimationTick;
   private int attacks;

   public Ogre(EntityType type, Level level) {
      super(type, level);
   }

   public boolean canDoTailAttack() {
      return this.attacks > 2;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.ogre_loot.get();
   }

   public boolean hasImpaledBody() {
      return (Boolean)this.entityData.get(HAS_IMPALED_BODY);
   }

   public void setHasImpaledBody(boolean value) {
      this.entityData.set(HAS_IMPALED_BODY, value);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("impaled_body", this.hasImpaledBody());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setHasImpaledBody(tag.getBoolean("impaled_body"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(HAS_IMPALED_BODY, false);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.ogre_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.ogre_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.ogre_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 7.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return Ogre.this.canDoTailAttack() ? super.getAttackReachSqr(entity) * (double)1.5F : super.getAttackReachSqr(entity);
         }
      });
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 40 == 0 && this.isAggressive()) {
         this.performRangedThrow(this);
      }

      if (this.tickCount % 200 == 0) {
         LivingEntity livingEntity = this.getTarget();
         if (livingEntity != null && this.hasLineOfSight(livingEntity)) {
            this.level().broadcastEntityEvent(this, (byte)4);

            for(int i = 0; i < this.random.nextInt(1, 4); ++i) {
               this.performRangedAttack(livingEntity, 0.0F);
            }
         }
      }

   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (this.canDoTailAttack() && !this.isVehicle() && entity instanceof LivingEntity living) {
         this.attackAnimationTick = 10;
         this.level().broadcastEntityEvent(this, (byte)4);
         living.knockback((double)10.0F, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
         this.attacks = 0;
         living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120));
      } else {
         ++this.attacks;
      }

      if (entity instanceof Player player) {
         if (Math.random() < 0.2) {
            player.startRiding(this);
         }
      }

      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0), this);
      }

      return super.doHurtTarget(entity);
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      if (this.canDoTailAttack() && !this.hasImpaledBody()) {
         this.setHasImpaledBody(true);
      }

      super.awardKillScore(entity, i, damageSource);
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      this.attackAnimationTick = 10;
      BlockState state = this.getBlock();
      if (!this.level().isClientSide && state != null) {
         ThrownBlockProjectile thrownBlockProjectile = new ThrownBlockProjectile(this.level(), this, 10.0F, state, this.TARGET_SELECTOR);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.0F;
         double dz = livingEntity.getZ() - this.getZ();
         thrownBlockProjectile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
         thrownBlockProjectile.shoot(dx, dy - thrownBlockProjectile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 1.0F, 6.0F);
         this.level().addFreshEntity(thrownBlockProjectile);
      }

   }

   public BlockState getBlock() {
      AABB aabb = this.getBoundingBox().inflate(0.2);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.getDestroySpeed(this.level(), blockpos) < 5.0F && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && !blockstate.isAir()) {
            this.level().destroyBlock(blockpos, false);
            return blockstate;
         }
      }

      return null;
   }

   public void performRangedThrow(LivingEntity entity) {
      LivingEntity livingEntity = this.getTarget();
      if (livingEntity != null) {
         this.attackAnimationTick = 10;
         Vec3 vec3 = entity.getDeltaMovement();
         double d0 = entity.getX() + vec3.x - livingEntity.getX();
         double d1 = entity.getEyeY() - (double)1.1F - this.getY();
         double d2 = entity.getZ() + vec3.z - livingEntity.getZ();
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         AABB boundingBox = entity.getBoundingBox().inflate(1.2);

         for(Entity en : entity.level().getEntities(entity, boundingBox)) {
            if (en instanceof Mob && ((List)SConfig.SERVER.can_be_carried.get()).contains(en.getEncodeId())) {
               en.setDeltaMovement(d0 * -0.2, (d1 + d3) * 0.02, d2 * -0.2);
               ((Mob)en).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            }
         }
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public float amountOfDamage(float value) {
      return this.canDoTailAttack() ? value * 0.5F : 0.0F;
   }

   protected void positionRider(Entity entity, MoveFunction function) {
      super.positionRider(entity, function);
      if (entity instanceof Player player) {
         Vec3 vec3 = (new Vec3(1.1, 1.4, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         player.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
      }

   }

   public boolean isVehicle() {
      Entity entity = this.getFirstPassenger();
      if (entity instanceof Player player) {
         player.setPose(Pose.SWIMMING);
      }

      return super.isVehicle();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.OGRE_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_VILLAGER_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   static {
      HAS_IMPALED_BODY = SynchedEntityData.defineId(Ogre.class, EntityDataSerializers.BOOLEAN);
   }
}
