package com.Harbinger.Spore.Sentities.MovementControls;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class SmoothLookControl extends LookControl {
   private final Mob mob;
   private final float maxYawChange;
   private final float maxPitchChange;
   private final float smoothing;

   public SmoothLookControl(Mob mob, float maxYawChange, float maxPitchChange, float smoothing) {
      super(mob);
      this.mob = mob;
      this.maxYawChange = maxYawChange;
      this.maxPitchChange = maxPitchChange;
      this.smoothing = smoothing;
   }

   public void tick() {
      super.tick();
      float currentYaw = this.mob.getYRot();
      float currentPitch = this.mob.getXRot();
      float targetYaw = this.mob.yHeadRot;
      float targetPitch = this.mob.getXRot();
      float yawDelta = this.wrapDegrees(targetYaw - currentYaw);
      float pitchDelta = targetPitch - currentPitch;
      yawDelta = this.clamp(yawDelta, -this.maxYawChange, this.maxYawChange);
      pitchDelta = this.clamp(pitchDelta, -this.maxPitchChange, this.maxPitchChange);
      float newYaw = currentYaw + yawDelta * this.smoothing;
      float newPitch = currentPitch + pitchDelta * this.smoothing;
      this.applyRotation(newYaw, newPitch);
   }

   private void applyRotation(float yaw, float pitch) {
      this.mob.setYRot(yaw);
      this.mob.setYHeadRot(yaw);
      this.mob.setYBodyRot(yaw);
      this.mob.setXRot(pitch);
      this.mob.yRotO = yaw;
      this.mob.yHeadRotO = yaw;
      this.mob.yBodyRotO = yaw;
      this.mob.xRotO = pitch;
   }

   private float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   private float wrapDegrees(float degrees) {
      while(degrees > 180.0F) {
         degrees -= 360.0F;
      }

      while(degrees < -180.0F) {
         degrees += 360.0F;
      }

      return degrees;
   }
}
