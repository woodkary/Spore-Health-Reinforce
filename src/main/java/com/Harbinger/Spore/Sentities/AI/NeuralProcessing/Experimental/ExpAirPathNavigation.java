package com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ExpAirPathNavigation extends FlyingPathNavigation {
   static final float EPSILON = 1.0E-8F;

   public ExpAirPathNavigation(Mob entity, Level world) {
      super(entity, world);
   }

   protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
      this.nodeEvaluator = new FlyNodeEvaluator();
      this.nodeEvaluator.setCanPassDoors(true);
      return new ExpPathFinder(this.nodeEvaluator, maxVisitedNodes);
   }

   /** @deprecated */
   @Deprecated
   public void hardStop() {
      this.path = null;
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
      if (this.tryShortcut(path, new Vec3(this.mob.getX(), this.mob.getY(), this.mob.getZ()), pathLength, base, max) && (this.isAt(path, 0.5F) || this.atElevationChange(path) && this.isAt(path, this.mob.getBbWidth() * 0.5F))) {
         path.setNextNodeIndex(path.getNextNodeIndex() + 1);
      }

      this.doStuckDetection(entityPos);
   }

   public void tick() {
      super.tick();
      if (this.getTargetPos() != null) {
         this.mob.getLookControl().setLookAt((double)this.getTargetPos().getX(), (double)this.getTargetPos().getY(), (double)this.getTargetPos().getZ());
      }

   }

   private boolean isAt(Path path, float threshold) {
      Vec3 pathPos = path.getNextEntityPos(this.mob);
      return Mth.abs((float)(this.mob.getX() - pathPos.x)) < threshold && Mth.abs((float)(this.mob.getZ() - pathPos.z)) < threshold && Math.abs(this.mob.getY() - pathPos.y) < (double)1.0F;
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
               int z = z0;

               while(z != z1) {
                  for(int y = y0; y != y1; y += stepy) {
                     BlockState block = this.level.getBlockState(pos.set(x, y, z));
                     if (!block.isPathfindable(this.level, new BlockPos(x, y, z), PathComputationType.AIR)) {
                        return false;
                     }
                  }

                  BlockPathTypes below = this.nodeEvaluator.getBlockPathType(this.level, x, y0 - 1, z);
                  if (below != BlockPathTypes.WATER && below != BlockPathTypes.LAVA && below != BlockPathTypes.OPEN) {
                     BlockPathTypes in = this.nodeEvaluator.getBlockPathType(this.level, x, y0, z);
                     float priority = this.mob.getPathfindingMalus(in);
                     if (!(priority < 0.0F) && !(priority >= 8.0F)) {
                        if (in != BlockPathTypes.DAMAGE_FIRE && in != BlockPathTypes.DANGER_FIRE && in != BlockPathTypes.DAMAGE_OTHER) {
                           z += stepz;
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }

                  return false;
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
}
