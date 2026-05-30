package com.Harbinger.Spore.Sentities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

public class IkLegWithHitbox {
   private final Entity owner;
   private final PartEntity[] entities;
   private final float maxLength;
   private final float minLength;
   private final Vec3 defaultBodyOffset;
   private final Vec3 defaultLimbOffset;
   private final float maxDistance;
   public final float length;
   private final RandomSource randomSource = RandomSource.create();
   private Vec3 sitPosition = null;
   private Vec3 lastSitPosition = null;

   public IkLegWithHitbox(Entity owner, PartEntity[] entities, Vec3 defaultBodyOffset, Vec3 defaultLimbOffset, float maxDistance, float length, float maxLength, float minLength) {
      this.owner = owner;
      this.entities = entities;
      this.defaultBodyOffset = defaultBodyOffset;
      this.defaultLimbOffset = defaultLimbOffset;
      this.maxDistance = maxDistance;
      this.length = length;
      this.maxLength = maxLength;
      this.minLength = minLength;
   }

   public Vec3 getSitPosition() {
      return this.sitPosition;
   }

   public PartEntity[] getEntities() {
      return this.entities;
   }

   public Vec3 getLastSitPosition() {
      return this.lastSitPosition;
   }

   public Vec3 getLegBasePos() {
      float yRotRad = -this.owner.getYRot() * ((float)Math.PI / 180F);
      double rotatedX = this.defaultLimbOffset.x * Math.cos((double)yRotRad) - this.defaultLimbOffset.z * Math.sin((double)yRotRad);
      double rotatedZ = this.defaultLimbOffset.x * Math.sin((double)yRotRad) + this.defaultLimbOffset.z * Math.cos((double)yRotRad);
      return this.owner.position().add(rotatedX, this.defaultLimbOffset.y, rotatedZ);
   }

   public Vec3 getDefaultBodyOffset() {
      float yRotRad = -this.owner.getYRot() * ((float)Math.PI / 180F);
      double rotatedX = this.defaultBodyOffset.x * Math.cos((double)yRotRad) - this.defaultBodyOffset.z * Math.sin((double)yRotRad);
      double rotatedZ = this.defaultBodyOffset.x * Math.sin((double)yRotRad) + this.defaultBodyOffset.z * Math.cos((double)yRotRad);
      return this.owner.position().add(rotatedX, this.defaultBodyOffset.y, rotatedZ);
   }

   private void moveSegmentTowards(int index, Vec3 target, boolean far) {
      Vec3 currentPos = this.entities[index].position();
      Vec3 newPos = currentPos.lerp(target, (double)0.25F);
      this.entities[index].setPos(far ? target : newPos);
   }

   public void applyIK() {
      if (this.entities != null && this.entities.length != 0) {
         Vec3[] oldPositions = new Vec3[this.entities.length];

         for(int j = 0; j < this.entities.length; ++j) {
            oldPositions[j] = this.entities[j].position();
         }

         Vec3 basePos = this.getDefaultBodyOffset();
         Vec3 defaultTipPos = this.getLegBasePos();
         boolean tooFar = this.entities[this.entities.length - 1].distanceToSqr(defaultTipPos) > (double)100.0F;
         if (this.sitPosition != null) {
            this.sitPosition = this.applyLengthConstraints(defaultTipPos, this.sitPosition);
         }

         Vec3 targetPos = this.sitPosition != null && !tooFar ? this.sitPosition : defaultTipPos;
         targetPos = this.applyLengthConstraints(defaultTipPos, targetPos);
         this.entities[0].setPos(basePos.x, basePos.y, basePos.z);
         this.moveSegmentTowards(this.entities.length - 1, targetPos, tooFar);

         for(int i = this.entities.length - 2; i >= 0; --i) {
            Vec3 nextPos = this.entities[i + 1].position();
            Vec3 dir = this.entities[i].position().subtract(nextPos).normalize();
            Vec3 solvedPos = nextPos.add(dir.scale((double)this.length));
            this.moveSegmentTowards(i, solvedPos, tooFar);
         }

         this.entities[0].setPos(basePos.x, basePos.y, basePos.z);

         for(int i = 1; i < this.entities.length; ++i) {
            Vec3 prevPos = this.entities[i - 1].position();
            Vec3 dir = this.entities[i].position().subtract(prevPos).normalize();
            Vec3 solvedPos = prevPos.add(dir.scale((double)this.length));
            this.moveSegmentTowards(i, solvedPos, tooFar);
         }

         for(int l = 0; l < this.entities.length; ++l) {
            this.entities[l].xo = oldPositions[l].x;
            this.entities[l].yo = oldPositions[l].y;
            this.entities[l].zo = oldPositions[l].z;
            this.entities[l].xOld = oldPositions[l].x;
            this.entities[l].yOld = oldPositions[l].y;
            this.entities[l].zOld = oldPositions[l].z;
         }

      }
   }

   public void refreshLegStandingPoint() {
      this.sitPosition = this.findStableFooting(this.entities[this.entities.length - 1]);
      if (!this.sitPosition.equals(this.lastSitPosition)) {
         this.lastSitPosition = this.sitPosition;
      }

   }

   private Vec3 applyLengthConstraints(Vec3 basePos, Vec3 targetPos) {
      double distance = basePos.distanceTo(targetPos);
      if (distance > (double)this.maxLength) {
         Vec3 dir = targetPos.subtract(basePos).normalize();
         return basePos.add(dir.scale((double)this.maxLength));
      } else if (distance < (double)this.minLength && distance > 1.0E-4) {
         Vec3 dir = targetPos.subtract(basePos).normalize();
         return basePos.add(dir.scale((double)this.minLength));
      } else {
         return targetPos;
      }
   }

   private Vec3 findStableFooting(PartEntity tip) {
      Level level = this.owner.level();
      Vec3 legBasePos = this.getLegBasePos();
      if (level.isClientSide) {
         return legBasePos;
      } else if (this.lastSitPosition != null && legBasePos.distanceTo(this.lastSitPosition) < (double)this.maxDistance) {
         return this.lastSitPosition;
      } else {
         double randX = (this.randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
         double randZ = (this.randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
         Vec3 randomizedBase = legBasePos.add(randX, (double)0.0F, randZ);
         double entityWidth = (double)this.owner.getBbWidth();
         double minDistance = entityWidth * 1.2;
         Vec3 horizontalVec = new Vec3(randomizedBase.x - legBasePos.x, (double)0.0F, randomizedBase.z - legBasePos.z);
         double horizontalDist = horizontalVec.length();
         if (horizontalDist < minDistance && horizontalDist > 1.0E-4) {
            Vec3 direction = horizontalVec.normalize();
            randomizedBase = legBasePos.add(direction.scale(minDistance));
         }

         BlockPos searchStart = new BlockPos((int)Math.floor(randomizedBase.x), (int)Math.floor(tip.position().y + (double)2.0F), (int)Math.floor(randomizedBase.z));

         for(int y = 0; y < 4; ++y) {
            BlockPos checkPos = searchStart.below(y);
            if (this.owner.level().getBlockState(checkPos).isSolidRender(this.owner.level(), checkPos)) {
               return new Vec3((double)checkPos.getX() + (double)0.5F, (double)checkPos.getY() - (double)0.5F, (double)checkPos.getZ() + (double)0.5F);
            }
         }

         return this.lastSitPosition == null ? legBasePos : randomizedBase;
      }
   }
}
