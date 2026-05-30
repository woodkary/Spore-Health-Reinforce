package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.SBlockEntities.BrainRemnantBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BrainRemnants extends BaseEntityBlock {
   public static final BooleanProperty LIT;
   public static final BooleanProperty OCCUPIED;

   public BrainRemnants() {
      super(Properties.of().sound(SoundType.STONE).strength(6.0F, 20.0F));
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, false)).setValue(OCCUPIED, false));
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BrainRemnantBlockEntity(pos, state, (Boolean)state.getValue(LIT), (Boolean)state.getValue(OCCUPIED));
   }

   public RenderShape getRenderShape(BlockState state) {
      return (Boolean)state.getValue(LIT) ? RenderShape.MODEL : RenderShape.INVISIBLE;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box(0.1, (double)0.0F, 0.1, 15.9, (double)16.0F, 15.9).move(offset.x, offset.y, offset.z);
   }

   @javax.annotation.Nullable
   public BlockEntityTicker getTicker(Level level, BlockState p_153274_, BlockEntityType type) {
      return createBrainTicker(level, type, (BlockEntityType)SblockEntities.BRAIN_REMNANTS.get());
   }

   @javax.annotation.Nullable
   protected static BlockEntityTicker createBrainTicker(Level level, BlockEntityType type, BlockEntityType p_151990_) {
      return level.isClientSide ? createTickerHelper(type, p_151990_, BrainRemnantBlockEntity::clientTick) : createTickerHelper(type, p_151990_, BrainRemnantBlockEntity::serverTick);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder blockStateBuilder) {
      super.createBlockStateDefinition(blockStateBuilder);
      blockStateBuilder.add(new Property[]{LIT}).add(new Property[]{OCCUPIED});
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      super.use(state, level, pos, player, hand, result);
      BlockEntity entity = level.getBlockEntity(pos);
      if (entity instanceof BrainRemnantBlockEntity brainRemnantBlock) {
         if (player.getItemInHand(hand).getItem() == Items.FLINT_AND_STEEL && !(Boolean)state.getValue(LIT)) {
            level.setBlock(pos, (BlockState)this.defaultBlockState().setValue(LIT, true), 3);
            brainRemnantBlock.setOnFire(true);
            brainRemnantBlock.ticksOnFire = 1;
            level.playSound(player, pos, (SoundEvent)Ssounds.BROKEN_SCREAMS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   static {
      LIT = BlockStateProperties.LIT;
      OCCUPIED = BlockStateProperties.OCCUPIED;
   }
}
