package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class BusserFlyAndDrop extends Goal {
   private final Busser busser;
   private final int high;
   public int tryTicks;

   public BusserFlyAndDrop(Busser busser, int high) {
      this.busser = busser;
      this.high = high;
   }

   public boolean canUse() {
      return this.busser.getTarget() != null && this.busser.isVehicle();
   }

   protected void moveMobToBlock() {
      this.busser.getNavigation().moveTo((double)((float)this.busser.getTarget().getX()) + (double)0.5F, this.busser.getTarget().getY() + (double)this.high, (double)((float)this.busser.getTarget().getZ()) + (double)0.5F, (double)1.0F);
   }

   public void start() {
      if (this.busser.getTarget() != null) {
         this.moveMobToBlock();
      }

      this.tryTicks = 0;
      super.start();
   }

   public void tick() {
      super.tick();
      ++this.tryTicks;
      if (this.busser.getTarget() != null) {
         if (this.busser.getSearchPos() != null && this.shouldRecalculatePath()) {
            this.busser.getNavigation().moveTo(this.busser.getTarget().getX(), this.busser.getTarget().getY() + (double)this.high, this.busser.getTarget().getZ(), (double)1.0F);
         }

         if (Math.abs(Math.abs(this.busser.getX()) - Math.abs(this.busser.getTarget().getX())) < (double)3.0F && Math.abs(Math.abs(this.busser.getZ()) - Math.abs(this.busser.getTarget().getZ())) < (double)3.0F) {
            LivingEntity entity = (LivingEntity)this.busser.getFirstPassenger();
            if (entity != null && ((List)SConfig.SERVER.can_be_carried.get()).contains(entity.getEncodeId())) {
               entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1));
               entity.stopRiding();
            }
         }
      }

   }

   public boolean shouldRecalculatePath() {
      return this.tryTicks % 40 == 0;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }
}
