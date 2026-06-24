package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
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

public class HybridPathNavigation extends GroundPathNavigation {
   private static final int STUCK_ON_NODE_TICKS = 16;
   private static final int WATER_STUCK_NODE_AVOID_TICKS = 200;
   private static final int WATER_STUCK_NODE_AVOID_HORIZONTAL_RADIUS = 1;
   private static final int WATER_STUCK_NODE_AVOID_VERTICAL_RADIUS = 1;
   private static final double MIN_NODE_PROGRESS_SQR = 0.0025D;
   private static final double LOW_HORIZONTAL_SPEED_SQR = 1.0E-4D;
   @Nullable
   private BlockPos pathToPosition;
   @Nullable
   private Path nodeProgressPath;
   private int nodeProgressIndex = -1;
   private double bestDistanceToNodeSqr = Double.MAX_VALUE;
   private int ticksWithoutNodeProgress;
   private final Map<BlockPos, Long> avoidedWaterNodes = new HashMap<>();

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

   private boolean isUsingWaterPathing() {
      return this.mob.isInFluidType();
   }

   private double getWantedYForNode(Vec3 nodePosition) {
      return this.isUsingWaterPathing() ? nodePosition.y : this.getGroundY(nodePosition);
   }

   private float getWaterNodeReachThreshold() {
      return Math.min(Math.max(this.mob.getBbWidth() * 0.65F, 1.0F), 2.5F);
   }

   private double getWaterNodeYReachThreshold() {
      return Math.max((double)(this.mob.getBbHeight() * 0.5F), 1.0D);
   }

   private void waterAwareTick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followWaterPath();
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
            if (this.tryRecoverFromWaterStuckNode(vec32) && !this.isDone()) {
               vec32 = this.path.getNextEntityPos(this.mob);
            }

            if (this.isDone()) {
               return;
            }

            this.mob.getMoveControl().setWantedPosition(vec32.x, this.getWantedYForNode(vec32), vec32.z, this.speedModifier);
         }
      }
   }

   private void followWaterPath() {
      Path path = this.path;
      if (path != null && this.isAt(path, this.getWaterNodeReachThreshold(), this.getWaterNodeYReachThreshold())) {
         path.advance();
      }

      Vec3 entityPos = this.getTempMobPos();
      this.doStuckDetection(entityPos);
   }

   private boolean tryRecoverFromWaterStuckNode(Vec3 nodePosition) {
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

      this.rememberAvoidedWaterNode(this.nodeToBlockPos(currentPath.getNode(nodeIndex)));
      this.resetNodeProgressTracking(null);
      this.forceRecomputePathNow();
      if (this.path == null && nodeIndex + 1 < currentPath.getNodeCount()) {
         currentPath.advance();
         this.path = currentPath;
         this.resetNodeProgressTracking(currentPath);
      }

      return true;
   }

   private boolean isAt(Path path, float threshold, double yThreshold) {
      Vec3 pathPos = path.getNextEntityPos(this.mob);
      return Mth.abs((float)(this.mob.getX() - pathPos.x)) < threshold && Mth.abs((float)(this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < yThreshold;
   }

   public void tick() {
      if (!this.isDone()) {
         if (this.isUsingWaterPathing()) {
            this.waterAwareTick();
         } else {
            super.tick();
         }
      } else if (this.pathToPosition != null) {
         if (this.pathToPosition.closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F)) || this.mob.getY() > (double)this.pathToPosition.getY() && (new BlockPos(this.pathToPosition.getX(), (int)this.mob.getY(), this.pathToPosition.getZ())).closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), (double)1.0F))) {
            this.pathToPosition = null;
         } else {
            if (this.isUsingWaterPathing() && this.shouldAvoidWaterNode(this.pathToPosition)) {
               this.pathToPosition = null;
               this.resetNodeProgressTracking(null);
               return;
            }

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
         this.nodeEvaluator = new SwimmingNode(this::shouldAvoidWaterNode);
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

   public void recomputePath() {
      super.recomputePath();
      this.resetNodeProgressTracking(this.path);
   }

   private void forceRecomputePathNow() {
      if (this.targetPos == null) {
         return;
      }

      this.path = null;
      this.path = this.createPath(this.targetPos, this.reachRange);
      this.timeLastRecompute = this.level.getGameTime();
      this.hasDelayedRecomputation = false;
      this.resetNodeProgressTracking(this.path);
   }

   public boolean canFloat() {
      return this.mob.getAirSupply() < 60;
   }

   private boolean isPhysicallyStalledAtNode() {
      return this.mob.horizontalCollision || this.mob.getDeltaMovement().horizontalDistanceSqr() < LOW_HORIZONTAL_SPEED_SQR;
   }

   private BlockPos nodeToBlockPos(Node node) {
      return new BlockPos(node.x, node.y, node.z);
   }

   private void rememberAvoidedWaterNode(BlockPos pos) {
      this.expireAvoidedWaterNodes();
      this.avoidedWaterNodes.put(pos.immutable(), this.level.getGameTime() + (long)WATER_STUCK_NODE_AVOID_TICKS);
   }

   private boolean shouldAvoidWaterNode(BlockPos pos) {
      return this.shouldAvoidWaterNode(pos.getX(), pos.getY(), pos.getZ());
   }

   private boolean shouldAvoidWaterNode(int x, int y, int z) {
      this.expireAvoidedWaterNodes();

      for(BlockPos avoided : this.avoidedWaterNodes.keySet()) {
         if (Math.abs(avoided.getX() - x) <= WATER_STUCK_NODE_AVOID_HORIZONTAL_RADIUS
                 && Math.abs(avoided.getZ() - z) <= WATER_STUCK_NODE_AVOID_HORIZONTAL_RADIUS
                 && Math.abs(avoided.getY() - y) <= WATER_STUCK_NODE_AVOID_VERTICAL_RADIUS) {
            return true;
         }
      }

      return false;
   }

   private void expireAvoidedWaterNodes() {
      long gameTime = this.level.getGameTime();
      Iterator<Map.Entry<BlockPos, Long>> iterator = this.avoidedWaterNodes.entrySet().iterator();

      while(iterator.hasNext()) {
         if (iterator.next().getValue() <= gameTime) {
            iterator.remove();
         }
      }
   }

   private void resetNodeProgressTracking(@Nullable Path path) {
      this.nodeProgressPath = path;
      this.nodeProgressIndex = -1;
      this.bestDistanceToNodeSqr = Double.MAX_VALUE;
      this.ticksWithoutNodeProgress = 0;
   }

   @FunctionalInterface
   private interface WaterNodeAvoidance {
      boolean shouldAvoid(int x, int y, int z);
   }

   protected static class SwimmingNode extends SwimNodeEvaluator {
      private final WaterNodeAvoidance avoidance;

      public SwimmingNode(WaterNodeAvoidance avoidance) {
         super(true);
         this.avoidance = avoidance;
      }

      public BlockPathTypes getBlockPathType(BlockGetter getter, int value, int value2, int value3) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(value, value2, value3);
         if (this.avoidance.shouldAvoid(value, value2, value3)) {
            return BlockPathTypes.BLOCKED;
         }

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
