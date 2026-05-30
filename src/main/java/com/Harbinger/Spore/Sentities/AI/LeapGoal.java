package com.Harbinger.Spore.Sentities.AI;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.Vec3;

public class LeapGoal extends Goal {
   protected final Mob mob;
   private LivingEntity target;
   private final float yd;

   public LeapGoal(Mob p_25492_, float p_25493_) {
      this.mob = p_25492_;
      this.yd = p_25493_;
      this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
   }

   public boolean canUse() {
      this.target = this.mob.getTarget();
      if (this.target == null) {
         return false;
      } else if (this.mob.isInWater()) {
         return false;
      } else {
         double d0 = this.mob.distanceToSqr(this.target);
         if (d0 > (double)32.0F) {
            if (!this.mob.onGround()) {
               return false;
            } else {
               return this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
            }
         } else {
            return false;
         }
      }
   }

   public boolean canContinueToUse() {
      return !this.mob.onGround();
   }

   public void tick() {
      if (this.mob.getTarget() != null) {
         this.mob.getLookControl().setLookAt(this.mob.getTarget(), 10.0F, (float)this.mob.getMaxHeadXRot());
      }

   }

   public void start() {
      Vec3 vec3 = this.mob.getDeltaMovement();
      Vec3 vec31 = new Vec3(this.target.getX() - this.mob.getX(), (double)0.0F, this.target.getZ() - this.mob.getZ());
      if (vec31.lengthSqr() > 1.0E-7) {
         vec31 = vec31.normalize().scale((double)2.0F).add(vec3.scale((double)1.5F));
      }

      this.mob.setDeltaMovement(vec31.x + (double)this.yd, (double)this.yd, vec31.z + (double)this.yd);
   }
}
