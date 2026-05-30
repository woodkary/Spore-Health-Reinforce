package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.SBlockEntities.ZoaholicBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZoaholicBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING;

   public ZoaholicBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ZoaholicBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box(0.1, (double)0.0F, 0.1, 15.9, (double)16.0F, 15.9).move(offset.x, offset.y, offset.z);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      super.use(state, level, pos, player, hand, result);
      BlockEntity entity = level.getBlockEntity(pos);
      if (!(entity instanceof ZoaholicBlockEntity zoaholicBlock)) {
         return InteractionResult.PASS;
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (!zoaholicBlock.HasBrain() && stack.getItem() == Sitems.CEREBRUM.get()) {
            zoaholicBlock.setBrain(true);
            stack.shrink(1);
         } else if (!zoaholicBlock.HasHeart() && stack.getItem() == Sitems.MUTATED_HEART.get()) {
            zoaholicBlock.setHasHeart(true);
            stack.shrink(1);
         } else if (!zoaholicBlock.hasEnoughInnards() && stack.getItem() == Sitems.INNARDS.get()) {
            zoaholicBlock.setAmountOfInnards(zoaholicBlock.getAmountOfInnards() + 1);
            stack.shrink(1);
         } else if (zoaholicBlock.getBiomass() <= 9000 && stack.getItem() == Sitems.BIOMASS.get()) {
            zoaholicBlock.addBiomass(3000);
            stack.shrink(1);
         } else if (zoaholicBlock.isActive() && zoaholicBlock.getProcessing() <= 0) {
            zoaholicBlock.setProcessing(200);
         } else if (zoaholicBlock.HasHeart() && zoaholicBlock.hasEnoughInnards() && zoaholicBlock.HasBrain()) {
            String string = Component.translatable(((Item)Sitems.BIOMASS.get()).getDescriptionId()).getString();
            player.displayClientMessage(Component.literal(string + " " + zoaholicBlock.getBiomass() + "/12000"), true);
         } else {
            player.displayClientMessage(Component.translatable("zoaholic.line_1"), true);
         }

         if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            if (!level.isClientSide) {
               NetworkHooks.openScreen(serverPlayer, zoaholicBlock, pos);
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createCDUTicker(level, type, (BlockEntityType)SblockEntities.ZOAHOLIC.get());
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createCDUTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? createTickerHelper(type, p_151990_, ZoaholicBlockEntity::clientTick) : createTickerHelper(type, p_151990_, ZoaholicBlockEntity::serverTick);
   }

   public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List components, TooltipFlag tooltipFlag) {
      CompoundTag tag = stack.getOrCreateTag();
      if (this.getBrain(tag) && this.getHeart(tag) && this.getInnards(tag) >= 2) {
         String string = Component.translatable(((Item)Sitems.BIOMASS.get()).getDescriptionId()).getString();
         components.add(Component.literal(string + " " + this.getBiomassTag(tag) + "/12000"));
      } else {
         components.add(Component.translatable("zoaholic.line_2"));
         if (!this.getBrain(tag)) {
            components.add(((Item)Sitems.CEREBRUM.get()).getDescription());
         }

         if (!this.getHeart(tag)) {
            components.add(((Item)Sitems.MUTATED_HEART.get()).getDescription());
         }

         if (this.getInnards(tag) < 1) {
            components.add(((Item)Sitems.INNARDS.get()).getDescription());
         }

         if (this.getInnards(tag) < 2) {
            components.add(((Item)Sitems.INNARDS.get()).getDescription());
         }
      }

      super.appendHoverText(stack, getter, components, tooltipFlag);
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      BlockEntity var8 = level.getBlockEntity(pos);
      if (var8 instanceof ZoaholicBlockEntity zoaholicBlockEntity) {
         ItemStack stack = new ItemStack(this);
         CompoundTag tag = stack.getOrCreateTag();
         this.setBiomassTag(zoaholicBlockEntity.getBiomass(), tag);
         this.setHeart(zoaholicBlockEntity.HasHeart(), tag);
         this.setBrain(zoaholicBlockEntity.HasBrain(), tag);
         this.setInnards(zoaholicBlockEntity.getAmountOfInnards(), tag);
         ItemEntity item = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
         level.addFreshEntity(item);
      }

      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack) {
      super.setPlacedBy(level, pos, state, entity, stack);
      BlockEntity var7 = level.getBlockEntity(pos);
      if (var7 instanceof ZoaholicBlockEntity zoaholicBlock) {
         CompoundTag tag = stack.getOrCreateTag();
         zoaholicBlock.setBiomass(this.getBiomassTag(tag));
         zoaholicBlock.setAmountOfInnards(this.getInnards(tag));
         zoaholicBlock.setHasHeart(this.getHeart(tag));
         zoaholicBlock.setBrain(this.getBrain(tag));
      }

   }

   public void setBiomassTag(int value, CompoundTag tag) {
      tag.putInt("biomass", value);
   }

   public int getBiomassTag(CompoundTag tag) {
      return tag.getInt("biomass");
   }

   public void setHeart(boolean value, CompoundTag tag) {
      tag.putBoolean("heart", value);
   }

   public boolean getHeart(CompoundTag tag) {
      return tag.getBoolean("heart");
   }

   public void setBrain(boolean value, CompoundTag tag) {
      tag.putBoolean("brain", value);
   }

   public boolean getBrain(CompoundTag tag) {
      return tag.getBoolean("brain");
   }

   public void setInnards(int value, CompoundTag tag) {
      tag.putInt("innards", value);
   }

   public int getInnards(CompoundTag tag) {
      return tag.getInt("innards");
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

   static {
      FACING = HorizontalDirectionalBlock.FACING;
   }
}
