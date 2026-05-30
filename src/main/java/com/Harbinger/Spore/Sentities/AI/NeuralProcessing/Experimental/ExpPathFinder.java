package com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpPathFinder extends PathFinder {
   public ExpPathFinder(NodeEvaluator processor, int maxVisitedNodes) {
      super(processor, maxVisitedNodes);
   }

   public @Nullable Path findPath(@NotNull PathNavigationRegion regionIn, @NotNull Mob mob, @NotNull Set targetPositions, float maxRange, int accuracy, float searchDepthMultiplier) {
      Path path = super.findPath(regionIn, mob, targetPositions, maxRange, accuracy, searchDepthMultiplier);
      return path == null ? null : new PatchedPath(path);
   }

   protected float distance(Node first, @NotNull Node second) {
      return first.distanceTo(second);
   }

   static class PatchedPath extends Path {
      public PatchedPath(Path original) {
         super(copyPathPoints(original), original.getTarget(), original.canReach());
      }

      private static List copyPathPoints(Path original) {
         List<Node> points = new ArrayList();

         for(int i = 0; i < original.getNodeCount(); ++i) {
            points.add(original.getNode(i));
         }

         return points;
      }

      public @NotNull Vec3 getEntityPosAtNode(Entity entity, int index) {
         Node point = this.getNode(index);
         double d0 = (double)point.x + (double)Mth.floor(entity.getBbWidth() + 1.0F) * (double)0.5F;
         double d1 = (double)point.y;
         double d2 = (double)point.z + (double)Mth.floor(entity.getBbWidth() + 1.0F) * (double)0.5F;
         return new Vec3(d0, d1, d2);
      }
   }
}
