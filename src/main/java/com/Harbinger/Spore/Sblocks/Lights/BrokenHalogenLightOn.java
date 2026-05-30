package com.Harbinger.Spore.Sblocks.Lights;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.HitResult;

public class BrokenHalogenLightOn extends BaseHalogenLight {
   public BrokenHalogenLightOn() {
      super(Properties.of().strength(6.0F, 4.0F).sound(SoundType.GLASS).lightLevel((s) -> 4).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true));
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.tick(state, level, pos, random);
      level.setBlock(pos, ((Block)Sblocks.BROKEN_HALOGEN_LIGHT.get()).defaultBlockState(), 3);
   }

   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return 4;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      return new ItemStack((ItemLike)Sitems.BROKEN_HALOGEN_LIGHT.get());
   }
}
