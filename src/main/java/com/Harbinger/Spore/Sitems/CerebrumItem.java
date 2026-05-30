package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Sblocks.Cerebrum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CerebrumItem extends OrganItem {
   private final BlockState BlockState;

   public CerebrumItem(String value, String advancementId, BlockState state) {
      super(value, advancementId);
      this.BlockState = state;
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         BlockPos placePos = pos.relative(context.getClickedFace());
         BlockState state = (BlockState)this.BlockState.setValue(Cerebrum.FACING, context.getHorizontalDirection());
         if (!level.isClientSide && level.getBlockState(placePos).canBeReplaced()) {
            level.setBlock(placePos, state, 3);
            if (!player.getAbilities().instabuild) {
               context.getItemInHand().shrink(1);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }
}
