package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FungalSaplings extends GenericFoliageBlock {
   public FungalSaplings() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP).randomTicks());
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
      if (state.is((Block)Sblocks.FUNGAL_STEM_SAPLING.get())) {
         int f = randomSource.nextInt(2, 5);

         for(int i = 0; i < f; ++i) {
            BlockState blockState = level.getBlockState(pos.above(i));
            if (blockState.isAir() || blockState.is((Block)Sblocks.FUNGAL_STEM_SAPLING.get())) {
               BlockPos blockPos = new BlockPos(pos.getX(), pos.getY() + i, pos.getZ());
               level.setBlock(blockPos, ((Block)Sblocks.FUNGAL_STEM.get()).defaultBlockState(), 3);
            }
         }

         for(int i = 0; i < f + 1; ++i) {
            BlockState blockState = level.getBlockState(pos.above(i));
            BlockState blockState1 = level.getBlockState(pos.above(i + 1));
            if (blockState.is((Block)Sblocks.FUNGAL_STEM.get()) && !blockState1.is((Block)Sblocks.FUNGAL_STEM.get())) {
               level.setBlock(pos.above(i), ((Block)Sblocks.FUNGAL_STEM_TOP.get()).defaultBlockState(), 3);
            }
         }
      }

   }
}
