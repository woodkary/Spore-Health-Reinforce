package com.Harbinger.Spore.Sentities.AI.LocHiv;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SearchAreaGoal extends Goal {
   public final Infected infected;
   public final double speed;
   public int tryTicks;

   public SearchAreaGoal(Infected infected1, double speed) {
      this.infected = infected1;
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   protected void moveMobToBlock() {
      this.infected.getNavigation().moveTo((double)((float)this.infected.getSearchPos().getX()) + (double)0.5F, (double)(this.infected.getSearchPos().getY() + 1), (double)((float)this.infected.getSearchPos().getZ()) + (double)0.5F, (double)1.0F);
   }

   public boolean canUse() {
      return this.infected.getSearchPos() != null && this.infected.getTarget() == null;
   }

   public void start() {
      this.moveMobToBlock();
      this.tryTicks = 0;
      super.start();
   }

   public boolean canContinueToUse() {
      return this.infected.getTarget() == null;
   }

   public void tick() {
      super.tick();
      ++this.tryTicks;
      if (this.infected.getSearchPos() != null && this.shouldRecalculatePath()) {
         this.infected.getNavigation().moveTo((double)this.infected.getSearchPos().getX(), (double)this.infected.getSearchPos().getY(), (double)this.infected.getSearchPos().getZ(), (double)1.0F);
      }

      if (this.infected.getSearchPos() != null && this.infected.getSearchPos().closerToCenterThan(this.infected.position(), (double)9.0F)) {
         this.infected.setSearchPos((BlockPos)null);
      }

   }

   public boolean shouldRecalculatePath() {
      return this.tryTicks % 40 == 0;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }
}
