package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Sentities.FoliageSpread;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FungalBonemeal extends BaseItem implements FoliageSpread {
   private static final RandomSource random = RandomSource.create();

   public FungalBonemeal() {
      super(new Properties());
   }

   public InteractionResult useOn(UseOnContext context) {
      ItemStack stack;
      label12: {
         BlockPos pos = context.getClickedPos();
         Level level = context.getLevel();
         stack = context.getItemInHand();
         BlockState state = level.getBlockState(pos);
         Block var8 = state.getBlock();
         if (var8 instanceof MushroomBlock mushroomBlock) {
            if (level instanceof ServerLevel serverLevel) {
               mushroomBlock.performBonemeal(serverLevel, random, pos, state);
               break label12;
            }
         }

         this.SpreadInfection(level, (double)7.0F, pos);
      }

      stack.shrink(1);
      return InteractionResult.SUCCESS;
   }
}
