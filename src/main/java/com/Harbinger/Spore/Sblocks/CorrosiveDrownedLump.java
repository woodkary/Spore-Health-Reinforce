package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public class CorrosiveDrownedLump extends GenericFoliageBlock {
   public CorrosiveDrownedLump() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK).randomTicks());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      stateBuilder.add(new Property[]{WATERLOGGED});
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      double offsetX = (random.nextDouble() - (double)0.5F) * 0.8;
      double offsetZ = (random.nextDouble() - (double)0.5F) * 0.8;
      double particleX = (double)pos.getX() + (double)0.5F + offsetX;
      double particleY = (double)pos.getY() + 0.2 + random.nextDouble() * 0.6;
      double particleZ = (double)pos.getZ() + (double)0.5F + offsetZ;
      level.addParticle(ParticleTypes.BUBBLE, particleX, particleY, particleZ, (double)0.0F, 0.1 + random.nextDouble() * 0.2, (double)0.0F);
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return fluid.getType() == Fluids.WATER ? (BlockState)this.defaultBlockState().setValue(WATERLOGGED, true) : null;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      FluidState fluid = level.getFluidState(pos);
      return fluid.getType() == Fluids.WATER;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      AABB aabb = new AABB((double)(pos.getX() - 3), (double)pos.getY(), (double)(pos.getZ() - 3), (double)(pos.getX() + 3), (double)pos.getY() + (double)3.0F, (double)(pos.getZ() + 3));

      for(Entity entity : level.getEntities((Entity)null, aabb)) {
         if (entity instanceof LivingEntity living) {
            if (Utilities.TARGET_SELECTOR.Test(living) && living.isInWater()) {
               living.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 100, 1));
               living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
         }
      }

   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }
}
