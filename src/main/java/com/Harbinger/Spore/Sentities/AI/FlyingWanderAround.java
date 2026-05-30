package com.Harbinger.Spore.Sentities.AI;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

public class FlyingWanderAround extends Goal {
   public PathfinderMob mob;
   public double speed;

   public FlyingWanderAround(PathfinderMob mob1, double speed) {
      this.mob = mob1;
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      return this.mob.getNavigation().isDone() && this.mob.tickCount % 40 == 0;
   }

   public boolean canContinueToUse() {
      return this.mob.getNavigation().isInProgress();
   }

   public void start() {
      Vec3 vec3 = this.findPos();
      if (vec3 != null) {
         this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speed);
      }

   }

   @Nullable
   private Vec3 findPos() {
      Vec3 vec3 = this.mob.getViewVector(0.0F);
      Vec3 vec32 = HoverRandomPos.getPos(this.mob, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
      return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
   }
}
