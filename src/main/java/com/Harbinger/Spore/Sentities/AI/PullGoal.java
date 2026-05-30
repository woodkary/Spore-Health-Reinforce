package com.Harbinger.Spore.Sentities.AI;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PullGoal extends Goal {
   protected final Mob mob;
   private final double range;
   private final double range_min;

   public PullGoal(Mob mob, double range, double range_min) {
      this.mob = mob;
      this.range = range;
      this.range_min = range_min;
      this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
   }

   public boolean canUse() {
      LivingEntity livingEntity = this.mob.getTarget();
      if (livingEntity != null) {
         double d0 = this.mob.distanceToSqr(livingEntity);
         if (this.mob.hasLineOfSight(livingEntity)) {
            return false;
         }

         if (livingEntity.isBlocking()) {
            return false;
         }

         if (d0 < (double)this.mob.getBbWidth() + this.range && d0 > (double)this.mob.getBbWidth() + this.range_min) {
            if (!livingEntity.onGround()) {
               return false;
            }

            return this.mob.getTarget().getRandom().nextInt(reducedTickDelay(5)) == 0;
         }
      }

      return false;
   }

   public boolean canContinueToUse() {
      if (this.mob.getTarget() != null) {
         return !this.mob.getTarget().onGround();
      } else {
         return false;
      }
   }

   public void tick() {
      if (this.mob.getTarget() != null) {
         this.mob.getLookControl().setLookAt(this.mob.getTarget(), 10.0F, (float)this.mob.getMaxHeadXRot());
      }

   }

   public void start() {
      if (this.mob.getTarget() != null) {
         Vec3 vec3 = this.mob.getTarget().getDeltaMovement();
         Vec3 vec31 = new Vec3(this.mob.getX() - this.mob.getTarget().getX(), this.mob.getY() - this.mob.getTarget().getY(), this.mob.getZ() - this.mob.getTarget().getZ());
         if (vec31.lengthSqr() > 1.0E-7) {
            vec31 = vec31.normalize().scale((double)1.5F).add(vec3.scale((double)0.75F));
         }

         this.mob.swing(InteractionHand.MAIN_HAND);
         this.mob.getTarget().setDeltaMovement(vec31.x, vec31.y, vec31.z);
         if (this.mob.getTarget() instanceof Player) {
            this.mob.getTarget().hurtMarked = true;
         }
      }

   }
}
