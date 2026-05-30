package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.SBlockEntities.SurgeryTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SurgeryTableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING;
   public static final BooleanProperty WATERLOGGED;
   public static final VoxelShape SHAPE;

   public SurgeryTableBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(WATERLOGGED, Boolean.FALSE));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
      return new SurgeryTableBlockEntity(blockPos, blockState);
   }

   public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
      return SHAPE;
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (pState.getBlock() != pNewState.getBlock()) {
         BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
         if (blockEntity instanceof SurgeryTableBlockEntity) {
            ((SurgeryTableBlockEntity)blockEntity).drops();
         }
      }

      super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (!pLevel.isClientSide()) {
         BlockEntity entity = pLevel.getBlockEntity(pPos);
         if (!(entity instanceof SurgeryTableBlockEntity)) {
            throw new IllegalStateException("Our Container provider is missing!");
         }

         NetworkHooks.openScreen((ServerPlayer)pPlayer, (SurgeryTableBlockEntity)entity, pPos);
      }

      return InteractionResult.sidedSuccess(pLevel.isClientSide());
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createSurgeryTicker(level, type, (BlockEntityType)SblockEntities.SURGERY_TABLE_ENTITY.get());
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createSurgeryTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? null : createTickerHelper(type, p_151990_, SurgeryTableBlockEntity::serverTick);
   }

   public BlockState rotate(BlockState p_54360_, Rotation p_54361_) {
      return (BlockState)p_54360_.setValue(FACING, p_54361_.rotate((Direction)p_54360_.getValue(FACING)));
   }

   public BlockState mirror(BlockState p_54357_, Mirror p_54358_) {
      return p_54357_.rotate(p_54358_.getRotation((Direction)p_54357_.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      super.createBlockStateDefinition(stateBuilder);
      stateBuilder.add(new Property[]{FACING, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
      SHAPE = Block.box(0.1, (double)0.0F, 0.1, 15.9, (double)16.0F, 15.9);
   }
}
