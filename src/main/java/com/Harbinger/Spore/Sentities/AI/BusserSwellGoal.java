package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import com.Harbinger.Spore.Sentities.Variants.BusserVariants;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class BusserSwellGoal extends Goal {
   private final Busser busser;
   @Nullable
   private LivingEntity target;

   public BusserSwellGoal(Busser busser1) {
      this.busser = busser1;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      LivingEntity livingentity = this.busser.getTarget();
      return this.busser.getVariant() == BusserVariants.BOMBER && (this.busser.getSwellDir() > 0 || livingentity != null && this.busser.distanceToSqr(livingentity) < (double)9.0F && this.busser.getHealth() <= this.busser.getMaxHealth() / 2.0F);
   }

   public void start() {
      this.target = this.busser.getTarget();
   }

   public void stop() {
      this.target = null;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void tick() {
      if (this.target == null) {
         this.busser.setSwellDir(-1);
      } else if (this.busser.distanceToSqr(this.target) > (double)49.0F) {
         this.busser.setSwellDir(-1);
      } else if (!this.busser.getSensing().hasLineOfSight(this.target)) {
         this.busser.setSwellDir(-1);
      } else {
         this.busser.setSwellDir(1);
      }

   }
}
