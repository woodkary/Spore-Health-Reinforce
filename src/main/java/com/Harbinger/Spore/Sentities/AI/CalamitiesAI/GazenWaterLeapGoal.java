package com.Harbinger.Spore.Sentities.AI.CalamitiesAI;

import com.Harbinger.Spore.Sentities.Calamities.Gazenbrecher;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.Vec3;

public class GazenWaterLeapGoal extends Goal {
   private final Gazenbrecher gazenbrecher;
   private LivingEntity target;

   public GazenWaterLeapGoal(Gazenbrecher p_25492_) {
      this.gazenbrecher = p_25492_;
      this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
   }

   public boolean canUse() {
      this.target = this.gazenbrecher.getTarget();
      if (this.target == null) {
         return false;
      } else if (this.gazenbrecher.isInFluidType() && this.target.onGround() && !this.target.isInFluidType()) {
         double d0 = this.gazenbrecher.distanceToSqr(this.target);
         if (d0 > (double)32.0F) {
            return this.gazenbrecher.getRandom().nextInt(reducedTickDelay(50)) == 0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean canContinueToUse() {
      return !this.gazenbrecher.isInFluidType();
   }

   public void tick() {
      if (this.target != null) {
         this.gazenbrecher.getLookControl().setLookAt(this.target, 10.0F, (float)this.gazenbrecher.getMaxHeadXRot());
      }

   }

   public void start() {
      Vec3 vec3 = this.gazenbrecher.getDeltaMovement();
      Vec3 vec31 = new Vec3(this.target.getX() - this.gazenbrecher.getX(), (double)0.0F, this.target.getZ() - this.gazenbrecher.getZ());
      if (vec31.lengthSqr() > 1.0E-7) {
         vec31 = vec31.normalize().scale((double)2.0F).add(vec3.scale((double)2.0F));
      }

      this.gazenbrecher.setDeltaMovement(vec31.x, (double)1.3F, vec31.z);
   }
}
