package com.Harbinger.Spore.Sblocks;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Branch extends Block {
   public static final DirectionProperty FACING;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape EAST_AABB;
   protected static final VoxelShape WEST_AABB;
   protected static final VoxelShape SOUTH_AABB;
   protected static final VoxelShape NORTH_AABB;

   public Branch() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP));
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(WATERLOGGED, Boolean.FALSE));
   }

   public VoxelShape getShape(BlockState state, BlockGetter p_54373_, BlockPos p_54374_, CollisionContext p_54375_) {
      switch ((Direction)state.getValue(FACING)) {
         case NORTH:
            return NORTH_AABB;
         case SOUTH:
            return SOUTH_AABB;
         case WEST:
            return WEST_AABB;
         case EAST:
         default:
            return EAST_AABB;
      }
   }

   private boolean canAttachTo(BlockGetter p_54349_, BlockPos p_54350_, Direction p_54351_) {
      BlockState blockstate = p_54349_.getBlockState(p_54350_);
      return blockstate.isFaceSturdy(p_54349_, p_54350_, p_54351_);
   }

   public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
      Direction direction = (Direction)state.getValue(FACING);
      return this.canAttachTo(reader, pos.relative(direction.getOpposite()), direction);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState blockState, LevelAccessor accessor, BlockPos pos, BlockPos blockPos) {
      if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(accessor, pos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ((Boolean)state.getValue(WATERLOGGED)) {
            accessor.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(accessor));
         }

         return super.updateShape(state, direction, blockState, accessor, pos, blockPos);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      if (!context.replacingClickedOnBlock()) {
         BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
         if (blockstate.is(this) && blockstate.getValue(FACING) == context.getClickedFace()) {
            return null;
         }
      }

      BlockState blockstate1 = this.defaultBlockState();
      LevelReader levelreader = context.getLevel();
      BlockPos blockpos = context.getClickedPos();
      FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

      for(Direction direction : context.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal()) {
            blockstate1 = (BlockState)blockstate1.setValue(FACING, direction.getOpposite());
            if (blockstate1.canSurvive(levelreader, blockpos)) {
               return (BlockState)blockstate1.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{FACING, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
      EAST_AABB = Block.box((double)0.0F, (double)4.0F, (double)4.0F, (double)10.0F, (double)12.0F, (double)12.0F);
      WEST_AABB = Block.box((double)3.0F, (double)4.0F, (double)4.0F, (double)16.0F, (double)12.0F, (double)12.0F);
      SOUTH_AABB = Block.box((double)4.0F, (double)4.0F, (double)0.0F, (double)12.0F, (double)12.0F, (double)10.0F);
      NORTH_AABB = Block.box((double)4.0F, (double)4.0F, (double)3.0F, (double)12.0F, (double)12.0F, (double)16.0F);
   }
}
