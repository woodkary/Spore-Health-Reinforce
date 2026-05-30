package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Cerebrum extends Block {
   public static final DirectionProperty FACING;

   public Cerebrum() {
      super(Properties.of().strength(0.0F).sound(SoundType.SLIME_BLOCK));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)4.0F, (double)15.0F).move(offset.x, offset.y, offset.z);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{FACING});
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      if (state.is((Block)Sblocks.CEREBRUM_BLOCK.get())) {
         return new ItemStack((ItemLike)Sitems.CEREBRUM.get());
      } else if (state.is((Block)Sblocks.INNARDS_BLOCK.get())) {
         return new ItemStack((ItemLike)Sitems.INNARDS.get());
      } else if (state.is((Block)Sblocks.HEART_BLOCK.get())) {
         return new ItemStack((ItemLike)Sitems.MUTATED_HEART.get());
      } else {
         return state.is((Block)Sblocks.BRAIO_BLOCK.get()) ? new ItemStack((ItemLike)Sitems.ALVEOLIC_SACK.get()) : ItemStack.EMPTY;
      }
   }

   static {
      FACING = DirectionProperty.create("facing", Plane.HORIZONTAL);
   }
}
