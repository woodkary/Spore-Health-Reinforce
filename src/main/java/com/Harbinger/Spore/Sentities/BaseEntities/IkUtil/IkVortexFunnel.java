package com.Harbinger.Spore.Sentities.BaseEntities.IkUtil;

import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class IkVortexFunnel extends IkKrakenLeg {
   public IkVortexFunnel(Grakensenker owner) {
      super(owner, 25, Vec3.ZERO, Vec3.ZERO, 0.0F, false);
   }

   public Vec3 getBodyOffset() {
      Vec3 pivot = this.owner.position().add((double)0.0F, (double)this.owner.getExtendedHeight(), (double)0.0F);
      return pivot.add(this.applyYaw(new Vec3((double)-4.0F, (double)4.5F, (double)1.5F)));
   }

   protected void moveSegmentTowards(int index, Vec3 target, boolean far) {
      this.entities[index] = target;
   }

   protected void moveTipTowards(Vec3 target) {
      this.entities[this.entities.length - 1] = target;
   }

   public void applyIK() {
      if (this.entities != null && this.entities.length != 0) {
         BlockPos pos = this.owner.getVortexVector();
         Vec3 basePos = this.getBodyOffset();
         Vec3 defaultTipPos = new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         float totalDistance = (float)basePos.distanceTo(defaultTipPos);
         float baseSegmentLength = 0.5F;
         float maxSegmentLength = 5.0F;
         float stretchFactor = 0.8F;
         float dynamicSegmentLength = Math.min(baseSegmentLength + totalDistance * stretchFactor / (float)this.entities.length, maxSegmentLength);
         float idealSegmentLength = Math.max(dynamicSegmentLength, 0.3F);
         int firstElasticSegment = 1;
         int lastElasticSegment = this.entities.length - 2;
         this.moveTipTowards(defaultTipPos);

         for(int i = this.entities.length - 2; i >= 0; --i) {
            Vec3 nextPos = this.entities[i + 1];
            Vec3 dir = this.entities[i].subtract(nextPos);
            boolean isElastic = i >= firstElasticSegment && i <= lastElasticSegment;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               if (isElastic) {
                  dir = dir.normalize();
               } else {
                  dir = dir.normalize().scale((double)idealSegmentLength);
               }
            } else {
               dir = new Vec3((double)idealSegmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = nextPos.add(dir);
            this.moveSegmentTowards(i, solvedPos, false);
         }

         this.moveSegmentTowards(0, basePos, false);

         for(int i = 1; i < this.entities.length; ++i) {
            Vec3 prevPos = this.entities[i - 1];
            Vec3 dir = this.entities[i].subtract(prevPos);
            boolean isElastic = i <= lastElasticSegment;
            if (dir.lengthSqr() > (double)1.0E-4F) {
               if (isElastic) {
                  dir = dir.normalize();
               } else {
                  dir = dir.normalize().scale((double)idealSegmentLength);
               }
            } else {
               dir = new Vec3((double)idealSegmentLength, (double)0.0F, (double)0.0F);
            }

            Vec3 solvedPos = prevPos.add(dir);
            this.moveSegmentTowards(i, solvedPos, false);
         }

      }
   }
}
