package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sitems;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CrustedBile extends Block implements BucketPickup {
   public CrustedBile() {
      super(Properties.of().strength(2.0F, 2.0F).sound(SoundType.SLIME_BLOCK));
   }

   public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
      levelAccessor.removeBlock(blockPos, false);
      return new ItemStack((ItemLike)Sitems.BUCKET_OF_BILE.get());
   }

   public Optional getPickupSound() {
      return Optional.of(SoundEvents.SLIME_HURT);
   }
}
