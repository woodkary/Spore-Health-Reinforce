package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental.ExpPathFinder;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class CalamityPathNavigation extends GroundPathNavigation {
   static final float EPSILON = 1.0E-8F;
   private static final int STUCK_ON_NODE_TICKS = 16;
   private static final double MIN_NODE_PROGRESS_SQR = 0.0025D;
   private static final double LOW_HORIZONTAL_SPEED_SQR = 1.0E-4D;
   protected final Calamity calamity;
   @Nullable
   private BlockPos pathToPosition;
   @Nullable
   private Path nodeProgressPath;
   private int nodeProgressIndex = -1;
   private double bestDistanceToNodeSqr = Double.MAX_VALUE;
   private int ticksWithoutNodeProgress;

   public CalamityPathNavigation(Calamity calamity, Level level) {
      super(calamity, level);
      this.calamity = calamity;
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
   private void superTick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec3 = this.getTempMobPos();
            Vec3 vec31 = this.path.getNextEntityPos(this.mob);
            if (vec3.y > vec31.y && !this.mob.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
               this.path.advance();
            }
         }

          DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
          if (!this.isDone()) {
             Vec3 vec32 = this.path.getNextEntityPos(this.mob);
             if (this.tryRecoverFromStuckNode(vec32) && !this.isDone()) {
                vec32 = this.path.getNextEntityPos(this.mob);
             }

             if (this.isDone()) {
                return;
             }

             this.mob.getMoveControl().setWantedPosition(vec32.x, this.getGroundY(vec32), vec32.z, this.speedModifier);
          }
       }

   }

   public void tick() {
      if (!this.isDone()) {
         //super.tick();
         superTick();
         BlockPos vec3 = this.getTargetPos();
         if (vec3 != null) {
            this.mob.getLookControl().setLookAt((double)vec3.getX(), (double)vec3.getY(), (double)vec3.getZ());
         }
      } else if (this.pathToPosition != null) {
         if (this.pathToPosition.closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F)) || this.mob.getY() > (double)this.pathToPosition.getY() && (new BlockPos(this.pathToPosition.getX(), (int)this.mob.getY(), this.pathToPosition.getZ())).closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F))) {
            this.pathToPosition = null;
         } else {
            this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX()+0.5, this.pathToPosition.getY(), this.pathToPosition.getZ()+0.5, this.speedModifier);
         }
      }

   }

   protected PathFinder createPathFinder(int value) {
      if (this.mob instanceof WaterInfected) {
         this.nodeEvaluator = new WaterCalamityNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new ExpPathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      } else if (this.mob instanceof FlyingInfected) {
         this.nodeEvaluator = new AirCalamityNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new ExpPathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      } else {
         this.nodeEvaluator = new CalamityNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         this.nodeEvaluator.canFloat();
         return new ExpPathFinder(this.nodeEvaluator, value) {
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

   /** @deprecated */
   @Deprecated
   public void hardStop() {
      this.path = null;
      this.resetNodeProgressTracking(null);
   }

   protected void followThePath() {
      Path path = (Path)Objects.requireNonNull(this.path);
      Vec3 entityPos = this.getTempMobPos();
      int pathLength = path.getNodeCount();

      for(int i = path.getNextNodeIndex(); i < path.getNodeCount(); ++i) {
         if ((double)path.getNode(i).y != Math.floor(entityPos.y)) {
            pathLength = i;
            break;
         }
      }

      Vec3 base = entityPos.add((double)(-this.mob.getBbWidth() * 0.5F), (double)0.0F, (double)(-this.mob.getBbWidth() * 0.5F));
      Vec3 max = base.add((double)this.mob.getBbWidth(), (double)this.mob.getBbHeight(), (double)this.mob.getBbWidth());
      if (this.tryShortcut(path, new Vec3(this.mob.getX(), this.mob.getY(), this.mob.getZ()), pathLength, base, max) && (this.isAt(path, this.mob.getBbWidth() * 0.35F) || this.atElevationChange(path) && this.isAt(path, this.mob.getBbWidth() * 0.5F))) {
         path.setNextNodeIndex(path.getNextNodeIndex() + 1);
      }

      this.doStuckDetection(entityPos);
   }

   private boolean isAt(Path path, float threshold) {
      Vec3 pathPos = path.getNextEntityPos(this.mob);
      return Mth.abs((float)(this.mob.getX() - pathPos.x)) < threshold && Mth.abs((float)(this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < (double)1.0F;
   }

   private boolean tryRecoverFromStuckNode(Vec3 nodePosition) {
      if (this.path == null || this.path.isDone()) {
         this.resetNodeProgressTracking(null);
         return false;
      }

      Path currentPath = this.path;
      int nodeIndex = currentPath.getNextNodeIndex();
      if (this.nodeProgressPath != currentPath || this.nodeProgressIndex != nodeIndex) {
         this.resetNodeProgressTracking(currentPath);
         this.nodeProgressIndex = nodeIndex;
      }

      double distanceToNodeSqr = this.mob.position().distanceToSqr(nodePosition);
      if (distanceToNodeSqr + MIN_NODE_PROGRESS_SQR < this.bestDistanceToNodeSqr) {
         this.bestDistanceToNodeSqr = distanceToNodeSqr;
         this.ticksWithoutNodeProgress = 0;
         return false;
      }

      this.bestDistanceToNodeSqr = Math.min(this.bestDistanceToNodeSqr, distanceToNodeSqr);
      ++this.ticksWithoutNodeProgress;
      if (this.ticksWithoutNodeProgress < STUCK_ON_NODE_TICKS || !this.isPhysicallyStalledAtNode()) {
         return false;
      }

      if (nodeIndex + 1 < currentPath.getNodeCount()) {
         currentPath.advance();
         this.resetNodeProgressTracking(currentPath);
      } else {
         this.resetNodeProgressTracking(null);
         this.recomputePath();
      }

      return true;
   }

   private boolean isPhysicallyStalledAtNode() {
      return this.mob.horizontalCollision || !(this.mob instanceof WaterInfected) && this.mob.isInFluidType() || this.mob.getDeltaMovement().horizontalDistanceSqr() < LOW_HORIZONTAL_SPEED_SQR;
   }

   private void resetNodeProgressTracking(@Nullable Path path) {
      this.nodeProgressPath = path;
      this.nodeProgressIndex = -1;
      this.bestDistanceToNodeSqr = Double.MAX_VALUE;
      this.ticksWithoutNodeProgress = 0;
   }

   private boolean atElevationChange(Path path) {
      int curr = path.getNextNodeIndex();
      int end = Math.min(path.getNodeCount(), curr + Mth.ceil(this.mob.getBbWidth() * 0.5F) + 1);
      int currY = path.getNode(curr).y;

      for(int i = curr + 1; i < end; ++i) {
         if (path.getNode(i).y != currY) {
            return true;
         }
      }

      return false;
   }

   private boolean tryShortcut(Path path, Vec3 entityPos, int pathLength, Vec3 base, Vec3 max) {
      int i = pathLength;

      Vec3 vec;
      do {
         --i;
         if (i <= path.getNextNodeIndex()) {
            return true;
         }

         vec = path.getEntityPosAtNode(this.mob, i).subtract(entityPos);
      } while(!this.sweep(vec, base, max));

      path.setNextNodeIndex(i);
      return false;
   }

   private boolean sweep(Vec3 vec, Vec3 base, Vec3 max) {
      float t = 0.0F;
      float max_t = (float)vec.length();
      if (max_t < 1.0E-8F) {
         return true;
      } else {
         float[] tr = new float[3];
         int[] ldi = new int[3];
         int[] tri = new int[3];
         int[] step = new int[3];
         float[] tDelta = new float[3];
         float[] tNext = new float[3];
         float[] normed = new float[3];

         for(int i = 0; i < 3; ++i) {
            float value = element(vec, i);
            boolean dir = value >= 0.0F;
            step[i] = dir ? 1 : -1;
            float lead = element(dir ? max : base, i);
            tr[i] = element(dir ? base : max, i);
            ldi[i] = leadEdgeToInt(lead, step[i]);
            tri[i] = trailEdgeToInt(tr[i], step[i]);
            normed[i] = value / max_t;
            tDelta[i] = Mth.abs(max_t / value);
            float dist = dir ? (float)(ldi[i] + 1) - lead : lead - (float)ldi[i];
            tNext[i] = tDelta[i] < Float.POSITIVE_INFINITY ? tDelta[i] * dist : Float.POSITIVE_INFINITY;
         }

         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

         do {
            int axis = tNext[0] < tNext[1] ? (tNext[0] < tNext[2] ? 0 : 2) : (tNext[1] < tNext[2] ? 1 : 2);
            float dt = tNext[axis] - t;
            t = tNext[axis];
            ldi[axis] += step[axis];
            tNext[axis] += tDelta[axis];

            for(int i = 0; i < 3; ++i) {
               tr[i] += dt * normed[i];
               tri[i] = trailEdgeToInt(tr[i], step[i]);
            }

            int stepx = step[0];
            int x0 = axis == 0 ? ldi[0] : tri[0];
            int x1 = ldi[0] + stepx;
            int stepy = step[1];
            int y0 = axis == 1 ? ldi[1] : tri[1];
            int y1 = ldi[1] + stepy;
            int stepz = step[2];
            int z0 = axis == 2 ? ldi[2] : tri[2];
            int z1 = ldi[2] + stepz;

            for(int x = x0; x != x1; x += stepx) {
               for(int z = z0; z != z1; z += stepz) {
                  for(int y = y0; y != y1; y += stepy) {
                     BlockState block = this.level.getBlockState(pos.set(x, y, z));
                     if (!block.isPathfindable(this.level, new BlockPos(x, y, z), PathComputationType.AIR)) {
                        return false;
                     }
                  }

                  BlockPathTypes in = this.nodeEvaluator.getBlockPathType(this.level, x, y0, z);
                  float priority = this.mob.getPathfindingMalus(in);
                  if (priority < 0.0F || priority >= 8.0F) {
                     return false;
                  }
               }
            }
         } while(t <= max_t);

         return true;
      }
   }

   static int leadEdgeToInt(float coord, int step) {
      return Mth.floor(coord - (float)step * 1.0E-8F);
   }

   static int trailEdgeToInt(float coord, int step) {
      return Mth.floor(coord + (float)step * 1.0E-8F);
   }

   static float element(Vec3 v, int i) {
      float var10000;
      switch (i) {
         case 0 -> var10000 = (float)v.x;
         case 1 -> var10000 = (float)v.y;
         case 2 -> var10000 = (float)v.z;
         default -> var10000 = 0.0F;
      }

      return var10000;
   }

   private static BlockPathTypes getCalamityBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      if (originalType != BlockPathTypes.BLOCKED || !ForgeEventFactory.getMobGriefingEvent(mob.level(), mob)) {
         return originalType;
      }
      BlockState blockState = getter.getBlockState(pos);
      if (canDestroyForPath(mob, pos, blockState)) {
         return BlockPathTypes.DANGER_OTHER;
      }
      return BlockPathTypes.BLOCKED;
   }

   private static boolean canDestroyForPath(Mob mob, BlockPos pos, BlockState blockState) {
      if (!(mob instanceof Calamity calamity)) {
         return false;
      }
      if (blockState.isAir()) {
         return false;
      }
      float destroySpeed = blockState.getDestroySpeed(mob.level(), pos);
      return blockState.is(Utilities.biomass) || destroySpeed >= 0.0F && destroySpeed < calamity.getDestroySpeed();
   }

   private static BlockPathTypes getLandOrAirCalamityBlockPathType(Mob mob, BlockGetter getter, BlockPos pos, BlockPathTypes originalType) {
      BlockPathTypes pathType = getCalamityBlockPathType(mob, getter, pos, originalType);
      if (pathType != BlockPathTypes.BLOCKED && shouldAvoidFluidOrSnow(getter, pos, pathType)) {
         return BlockPathTypes.DANGER_OTHER;
      }
      return pathType;
   }

   private static boolean shouldAvoidFluidOrSnow(BlockGetter getter, BlockPos pos, BlockPathTypes pathType) {
      return pathType == BlockPathTypes.WATER
              || pathType == BlockPathTypes.WATER_BORDER
              || pathType == BlockPathTypes.LAVA
              || pathType == BlockPathTypes.POWDER_SNOW
              || pathType == BlockPathTypes.DANGER_POWDER_SNOW
              || !getter.getFluidState(pos).isEmpty();
   }

   protected static class CalamityNodeEvaluator extends WalkNodeEvaluator {
      protected BlockPathTypes evaluateBlockPathType(BlockGetter getter, BlockPos pos, BlockPathTypes pathTypes) {
         return getLandOrAirCalamityBlockPathType(this.mob, getter, pos, super.evaluateBlockPathType(getter, pos, pathTypes));
      }
   }

   protected static class AirCalamityNodeEvaluator extends FlyNodeEvaluator {
      protected BlockPathTypes evaluateBlockPathType(BlockGetter getter, BlockPos pos, BlockPathTypes pathTypes) {
         return getLandOrAirCalamityBlockPathType(this.mob, getter, pos, super.evaluateBlockPathType(getter, pos, pathTypes));
      }
   }

   protected static class WaterCalamityNodeEvaluator extends SwimNodeEvaluator {
      public WaterCalamityNodeEvaluator() {
         super(true);
      }

      public BlockPathTypes getBlockPathType(BlockGetter getter, int value, int value2, int value3) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(value, value2, value3);
         BlockState blockstate1 = getter.getBlockState(blockpos$mutableblockpos);
         if (blockstate1.isPathfindable(getter, blockpos$mutableblockpos, PathComputationType.WATER)) {
            return BlockPathTypes.WATER;
         } else if (blockstate1.isPathfindable(getter, blockpos$mutableblockpos, PathComputationType.LAND)) {
            return BlockPathTypes.OPEN;
         } else {
            return getCalamityBlockPathType(this.mob, getter, blockpos$mutableblockpos, super.getBlockPathType(getter, value, value2, value3));
         }
      }
   }
}
