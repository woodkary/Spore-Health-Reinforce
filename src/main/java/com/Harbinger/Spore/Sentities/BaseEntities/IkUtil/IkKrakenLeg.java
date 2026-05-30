package com.Harbinger.Spore.Sentities.BaseEntities.IkUtil;

import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class IkKrakenLeg {
   protected final RandomSource randomSource = RandomSource.create();
   protected final Grakensenker owner;
   protected final Vec3[] entities;
   protected int[] segmentVar;
   protected final Vec3 defaultBodyOffset;
   protected final Vec3 defaultLimbOffset;
   protected final float maxDistance;
   protected final float[] wiggleTimers;
   protected final float[] wiggleSpeeds;
   protected final float[] wiggleAmplitudes;
   protected final float[] wiggleOffsets;
   protected final Vec3[] segmentVelocities;
   protected final boolean reverseSpin;
   protected Vec3 sitPosition = null;
   protected Vec3 lastSitPosition = null;
   protected Vec3 lastOwnerPosition;
   protected Vec3 ownerMovementDelta;
   protected float lastYaw;
   protected float yawDelta;
   protected float stepAngle;
   protected float stepSpeed;

   public IkKrakenLeg(Grakensenker owner, int amount, Vec3 defaultBodyOffset, Vec3 defaultLimbOffset, float maxDistance, boolean reverseSpin) {
      this.lastOwnerPosition = Vec3.ZERO;
      this.ownerMovementDelta = Vec3.ZERO;
      this.lastYaw = 0.0F;
      this.yawDelta = 0.0F;
      this.stepAngle = 0.0F;
      this.stepSpeed = 6.0F;
      this.owner = owner;
      this.entities = new Vec3[amount];
      this.segmentVar = new int[amount];
      this.wiggleTimers = new float[amount];
      this.wiggleSpeeds = new float[amount];
      this.wiggleAmplitudes = new float[amount];
      this.wiggleOffsets = new float[amount];
      this.segmentVelocities = new Vec3[amount];
      this.reverseSpin = reverseSpin;

      for(int i = 0; i < amount; ++i) {
         this.entities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
         this.segmentVar[i] = this.randomSource.nextInt(12);
         this.wiggleSpeeds[i] = 0.5F + this.randomSource.nextFloat() * this.getWiggleSpeed();
         this.wiggleAmplitudes[i] = 0.02F + this.randomSource.nextFloat() * this.getWiggleAmplitude();
         this.wiggleOffsets[i] = this.randomSource.nextFloat() * (float)Math.PI * 2.0F;
         this.wiggleTimers[i] = this.randomSource.nextFloat() * 100.0F;
         this.segmentVelocities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
      }

      this.defaultBodyOffset = defaultBodyOffset;
      this.defaultLimbOffset = defaultLimbOffset;
      this.maxDistance = maxDistance;
   }

   public float getWiggleSpeed() {
      return 0.75F;
   }

   public float getWiggleAmplitude() {
      return 0.03F;
   }

   protected void updateWiggleTimers() {
      for(int i = 0; i < this.wiggleTimers.length; ++i) {
         float[] var10000 = this.wiggleTimers;
         var10000[i] += 0.05F * this.wiggleSpeeds[i];
         if (this.wiggleTimers[i] > 1000.0F) {
            var10000 = this.wiggleTimers;
            var10000[i] -= 1000.0F;
         }
      }

   }

   public Vec3 getSitPosition() {
      return this.sitPosition;
   }

   protected void applyIdleWiggle() {
      RandomSource rand = this.randomSource;

      for(int i = 1; i < this.entities.length - 1; ++i) {
         Vec3 current = this.entities[i];
         float time = this.wiggleTimers[i] + this.wiggleOffsets[i];
         float xWiggle = (float)Math.sin((double)(time * 0.7F)) * this.wiggleAmplitudes[i];
         float yWiggle = (float)Math.sin((double)(time * 1.2F + 1.5F)) * this.wiggleAmplitudes[i] * 0.8F;
         float zWiggle = (float)Math.sin((double)(time * 0.9F + 2.0F)) * this.wiggleAmplitudes[i] * 0.6F;
         if (rand.nextFloat() < 0.05F) {
            xWiggle += (rand.nextFloat() - 0.5F) * 0.02F;
            yWiggle += (rand.nextFloat() - 0.5F) * 0.01F;
            zWiggle += (rand.nextFloat() - 0.5F) * 0.02F;
         }

         this.entities[i] = current.add((double)xWiggle, (double)yWiggle, (double)zWiggle);
      }

   }

   public Vec3[] getEntities() {
      return this.entities;
   }

   public Vec3 getLastSitPosition() {
      return this.lastSitPosition;
   }

   public int[] getSegmentVar() {
      return this.segmentVar;
   }

   public void writeVariants(CompoundTag tag, int ikN) {
      tag.putIntArray("variants" + ikN, this.segmentVar);
   }

   public void readVariants(CompoundTag tag, int ikN) {
      this.segmentVar = tag.getIntArray("variants" + ikN);
   }

   public Vec3 applyYaw(Vec3 offset) {
      float yawRad = this.owner.getYRot() * ((float)Math.PI / 180F);
      float spinRad = (float)this.owner.getWaterTicks() * 0.05F;
      return offset.yRot(-yawRad - ((float)Math.PI / 2F) + spinRad);
   }

   public Vec3 getLegBasePos() {
      Vec3 pivot = this.owner.position();
      Vec3 extend = this.isOwnerMoving() ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : Vec3.ZERO;
      return pivot.add(this.applyYaw(this.defaultLimbOffset.add(extend)));
   }

   public Vec3 getBodyOffset() {
      Vec3 pivot = this.owner.position().add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
      return pivot.add(this.applyYaw(this.defaultBodyOffset));
   }

   protected void moveSegmentTowards(int index, Vec3 target, boolean far) {
      Vec3 currentPos = this.entities[index];
      Vec3 newPos = currentPos.lerp(target, this.owner.isInDeepWater() ? (double)0.5F : (double)0.35F);
      this.entities[index] = far ? target : newPos;
   }

   protected void moveTipTowards(Vec3 target) {
      int tip = this.entities.length - 1;
      Vec3 currentPos = this.entities[tip];
      this.entities[tip] = currentPos.lerp(target, (double)0.25F);
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
         boolean inWater = this.owner.isInDeepWater();
         if (!inWater) {
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
   }

   protected void applyEntityMovementToLegs() {
      boolean inWater = this.owner.isInDeepWater();
      if (inWater) {
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
   }

   protected Vec3 applyStepCircle(Vec3 baseTipPos) {
      if (this.isOwnerMoving() && !(this instanceof IkKrakenArm)) {
         this.stepAngle += this.stepSpeed;
         this.stepAngle = Mth.wrapDegrees(this.stepAngle);
         float rad = this.reverseSpin ? -this.stepAngle * ((float)Math.PI / 180F) : this.stepAngle * ((float)Math.PI / 180F);
         double x = Math.cos((double)rad) * (double)4.0F;
         double y = Math.sin((double)rad) * (double)6.0F;
         Vec3 circularOffset = new Vec3(-x, y > (double)-0.5F ? y : (double)0.0F, (double)0.0F);
         circularOffset = this.applyYaw(circularOffset);
         return baseTipPos.add(circularOffset);
      } else {
         return baseTipPos;
      }
   }

   public void applyIK() {
      if (this.entities != null && this.entities.length != 0) {
         Vec3 basePos = this.getBodyOffset();
         Vec3 defaultTipPos = this.sitPosition == null ? this.getLegBasePos() : this.sitPosition;
         defaultTipPos = this.applyStepCircle(defaultTipPos);
         this.updateOwnerMovementDelta();
         this.applyEntityMovementToLegs();
         this.applyBodySpin();
         if (!this.owner.isInDeepWater()) {
            this.moveTipTowards(defaultTipPos);
         }

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
            float segmentLength = 1.0F;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               dir = dir.normalize().scale((double)segmentLength);
            } else {
               dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = prevPos.add(dir);
            this.moveSegmentTowards(i, solvedPos, this.entities[i - 1].distanceTo(this.entities[i]) > (double)5.0F);
         }

         this.applyIdleWiggle();
         this.updateWiggleTimers();
      }
   }

   public void refreshLegStandingPoint() {
      if (!this.owner.isInDeepWater()) {
         if (this.lastSitPosition == null || !(this.getLegBasePos().distanceTo(this.lastSitPosition) < (double)this.maxDistance)) {
            this.sitPosition = this.findStableFooting();
            if (!this.sitPosition.equals(this.lastSitPosition)) {
               this.lastSitPosition = this.sitPosition;
            }

         }
      }
   }

   protected Vec3 findStableFooting() {
      Level level = this.owner.level();
      if (level.isClientSide()) {
         return this.getLegBasePos();
      } else {
         Vec3 worldBasePos = this.getLegBasePos();
         int searchRadius = 6;
         int maxSearchDown = 12;
         int maxSearchUp = 6;
         BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

         for(int y = 0; y >= -maxSearchDown; --y) {
            checkPos.set(worldBasePos.x, worldBasePos.y + (double)y, worldBasePos.z);
            if (this.isSolidGround(level, checkPos)) {
               return new Vec3((double)checkPos.getX() + (double)0.5F, (double)checkPos.getY() - (double)1.0F, (double)checkPos.getZ() + (double)0.5F);
            }
         }

         for(int x = -searchRadius; x <= searchRadius; ++x) {
            for(int z = -searchRadius; z <= searchRadius; ++z) {
               for(int y = maxSearchUp; y >= -maxSearchDown; --y) {
                  checkPos.set(worldBasePos.x + (double)x, worldBasePos.y + (double)y, worldBasePos.z + (double)z);
                  if (this.isSolidGround(level, checkPos) && level.isEmptyBlock(checkPos.above())) {
                     return new Vec3((double)checkPos.getX() + (double)0.5F, (double)checkPos.getY() - (double)1.0F, (double)checkPos.getZ() + (double)0.5F);
                  }
               }
            }
         }

         return worldBasePos;
      }
   }

   private boolean isSolidGround(Level level, BlockPos pos) {
      return level.getBlockState(pos).isSolid() || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
   }
}
