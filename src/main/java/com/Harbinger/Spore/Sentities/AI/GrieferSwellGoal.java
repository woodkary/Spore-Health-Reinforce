package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Griefer;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class GrieferSwellGoal extends Goal {
   private final Griefer griefer;
   @Nullable
   private LivingEntity target;

   public GrieferSwellGoal(Griefer griefer1) {
      this.griefer = griefer1;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      LivingEntity livingentity = this.griefer.getTarget();
      return this.griefer.getSwellDir() > 0 || livingentity != null && this.griefer.distanceToSqr(livingentity) < (double)9.0F && this.griefer.getHealth() <= this.griefer.getMaxHealth() / 2.0F;
   }

   public void start() {
      this.target = this.griefer.getTarget();
   }

   public void stop() {
      this.target = null;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      if (this.target == null) {
         this.griefer.setSwellDir(-1);
      } else if (this.griefer.distanceToSqr(this.target) > (double)49.0F) {
         this.griefer.setSwellDir(-1);
      } else if (!this.griefer.getSensing().hasLineOfSight(this.target)) {
         this.griefer.setSwellDir(-1);
      } else {
         this.griefer.setSwellDir(1);
      }

   }
}
