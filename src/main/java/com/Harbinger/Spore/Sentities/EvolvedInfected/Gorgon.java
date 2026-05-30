package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class Gorgon extends EvolvedInfected {
   private static final EntityDataAccessor TARGET;
   private static final EntityDataAccessor SPORES;
   private int attackAnimationTick;
   private int mouthAnimationTick;
   private int mouthAnimationTimer;

   public Gorgon(EntityType type, Level level) {
      super(type, level);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new GorgonHybridAttackGoal(this, 1.3, true) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_gorgon_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.gorgon_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.gorgon_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.gorgon_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TARGET, -1);
      this.entityData.define(SPORES, 0.0F);
   }

   public boolean spooky() {
      return this.getName().equals(Component.literal("Spooky"));
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      LocalDate localdate = LocalDate.now();
      int j = localdate.get(ChronoField.MONTH_OF_YEAR);
      if (j == 10 && Math.random() < (double)0.5F) {
         this.setCustomName(Component.literal("Spooky"));
      }

      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   public void setTargetId(int e) {
      this.entityData.set(TARGET, e);
   }

   public int getTargetId() {
      return (Integer)this.entityData.get(TARGET);
   }

   public void setSpores(float spores) {
      this.entityData.set(SPORES, spores);
   }

   public float getSpores() {
      return (Float)this.entityData.get(SPORES);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WITCH_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else if (value == 5) {
         this.mouthAnimationTimer = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public void tick() {
      super.tick();
      if (this.tickCount % 5 == 0 && this.getSpores() <= 10.0F) {
         this.setSpores(this.getSpores() + 0.1F);
      }

   }

   public boolean hasLineOfSight(Entity entity) {
      return entity.isInWater() ? false : super.hasLineOfSight(entity);
   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.mouthAnimationTimer > 0) {
         --this.mouthAnimationTimer;
      }

      if (this.mouthAnimationTimer > 0) {
         if (this.mouthAnimationTick < 10) {
            ++this.mouthAnimationTick;
         }
      } else if (this.mouthAnimationTick > 0) {
         --this.mouthAnimationTick;
      }

   }

   public int getMouthAnimationTick() {
      return this.mouthAnimationTick;
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public void activateMouth() {
      this.mouthAnimationTimer = 10;
   }

   static {
      TARGET = SynchedEntityData.defineId(Gorgon.class, EntityDataSerializers.INT);
      SPORES = SynchedEntityData.defineId(Gorgon.class, EntityDataSerializers.FLOAT);
   }

   private static class GorgonHybridAttackGoal extends Goal {
      private final Gorgon gorgon;
      private final double meleeSpeedModifier;
      private final boolean followingTargetEvenIfNotSeen;
      private Path path;
      private double pathedTargetX;
      private double pathedTargetY;
      private double pathedTargetZ;
      private int ticksUntilNextPathRecalculation;
      private int ticksUntilNextAttack;
      private int ticksUntilNextRangedAttack;
      private static final int ATTACK_INTERVAL = 20;
      private static final int RANGED_ATTACK_INTERVAL = 20;
      private long lastCanUseCheck;
      private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
      private int failedPathFindingPenalty = 0;
      private boolean canPenalize = false;
      private static final double RANGED_ATTACK_RANGE = (double)25.0F;
      private boolean canShoot;

      public GorgonHybridAttackGoal(Gorgon gorgon, double meleeSpeedModifier, boolean followingTargetEvenIfNotSeen) {
         this.gorgon = gorgon;
         this.meleeSpeedModifier = meleeSpeedModifier;
         this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         long i = this.gorgon.level().getGameTime();
         if (i - this.lastCanUseCheck < 20L) {
            return false;
         } else {
            this.lastCanUseCheck = i;
            LivingEntity target = this.gorgon.getTarget();
            if (target == null) {
               return false;
            } else if (!target.isAlive()) {
               return false;
            } else if (this.gorgon.getSpores() > 6.0F && this.canRangedAttack(target)) {
               return true;
            } else if (this.canPenalize) {
               if (--this.ticksUntilNextPathRecalculation <= 0) {
                  this.path = this.gorgon.getNavigation().createPath(target, 0);
                  this.ticksUntilNextPathRecalculation = 4 + this.gorgon.getRandom().nextInt(7);
                  return this.path != null;
               } else {
                  return true;
               }
            } else {
               this.path = this.gorgon.getNavigation().createPath(target, 0);
               if (this.path != null) {
                  return true;
               } else {
                  return this.getAttackReachSqr(target) >= this.gorgon.distanceToSqr(target.getX(), target.getY(), target.getZ());
               }
            }
         }
      }

      public boolean canContinueToUse() {
         LivingEntity target = this.gorgon.getTarget();
         if (target == null) {
            return false;
         } else if (!target.isAlive()) {
            return false;
         } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.gorgon.getNavigation().isDone();
         } else if (!this.gorgon.isWithinRestriction(target.blockPosition())) {
            return false;
         } else {
            return !(target instanceof Player) || !target.isSpectator() && !((Player)target).isCreative();
         }
      }

      public void start() {
         this.gorgon.getNavigation().moveTo(this.path, this.meleeSpeedModifier);
         this.gorgon.setAggressive(true);
         this.ticksUntilNextPathRecalculation = 0;
         this.ticksUntilNextAttack = 0;
         this.ticksUntilNextRangedAttack = 0;
      }

      public void stop() {
         LivingEntity target = this.gorgon.getTarget();
         if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            this.gorgon.setTarget((LivingEntity)null);
         }

         this.gorgon.setAggressive(false);
         this.gorgon.getNavigation().stop();
         this.gorgon.setTargetId(-1);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void tick() {
         LivingEntity target = this.gorgon.getTarget();
         if (target != null) {
            this.gorgon.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceToTarget = this.gorgon.distanceToSqr(target.getX(), target.getY(), target.getZ());
            double distance = Math.sqrt(distanceToTarget);
            if (!this.canShoot && this.gorgon.getSpores() > 6.0F) {
               this.gorgon.playSound((SoundEvent)Ssounds.GORGON_SPEW.get());
               this.canShoot = true;
            }

            if (this.canShoot && this.gorgon.getSpores() <= 0.0F) {
               this.canShoot = false;
            }

            boolean shouldUseRanged = this.canShoot && distance <= (double)25.0F && this.hasLineOfSight(target);
            if (shouldUseRanged) {
               this.ticksUntilNextRangedAttack = Math.max(this.ticksUntilNextRangedAttack - 1, 0);
               this.gorgon.level().broadcastEntityEvent(this.gorgon, (byte)5);
               this.gorgon.activateMouth();
               this.gorgon.setSpores(this.gorgon.getSpores() - 0.1F);
               if (this.ticksUntilNextRangedAttack == 0) {
                  this.performRangedAttack(target);
                  this.ticksUntilNextRangedAttack = this.adjustedTickDelay(20);
               }

               this.gorgon.getNavigation().stop();
            } else {
               this.gorgon.setTargetId(-1);
               this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
               this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
               if ((this.followingTargetEvenIfNotSeen || this.gorgon.getSensing().hasLineOfSight(target)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == (double)0.0F && this.pathedTargetY == (double)0.0F && this.pathedTargetZ == (double)0.0F || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= (double)1.0F || this.gorgon.getRandom().nextFloat() < 0.05F)) {
                  this.pathedTargetX = target.getX();
                  this.pathedTargetY = target.getY();
                  this.pathedTargetZ = target.getZ();
                  this.ticksUntilNextPathRecalculation = 4 + this.gorgon.getRandom().nextInt(7);
                  if (this.canPenalize) {
                     this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;
                     if (this.gorgon.getNavigation().getPath() != null) {
                        Node finalPathPoint = this.gorgon.getNavigation().getPath().getEndNode();
                        if (finalPathPoint != null && target.distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z) < (double)1.0F) {
                           this.failedPathFindingPenalty = 0;
                        } else {
                           this.failedPathFindingPenalty += 10;
                        }
                     } else {
                        this.failedPathFindingPenalty += 10;
                     }
                  }

                  if (distanceToTarget > (double)1024.0F) {
                     this.ticksUntilNextPathRecalculation += 10;
                  } else if (distanceToTarget > (double)256.0F) {
                     this.ticksUntilNextPathRecalculation += 5;
                  }

                  if (!this.gorgon.getNavigation().moveTo(target, this.meleeSpeedModifier)) {
                     this.ticksUntilNextPathRecalculation += 15;
                  }

                  this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
               }

               double attackReach = this.getAttackReachSqr(target);
               if (distanceToTarget <= attackReach && this.ticksUntilNextAttack <= 0 && this.hasLineOfSight(target)) {
                  this.resetAttackCooldown(20);
                  this.gorgon.swing(InteractionHand.MAIN_HAND);
                  this.gorgon.doHurtTarget(target);
               }
            }

         }
      }

      private void performRangedAttack(LivingEntity target) {
         if (this.hasLineOfSight(target)) {
            this.gorgon.setTargetId(target.getId());
            if (!target.isBlocking()) {
               target.hurt(this.gorgon.level().damageSources().mobAttack(this.gorgon), (float)((Double)SConfig.SERVER.gorgon_ranged_damage.get() * (double)1.0F));
               this.tryToApply(target, MobEffects.MOVEMENT_SLOWDOWN, 400, 2);
               this.tryToApply(target, (MobEffect)Seffects.MYCELIUM.get(), 400, 1);
               this.tryToApply(target, MobEffects.BLINDNESS, 80, 0);
            }
         }

      }

      private boolean hasLineOfSight(LivingEntity target) {
         return target != null && this.gorgon.hasLineOfSight(target);
      }

      private boolean canRangedAttack(LivingEntity target) {
         return this.hasLineOfSight(target);
      }

      private void tryToApply(LivingEntity living, MobEffect effect, int duration, int amp) {
         if (!living.hasEffect(effect)) {
            living.addEffect(new MobEffectInstance(effect, duration, amp));
         }

      }

      protected void resetAttackCooldown(int value) {
         this.ticksUntilNextAttack = this.adjustedTickDelay(value);
      }

      protected double getAttackReachSqr(LivingEntity target) {
         return (double)(this.gorgon.getBbWidth() * 2.0F * this.gorgon.getBbWidth() * 2.0F + target.getBbWidth());
      }
   }
}
