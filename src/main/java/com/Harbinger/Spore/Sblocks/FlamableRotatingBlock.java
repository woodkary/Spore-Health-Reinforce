package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FlamableRotatingBlock extends RotatedPillarBlock {
   private final List BLOCKS;

   public FlamableRotatingBlock(Properties p_55926_) {
      super(p_55926_);
      this.BLOCKS = List.of((Block)Sblocks.WALL_GROWTHS.get(), (Block)Sblocks.WALL_GROWTHS_BIG.get(), (Block)Sblocks.WALL_GROWTHS_FLESHY.get());
   }

   public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return true;
   }

   public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 5;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 5;
   }

   public boolean isRandomlyTicking(BlockState state) {
      return state.is((Block)Sblocks.ROTTEN_LOG.get());
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextFloat() < 0.2F && state.is((Block)Sblocks.ROTTEN_LOG.get())) {
         this.tryPlaceLadderOnRandomFace(level, pos, random);
      }

   }

   private void tryPlaceLadderOnRandomFace(ServerLevel level, BlockPos pos, RandomSource random) {
      Direction[] directions = Direction.values();
      Direction randomDirection = directions[random.nextInt(directions.length)];
      if (!randomDirection.equals(Direction.DOWN) && !randomDirection.equals(Direction.UP)) {
         BlockPos targetPos = pos.relative(randomDirection);
         if (this.canPlaceFoliage(level, targetPos, randomDirection.getOpposite())) {
            Block block = (Block)this.BLOCKS.get(random.nextInt(this.BLOCKS.size()));
            BlockState ladderState = (BlockState)block.defaultBlockState().setValue(WallFolliage.FACING, randomDirection);
            level.setBlock(targetPos, ladderState, 3);
         }

      }
   }

   private boolean canPlaceFoliage(ServerLevel level, BlockPos pos, Direction attachedFace) {
      BlockState targetState = level.getBlockState(pos);
      if (!targetState.isAir() && !targetState.canBeReplaced()) {
         return false;
      } else {
         BlockPos attachedPos = pos.relative(attachedFace);
         BlockState attachedState = level.getBlockState(attachedPos);
         return attachedState.isFaceSturdy(level, attachedPos, attachedFace.getOpposite());
      }
   }
}
