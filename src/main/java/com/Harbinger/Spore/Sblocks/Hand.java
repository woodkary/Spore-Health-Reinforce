package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Hand extends HangingRootsBlock {
   public static final BooleanProperty ENABLED;
   protected static final VoxelShape SHAPE;

   public Hand() {
      super(Properties.of().strength(4.0F, 2.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(ENABLED, Boolean.FALSE)).setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE));
   }

   public VoxelShape getShape(BlockState p_153342_, BlockGetter p_153343_, BlockPos p_153344_, CollisionContext p_153345_) {
      return SHAPE;
   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Boolean)state.getValue(ENABLED);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder builder) {
      builder.add(new Property[]{ENABLED}).add(new Property[]{BlockStateProperties.WATERLOGGED});
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      super.entityInside(state, level, pos, entity);
      if (!(Boolean)state.getValue(ENABLED)) {
         level.setBlock(pos, (BlockState)((BlockState)((BlockState)((Block)Sblocks.HAND.get()).defaultBlockState().setValue(ENABLED, true)).setValue(ENABLED, true)).setValue(BlockStateProperties.WATERLOGGED, false), 3);
      } else if (entity instanceof LivingEntity) {
         LivingEntity living = (LivingEntity)entity;
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            entity.makeStuckInBlock(state, new Vec3((double)0.4F, (double)0.0F, (double)0.4F));
         }
      }

   }

   public void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource randomSource) {
      super.randomTick(state, serverLevel, pos, randomSource);
      if (Math.random() < (double)0.5F) {
         serverLevel.setBlock(pos, (BlockState)((BlockState)((Block)Sblocks.HAND.get()).defaultBlockState().setValue(ENABLED, false)).setValue(BlockStateProperties.WATERLOGGED, false), 3);
      }

   }

   static {
      ENABLED = BlockStateProperties.ENABLED;
      SHAPE = Block.box((double)2.0F, (double)4.0F, (double)2.0F, (double)14.0F, (double)16.0F, (double)14.0F);
   }
}
