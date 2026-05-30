package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import com.Harbinger.Spore.Sentities.Variants.BusserVariants;
import net.minecraft.world.entity.ai.goal.Goal;

public class PhayerGrabAndDropTargets extends Goal {
   private final Busser busser;

   public PhayerGrabAndDropTargets(Busser busser) {
      this.busser = busser;
   }

   public boolean canUse() {
      return this.busser.getTarget() != null && this.busser.getVariant() == BusserVariants.ENHANCED && !this.busser.isVehicle() && this.busser.getRandom().nextInt(15) == 0;
   }

   public void tick() {
      super.tick();
      if (this.busser.getTarget() != null && this.busser.distanceToSqr(this.busser.getTarget()) < (double)5.0F) {
         this.busser.getTarget().startRiding(this.busser);
      }

   }
}
