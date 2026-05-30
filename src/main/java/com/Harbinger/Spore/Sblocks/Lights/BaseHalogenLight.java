package com.Harbinger.Spore.Sblocks.Lights;

import com.Harbinger.Spore.Core.Sblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseHalogenLight extends Block {
   public BaseHalogenLight(Properties properties) {
      super(properties);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box((double)0.0F, (double)12.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
   }

   protected boolean isPoweredVariant(BlockState state) {
      return state.getBlock() instanceof HalogenLightOn;
   }

   protected Block getOnBlock() {
      return (Block)Sblocks.HALOGEN_LIGHT_ON.get();
   }

   protected Block getOffBlock() {
      return (Block)Sblocks.HALOGEN_LIGHT.get();
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
      if (!level.isClientSide) {
         if (!state.is((Block)Sblocks.BROKEN_HALOGEN_LIGHT_ON.get()) && !state.is((Block)Sblocks.BROKEN_HALOGEN_LIGHT.get())) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean isCurrentlyOn = this.isPoweredVariant(state);
            if (hasSignal && !isCurrentlyOn) {
               this.turnOn(level, pos);
            } else if (!hasSignal && isCurrentlyOn) {
               this.turnOff(level, pos);
            }

         }
      }
   }

   private void turnOn(Level level, BlockPos startPos) {
      Block block = this.getOnBlock();
      level.updateNeighborsAt(startPos, block);
      level.updateNeighborsAt(startPos.below(), block);
      level.setBlock(startPos, block.defaultBlockState(), 3);
   }

   private void turnOff(Level level, BlockPos startPos) {
      Block block = this.getOffBlock();
      level.updateNeighborsAt(startPos, block);
      level.updateNeighborsAt(startPos.below(), block);
      level.setBlock(startPos, block.defaultBlockState(), 3);
   }
}
