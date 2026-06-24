package com.Harbinger.Spore.Sentities.MovementControls;

import com.Harbinger.Spore.Sentities.WaterInfected;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class WaterXlandMovement extends MoveControl {
   public final Mob mob;

   public WaterXlandMovement(Mob mob) {
      super(mob);
      this.mob = mob;
   }

   public void tick() {
      if (this.mob instanceof WaterInfected && this.mob.isInFluidType()) {
         if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            this.tickWaterMoveTo();
         } else {
            super.tick();
         }

         return;
      }

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
            this.mob.setYya(d1 > (double)0.0F ? f1 : -f1);
         }
      }

      if (this.mob instanceof WaterInfected && this.mob.isInFluidType()) {
         if (this.wantedY > this.mob.getY()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, 0.01, (double)0.0F));
         } else {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F));
         }
      }

   }

   private void tickWaterMoveTo() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedY - this.mob.getY();
      double d2 = this.wantedZ - this.mob.getZ();
      double horizontalDistanceSqr = d0 * d0 + d2 * d2;
      double distanceSqr = horizontalDistanceSqr + d1 * d1;
      if (distanceSqr < (double)2.5000003E-7F) {
         this.mob.setYya(0.0F);
         this.mob.setZza(0.0F);
         return;
      }

      double horizontalDistance = Math.sqrt(horizontalDistanceSqr);
      if (horizontalDistance > (double)1.0E-5F) {
         float targetYaw = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetYaw, 90.0F));
      }

      float speed = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
      this.mob.setSpeed(speed);
      this.mob.setZza(speed);
      this.mob.setYya(Math.abs(d1) > (double)1.0E-5F ? (d1 > (double)0.0F ? speed : -speed) : 0.0F);

      Vec3 deltaMovement = this.mob.getDeltaMovement();
      Vec3 horizontalDirection = horizontalDistance > (double)1.0E-5F ? new Vec3(d0 / horizontalDistance, (double)0.0F, d2 / horizontalDistance) : Vec3.ZERO;
      if (horizontalDirection != Vec3.ZERO) {
         horizontalDirection = this.getWaterSlideDirection(horizontalDirection);
         double acceleration = Mth.clamp((double)speed * 0.35D, 0.025D, 0.12D);
         deltaMovement = deltaMovement.add(horizontalDirection.x * acceleration, (double)0.0F, horizontalDirection.z * acceleration);
         deltaMovement = this.limitHorizontalSpeed(deltaMovement, Mth.clamp((double)speed * 1.2D + 0.03D, 0.08D, 0.25D));
      }

      double verticalAcceleration = Mth.clamp(d1 * 0.03D, -0.03D, 0.06D);
      if (this.mob.horizontalCollision && this.wantedY > this.mob.getY()) {
         verticalAcceleration += 0.05D;
      } else if (this.wantedY <= this.mob.getY()) {
         verticalAcceleration -= 0.01D;
      }

      deltaMovement = deltaMovement.add((double)0.0F, verticalAcceleration, (double)0.0F);
      deltaMovement = new Vec3(deltaMovement.x, Mth.clamp(deltaMovement.y, -0.18D, 0.18D), deltaMovement.z);
      this.mob.setDeltaMovement(deltaMovement);
   }

   private Vec3 getWaterSlideDirection(Vec3 forward) {
      if (!this.mob.horizontalCollision) {
         return forward;
      }

      Vec3 left = new Vec3(-forward.z, (double)0.0F, forward.x);
      Vec3 right = new Vec3(forward.z, (double)0.0F, -forward.x);
      double probeDistance = Math.max((double)this.mob.getBbWidth() * 0.35D, 0.3D);
      boolean leftClear = this.mob.level().noCollision(this.mob, this.mob.getBoundingBox().move(left.x * probeDistance, (double)0.0F, left.z * probeDistance));
      boolean rightClear = this.mob.level().noCollision(this.mob, this.mob.getBoundingBox().move(right.x * probeDistance, (double)0.0F, right.z * probeDistance));
      Vec3 side = leftClear != rightClear ? (leftClear ? left : right) : (this.mob.tickCount / 20 % 2 == 0 ? left : right);
      return forward.scale(0.35D).add(side.scale(0.65D)).normalize();
   }

   private Vec3 limitHorizontalSpeed(Vec3 movement, double maxHorizontalSpeed) {
      double horizontalSpeedSqr = movement.x * movement.x + movement.z * movement.z;
      double maxHorizontalSpeedSqr = maxHorizontalSpeed * maxHorizontalSpeed;
      if (horizontalSpeedSqr <= maxHorizontalSpeedSqr) {
         return movement;
      }

      double scale = maxHorizontalSpeed / Math.sqrt(horizontalSpeedSqr);
      return new Vec3(movement.x * scale, movement.y, movement.z * scale);
   }
}
