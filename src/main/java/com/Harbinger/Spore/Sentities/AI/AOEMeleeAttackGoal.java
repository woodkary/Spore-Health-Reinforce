package com.Harbinger.Spore.Sentities.AI;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.Hyper.Inquisitor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

public class AOEMeleeAttackGoal extends Goal implements ASMSetHealthMeleeAttackGoal {
   protected final PathfinderMob mob;
   protected Predicate<LivingEntity> victims;
   private final double speedModifier;
   private final boolean followingTargetEvenIfNotSeen;
   private Path path;
   private double pathedTargetX;
   private double pathedTargetY;
   private double pathedTargetZ;
   private int ticksUntilNextPathRecalculation;
   protected int ticksUntilNextAttack;
   private final int attackInterval;
   private long lastCanUseCheck;
   private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
   private int failedPathFindingPenalty;
   private final boolean canPenalize;
   public double box;
   public float ranged;

   public AOEMeleeAttackGoal(PathfinderMob mob, double speed, boolean p_25554_, double hitbox, float range, Predicate<LivingEntity> targets) {
      this.attackInterval = 20;
      this.failedPathFindingPenalty = 0;
      this.canPenalize = false;
      this.victims = targets;
      this.box = hitbox;
      this.ranged = range;
      this.mob = mob;
      this.speedModifier = speed;
      this.followingTargetEvenIfNotSeen = p_25554_;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public AOEMeleeAttackGoal(PathfinderMob mob, double speed, boolean p_25554_, double hitbox, float range) {
      this(mob, speed, p_25554_, hitbox, range, (entity) -> true);
   }

   protected double getAttackReachSqr(LivingEntity entity) {
      return (double)(this.mob.getBbWidth() + this.ranged);
   }

   public boolean canUse() {
      long i = this.mob.level().getGameTime();
      if (i - this.lastCanUseCheck < 20L) {
         return false;
      } else {
         this.lastCanUseCheck = i;
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity == null) {
            return false;
         } else if (!livingentity.isAlive()) {
            return false;
         } else {
            this.path = this.mob.getNavigation().createPath(livingentity, 0);
            if (this.path != null) {
               return true;
            } else {
               return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            }
         }
      }
   }

   public boolean canContinueToUse() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity == null) {
         return false;
      } else if (!livingentity.isAlive()) {
         return false;
      } else if (!this.followingTargetEvenIfNotSeen) {
         return !this.mob.getNavigation().isDone();
      } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
         return false;
      } else {
         return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
      }
   }

   public void start() {
      this.mob.getNavigation().moveTo(this.path, this.speedModifier);
      this.mob.setAggressive(true);
      this.ticksUntilNextPathRecalculation = 0;
      this.ticksUntilNextAttack = 0;
   }

   public void stop() {
      LivingEntity livingentity = this.mob.getTarget();
      if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
         this.mob.setTarget((LivingEntity)null);
      }

      this.mob.setAggressive(false);
      this.mob.getNavigation().stop();
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity != null) {
         this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
         double d0 = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
         this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
         if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == (double)0.0F && this.pathedTargetY == (double)0.0F && this.pathedTargetZ == (double)0.0F || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= (double)1.0F || this.mob.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            Objects.requireNonNull(this);
            if (d0 > (double)1024.0F) {
               this.ticksUntilNextPathRecalculation += 10;
            } else if (d0 > (double)256.0F) {
               this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
               this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
         }

         this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
         tickASMAttack();
         this.checkAndPerformAttack(livingentity, d0);
      }

   }

   protected void checkAndPerformAttack(LivingEntity entity, double p_25558_) {
      double d0 = this.getAttackReachSqr(entity);
      if (p_25558_ <= d0 && this.ticksUntilNextAttack <= 0 && this.mob.hasLineOfSight(entity)) {
         this.resetAttackCooldown();
         this.mob.swing(InteractionHand.MAIN_HAND);
         this.mob.doHurtTarget(entity);
         AABB hitbox = entity.getBoundingBox().inflate(this.box);

         for(LivingEntity en : entity.level().getEntitiesOfClass(LivingEntity.class, hitbox, this.victims)) {
            if(!SporeJudge.isSporeEntity(en)){
               float attackDamage = (float) this.mob.attributes.getValue(Attributes.ATTACK_DAMAGE);
               attackDamage+=this.mob.entityData.get(Inquisitor.DAMAGE_BONUS);
               SporeAttackUtil.INSTANCE.attack(en, this.mob, attackDamage);
            }
            this.mob.doHurtTarget(en);
         }
      }

   }
   @Override
   public Mob mob() {
      return this.mob;
   }

   @Override
   public double attackReachSqr(LivingEntity target) {
      return getAttackReachSqr(target);
   }

   @Override
   public int ticksUntilNextAttack() {
      return ticksUntilNextAttack;
   }
   protected void resetAttackCooldown() {
      this.ticksUntilNextAttack = this.adjustedTickDelay(20);
   }

   protected boolean isTimeToAttack() {
      return this.ticksUntilNextAttack <= 0;
   }

   protected int getTicksUntilNextAttack() {
      return this.ticksUntilNextAttack;
   }

   protected int getAttackInterval() {
      return this.adjustedTickDelay(20);
   }
}
