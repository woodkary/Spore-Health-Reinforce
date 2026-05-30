package com.Harbinger.Spore.Sblocks;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LabBedBlock extends HorizontalDirectionalBlock {
   public static final EnumProperty PART = EnumProperty.create("part", TablePart.class);
   public static final DirectionProperty FACING;

   public LabBedBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(PART, TablePart.BOTTOM));
   }

   public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
      return Shapes.or(box(0.1, 0.1, 0.1, 15.9, 15.9, 15.9), new VoxelShape[0]);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getHorizontalDirection();
      BlockPos pos = context.getClickedPos();
      Level level = context.getLevel();
      BlockPos offsetPos = pos.relative(facing);
      return !level.getBlockState(offsetPos).canBeReplaced(context) ? null : (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, facing)).setValue(PART, TablePart.BOTTOM);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockPos otherPos = pos.relative(facing);
      BlockState otherState = (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, facing)).setValue(PART, TablePart.TOP);
      level.setBlock(otherPos, otherState, 3);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         TablePart part = (TablePart)state.getValue(PART);
         Direction facing = (Direction)state.getValue(FACING);
         BlockPos otherPartPos = part == TablePart.BOTTOM ? pos.relative(facing) : pos.relative(facing.getOpposite());
         if (level.getBlockState(otherPartPos).getBlock() == this) {
            level.destroyBlock(otherPartPos, false);
         }
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FACING, PART});
   }

   public List getDrops(BlockState state, LootParams.Builder builder) {
      return state.getValue(PART) == TablePart.BOTTOM ? super.getDrops(state, builder) : Collections.emptyList();
   }

   static {
      FACING = BlockStateProperties.HORIZONTAL_FACING;
   }

   public static enum TablePart implements StringRepresentable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private TablePart(String name) {
         this.name = name;
      }

      public String getSerializedName() {
         return this.name;
      }

      // $FF: synthetic method
      private static TablePart[] $values() {
         return new TablePart[]{TOP, BOTTOM};
      }
   }
}
