package com.Harbinger.Spore.Sentities.AI.CalamitiesAI;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.Organoids.Vigil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;

public class CalamityVigilCall extends Goal {
   private final Calamity calamity;

   public CalamityVigilCall(Calamity calamity) {
      this.calamity = calamity;
   }

   public boolean canUse() {
      return this.calamity.isAlive() && this.calamity.getHealth() < this.calamity.getMaxHealth() / 2.0F && this.calamity.getRandom().nextInt(200) == 0 && this.calamity.getTarget() != null;
   }

   public void start() {
      super.start();
      RandomSource randomSource = RandomSource.create();
      Vigil vigil = new Vigil((EntityType)Sentities.VIGIL.get(), this.calamity.level());
      vigil.setProto(this.calamity);
      vigil.tickEmerging();
      vigil.randomTeleport(this.calamity.getX() + (double)randomSource.nextInt(-20, 20), this.calamity.getY(), this.calamity.getZ() + (double)randomSource.nextInt(-20, 20), false);
      this.calamity.level().addFreshEntity(vigil);
   }
}
