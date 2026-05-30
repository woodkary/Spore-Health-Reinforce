package com.Harbinger.Spore.Sentities.AI.LocHiv;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;

public class BufferAI extends Goal {
   public Infected infected;

   public BufferAI(Infected infected1) {
      this.infected = infected1;
   }

   public boolean canUse() {
      return this.infected.isAlive() && this.infected.getKills() > 0 && this.infected.getRandom().nextInt(10) == 0;
   }

   public void tick() {
      super.tick();
      if (this.infected.getKills() > 0) {
         if (this.infected.getHealth() < this.infected.getMaxHealth() && !this.infected.hasEffect(MobEffects.REGENERATION)) {
            this.infected.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            this.infected.setKills(this.infected.getKills() - 1);
         }

         if (this.infected.getLastDamageSource() == this.infected.damageSources().drown() && !this.infected.hasEffect(MobEffects.WATER_BREATHING)) {
            this.infected.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0));
            this.infected.setKills(this.infected.getKills() - 1);
         }

         if (this.infected.getRandom().nextInt(40) == 0 && this.infected.getKills() > 2) {
            if (!this.infected.hasEffect(MobEffects.MOVEMENT_SPEED) && this.infected.getTarget() != null && this.infected.distanceToSqr(this.infected.getTarget()) > (double)200.0F) {
               this.infected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
               this.infected.setKills(this.infected.getKills() - 1);
            }

            if (!this.infected.hasEffect(MobEffects.DAMAGE_BOOST) && this.infected.getTarget() != null && this.infected.distanceToSqr(this.infected.getTarget()) < (double)60.0F) {
               this.infected.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0));
               this.infected.setKills(this.infected.getKills() - 1);
            }
         }
      }

   }
}
