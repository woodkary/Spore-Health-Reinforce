package com.Harbinger.Spore.Sentities.MovementControls;

import com.Harbinger.Spore.Sentities.WaterInfected;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class CalamityMovementControl extends MoveControl {
   private final int maxTurn;

   public CalamityMovementControl(Mob mob, int maxTurn) {
      super(mob);
      this.maxTurn = maxTurn;
   }

   public void tick() {
      super.tick();
      if (this.operation == Operation.MOVE_TO) {
         this.operation = Operation.WAIT;
         double d0 = this.wantedX - this.mob.getX();
         double d1 = this.wantedY - this.mob.getY();
         double d2 = this.wantedZ - this.mob.getZ();
         double d4 = Math.sqrt(d0 * d0 + d2 * d2);
         float f = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, 90.0F));
         float f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
         if (Math.abs(d1) > (double)1.0E-5F || Math.abs(d4) > (double)1.0E-5F) {
            float f2 = (float)(-(Mth.atan2(d1, d4) * (double)(180F / (float)Math.PI)));
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, (float)this.maxTurn));
            this.mob.setYya(d1 > (double)0.0F ? f1 : -f1);
         }
      }

      if (this.mob instanceof WaterInfected && this.mob.isInFluidType()) {
         if (this.mob.tickCount % 10 == 0 && this.mob.horizontalCollision) {
            this.mob.getJumpControl().jump();
         }

         if (this.wantedY > this.mob.getY()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
         } else {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F));
         }
      }

   }
}
