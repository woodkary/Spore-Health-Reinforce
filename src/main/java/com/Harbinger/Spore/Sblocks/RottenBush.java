package com.Harbinger.Spore.Sblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class RottenBush extends GenericFoliageBlock {
   public static final int MAX_AGE = 5;
   public static final IntegerProperty AGE;

   public RottenBush() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0)).setValue(WATERLOGGED, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      super.createBlockStateDefinition(stateBuilder);
      stateBuilder.add(new Property[]{AGE});
   }

   public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return true;
   }

   public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 10;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 15;
   }

   public boolean isRandomlyTicking(BlockState state) {
      return super.isRandomlyTicking(state) || (Integer)state.getValue(AGE) < 5;
   }

   public BlockState getStateForAge(int va) {
      return (BlockState)this.defaultBlockState().setValue(AGE, va);
   }

   public void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource randomSource) {
      super.randomTick(state, serverLevel, pos, randomSource);
      if ((Integer)state.getValue(AGE) < 5 && Math.random() < 0.3) {
         serverLevel.setBlock(pos, this.getStateForAge((Integer)state.getValue(AGE) + 1), 2);
      } else if ((Integer)state.getValue(AGE) >= 5) {
         serverLevel.removeBlock(pos, false);
      }

   }

   static {
      AGE = BlockStateProperties.AGE_5;
   }
}
