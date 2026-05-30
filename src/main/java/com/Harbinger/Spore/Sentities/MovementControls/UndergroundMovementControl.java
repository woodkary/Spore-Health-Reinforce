package com.Harbinger.Spore.Sentities.MovementControls;

import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class UndergroundMovementControl extends CalamityMovementControl {
   public UndergroundMovementControl(Mob mob) {
      super(mob, 30);
   }

   public void tick() {
      this.moveUnderground();
      this.mob.setNoGravity(this.isInWall(this.mob));
   }

   boolean isInWall(LivingEntity mob) {
      if (!mob.isInWater() && !mob.isInLava()) {
         float f = mob.getBbWidth() * 0.8F;
         AABB aabb = AABB.ofSize(mob.getEyePosition().add((double)0.0F, -0.05, (double)0.0F), (double)f, 1.0E-6, (double)f);
         return BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = mob.level().getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(mob.level(), p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(mob.level(), p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
         });
      } else {
         return true;
      }
   }

   public void moveUnderground() {
      if (this.operation == Operation.MOVE_TO) {
         Vec3 vec3;
         double var10000;
         label37: {
            vec3 = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            vec3 = vec3.normalize();
            vec3 = vec3.multiply((double)1.0F, this.isInWall(this.mob) ? (double)1.0F : (double)0.0F, (double)1.0F);
            Mob var5 = this.mob;
            if (var5 instanceof Hohlfresser) {
               Hohlfresser hohlfresser = (Hohlfresser)var5;
               if (hohlfresser.isUnderground()) {
                  var10000 = 0.05;
                  break label37;
               }
            }

            var10000 = 0.15;
         }

         double speed = var10000;
         double speedRep = this.mob.isInWater() ? speed * (double)0.5F : speed;
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(vec3.scale(speedRep)));
         float yaw = (float)(Mth.atan2(vec3.z, vec3.x) * (180D / Math.PI)) - 90.0F;
         this.mob.setYRot(yaw);
         this.mob.setYHeadRot(yaw);
         if (this.mob.horizontalCollision && this.getWantedY() > this.mob.getY()) {
            Vec3 initialVec = this.mob.getDeltaMovement();
            Vec3 climbVec = new Vec3(initialVec.x, 0.2, initialVec.z);
            this.mob.setDeltaMovement(climbVec.x * 0.91, climbVec.y * 0.98, climbVec.z * 0.91);
         }
      } else {
         this.operation = Operation.WAIT;
      }

      if (this.operation == Operation.WAIT && !this.hasWanted() && this.mob.getTarget() == null) {
         this.mob.setDeltaMovement(Vec3.ZERO);
         this.mob.setYRot(0.0F);
         this.mob.setYHeadRot(0.0F);
      }

   }
}
