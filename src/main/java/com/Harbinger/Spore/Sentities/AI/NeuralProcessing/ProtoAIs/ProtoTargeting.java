package com.Harbinger.Spore.Sentities.AI.NeuralProcessing.ProtoAIs;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

public class ProtoTargeting extends Goal {
   public Proto proto;

   public ProtoTargeting(Proto p) {
      this.proto = p;
   }

   public boolean canUse() {
      return this.proto.getTarget() != null && this.proto.getRandom().nextInt(0, 5) == 3;
   }

   public boolean canContinueToUse() {
      return this.proto.getTarget() != null;
   }

   public void start() {
      super.start();
      this.Targeting(this.proto);
   }

   public void Targeting(Entity entity) {
      AABB boundingBox = entity.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.proto_range.get());

      for(Entity entity1 : entity.level().getEntities(entity, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (entity1 instanceof Infected infected) {
            if (infected.getTarget() == null && this.proto.getTarget() != null && this.proto.getTarget().isAlive() && !this.proto.getTarget().isInvulnerable()) {
               infected.setTarget(this.proto.getTarget());
            }
         }
      }

   }
}
