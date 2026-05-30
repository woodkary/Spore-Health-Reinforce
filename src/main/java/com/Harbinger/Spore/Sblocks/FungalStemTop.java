package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FungalStemTop extends GenericFoliageBlock {
   public FungalStemTop() {
      super(Properties.of().strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP));
   }

   public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState groundState = worldIn.getBlockState(blockpos);
      return this.mayPlaceOn(groundState, worldIn, blockpos);
   }

   protected boolean mayPlaceOn(BlockState blockState, BlockGetter p_51043_, BlockPos p_51044_) {
      return blockState.canOcclude() || blockState.getBlock() == Sblocks.FUNGAL_STEM.get();
   }
}
