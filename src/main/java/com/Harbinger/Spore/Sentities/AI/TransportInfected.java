package com.Harbinger.Spore.Sentities.AI;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;

public class TransportInfected extends Goal {
   public Mob mob;
   private final Class<? extends LivingEntity> partnerClass;
   protected final TargetingConditions partneerT;
   protected Level level;
   protected final double speed;
   @Nullable
   protected LivingEntity partner;

   public TransportInfected(Mob mob, Class<? extends LivingEntity> partnerClass, double speed, @Nullable Predicate<LivingEntity> en) {
      this.mob = mob;
      this.level = mob.level();
      this.partnerClass = partnerClass;
      this.speed = speed;
      this.partneerT = TargetingConditions.forNonCombat().range((double)16.0F).selector(en);
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Nullable
   private LivingEntity getFreePartner() {
      List<? extends LivingEntity> list = this.level.getNearbyEntities(this.partnerClass, this.partneerT, this.mob, this.mob.getBoundingBox().inflate((double)32.0F));
      double d0 = Double.MAX_VALUE;
      LivingEntity inf = null;

      for(LivingEntity inf1 : list) {
         if (this.mob.distanceToSqr(inf1) < d0) {
            inf = inf1;
            d0 = this.mob.distanceToSqr(inf1);
         }
      }

      return inf;
   }

   public boolean canUse() {
      if (this.mob.tickCount % 20 == 0) {
         this.partner = this.getFreePartner();
      }

      return !this.mob.isVehicle() && this.mob.getTarget() == null && this.partner != null;
   }

   public void tick() {
      if (!this.mob.isVehicle() && this.partner != null && !this.partner.isPassenger()) {
         this.mob.getLookControl().setLookAt(this.partner, 10.0F, (float)this.mob.getMaxHeadXRot());
         this.mob.getNavigation().moveTo(this.partner, this.speed);
         if (this.mob.distanceToSqr(this.partner) < (double)9.0F && !this.partner.isPassenger()) {
            this.equip();
         }
      }

   }

   private void equip() {
      assert this.partner != null;

      this.partner.startRiding(this.mob);
   }
}
