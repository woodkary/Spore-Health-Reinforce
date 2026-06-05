package com.Harbinger.Spore.Sblocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class CasingBiomassBlock extends Block {
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public CasingBiomassBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)this.stateDefinition.any().setValue(LIT, false));
   }

   public static int lightLevel(BlockState state) {
      return state.hasProperty(LIT) && (Boolean)state.getValue(LIT) ? 15 : 0;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(LIT);
   }
}
