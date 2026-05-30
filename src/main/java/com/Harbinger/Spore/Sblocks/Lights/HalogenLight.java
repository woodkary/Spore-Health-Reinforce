package com.Harbinger.Spore.Sblocks.Lights;

import com.Harbinger.Spore.Core.Sblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolActions;

public class HalogenLight extends BaseHalogenLight {
   public HalogenLight() {
      super(Properties.of().strength(6.0F, 4.0F).sound(SoundType.GLASS));
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem().canPerformAction(stack, ToolActions.PICKAXE_DIG) && !level.isClientSide()) {
         player.playNotifySound(SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.setBlock(pos, ((Block)Sblocks.BROKEN_HALOGEN_LIGHT.get()).defaultBlockState(), 3);
      }

      return super.use(state, level, pos, player, hand, blockHitResult);
   }
}
