package com.Harbinger.Spore.Sentities.MovementControls;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class InfectedWallMovementControl extends MoveControl {
   private final Mob mob;

   public InfectedWallMovementControl(Mob mob) {
      super(mob);
      this.mob = mob;
   }

   public void tick() {
      super.tick();
      if (this.mob.getTarget() == null || !(this.mob.getTarget().getY() <= this.mob.getY())) {
         if (this.mob.horizontalCollision) {
            Vec3 initialVec = this.mob.getDeltaMovement();
            Vec3 climbVec = new Vec3(initialVec.x, 0.2, initialVec.z);
            this.mob.setDeltaMovement(climbVec.x * 0.91, climbVec.y * 0.98, climbVec.z * 0.91);
         }

      }
   }
}
