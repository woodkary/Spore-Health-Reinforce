package com.Harbinger.Spore.Sentities.BaseEntities.IkUtil;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class IkLeviFin {
   protected final RandomSource randomSource = RandomSource.create();
   protected final LivingEntity owner;
   protected final Vec3[] entities;
   protected final Vec3 defaultBodyOffset;
   protected final Vec3 defaultLimbOffset;
   protected final float maxDistance;
   protected final Vec3[] segmentVelocities;
   protected Vec3 sitPosition = null;
   protected Vec3 lastSitPosition = null;
   protected Vec3 lastOwnerPosition;
   protected Vec3 ownerMovementDelta;
   protected float lastYaw;
   protected float yawDelta;
   protected float swimAngle;
   protected float swimSpeed;

   public IkLeviFin(LivingEntity owner, int amount, Vec3 defaultBodyOffset, Vec3 defaultLimbOffset, float maxDistance) {
      this.lastOwnerPosition = Vec3.ZERO;
      this.ownerMovementDelta = Vec3.ZERO;
      this.lastYaw = 0.0F;
      this.yawDelta = 0.0F;
      this.swimAngle = 0.0F;
      this.swimSpeed = 8.0F;
      this.owner = owner;
      this.entities = new Vec3[amount];
      this.segmentVelocities = new Vec3[amount];

      for(int i = 0; i < amount; ++i) {
         this.entities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
         this.segmentVelocities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
      }

      this.defaultBodyOffset = defaultBodyOffset;
      this.defaultLimbOffset = defaultLimbOffset;
      this.maxDistance = maxDistance;
   }

   public Vec3[] getEntities() {
      return this.entities;
   }

   public Vec3 applyYaw(Vec3 offset) {
      float yawRad = this.owner.getYRot() * ((float)Math.PI / 180F);
      return offset.yRot(-yawRad - ((float)Math.PI / 2F));
   }

   public Vec3 getLegBasePos() {
      Vec3 pivot = this.owner.position();
      Vec3 extend = this.isOwnerMoving() ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : Vec3.ZERO;
      return pivot.add(this.applyYaw(this.defaultLimbOffset.add(extend)));
   }

   public Vec3 getBodyOffset() {
      Vec3 pivot = this.owner.position();
      return pivot.add(this.applyYaw(this.defaultBodyOffset));
   }

   protected Vec3 applySwimCircle(Vec3 baseTipPos) {
      if (!this.isOwnerMoving()) {
         return baseTipPos;
      } else {
         this.swimAngle += this.swimSpeed;
         this.swimAngle = Mth.wrapDegrees(this.swimAngle);
         float rad = this.swimAngle * ((float)Math.PI / 180F);
         if (this.owner.isInWater()) {
            double x = Math.cos((double)rad) * (double)7.0F;
            double y = Math.sin((double)rad) * (double)3.5F;
            Vec3 circularOffset = new Vec3(-x, y, (double)0.0F);
            circularOffset = this.applyYaw(circularOffset);
            return baseTipPos.add(circularOffset);
         } else {
            double x = Math.cos((double)rad) * (double)3.5F;
            Vec3 circularOffset = new Vec3(-x, (double)0.0F, (double)0.0F);
            circularOffset = this.applyYaw(circularOffset);
            return baseTipPos.add(circularOffset);
         }
      }
   }

   protected void moveSegmentTowards(int index, Vec3 target, boolean far) {
      Vec3 currentPos = this.entities[index];
      Vec3 newPos = currentPos.lerp(target, (double)0.5F);
      this.entities[index] = far ? target : newPos;
   }

   protected void moveTipTowards(Vec3 target) {
      int tip = this.entities.length - 1;
      Vec3 currentPos = this.entities[tip];
      this.entities[tip] = currentPos.lerp(target, (double)0.35F);
   }

   protected boolean isOwnerMoving() {
      return this.owner.getDeltaMovement().lengthSqr() > 0.005;
   }

   protected void updateOwnerMovementDelta() {
      Vec3 currentOwnerPos = this.owner.position();
      this.ownerMovementDelta = currentOwnerPos.subtract(this.lastOwnerPosition);
      this.lastOwnerPosition = currentOwnerPos;
      float currentYaw = this.owner.getYRot();
      this.yawDelta = Mth.wrapDegrees(currentYaw - this.lastYaw);
      this.lastYaw = currentYaw;
   }

   protected Vec3 rotateAroundYaw(Vec3 pos, Vec3 pivot, float degrees) {
      double rad = (double)(degrees * ((float)Math.PI / 180F));
      Vec3 rel = pos.subtract(pivot);
      Vec3 rotated = rel.yRot((float)(-rad));
      return pivot.add(rotated);
   }

   protected void applyBodySpin() {
      if (!(Math.abs(this.yawDelta) < 0.001F)) {
         Vec3 pivot = this.owner.position();

         for(int i = 0; i < this.entities.length; ++i) {
            this.entities[i] = this.rotateAroundYaw(this.entities[i], pivot, this.yawDelta);
         }

         if (this.sitPosition != null) {
            this.sitPosition = this.rotateAroundYaw(this.sitPosition, pivot, this.yawDelta);
         }

         if (this.lastSitPosition != null) {
            this.lastSitPosition = this.rotateAroundYaw(this.lastSitPosition, pivot, this.yawDelta);
         }

         for(int i = 0; i < this.segmentVelocities.length; ++i) {
            this.segmentVelocities[i] = this.segmentVelocities[i].yRot(-this.yawDelta * ((float)Math.PI / 180F));
         }

      }
   }

   protected void applyEntityMovementToLegs() {
      if (this.ownerMovementDelta.lengthSqr() < 1.0E-5) {
         Vec3 defaultTipPos = this.getLegBasePos();
         int tip = this.entities.length - 1;
         Vec3 currentPos = this.entities[tip];
         this.entities[tip] = currentPos.lerp(defaultTipPos, (double)0.1F);
      } else {
         int last = this.entities.length - 1;

         for(int i = 0; i < this.entities.length; ++i) {
            float t = (float)i / (float)last;
            float followStrength = Mth.lerp(t, 0.5F, 0.05F);
            float drag = Mth.lerp(t, 0.9F, 0.65F);
            this.segmentVelocities[i] = this.segmentVelocities[i].add(this.ownerMovementDelta.scale((double)followStrength));
            this.segmentVelocities[i] = this.segmentVelocities[i].scale((double)drag);
            this.entities[i] = this.entities[i].add(this.segmentVelocities[i]);
         }

         if (this.sitPosition != null) {
            this.sitPosition = this.sitPosition.add(this.ownerMovementDelta);
         }

         if (this.lastSitPosition != null) {
            this.lastSitPosition = this.lastSitPosition.add(this.ownerMovementDelta);
         }

      }
   }

   public void applyIK() {
      if (this.entities.length != 0) {
         Vec3 basePos = this.getBodyOffset();
         Vec3 defaultTipPos = this.sitPosition == null ? this.getLegBasePos() : this.sitPosition;
         defaultTipPos = this.applySwimCircle(defaultTipPos);
         this.updateOwnerMovementDelta();
         this.applyEntityMovementToLegs();
         this.applyBodySpin();
         this.moveTipTowards(defaultTipPos);

         for(int i = this.entities.length - 2; i >= 0; --i) {
            Vec3 nextPos = this.entities[i + 1];
            Vec3 dir = this.entities[i].subtract(nextPos);
            float segmentLength = 1.0F;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               dir = dir.normalize().scale((double)segmentLength);
            } else {
               dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = nextPos.add(dir);
            this.moveSegmentTowards(i, solvedPos, this.entities[i + 1].distanceTo(this.entities[i]) > (double)5.0F);
         }

         this.entities[0] = basePos;

         for(int i = 1; i < this.entities.length; ++i) {
            Vec3 prevPos = this.entities[i - 1];
            Vec3 dir = this.entities[i].subtract(prevPos);
            float segmentLength = 2.0F;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               dir = dir.normalize().scale((double)segmentLength);
            } else {
               dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = prevPos.add(dir);
            this.moveSegmentTowards(i, solvedPos, this.entities[i - 1].distanceTo(this.entities[i]) > (double)5.0F);
         }

      }
   }
}
