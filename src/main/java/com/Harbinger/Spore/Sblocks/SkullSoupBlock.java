package com.Harbinger.Spore.Sblocks;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkullSoupBlock extends Block {
   public static final DirectionProperty FACING;

   public SkullSoupBlock() {
      super(Properties.of().strength(0.0F).sound(SoundType.BONE_BLOCK));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)5.0F, (double)0.0F, (double)5.0F, (double)11.0F, (double)5.0F, (double)11.0F).move(offset.x, offset.y, offset.z);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{FACING});
   }

   static {
      FACING = DirectionProperty.create("facing", Plane.HORIZONTAL);
   }
}
