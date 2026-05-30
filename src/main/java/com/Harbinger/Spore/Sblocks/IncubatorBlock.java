package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.SBlockEntities.IncubatorBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class IncubatorBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING;

   public IncubatorBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new IncubatorBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box(0.1, (double)0.0F, 0.1, 15.9, (double)16.0F, 15.9).move(offset.x, offset.y, offset.z);
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createIncubatorTicker(level, type, (BlockEntityType)SblockEntities.INCUBATOR.get());
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createIncubatorTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? createTickerHelper(type, p_151990_, IncubatorBlockEntity::clientTick) : createTickerHelper(type, p_151990_, IncubatorBlockEntity::serverTick);
   }

   public BlockState rotate(BlockState p_54360_, Rotation p_54361_) {
      return (BlockState)p_54360_.setValue(FACING, p_54361_.rotate((Direction)p_54360_.getValue(FACING)));
   }

   public BlockState mirror(BlockState p_54357_, Mirror p_54358_) {
      return p_54357_.rotate(p_54358_.getRotation((Direction)p_54357_.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder p_54370_) {
      p_54370_.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      super.use(state, level, pos, player, hand, result);
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity instanceof IncubatorBlockEntity blockEntity) {
         ItemStack item = player.getItemInHand(hand);
         if (item.getItem() == Sitems.BIOMASS.get() && blockEntity.getFuel() <= 750) {
            blockEntity.setFuel(blockEntity.getFuel() + 250);
            item.shrink(1);
            return InteractionResult.SUCCESS;
         }

         if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
            return InteractionResult.SUCCESS;
         }

         if (!blockEntity.getItem(0).isEmpty()) {
            if (level.isClientSide) {
               blockEntity.setItem(0, ItemStack.EMPTY);
            } else {
               popResource(level, pos, blockEntity.getItem(0).copy());
               blockEntity.setItem(0, ItemStack.EMPTY);
            }

            return InteractionResult.SUCCESS;
         }

         if (item.is(ItemTags.create(new ResourceLocation("spore:weapons")))) {
            if (level.isClientSide) {
               blockEntity.setItem(0, item.copy());
            } else {
               blockEntity.setItem(0, item.copy());
               item.shrink(1);
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   public void setFuelTag(ItemStack stack, int value) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putInt("fuel", value);
   }

   public int getFuelTag(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      return tag.getInt("fuel");
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      BlockEntity var8 = level.getBlockEntity(pos);
      if (var8 instanceof IncubatorBlockEntity incubatorBlock) {
         ItemStack stack = new ItemStack(this);
         this.setFuelTag(stack, incubatorBlock.getFuel());
         ItemEntity item = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
         level.addFreshEntity(item);
         popResource(level, pos, incubatorBlock.getItem(0).copy());
      }

      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
      super.setPlacedBy(level, pos, state, entity, stack);
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof IncubatorBlockEntity incubatorBlock) {
         incubatorBlock.setFuel(this.getFuelTag(stack));
      }

   }

   public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List components, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, getter, components, tooltipFlag);
      String string = Component.translatable(((Item)Sitems.BIOMASS.get()).getDescriptionId()).getString();
      components.add(Component.literal(string + " " + this.getFuelTag(stack) + "/1000"));
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
   }
}
