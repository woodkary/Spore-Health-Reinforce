package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.SBlockEntities.ContainerBlockEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

public class Container extends BaseEntityBlock {
   public static final DirectionProperty FACING;

   public Container() {
      super(Properties.of().strength(2.0F, 2.0F).noOcclusion());
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{FACING});
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
      Vec3 offset = state.getOffset(world, pos);
      return box((double)2.0F, (double)0.0F, (double)2.0F, (double)14.0F, (double)16.0F, (double)14.0F).move(offset.x, offset.y, offset.z);
   }

   public InteractionResult use(BlockState p_49069_, Level p_49070_, BlockPos p_49071_, Player p_49072_, InteractionHand p_49073_, BlockHitResult p_49074_) {
      if (p_49070_.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = p_49070_.getBlockEntity(p_49071_);
         if (blockentity instanceof ContainerBlockEntity) {
            p_49072_.openMenu((ContainerBlockEntity)blockentity);
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(p_49072_, p_49072_.containerMenu));
         }

         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos p_152102_, BlockState p_152103_) {
      return new ContainerBlockEntity(p_152102_, p_152103_);
   }

   public RenderShape getRenderShape(BlockState p_49090_) {
      return RenderShape.MODEL;
   }

   public void onRemove(BlockState p_49076_, Level p_49077_, BlockPos p_49078_, BlockState p_49079_, boolean p_49080_) {
      if (!p_49076_.is(p_49079_.getBlock())) {
         BlockEntity blockentity = p_49077_.getBlockEntity(p_49078_);
         if (blockentity instanceof net.minecraft.world.Container) {
            Containers.dropContents(p_49077_, p_49078_, (net.minecraft.world.Container)blockentity);
            p_49077_.updateNeighbourForOutputSignal(p_49078_, this);
         }

         super.onRemove(p_49076_, p_49077_, p_49078_, p_49079_, p_49080_);
      }

   }

   public void appendHoverText(ItemStack itemStack, @org.jetbrains.annotations.Nullable BlockGetter blockGetter, List components, TooltipFlag flag) {
      if (Screen.hasShiftDown()) {
         components.add(Component.translatable("container.line.shift").withStyle(ChatFormatting.DARK_RED));
      } else {
         components.add(Component.translatable("container.line.normal").withStyle(ChatFormatting.GOLD));
      }

      super.appendHoverText(itemStack, blockGetter, components, flag);
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
   }
}
