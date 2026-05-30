package com.Harbinger.Spore.Sentities.MovementControls;

import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class UndergroundPathNavigation extends GroundPathNavigation {
   @Nullable
   private BlockPos pathToPosition;

   public UndergroundPathNavigation(PathfinderMob mob, Level level) {
      super(mob, level);
   }

   protected @NotNull PathFinder createPathFinder(int maxNodes) {
      NodeEvaluator evaluator = new HohlfresserNodeEvaluator();
      evaluator.setCanPassDoors(true);
      evaluator.canFloat();
      return new PathFinder(evaluator, maxNodes);
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

   protected @NotNull Vec3 getTempMobPos() {
      return this.mob.position().add((double)0.0F, (double)this.mob.getBbHeight() * (double)0.5F, (double)0.0F);
   }

   protected boolean canUpdatePath() {
      return this.mob.getTarget() != null && this.mob.getNavigation().isInProgress();
   }

   public boolean isStableDestination(BlockPos pos) {
      Mob var3 = this.mob;
      if (var3 instanceof Hohlfresser hohlfresser) {
         if (hohlfresser.canGoUnderground()) {
            return hohlfresser.isColliding(pos, this.level.getBlockState(pos));
         }
      }

      return super.isStableDestination(pos);
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

   }

   public static class HohlfresserNodeEvaluator extends SwimNodeEvaluator {
      public HohlfresserNodeEvaluator() {
         super(true);
      }

      public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
         return BlockPathTypes.OPEN;
      }

      public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {
         return this.getBlockPathType(blockGetter, x, y, z);
      }
   }
}
