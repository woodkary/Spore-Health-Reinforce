package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.Calamities.Hinderburg;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

public class AerialRangedGoal extends ScatterShotRangedGoal {
   @Nullable
   protected Path path;
   protected final PathNavigation pathNav;

   public AerialRangedGoal(Hinderburg mob, double speed, int interval, float range, int min, int max) {
      super(mob, speed, interval, range, min, max);
      this.pathNav = mob.getNavigation();
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public BlockPos getRandomPosition() {
      int x = this.mob.getBlockX() + this.mob.getRandom().nextInt(-20, 20);
      int y = this.mob.getOnPos().getY();
      int z = this.mob.getBlockZ() + this.mob.getRandom().nextInt(-20, 20);
      return new BlockPos(x, y, z);
   }

   public boolean canUse() {
      if (this.target != null) {
         double d0 = this.mob.distanceToSqr(this.target);
         if (this.mob.getSensing().hasLineOfSight(this.target) && d0 < (double)this.attackRadiusSqr) {
            this.path = this.pathNav.createPath(this.getRandomPosition(), 16);
         } else {
            this.path = this.pathNav.createPath(this.target, 16);
         }
      }

      return super.canUse();
   }

   public void tick() {
      if (this.target != null) {
         double d0 = this.mob.distanceToSqr(this.target);
         boolean flag = this.mob.getSensing().hasLineOfSight(this.target);
         if (d0 / (double)4.0F < (double)this.attackRadiusSqr && d0 / (double)2.0F > (double)this.attackRadiusSqr) {
            this.Orbit(this.target);
         }

         if (this.mob.getY() < this.target.getY() + (double)8.0F || d0 / (double)2.0F < (double)this.attackRadiusSqr) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
         }

         this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
         if (--this.attackTime == 0) {
            Mob var5 = this.mob;
            if (var5 instanceof Hinderburg) {
               Hinderburg hinderburg = (Hinderburg)var5;
               if (hinderburg.tryToSummonNUKE(this.target)) {
                  hinderburg.tickBomb();
               }
            }

            if (!flag) {
               return;
            }

            RandomSource randomSource = RandomSource.create();
            int shot = randomSource.nextInt(this.minShots, this.maxShots + this.getExtraShots());
            float f = (float)Math.sqrt(d0) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);

            for(int i = 0; i < shot; ++i) {
               this.rangedAttackMob.performRangedAttack(this.target, f1);
            }

            this.attackTime = Mth.floor(f * (float)this.attackInterval + (float)this.attackInterval);
         } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(d0) / (double)this.attackRadius, (double)this.attackInterval, (double)this.attackInterval));
         }
      }

   }

   private void Orbit(LivingEntity target) {
      this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply((double)0.0F, (double)1.0F, (double)0.0F).add(target.position().subtract(this.mob.position()).normalize().multiply((double)1.0F, (double)0.0F, (double)1.0F).yRot(90.0F)).scale(this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)1.5F));
   }

   public void start() {
      if (this.mob.tickCount % 40 == 0) {
         this.mob.getNavigation().moveTo(this.path, this.speedModifier);
      }

      super.start();
   }
}
