package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Braionmil;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class BraionmilSwellGoal extends Goal {
   public final Braionmil braionmil;
   private final double speedModifier;

   public BraionmilSwellGoal(Braionmil braionmil, double speedModifier) {
      this.braionmil = braionmil;
      this.speedModifier = speedModifier;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      return this.braionmil.getTarget() != null && this.braionmil.distanceToSqr(this.braionmil.getTarget()) < (double)80.0F;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      if (this.braionmil.getTarget() != null) {
         this.braionmil.getLookControl().setLookAt(this.braionmil.getTarget(), 10.0F, (float)this.braionmil.getMaxHeadXRot());
         this.braionmil.getNavigation().moveTo(this.braionmil.getTarget(), this.speedModifier);
         if (this.braionmil.getTarget() == null) {
            this.braionmil.setSwellDir(-1);
         } else if (this.braionmil.distanceToSqr(this.braionmil.getTarget()) > (double)49.0F) {
            this.braionmil.setSwellDir(-1);
         } else if (!this.braionmil.getSensing().hasLineOfSight(this.braionmil.getTarget())) {
            this.braionmil.setSwellDir(-1);
         } else {
            this.braionmil.setSwellDir(1);
         }
      }

   }
}
