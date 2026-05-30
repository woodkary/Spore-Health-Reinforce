package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CDUBlock extends BaseEntityBlock {
   public static final BooleanProperty LIT;
   public static final DirectionProperty FACING;

   public CDUBlock() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, false)).setValue(FACING, Direction.NORTH));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CDUBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   public static void replaceCDU(BlockPos pos, Level level) {
      if (level != null && !level.isClientSide) {
         BlockState blockState = level.getBlockState(pos);
         if (blockState.getBlock() == Sblocks.CDU.get()) {
            BlockState newState = (BlockState)blockState.setValue(LIT, true);
            level.setBlock(pos, newState, 3);
         }

      }
   }

   public static boolean isCDUUsable(BlockPos pos, Level level) {
      if (level != null && !level.isClientSide) {
         BlockState state = level.getBlockState(pos);
         if (!state.getBlock().equals(Sblocks.CDU.get())) {
            return true;
         } else {
            return !(Boolean)state.getValue(LIT);
         }
      } else {
         return false;
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      BlockEntity entity = level.getBlockEntity(pos);
      if (!(Boolean)state.getValue(LIT) && entity instanceof CDUBlockEntity blockEntity) {
         if (blockEntity.getFuel() > 0) {
            Vec3 localOffset = new Vec3((double)0.75F, (double)1.0F, (double)0.75F);
            Vec3 rotated = rotateOffset(localOffset, (Direction)state.getValue(FACING));
            double px = (double)pos.getX() + rotated.x;
            double py = (double)pos.getY() + rotated.y;
            double pz = (double)pos.getZ() + rotated.z;

            for(int i = 0; i < 360; i += 20) {
               double yy = Math.sin((double)i) * Math.cos((double)i) * (double)0.25F;
               level.addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, Math.cos((double)i) * 0.15, yy, Math.sin((double)i) * 0.15);
            }
         }
      }

   }

   public static Vec3 rotateOffset(Vec3 offset, Direction facing) {
      double x = offset.x;
      double z = offset.z;
      Vec3 var10000;
      switch (facing) {
         case NORTH -> var10000 = new Vec3(x, offset.y, z);
         case SOUTH -> var10000 = new Vec3((double)1.0F - x, offset.y, (double)1.0F - z);
         case WEST -> var10000 = new Vec3(z, offset.y, (double)1.0F - x);
         case EAST -> var10000 = new Vec3((double)1.0F - z, offset.y, x);
         default -> var10000 = offset;
      }

      return var10000;
   }

   public BlockState rotate(BlockState p_54360_, Rotation p_54361_) {
      return (BlockState)p_54360_.setValue(FACING, p_54361_.rotate((Direction)p_54360_.getValue(FACING)));
   }

   public BlockState mirror(BlockState p_54357_, Mirror p_54358_) {
      return p_54357_.rotate(p_54358_.getRotation((Direction)p_54357_.getValue(FACING)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box(0.1, (double)0.0F, 0.1, 15.9, (double)16.0F, 15.9).move(offset.x, offset.y, offset.z);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder blockStateBuilder) {
      super.createBlockStateDefinition(blockStateBuilder);
      blockStateBuilder.add(new Property[]{LIT}).add(new Property[]{FACING});
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      super.use(state, level, pos, player, hand, result);
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity instanceof CDUBlockEntity blockEntity) {
         ItemStack item = player.getItemInHand(hand);
         if (item.getItem() == Sitems.ICE_CANISTER.get()) {
            if (blockEntity.getFuel() > 0) {
               int var11 = blockEntity.getFuel();
               player.displayClientMessage(Component.literal("Current fuel " + var11 + "/" + blockEntity.maxFuel), true);
            } else {
               level.playLocalSound(pos, (SoundEvent)Ssounds.CDU_INSERT.get(), SoundSource.BLOCKS, 2.0F, 2.0F, true);
               level.playLocalSound(pos, (SoundEvent)Ssounds.CDU_AMBIENT.get(), SoundSource.BLOCKS, 1.0F, 1.0F, true);
               blockEntity.setFuel(blockEntity.maxFuel);
               item.shrink(1);
            }

            return InteractionResult.SUCCESS;
         }

         if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            if (!level.isClientSide) {
               NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
               return InteractionResult.SUCCESS;
            }
         }

         int var10001 = blockEntity.getFuel();
         player.displayClientMessage(Component.literal("Current fuel " + var10001 + "/" + blockEntity.maxFuel), true);
      }

      return InteractionResult.SUCCESS;
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createCDUTicker(level, type, (BlockEntityType)SblockEntities.CDU.get());
   }

   public int getEntityFuel(Level level, BlockPos pos) {
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity instanceof CDUBlockEntity blockEntity) {
         return blockEntity.getFuel();
      } else {
         return 0;
      }
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createCDUTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? createTickerHelper(type, p_151990_, CDUBlockEntity::clientTick) : createTickerHelper(type, p_151990_, CDUBlockEntity::serverTick);
   }

   public void setFuelTag(ItemStack stack, int value) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putInt("fuel", value);
   }

   public int getFuelTag(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      return tag.getInt("fuel");
   }

   public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List components, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, getter, components, tooltipFlag);
      components.add(Component.translatable("cdu.line").withStyle(ChatFormatting.BLUE));
      components.add(((Item)Sitems.ICE_CANISTER.get()).getDescription());
      int var10001 = this.getFuelTag(stack);
      components.add(Component.literal(var10001 + "/12000").withStyle(ChatFormatting.DARK_BLUE));
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      ItemStack stack = new ItemStack(this);
      this.setFuelTag(stack, this.getEntityFuel(level, pos));
      ItemEntity item = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
      level.addFreshEntity(item);
      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
      super.setPlacedBy(level, pos, state, entity, stack);
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof CDUBlockEntity cduBlockEntity) {
         CompoundTag tag = stack.getOrCreateTag();
         cduBlockEntity.setFuel(tag.getInt("fuel"));
      }

   }

   static {
      LIT = BlockStateProperties.LIT;
      FACING = HorizontalDirectionalBlock.FACING;
   }
}
