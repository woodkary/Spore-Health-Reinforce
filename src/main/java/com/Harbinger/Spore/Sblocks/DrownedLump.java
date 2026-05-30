package com.Harbinger.Spore.Sblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class DrownedLump extends GenericFoliageBlock {
   public static final BooleanProperty SHORT;

   public DrownedLump() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(SHORT, Boolean.TRUE)).setValue(SHORT, Boolean.TRUE));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      stateBuilder.add(new Property[]{WATERLOGGED}).add(new Property[]{SHORT});
   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Boolean)state.getValue(SHORT);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return fluid.getType() == Fluids.WATER ? (BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, true)).setValue(SHORT, true) : null;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      FluidState fluid = level.getFluidState(pos);
      return fluid.getType() == Fluids.WATER;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      BlockPos above = pos.above();
      FluidState fluidAbove = level.getFluidState(above);
      boolean hasWaterAbove = fluidAbove.getType() == Fluids.WATER;
      if (hasWaterAbove && (Boolean)state.getValue(SHORT)) {
         level.setBlock(pos, (BlockState)state.setValue(SHORT, false), 2);
      } else if (!hasWaterAbove && !(Boolean)state.getValue(SHORT)) {
         level.setBlock(pos, (BlockState)state.setValue(SHORT, true), 2);
      }

   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   static {
      SHORT = BlockStateProperties.SHORT;
   }
}
