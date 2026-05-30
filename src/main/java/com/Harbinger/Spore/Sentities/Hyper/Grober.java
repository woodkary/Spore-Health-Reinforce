package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.LeapGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Grober extends Hyper implements ArmorPersentageBypass {
   public static final EntityDataAccessor ATTACK_TYPE;
   public static final EntityDataAccessor RAVAGE_COOLDOWN;
   public static final EntityDataAccessor RAVAGE_TIME;
   private int attackAnimationTick;
   public AnimationState kickAnimation = new AnimationState();

   public Grober(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new WallClimberNavigation(this, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.grober_loot.get();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ATTACK_TYPE, 0);
      this.entityData.define(RAVAGE_COOLDOWN, 0);
      this.entityData.define(RAVAGE_TIME, 0);
   }

   public void setRavageTime(int val) {
      this.entityData.set(RAVAGE_TIME, val);
   }

   public void addRavageTime() {
      this.entityData.set(RAVAGE_TIME, (Integer)this.entityData.get(RAVAGE_TIME) + 1);
   }

   public int getRavageTime() {
      return (Integer)this.entityData.get(RAVAGE_TIME);
   }

   protected int calculateFallDamage(float v1, float v2) {
      if (v1 >= 8.0F) {
         this.damageStomp(this.level(), this.getOnPos(), (double)7.0F, true);
      }

      return 0;
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("ravage", (Integer)this.entityData.get(RAVAGE_COOLDOWN));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(RAVAGE_COOLDOWN, tag.getInt("ravage"));
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (this.getMeleeState() == MELEE_STATES.SMASH) {
            this.damageStomp(this.level(), entity.getOnPos(), (double)3.0F, false);
         }

         if (this.getMeleeState() == MELEE_STATES.KICK) {
            living.hurtMarked = true;
            living.knockback((double)3.0F, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.playSound((SoundEvent)Ssounds.GROBER_KICK.get());
         }

         if (this.getMeleeState() == MELEE_STATES.RIGHT_SLAP || this.getMeleeState() == MELEE_STATES.LEFT_SLAP) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
            this.playSound((SoundEvent)Ssounds.GROBER_SLAP.get());
         }
      }

      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new Ravage(this));
      this.goalSelector.addGoal(2, new LeapGoal(this, 0.8F) {
         public boolean canUse() {
            return super.canUse() && !this.mob.isInWater();
         }

         public boolean canContinueToUse() {
            return super.canContinueToUse() && !this.mob.isInWater();
         }

         public void start() {
            Grober.this.triggerAnimation(MELEE_STATES.SMASH.value);
            super.start();
            this.mob.level().broadcastEntityEvent(this.mob, (byte)4);
         }
      });
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected void checkAndPerformAttack(LivingEntity entity, double val) {
            double d0 = this.getAttackReachSqr(entity);
            if (val <= d0 && this.ticksUntilNextAttack <= 0 && this.mob.hasLineOfSight(entity)) {
               Grober.this.triggerAnimation(((MELEE_STATES)Util.getRandom(MELEE_STATES.values(), this.mob.getRandom())).getValue());
               this.resetAttackCooldown();
               this.mob.swing(InteractionHand.MAIN_HAND);
               this.mob.doHurtTarget(entity);
               AABB hitbox = entity.getBoundingBox().inflate(this.box);

               for(LivingEntity en : entity.level().getEntitiesOfClass(LivingEntity.class, hitbox, this.victims)) {
                  this.mob.doHurtTarget(en);
               }
            }

         }
      });
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.grober_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.grober_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.grober_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.getRavageTime() > 0) {
         amount *= 0.5F;
      }

      return super.hurt(source, amount);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.attackAnimationTick > 0 && this.getMeleeState() == MELEE_STATES.KICK && !this.kickAnimation.isStarted()) {
            this.kickAnimation.start(this.tickCount);
         }

         if (this.attackAnimationTick <= 0 && this.kickAnimation.isStarted()) {
            this.kickAnimation.stop();
         }
      }

      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if ((Integer)this.entityData.get(RAVAGE_COOLDOWN) > 0) {
         this.entityData.set(RAVAGE_COOLDOWN, (Integer)this.entityData.get(RAVAGE_COOLDOWN) - 1);
      }

   }

   public boolean isOmniMan() {
      return Objects.equals(this.getCustomName(), Component.literal("Omni-Man")) || Objects.equals(this.getCustomName(), Component.literal("Nolan"));
   }

   protected SoundEvent getAmbientSound() {
      return this.isOmniMan() ? (SoundEvent)Ssounds.OMNI_AMBIENT.get() : (SoundEvent)Ssounds.GROBER_AMBIENT.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void damageStomp(Level level, BlockPos pos, double range, boolean fall) {
      if (level instanceof ServerLevel serverLevel) {
         for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
            for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
               for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
                  double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
                  if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < range + (double)0.5F) {
                     BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                     BlockState state = level.getBlockState(blockpos);
                     boolean airBelow = level.getBlockState(blockpos.below()).isAir();
                     double breakSpeed = (double)state.getDestroySpeed(level, pos);
                     if (airBelow && state.getDestroySpeed(level, pos) >= 0.0F && breakSpeed <= (double)this.getBreaking() && Math.random() < 0.3 && !state.isAir()) {
                        FallingBlockEntity.fall(serverLevel, blockpos, state);
                        serverLevel.removeBlock(blockpos, false);
                     }
                  }
               }
            }
         }
      }

      this.playSound(fall ? (SoundEvent)Ssounds.LANDING.get() : (SoundEvent)Ssounds.GROBER_SMASH.get());
   }

   public MELEE_STATES getMeleeState() {
      return MELEE_STATES.byId((Integer)this.entityData.get(ATTACK_TYPE) & 255);
   }

   public void triggerAnimation(int states) {
      this.entityData.set(ATTACK_TYPE, states);
   }

   public float amountOfDamage(float value) {
      return this.getMeleeState() == MELEE_STATES.KICK ? value / 2.0F : 0.0F;
   }

   static {
      ATTACK_TYPE = SynchedEntityData.defineId(Grober.class, EntityDataSerializers.INT);
      RAVAGE_COOLDOWN = SynchedEntityData.defineId(Grober.class, EntityDataSerializers.INT);
      RAVAGE_TIME = SynchedEntityData.defineId(Grober.class, EntityDataSerializers.INT);
   }

   public static enum MELEE_STATES {
      SMASH(0),
      KICK(1),
      RIGHT_SLAP(2),
      LEFT_SLAP(3);

      private final int value;
      private static final MELEE_STATES[] BY_ID = (MELEE_STATES[])Arrays.stream(values()).sorted(Comparator.comparingInt(MELEE_STATES::getValue)).toArray((x$0) -> new MELEE_STATES[x$0]);

      private MELEE_STATES(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }

      public static MELEE_STATES byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      // $FF: synthetic method
      private static MELEE_STATES[] $values() {
         return new MELEE_STATES[]{SMASH, KICK, RIGHT_SLAP, LEFT_SLAP};
      }
   }

   public static class Ravage extends Goal {
      private final Grober mob;
      private LivingEntity target;
      private static final int MAX_CHARGE_TIME = 20;
      private static final double CHARGE_SPEED = 1.6;
      private static final double RANGE = (double)6.0F;

      public Ravage(Grober mob) {
         this.mob = mob;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         if ((Integer)this.mob.entityData.get(Grober.RAVAGE_COOLDOWN) > 0) {
            return false;
         } else {
            this.target = this.mob.getTarget();
            if (this.target != null && this.target.isAlive() && !this.mob.isInWater()) {
               double distance = (double)this.mob.distanceTo(this.target);
               return distance > (double)3.0F && distance <= (double)6.0F;
            } else {
               return false;
            }
         }
      }

      public void start() {
         this.mob.playSound((SoundEvent)Ssounds.GROBER_CHARGE.get());
         this.mob.setRavageTime(0);
         this.mob.entityData.set(Grober.RAVAGE_COOLDOWN, 200);
      }

      public boolean canContinueToUse() {
         return this.mob.getRavageTime() < 20 && this.target != null && this.target.isAlive();
      }

      public void tick() {
         this.mob.addRavageTime();
         if (this.target != null) {
            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            Vec3 direction = (new Vec3(this.target.getX() - this.mob.getX(), (double)0.0F, this.target.getZ() - this.mob.getZ())).normalize();
            this.mob.setDeltaMovement(direction.scale(1.6));
            AABB hitbox = this.mob.getBoundingBox().inflate((double)1.0F);
            List<LivingEntity> victims = this.mob.level().getEntitiesOfClass(LivingEntity.class, hitbox, (e) -> Utilities.TARGET_SELECTOR.Test(e));
            float damage = (float)this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.25F;

            for(LivingEntity living : victims) {
               this.mob.playSound((SoundEvent)Ssounds.GROBER_CHOKE.get());
               living.hurt(this.mob.damageSources().mobAttack(this.mob), damage);
               living.knockback((double)1.2F, (double)Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F))));
            }

         }
      }

      public void stop() {
         this.mob.setRavageTime(0);
         this.mob.setDeltaMovement(Vec3.ZERO);
      }
   }
}
