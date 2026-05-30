package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HeartPie extends Block {
   public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);

   public HeartPie() {
      super(Properties.of().strength(0.5F).sound(SoundType.SLIME_BLOCK));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(BITES, 0));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{BITES});
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)4.0F, (double)15.0F);
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!world.isClientSide) {
         ItemStack stack = new ItemStack((ItemLike)Sitems.SLICE_OF_HEARTPIE.get());
         int bites = (Integer)state.getValue(BITES);
         world.addFreshEntity(new ItemEntity(world, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, stack.copy()));
         if (bites < 3) {
            world.setBlock(pos, (BlockState)state.setValue(BITES, bites + 1), 2);
         } else {
            world.removeBlock(pos, false);
         }
      }

      return InteractionResult.SUCCESS;
   }
}
