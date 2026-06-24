package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public final class HybridPathNavigation extends GroundPathNavigation {
   @Nullable
   private BlockPos pathToPosition;

   public HybridPathNavigation(Mob mob, Level level) {
      super(mob, level);
   }

   public Path createPath(BlockPos pos, int value) {
      this.pathToPosition = pos;
      return super.createPath(pos, value);
   }

   public Path createPath(Entity entity, int value) {
      this.pathToPosition = entity.blockPosition();
      return super.createPath(entity, value);
   }

   public boolean moveTo(Entity entity, double value) {
      Path path = this.createPath(entity, 0);
      if (path != null) {
         return this.moveTo(path, value);
      } else {
         this.pathToPosition = entity.blockPosition();
         this.speedModifier = value;
         return true;
      }
   }

   protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec31) {
      return isClearForMovementBetween(this.mob, vec3, vec31, true);
   }

   public void tick() {
      if (!this.isDone()) {
         super.tick();
      } else if (this.pathToPosition != null) {
         if (this.pathToPosition.closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F)) || this.mob.getY() > (double)this.pathToPosition.getY() && (new BlockPos(this.pathToPosition.getX(), (int)this.mob.getY(), this.pathToPosition.getZ())).closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F))) {
            this.pathToPosition = null;
         } else {
            this.mob.getMoveControl().setWantedPosition((double)this.pathToPosition.getX(), (double)this.pathToPosition.getY(), (double)this.pathToPosition.getZ(), this.speedModifier);
         }
      }

      LivingEntity living = this.mob.getTarget();
      if (living != null && this.mob.isInFluidType()) {
         this.UnderWaterLeaps(living);
      }

   }

   public void UnderWaterLeaps(LivingEntity target) {
      Mob var3 = this.mob;
      if (var3 instanceof Grakensenker grakensenker) {
         if (grakensenker.hasVortex()) {
            return;
         }
      }

      Vec3 vec3 = this.mob.getDeltaMovement();
      Vec3 vec31 = new Vec3(target.getX() - this.mob.getX(), target.getY() - this.mob.getY(), target.getZ() - this.mob.getZ());
      if (vec31.lengthSqr() > 1.0E-7) {
         vec31 = vec31.normalize().scale((double)0.25F).add(vec3.scale(0.01));
      }

      this.mob.setDeltaMovement(vec31.x, vec31.y, vec31.z);
   }

   protected PathFinder createPathFinder(int value) {
      if (this.mob.isInFluidType()) {
         this.nodeEvaluator = new SwimmingNode();
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      } else {
         this.nodeEvaluator = new ClimberNodeNavigator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      }
   }

   public boolean isStuck() {
      this.recomputePath();
      return super.isStuck();
   }

   public boolean canFloat() {
      return this.mob.getAirSupply() < 60;
   }

   protected static class SwimmingNode extends SwimNodeEvaluator {
      public SwimmingNode() {
         super(true);
      }

      public BlockPathTypes getBlockPathType(BlockGetter getter, int value, int value2, int value3) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         BlockState blockstate1 = getter.getBlockState(blockpos$mutableblockpos);
         if (blockstate1.isPathfindable(getter, blockpos$mutableblockpos, PathComputationType.WATER)) {
            return BlockPathTypes.WATER;
         } else if (blockstate1.isPathfindable(getter, blockpos$mutableblockpos, PathComputationType.LAND)) {
            return BlockPathTypes.OPEN;
         } else {
            return ForgeEventFactory.getMobGriefingEvent(this.mob.level(), this.mob) ? BlockPathTypes.BLOCKED : super.getBlockPathType(getter, value, value2, value3);
         }
      }
   }
}
