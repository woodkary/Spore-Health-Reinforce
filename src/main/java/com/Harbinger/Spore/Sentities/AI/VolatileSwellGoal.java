package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Volatile;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class VolatileSwellGoal extends Goal {
   private final Volatile volat;
   @Nullable
   private LivingEntity target;

   public VolatileSwellGoal(Volatile volat) {
      this.volat = volat;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      LivingEntity livingentity = this.volat.getTarget();
      return this.volat.getSwellDir() > 0 || livingentity != null && this.volat.distanceToSqr(livingentity) < (double)9.0F && this.volat.getHealth() <= this.volat.getMaxHealth() / 4.0F;
   }

   public void start() {
      this.target = this.volat.getTarget();
   }

   public void stop() {
      this.target = null;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      if (this.target == null) {
         this.volat.setSwellDir(-1);
      } else if (this.volat.distanceToSqr(this.target) > (double)49.0F) {
         this.volat.setSwellDir(-1);
      } else if (!this.volat.getSensing().hasLineOfSight(this.target)) {
         this.volat.setSwellDir(-1);
      } else {
         this.volat.setSwellDir(1);
      }

   }
}
