package com.Harbinger.Spore.Sentities.AI.LocHiv;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

public class LocalTargettingGoal extends Goal {
   private final Infected mob;

   public LocalTargettingGoal(Infected mob) {
      this.mob = mob;
   }

   public boolean canUse() {
      return (this.mob.getTarget() != null || this.mob.getSearchPos() != null) && this.mob.getLinked() && this.mob.getRandom().nextInt(10) == 0;
   }

   public boolean canContinueToUse() {
      return (this.mob.getTarget() != null || this.mob.getSearchPos() != null) && this.mob.getLinked();
   }

   public void start() {
      super.start();
      this.Targeting(this.mob);
   }

   public void Targeting(Entity entity) {
      double range;
      if (this.mob.getAttributeBaseValue(Attributes.FOLLOW_RANGE) < (double)32.0F) {
         range = this.mob.getAttributeBaseValue(Attributes.FOLLOW_RANGE);
      } else {
         range = (double)32.0F;
      }

      AABB boundingBox = entity.getBoundingBox().inflate(range);

      for(Infected livingEntity : entity.level().getEntitiesOfClass(Infected.class, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (livingEntity.getTarget() == null && this.mob.getTarget() != null && this.mob.getTarget().isAlive() && !this.mob.getTarget().isInvulnerable()) {
            livingEntity.setTarget(this.mob.getTarget());
         } else if (livingEntity.getSearchPos() == null && this.mob.getSearchPos() != null) {
            livingEntity.setSearchPos(this.mob.getSearchPos());
         }
      }

   }
}
