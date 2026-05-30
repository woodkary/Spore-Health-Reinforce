package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Acid extends FallingBlock {
   public Acid() {
      super(Properties.of().noOcclusion().randomTicks());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)1.0F, (double)16.0F).move(offset.x, offset.y, offset.z);
   }

   public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
      super.onPlace(blockstate, world, pos, oldState, moving);
      world.scheduleTick(pos, this, 1);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
      level.removeBlock(pos, false);
      super.randomTick(state, level, pos, randomSource);
   }

   public void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {
      if (entity instanceof LivingEntity _entity) {
         if (!(entity instanceof Infected)) {
            _entity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 100, 0));
         }
      }

      super.entityInside(blockState, level, pos, entity);
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState blockstate, Level world, BlockPos pos, RandomSource random) {
      super.animateTick(blockstate, world, pos, random);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();

      for(int l = 0; l < 2; ++l) {
         double x0 = (double)((float)x + random.nextFloat());
         double z0 = (double)((float)z + random.nextFloat());
         double dx = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
         double dy = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
         double dz = ((double)random.nextFloat() - (double)0.5F) * (double)0.5F;
         world.addParticle((ParticleOptions)Sparticles.ACID_PARTICLE.get(), x0, (double)y, z0, dx, dy, dz);
      }

   }
}
