package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class UnderWaterFungalStem extends GenericFoliageBlock {
   public UnderWaterFungalStem() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, Boolean.TRUE));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)4.0F, (double)0.0F, (double)4.0F, (double)12.0F, (double)16.0F, (double)12.0F).move(offset.x, offset.y, offset.z);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState groundState = worldIn.getBlockState(blockpos);
      return this.mayPlaceOn(groundState, worldIn, blockpos);
   }

   protected boolean mayPlaceOn(BlockState blockState, BlockGetter getter, BlockPos blockPos) {
      return blockState.canOcclude() || blockState.getBlock() == Sblocks.UNDERWATER_FUNGAL_STEM.get();
   }
}
