package com.Harbinger.Spore.Sentities.AI;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ClimberMovement extends MoveControl {
   private final float maxClampDist;
   private final float minClampDist;
   private final float multiplier;

   public ClimberMovement(Mob mob, float maxClampDist, float minClampDist, float multiplier) {
      super(mob);
      this.maxClampDist = maxClampDist;
      this.minClampDist = minClampDist;
      this.multiplier = multiplier;
   }

   private void clampToSurfaceOrDrop() {
      float maxDistSq = this.maxClampDist * this.maxClampDist;
      float minDistSq = maxDistSq;
      Vec3 yank = Vec3.ZERO;

      for(int i = -1; i < 2; ++i) {
         for(int j = -1; j < 2; ++j) {
            for(int k = -1; k < 2; ++k) {
               if (i != 0 || j != 0 || k != 0) {
                  float d = this.raycastDistSq(new Vec3((double)i, (double)j, (double)k));
                  if (d < maxDistSq) {
                     yank = yank.add((double)i, (double)j, (double)k);
                     if (d < minDistSq) {
                        minDistSq = d;
                     }
                  }
               }
            }
         }
      }

      if (minDistSq > this.minClampDist * this.minClampDist) {
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(yank.scale((double)(this.multiplier * minDistSq))));
      }

      this.mob.setNoGravity(minDistSq < maxDistSq);
   }

   private float raycastDistSq(Vec3 direction) {
      Vec3 start = this.mob.position().add((double)0.0F, (double)this.mob.getBbHeight() / (double)2.0F, (double)0.0F);
      Vec3 end = start.add(direction.scale((double)this.maxClampDist));
      BlockHitResult result = this.mob.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this.mob));
      return (float)result.getLocation().distanceToSqr(start);
   }

   public void tick() {
      super.tick();
      if (this.mob.getTarget() != null && this.mob.getTarget().getY() <= this.mob.getY()) {
         this.mob.setNoGravity(false);
      } else {
         if ((this.mob.horizontalCollision || this.mob.verticalCollision) && !this.mob.onGround()) {
            this.clampToSurfaceOrDrop();
         }

      }
   }
}
