package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.SBlockEntities.CabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.Nullable;

public class Cabinet extends BaseEntityBlock {
   public static final DirectionProperty FACING;
   public static final BooleanProperty OPEN;

   public Cabinet() {
      super(Properties.of().strength(2.0F, 2.0F).noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(OPEN, Boolean.FALSE));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{FACING, OPEN});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(FACING, rot.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation((Direction)state.getValue(FACING)));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      VoxelShape var10000;
      switch ((Direction)state.getValue(FACING)) {
         case EAST -> var10000 = box((double)0.0F, (double)0.0F, (double)0.0F, (double)8.0F, (double)16.0F, (double)16.0F);
         case WEST -> var10000 = box((double)8.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
         case NORTH -> var10000 = box((double)0.0F, (double)0.0F, (double)8.0F, (double)16.0F, (double)16.0F, (double)16.0F);
         default -> var10000 = box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)8.0F);
      }

      return var10000;
   }

   public InteractionResult use(BlockState p_49069_, Level level, BlockPos pos, Player player, InteractionHand p_49073_, BlockHitResult p_49074_) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = level.getBlockEntity(pos);
         if (blockentity instanceof CabinetBlockEntity) {
            CabinetBlockEntity containerBlockEntity = (CabinetBlockEntity)blockentity;
            player.openMenu(containerBlockEntity);
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
         }

         return InteractionResult.CONSUME;
      }
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
      return new CabinetBlockEntity(blockPos, blockState);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState states, boolean val) {
      if (!state.is(states.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(pos);
         if (blockentity instanceof CabinetBlockEntity) {
            CabinetBlockEntity blockEntity = (CabinetBlockEntity)blockentity;
            Containers.dropContents(level, pos, blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, states, val);
      }

   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
      OPEN = BlockStateProperties.OPEN;
   }
}
