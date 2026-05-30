package com.Harbinger.Spore.Client.Models;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface TentacledModel {
   default void animateTentacleX(ModelPart part, float value) {
      part.xRot = part.getInitialPose().xRot + value;
   }

   default void animateTentacleY(ModelPart part, float value) {
      part.yRot = part.getInitialPose().yRot + value;
   }

   default void animateTentacleZ(ModelPart part, float value) {
      part.zRot = part.getInitialPose().zRot + value;
   }

   default void animateTumor(ModelPart part, float value) {
      part.xScale = 1.0F + value;
      part.yScale = 1.0F + value;
      part.zScale = 1.0F + value;
   }

   public static class InnerClassIkLeg {
      protected final RandomSource randomSource = RandomSource.create();
      protected final Vec3[] entities;
      protected final Vec3 defaultBodyOffset;
      protected final Vec3 defaultLimbOffset;
      protected final float maxDistance;
      protected final Vec3[] segmentVelocities;
      protected final ModelPart[] parts;
      protected final float[] segmetsL;
      protected Vec3 sitPosition = null;
      protected Vec3 lastSitPosition = null;
      protected Vec3 lastOwnerPosition;
      protected Vec3 ownerMovementDelta;
      protected float lastYaw;
      protected float yawDelta;
      protected float stepAngle;
      protected final float stepSpeed;
      protected final float stepSize;

      public InnerClassIkLeg(int amount, ModelPart[] parts, float[] segmetsL, Vec3 defaultBodyOffset, Vec3 defaultLimbOffset, float maxDistance, float stepSpeed, float stepSize) {
         this.lastOwnerPosition = Vec3.ZERO;
         this.ownerMovementDelta = Vec3.ZERO;
         this.lastYaw = 0.0F;
         this.yawDelta = 0.0F;
         this.stepAngle = 0.0F;
         this.entities = new Vec3[amount];
         this.segmentVelocities = new Vec3[amount];
         this.stepSpeed = stepSpeed;
         this.stepSize = stepSize;

         for(int i = 0; i < amount; ++i) {
            this.entities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
            this.segmentVelocities[i] = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
         }

         this.parts = parts;
         this.segmetsL = segmetsL;
         this.defaultBodyOffset = defaultBodyOffset;
         this.defaultLimbOffset = defaultLimbOffset;
         this.maxDistance = maxDistance;
      }

      public Vec3[] getEntities() {
         return this.entities;
      }

      public Vec3 applyYaw(LivingEntity owner, Vec3 offset) {
         float yawRad = owner.getYRot() * ((float)Math.PI / 180F);
         return offset.yRot(-yawRad - ((float)Math.PI / 2F));
      }

      public Vec3 getLegBasePos(LivingEntity owner) {
         Vec3 pivot = owner.position();
         Vec3 extend = this.isOwnerMoving(owner) ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : Vec3.ZERO;
         return pivot.add(this.applyYaw(owner, this.defaultLimbOffset.add(extend)));
      }

      public Vec3 getBodyOffset(LivingEntity owner) {
         Vec3 pivot = owner.position();
         return pivot.add(this.applyYaw(owner, this.defaultBodyOffset));
      }

      protected void moveSegmentTowards(int index, Vec3 target, boolean far) {
         Vec3 currentPos = this.entities[index];
         Vec3 newPos = currentPos.lerp(target, (double)0.35F);
         this.entities[index] = far ? target : newPos;
      }

      protected void moveTipTowards(Vec3 target) {
         int tip = this.entities.length - 1;
         Vec3 currentPos = this.entities[tip];
         this.entities[tip] = currentPos.lerp(target, (double)0.45F);
      }

      protected boolean isOwnerMoving(LivingEntity owner) {
         return owner.getDeltaMovement().lengthSqr() > 0.005;
      }

      protected void updateOwnerMovementDelta(LivingEntity owner) {
         Vec3 currentOwnerPos = owner.position();
         this.ownerMovementDelta = currentOwnerPos.subtract(this.lastOwnerPosition);
         this.lastOwnerPosition = currentOwnerPos;
         float currentYaw = owner.getYRot();
         this.yawDelta = Mth.wrapDegrees(currentYaw - this.lastYaw);
         this.lastYaw = currentYaw;
      }

      protected Vec3 rotateAroundYaw(Vec3 pos, Vec3 pivot, float degrees) {
         double rad = (double)(degrees * ((float)Math.PI / 180F));
         Vec3 rel = pos.subtract(pivot);
         Vec3 rotated = rel.yRot((float)(-rad));
         return pivot.add(rotated);
      }

      protected void applyBodySpin(LivingEntity owner) {
         if (!(Math.abs(this.yawDelta) < 0.001F)) {
            Vec3 pivot = owner.position();

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

      protected void applyEntityMovementToLegs(LivingEntity owner) {
         if (this.ownerMovementDelta.lengthSqr() < 1.0E-5) {
            Vec3 defaultTipPos = this.getLegBasePos(owner);
            int tip = this.entities.length - 1;
            Vec3 currentPos = this.entities[tip];
            this.entities[tip] = currentPos.lerp(defaultTipPos, (double)0.1F);
         } else {
            int last = this.entities.length - 1;

            for(int i = 0; i < this.entities.length - 1; ++i) {
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

      protected Vec3 applyStep(LivingEntity owner, Vec3 baseTipPos) {
         if (!this.isOwnerMoving(owner)) {
            return baseTipPos;
         } else {
            this.stepAngle += this.stepSpeed;
            this.stepAngle = Mth.wrapDegrees(this.stepAngle);
            float rad = this.stepAngle * ((float)Math.PI / 180F);
            double x = Math.cos((double)rad) * (double)this.stepSize;
            double y = Math.cos((double)rad) * (double)this.stepSize * (double)0.5F;
            y = y > (double)0.0F ? y : (double)0.0F;
            Vec3 circularOffset = new Vec3(-x, y, (double)0.0F);
            circularOffset = this.applyYaw(owner, circularOffset);
            return baseTipPos.add(circularOffset);
         }
      }

      public void applyIK(LivingEntity owner) {
         if (this.entities.length != 0) {
            Vec3 basePos = this.getBodyOffset(owner);
            Vec3 defaultTipPos = this.sitPosition == null ? this.getLegBasePos(owner) : this.sitPosition;
            this.updateOwnerMovementDelta(owner);
            this.applyEntityMovementToLegs(owner);
            this.applyBodySpin(owner);
            defaultTipPos = this.applyStep(owner, defaultTipPos);
            this.moveTipTowards(defaultTipPos);

            for(int i = this.entities.length - 2; i >= 0; --i) {
               Vec3 nextPos = this.entities[i + 1];
               Vec3 dir = this.entities[i].subtract(nextPos);
               float segmentLength = this.segmetsL[i];
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
               float segmentLength = this.segmetsL[i - 1];
               if (dir.lengthSqr() > (double)1.0E-4F) {
                  dir = dir.normalize().scale((double)segmentLength);
               } else {
                  dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
               }

               Vec3 solvedPos = prevPos.add(dir);
               this.moveSegmentTowards(i, solvedPos, this.entities[i - 1].distanceTo(this.entities[i]) > (double)5.0F);
            }

            this.refreshLegStandingPoint(owner);
            this.handleSegmentUpdating(owner);

            for(Vec3 entity : this.entities) {
               owner.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, entity.x, entity.y, entity.z, (double)0.0F, 0.1, (double)0.0F);
            }

         }
      }

      public void handleSegmentUpdating(LivingEntity owner) {
         Vec3 origin = null;
         float[] yx = new float[]{0.0F, 0.0F};

         for(int i = 0; i < this.entities.length; ++i) {
            Vec3 currentPos = this.entities[i];
            yx = this.calculateRotations(origin, currentPos, i, yx, owner);
            origin = currentPos;
         }

      }

      public float[] calculateRotations(Vec3 from, Vec3 to, int i, float[] yx, LivingEntity owner) {
         if (from != null && to != null) {
            Vec3 direction = to.subtract(from);
            if (direction.lengthSqr() < (double)1.0E-4F) {
               return new float[]{0.0F, 0.0F};
            } else {
               direction = direction.normalize();
               direction = direction.yRot(owner.getYRot() * ((float)Math.PI / 180F));
               float yaw = (float)Math.atan2(direction.x, direction.z) - ((float)Math.PI / 2F);
               float pitch = (float)(-Math.asin(direction.y)) - ((float)Math.PI / 2F);
               if (i > 0 && i - 1 < this.parts.length) {
                  ModelPart part = this.parts[i - 1];
                  part.yRot = yaw - yx[0];
                  part.xRot = pitch - yx[1];
                  return new float[]{part.yRot, part.xRot};
               } else {
                  return new float[]{0.0F, 0.0F};
               }
            }
         } else {
            return new float[]{0.0F, 0.0F};
         }
      }

      public void refreshLegStandingPoint(LivingEntity owner) {
         if (!owner.isInWater()) {
            if (this.lastSitPosition == null || !(this.getLegBasePos(owner).distanceTo(this.lastSitPosition) < (double)this.maxDistance)) {
               this.sitPosition = this.findStableFooting(owner);
               if (!this.sitPosition.equals(this.lastSitPosition)) {
                  this.lastSitPosition = this.sitPosition;
               }

            }
         }
      }

      protected Vec3 findStableFooting(LivingEntity owner) {
         Level level = owner.level();
         Vec3 worldBasePos = this.getLegBasePos(owner);
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

      private boolean isSolidGround(Level level, BlockPos pos) {
         return level.getBlockState(pos).isSolid() || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
      }
   }
}
