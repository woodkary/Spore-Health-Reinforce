package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental.ExpPathFinder;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
   private static final int TERMINAL_RECOMPUTES_BEFORE_DETOUR = 2;
   private static final int MAX_DETOUR_DEPTH = 4;
   private static final int DETOUR_SEARCH_RADIUS = 6;
   private static final int DETOUR_VERTICAL_RANGE = 2;
   private static final int WATER_STUCK_NODE_AVOID_TICKS = 200;
   private static final int WATER_STUCK_NODE_AVOID_HORIZONTAL_RADIUS = 1;
   private static final int WATER_STUCK_NODE_AVOID_VERTICAL_RADIUS = 1;
   private static final double MIN_NODE_PROGRESS_SQR = 0.0025D;
   private static final double LOW_HORIZONTAL_SPEED_SQR = 1.0E-4D;
   protected final Calamity calamity;
   @Nullable
   private BlockPos pathToPosition;
   private final Deque<BlockPos> detourTargetStack = new ArrayDeque<>();
   private final Set<Long> triedDetourTargets = new HashSet<>();
   @Nullable
   private BlockPos activeDetourTarget;
   @Nullable
   private BlockPos lastTerminalStuckTarget;
   @Nullable
   private BlockPos fallbackProgressTarget;
   @Nullable
   private Path nodeProgressPath;
   private int nodeProgressIndex = -1;
   private double bestDistanceToNodeSqr = Double.MAX_VALUE;
   private int ticksWithoutNodeProgress;
   private double bestDistanceToFallbackTargetSqr = Double.MAX_VALUE;
   private int ticksWithoutFallbackProgress;
   private int terminalStuckRecoveries;
   private final Map<BlockPos, Long> avoidedWaterNodes = new HashMap<>();

   public CalamityPathNavigation(Calamity calamity, Level level) {
      super(calamity, level);
      this.calamity = calamity;
   }

   @Override
   public Path createPath(BlockPos pos, int value) {
      if (!this.isDetourRelatedTarget(pos)) {
         this.clearDetourState();
      }

      this.pathToPosition = pos;
      return super.createPath(pos, value);
   }

   @Override
   public Path createPath(Entity entity, int value) {
      if (!this.isDetourRelatedTarget(entity.blockPosition())) {
         this.clearDetourState();
      }

      this.pathToPosition = entity.blockPosition();
      return super.createPath(entity, value);
   }

   @Override
   public boolean moveTo(Entity entity, double value) {
      Path path = this.createPath(entity, 0);
      if (path != null) {
         return this.moveTo(path, value);
      } else {
         this.path = null;
         this.targetPos = entity.blockPosition();
         this.pathToPosition = entity.blockPosition();
         this.speedModifier = value;
         this.resetNodeProgressTracking(null);
         return true;
      }
   }

   @Override
   public boolean moveTo(double x, double y, double z, double speed) {
      Path path = this.createPath(x, y, z, 1);
      if (path != null) {
         return this.moveTo(path, speed);
      } else {
         BlockPos target = BlockPos.containing(x, y, z);
         this.path = null;
         this.targetPos = target;
         this.pathToPosition = target;
         this.speedModifier = speed;
         this.resetNodeProgressTracking(null);
         return true;
      }
   }

   protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec31) {
      return isClearForMovementBetween(this.mob, vec3, vec31, true);
   }

   private boolean isUsingWaterPathing() {
      return this.mob instanceof WaterInfected && this.mob.isInFluidType();
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

             this.mob.getMoveControl().setWantedPosition(vec32.x, this.getWantedYForNode(vec32), vec32.z, this.speedModifier);
          }
       }

   }

   public void tick() {
      if (this.tryCompleteActiveDetourTarget()) {
         return;
      }

      this.restoreActiveDetourTargetIfInterrupted();

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
            this.resetFallbackProgressTracking(null);
         } else {
            if (this.isUsingWaterPathing() && this.shouldAvoidWaterNode(this.pathToPosition)) {
               if (this.tryStartDetourAround(this.pathToPosition)) {
                  return;
               }

               this.pathToPosition = null;
               this.resetFallbackProgressTracking(null);
               return;
            }

            if (this.tryRecoverFromStuckFallbackTarget(this.pathToPosition)) {
               return;
            }

            this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX()+0.5, this.pathToPosition.getY(), this.pathToPosition.getZ()+0.5, this.speedModifier);
         }
      }

   }

   protected PathFinder createPathFinder(int value) {
      if (this.mob instanceof WaterInfected) {
         this.nodeEvaluator = new WaterCalamityNodeEvaluator(this.mob, this::shouldAvoidWaterNode);
         this.nodeEvaluator.setCanPassDoors(true);
         return new ExpPathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      } else if (this.mob instanceof FlyingInfected) {
         this.nodeEvaluator = new AirCalamityNodeEvaluator(this.mob);
         this.nodeEvaluator.setCanPassDoors(true);
         return new ExpPathFinder(this.nodeEvaluator, value) {
            protected float distance(Node node, Node node1) {
               return node.distanceManhattan(node1);
            }
         };
      } else {
         this.nodeEvaluator = new CalamityNodeEvaluator(this.mob);
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
      this.clearDetourState();
      this.resetNodeProgressTracking(null);
      this.resetFallbackProgressTracking(null);
   }

   protected void followThePath() {
      Path path = (Path)Objects.requireNonNull(this.path);
      Vec3 entityPos = this.getTempMobPos();

      if (this.isUsingWaterPathing()) {
         if (this.isAt(path, this.getWaterNodeReachThreshold(), this.getWaterNodeYReachThreshold())) {
            path.advance();
         }
      } else {
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
            path.advance();
         }
      }

      this.doStuckDetection(entityPos);
   }

   private boolean isAt(Path path, float threshold) {
      return this.isAt(path, threshold, 1.0D);
   }

   private boolean isAt(Path path, float threshold, double yThreshold) {
      Vec3 pathPos = path.getNextEntityPos(this.mob);
      return Mth.abs((float)(this.mob.getX() - pathPos.x)) < threshold && Mth.abs((float)(this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < yThreshold;
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

      if (this.isUsingWaterPathing()) {
         return this.recoverFromWaterStuckNode(currentPath, nodeIndex);
      }

      if (nodeIndex + 1 < currentPath.getNodeCount()) {
         currentPath.advance();
         this.resetNodeProgressTracking(currentPath);
      } else {
         this.recoverFromStuckTerminalNode();
      }

      return true;
   }

   private boolean recoverFromWaterStuckNode(Path currentPath, int nodeIndex) {
      BlockPos stuckNode = this.nodeToBlockPos(currentPath.getNode(nodeIndex));
      this.rememberAvoidedWaterNode(stuckNode);
      this.resetNodeProgressTracking(null);

      if (nodeIndex + 1 >= currentPath.getNodeCount()) {
         BlockPos target = this.getCurrentNavigationTarget();
         if (target != null) {
            if (this.isCloseEnoughToNavigationTarget(target)) {
               return this.completeCurrentNavigationTarget();
            }

            if (this.tryStartDetourAround(target)) {
               return true;
            }
         }
      }

      this.forceRecomputePathNow();
      if (this.path == null && nodeIndex + 1 < currentPath.getNodeCount()) {
         currentPath.advance();
         this.path = currentPath;
         this.resetNodeProgressTracking(currentPath);
      }

      return true;
   }

   private void recoverFromStuckTerminalNode() {
      this.resetNodeProgressTracking(null);
      BlockPos target = this.getCurrentNavigationTarget();
      if (target == null) {
         this.recomputePath();
         return;
      }

      if (this.isCloseEnoughToNavigationTarget(target)) {
         this.completeCurrentNavigationTarget();
         return;
      }

      if (!target.equals(this.lastTerminalStuckTarget)) {
         this.lastTerminalStuckTarget = target;
         this.terminalStuckRecoveries = 0;
      }

      ++this.terminalStuckRecoveries;
      if (this.terminalStuckRecoveries <= TERMINAL_RECOMPUTES_BEFORE_DETOUR) {
         this.recomputePath();
      } else if (!this.tryStartDetourAround(target)) {
         this.recomputePath();
      }
   }

   public void recomputePath() {
      if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
         if (this.targetPos != null) {
            this.path = null;
            this.path = this.createPath(this.targetPos, this.reachRange);
            this.timeLastRecompute = this.level.getGameTime();
            this.hasDelayedRecomputation = false;
            this.resetNodeProgressTracking(this.path);
         }
      } else {
         this.hasDelayedRecomputation = true;
      }

   }

   private void forceRecomputePathNow() {
      BlockPos target = this.getCurrentNavigationTarget();
      if (target == null) {
         return;
      }

      this.path = null;
      this.targetPos = target;
      this.path = this.createPath(target, this.reachRange);
      this.timeLastRecompute = this.level.getGameTime();
      this.hasDelayedRecomputation = false;
      this.resetNodeProgressTracking(this.path);
   }

   private boolean tryStartDetourAround(BlockPos blockedTarget) {
      if (this.detourTargetStack.size() >= MAX_DETOUR_DEPTH) {
         return false;
      }

      TemporaryPath temporaryPath = this.findReachableTemporaryTarget(blockedTarget);
      if (temporaryPath == null) {
         return false;
      }

      Path previousPath = this.path;
      BlockPos previousTarget = this.targetPos;
      BlockPos previousPathToPosition = this.pathToPosition;
      boolean previousDelayedRecomputation = this.hasDelayedRecomputation;
      long previousTimeLastRecompute = this.timeLastRecompute;
      BlockPos previousActiveTarget = this.activeDetourTarget;
      this.detourTargetStack.push(blockedTarget);
      this.activeDetourTarget = temporaryPath.target;
      this.triedDetourTargets.add(temporaryPath.target.asLong());
      if (!this.applyPathTo(temporaryPath.target, temporaryPath.path)) {
         this.path = previousPath;
         this.targetPos = previousTarget;
         this.pathToPosition = previousPathToPosition;
         this.hasDelayedRecomputation = previousDelayedRecomputation;
         this.timeLastRecompute = previousTimeLastRecompute;
         this.activeDetourTarget = previousActiveTarget;
         this.detourTargetStack.pop();
         this.resetNodeProgressTracking(this.path);
         return false;
      }

      this.lastTerminalStuckTarget = null;
      this.terminalStuckRecoveries = 0;
      return true;
   }

   @Nullable
   private TemporaryPath findReachableTemporaryTarget(BlockPos blockedTarget) {
      TemporaryPath best = null;
      double bestScore = Double.MAX_VALUE;

      for(int radius = 1; radius <= DETOUR_SEARCH_RADIUS; ++radius) {
         for(int y = -DETOUR_VERTICAL_RANGE; y <= DETOUR_VERTICAL_RANGE; ++y) {
            for(int x = -radius; x <= radius; ++x) {
               for(int z = -radius; z <= radius; ++z) {
                  if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                     continue;
                  }

                  BlockPos candidate = blockedTarget.offset(x, y, z);
                  if (!this.canUseTemporaryTarget(candidate, blockedTarget)) {
                     continue;
                  }

                  Path candidatePath = this.createPathForCandidate(candidate);
                  if (candidatePath == null || !candidatePath.canReach()) {
                     continue;
                  }

                  double score = candidatePath.getNodeCount() + this.distanceSqr(candidate, blockedTarget) * 0.25D + this.mob.distanceToSqr(Vec3.atCenterOf(candidate)) * 0.01D;
                  if (score < bestScore) {
                     best = new TemporaryPath(candidate, candidatePath);
                     bestScore = score;
                  }
               }
            }
         }

         if (best != null) {
            return best;
         }
      }

      return null;
   }

   private boolean canUseTemporaryTarget(BlockPos candidate, BlockPos blockedTarget) {
      if (candidate.equals(blockedTarget) || candidate.equals(this.activeDetourTarget) || this.detourTargetStack.contains(candidate)) {
         return false;
      }

      if (this.triedDetourTargets.contains(candidate.asLong()) || !this.level.getWorldBorder().isWithinBounds(candidate)) {
         return false;
      }

      if (this.isUsingWaterPathing() && this.shouldAvoidWaterNode(candidate)) {
         return false;
      }

      if (this.nodeEvaluator == null) {
         return true;
      }

      BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, candidate.getX(), candidate.getY(), candidate.getZ());
      float priority = this.mob.getPathfindingMalus(type);
      return priority >= 0.0F && priority < 8.0F;
   }

   @Nullable
   private Path createPathForCandidate(BlockPos target) {
      BlockPos previousTarget = this.targetPos;
      BlockPos previousPathToPosition = this.pathToPosition;
      boolean previousDelayedRecomputation = this.hasDelayedRecomputation;
      Path candidatePath = super.createPath(target, this.reachRange);
      this.targetPos = previousTarget;
      this.pathToPosition = previousPathToPosition;
      this.hasDelayedRecomputation = previousDelayedRecomputation;
      return candidatePath;
   }

   private boolean tryCompleteActiveDetourTarget() {
      return this.activeDetourTarget != null && this.isCloseEnoughToNavigationTarget(this.activeDetourTarget) && this.completeCurrentNavigationTarget();
   }

   private void restoreActiveDetourTargetIfInterrupted() {
      if (this.activeDetourTarget == null) {
         return;
      }

      if (this.isActiveDetourTargetCurrent()) {
         return;
      }

      Path detourPath = this.createPathForCandidate(this.activeDetourTarget);
      if (detourPath != null) {
         this.applyPathTo(this.activeDetourTarget, detourPath);
      } else {
         this.path = null;
         this.targetPos = this.activeDetourTarget;
         this.pathToPosition = this.activeDetourTarget;
         this.hasDelayedRecomputation = false;
         this.resetNodeProgressTracking(null);
         this.resetFallbackProgressTracking(this.activeDetourTarget);
      }
   }

   private boolean isActiveDetourTargetCurrent() {
      if (this.activeDetourTarget == null || !this.activeDetourTarget.equals(this.targetPos) || !this.activeDetourTarget.equals(this.pathToPosition)) {
         return false;
      }

      return this.path == null || this.activeDetourTarget.equals(this.path.getTarget());
   }

   private boolean completeCurrentNavigationTarget() {
      this.resetNodeProgressTracking(null);
      this.lastTerminalStuckTarget = null;
      this.terminalStuckRecoveries = 0;

      if (!this.detourTargetStack.isEmpty()) {
         BlockPos nextTarget = this.detourTargetStack.pop();
         this.activeDetourTarget = nextTarget;
         Path nextPath = this.createPathForCandidate(nextTarget);
         if (nextPath != null && this.applyPathTo(nextTarget, nextPath)) {
            return true;
         }

         this.path = null;
         this.targetPos = nextTarget;
         this.pathToPosition = nextTarget;
         return this.tryStartDetourAround(nextTarget);
      }

      this.finishNavigationTarget();
      return true;
   }

   private boolean applyPathTo(BlockPos target, Path newPath) {
      this.targetPos = target;
      this.pathToPosition = target;
      this.hasDelayedRecomputation = false;
      this.timeLastRecompute = this.level.getGameTime();
      this.resetNodeProgressTracking(newPath);
      this.resetFallbackProgressTracking(null);
      return super.moveTo(newPath, this.speedModifier);
   }

   private boolean tryRecoverFromStuckFallbackTarget(BlockPos target) {
      if (!target.equals(this.fallbackProgressTarget)) {
         this.resetFallbackProgressTracking(target);
      }

      double distanceToTargetSqr = this.mob.position().distanceToSqr(Vec3.atCenterOf(target));
      if (distanceToTargetSqr + MIN_NODE_PROGRESS_SQR < this.bestDistanceToFallbackTargetSqr) {
         this.bestDistanceToFallbackTargetSqr = distanceToTargetSqr;
         this.ticksWithoutFallbackProgress = 0;
         return false;
      }

      this.bestDistanceToFallbackTargetSqr = Math.min(this.bestDistanceToFallbackTargetSqr, distanceToTargetSqr);
      ++this.ticksWithoutFallbackProgress;
      if (this.ticksWithoutFallbackProgress < STUCK_ON_NODE_TICKS || !this.isPhysicallyStalledAtNode()) {
         return false;
      }

      this.resetFallbackProgressTracking(null);
      this.recoverFromStuckTerminalNode();
      return true;
   }

   private void finishNavigationTarget() {
      this.path = null;
      this.targetPos = null;
      this.pathToPosition = null;
      this.activeDetourTarget = null;
      this.detourTargetStack.clear();
      this.triedDetourTargets.clear();
      this.hasDelayedRecomputation = false;
      this.resetFallbackProgressTracking(null);
   }

   private boolean isCloseEnoughToNavigationTarget(BlockPos target) {
      return target.closerToCenterThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), 1.5D));
   }

   @Nullable
   private BlockPos getCurrentNavigationTarget() {
      if (this.activeDetourTarget != null) {
         return this.activeDetourTarget;
      }

      if (this.pathToPosition != null && this.isDone()) {
         return this.pathToPosition;
      }

      return this.targetPos != null ? this.targetPos : this.pathToPosition;
   }

   private boolean isDetourRelatedTarget(BlockPos target) {
      return target.equals(this.targetPos) || target.equals(this.activeDetourTarget) || this.detourTargetStack.contains(target);
   }

   private void clearDetourState() {
      this.detourTargetStack.clear();
      this.triedDetourTargets.clear();
      this.activeDetourTarget = null;
      this.lastTerminalStuckTarget = null;
      this.terminalStuckRecoveries = 0;
      this.resetFallbackProgressTracking(null);
   }

   private double distanceSqr(BlockPos a, BlockPos b) {
      double x = a.getX() - b.getX();
      double y = a.getY() - b.getY();
      double z = a.getZ() - b.getZ();
      return x * x + y * y + z * z;
   }

   private boolean isPhysicallyStalledAtNode() {
      return this.mob.horizontalCollision || !(this.mob instanceof WaterInfected) && this.mob.isInFluidType() || this.mob.getDeltaMovement().horizontalDistanceSqr() < LOW_HORIZONTAL_SPEED_SQR;
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

   private void resetFallbackProgressTracking(@Nullable BlockPos target) {
      this.fallbackProgressTarget = target;
      this.bestDistanceToFallbackTargetSqr = Double.MAX_VALUE;
      this.ticksWithoutFallbackProgress = 0;
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
      private final Mob owner;

      public CalamityNodeEvaluator(Mob owner) {
         this.owner = owner;
      }

      protected BlockPathTypes evaluateBlockPathType(BlockGetter getter, BlockPos pos, BlockPathTypes pathTypes) {
         return getLandOrAirCalamityBlockPathType(this.getMob(), getter, pos, super.evaluateBlockPathType(getter, pos, pathTypes));
      }

      protected Mob getMob() {
         return this.mob != null ? this.mob : this.owner;
      }
   }

   protected static class AirCalamityNodeEvaluator extends FlyNodeEvaluator {
      private final Mob owner;

      public AirCalamityNodeEvaluator(Mob owner) {
         this.owner = owner;
      }

      protected BlockPathTypes evaluateBlockPathType(BlockGetter getter, BlockPos pos, BlockPathTypes pathTypes) {
         return getLandOrAirCalamityBlockPathType(this.getMob(), getter, pos, super.evaluateBlockPathType(getter, pos, pathTypes));
      }

      protected Mob getMob() {
         return this.mob != null ? this.mob : this.owner;
      }
   }

   @FunctionalInterface
   private interface WaterNodeAvoidance {
      boolean shouldAvoid(int x, int y, int z);
   }

   protected static class WaterCalamityNodeEvaluator extends SwimNodeEvaluator {
      private final Mob owner;
      private final WaterNodeAvoidance avoidance;

      public WaterCalamityNodeEvaluator(Mob owner, WaterNodeAvoidance avoidance) {
         super(true);
         this.owner = owner;
         this.avoidance = avoidance;
      }

      public BlockPathTypes getBlockPathType(BlockGetter getter, int value, int value2, int value3) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(value, value2, value3);
         if (this.avoidance.shouldAvoid(value, value2, value3)) {
            return BlockPathTypes.BLOCKED;
         }

         BlockState blockstate1 = getter.getBlockState(blockpos$mutableblockpos);
         BlockPathTypes calamityBlockPathType = getCalamityBlockPathType(this.getMob(), getter, blockpos$mutableblockpos, super.getBlockPathType(getter, value, value2, value3));
         if (calamityBlockPathType != BlockPathTypes.BLOCKED && shouldAvoidFluidOrSnow(getter, blockpos$mutableblockpos, calamityBlockPathType)) {
            return BlockPathTypes.DANGER_OTHER;
         }
         if (blockstate1.isPathfindable(getter, blockpos$mutableblockpos, PathComputationType.WATER)) {
            return BlockPathTypes.WATER;
         }
         return calamityBlockPathType;

      }
      private boolean shouldAvoidFluidOrSnow(BlockGetter getter, BlockPos pos, BlockPathTypes pathType) {
         return pathType == BlockPathTypes.WATER_BORDER
                 || pathType == BlockPathTypes.LAVA
                 || pathType == BlockPathTypes.POWDER_SNOW
                 || pathType == BlockPathTypes.DANGER_POWDER_SNOW
                 || !getter.getFluidState(pos).isEmpty();
      }

      private Mob getMob() {
         return this.mob != null ? this.mob : this.owner;
      }
   }

   private static class TemporaryPath {
      private final BlockPos target;
      private final Path path;

      private TemporaryPath(BlockPos target, Path path) {
         this.target = target;
         this.path = path;
      }
   }
}
