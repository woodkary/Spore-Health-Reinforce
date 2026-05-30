package com.Harbinger.Spore.Sentities.AI.CalamitiesAI;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

public class SummonScentInCombat extends Goal {
   private final Calamity calamity;

   public SummonScentInCombat(Calamity calamity) {
      this.calamity = calamity;
   }

   public boolean canUse() {
      if (!(Boolean)SConfig.SERVER.scent_spawn.get()) {
         return false;
      } else {
         return this.calamity.isAlive() && this.calamity.getRandom().nextInt(400) == 0 && this.calamity.isAggressive() && this.checkForScent();
      }
   }

   public void start() {
      this.SummonScent();
      this.calamity.setStun(80);
      super.start();
   }

   private void SummonScent() {
      ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), this.calamity.level());
      scent.moveTo(this.calamity.getX(), this.calamity.getY(), this.calamity.getZ());
      this.calamity.level().addFreshEntity(scent);
   }

   private boolean checkForScent() {
      AABB hitbox = this.calamity.getBoundingBox().inflate((double)8.0F);
      List<ScentEntity> entities = this.calamity.level().getEntitiesOfClass(ScentEntity.class, hitbox);
      return entities.size() < 2;
   }
}
