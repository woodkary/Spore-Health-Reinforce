package com.Harbinger.Spore.Sentities.AI;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class ClimberNodeNavigator extends NodeEvaluator {
   private final Long2ObjectMap pathTypesByPosCache = new Long2ObjectOpenHashMap();

   public Node getStart() {
      return this.getStartNode(this.mob.blockPosition());
   }

   public Target getGoal(double var1, double var3, double var5) {
      return this.getTargetFromNode(this.getNode(Mth.floor(var1), Mth.floor(var3), Mth.floor(var5)));
   }

   public void done() {
      super.done();
      this.pathTypesByPosCache.clear();
   }

   public int getNeighbors(Node[] options, Node current) {
      int[] numOptions = new int[]{0};

      for(Direction direction : Direction.values()) {
         this.findNode(current.x + direction.getStepX(), current.y + direction.getStepY(), current.z + direction.getStepZ()).filter((node) -> this.isNeighborValid(node, current)).ifPresent((newNode) -> {
            options[numOptions[0]++] = newNode;

            for(Direction direction2 : Direction.stream().filter((d) -> d != direction && d != direction.getOpposite()).toArray((x$0) -> new Direction[x$0])) {
               this.findNode(newNode.x + direction2.getStepX(), newNode.y + direction2.getStepY(), newNode.z + direction2.getStepZ()).filter((node) -> this.isNeighborValid(node, newNode)).ifPresent((newerNode) -> options[numOptions[0]++] = newerNode);
            }

         });
      }

      return numOptions[0];
   }

   protected boolean isNeighborValid(Node $$0, Node $$1) {
      return !$$0.closed && ($$0.costMalus >= 0.0F || $$1.costMalus < 0.0F);
   }

   private Optional<Node> findNode(int x, int y, int z) {
      Node node = null;
      if (!this.nextToClimbable(x, y, z)) {
         return Optional.empty();
      } else {
         BlockPathTypes pathTypes = this.getCachedBlockType(this.mob, x, y, z);
         float malus = this.mob.getPathfindingMalus(pathTypes);
         if (malus >= 0.0F) {
            node = this.getNodeAndUpdateCostToMax(x, y, z, pathTypes, malus);
         }

         return Optional.ofNullable(node);
      }
   }

   private boolean nextToClimbable(int x, int y, int z) {
      for(Direction direction : Direction.values()) {
         BlockPathTypes type = this.getCachedBlockType(this.mob, x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
         if (this.isClimbableType(type)) {
            return true;
         }

         for(Direction direction2 : (Direction[])Direction.stream().filter((d) -> d != direction && d != direction.getOpposite()).toArray((x$0) -> new Direction[x$0])) {
            BlockPathTypes type2 = this.getCachedBlockType(this.mob, x + direction.getStepX() + direction2.getStepX(), y + direction.getStepY() + direction2.getStepY(), z + direction.getStepZ() + direction2.getStepZ());
            if (this.isClimbableType(type2)) {
               return true;
            }
         }
      }

      return false;
   }

   protected boolean isClimbableType(BlockPathTypes type) {
      return type != BlockPathTypes.OPEN && type != BlockPathTypes.WALKABLE;
   }

   private Node getNodeAndUpdateCostToMax(int $$0, int $$1, int $$2, BlockPathTypes $$3, float $$4) {
      Node $$5 = this.getNode($$0, $$1, $$2);
      $$5.type = $$3;
      $$5.costMalus = Math.max($$5.costMalus, $$4);
      return $$5;
   }

   public BlockPathTypes getBlockPathType(BlockGetter var1, int var2, int var3, int var4, Mob var5) {
      return WalkNodeEvaluator.getBlockPathTypeStatic(var1, new BlockPos.MutableBlockPos(var2, var3, var4));
   }

   public BlockPathTypes getBlockPathType(BlockGetter var1, int var2, int var3, int var4) {
      return WalkNodeEvaluator.getBlockPathTypeStatic(var1, new BlockPos.MutableBlockPos(var2, var3, var4));
   }

   protected Node getStartNode(BlockPos $$0) {
      Node $$1 = this.getNode($$0);
      $$1.type = this.getBlockPathType(this.mob, $$1.asBlockPos());
      $$1.costMalus = this.mob.getPathfindingMalus($$1.type);
      return $$1;
   }

   protected BlockPathTypes getBlockPathType(Mob $$0, BlockPos $$1) {
      return this.getCachedBlockType($$0, $$1.getX(), $$1.getY(), $$1.getZ());
   }

   protected BlockPathTypes getCachedBlockType(Mob $$0, int $$1, int $$2, int $$3) {
      return (BlockPathTypes)this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong($$1, $$2, $$3), ($$4) -> this.getBlockPathType(this.level, $$1, $$2, $$3, $$0));
   }
}
