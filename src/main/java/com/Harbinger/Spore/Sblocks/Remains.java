package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Remains extends Block {
   public Remains() {
      super(Properties.of().sound(SoundType.SLIME_BLOCK).noOcclusion().strength(3.0F).noCollission());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)10.0F, (double)16.0F).move(offset.x, offset.y, offset.z);
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (!(entity instanceof Infected) && !(entity instanceof UtilityEntity) && !((List)SConfig.SERVER.blacklist.get()).contains(entity.getEncodeId()) && !((List)SConfig.SERVER.mycelium.get()).contains(entity.getEncodeId()) && !livingEntity.hasEffect((MobEffect)Seffects.MYCELIUM.get())) {
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 400, 1));
         }
      }

   }

   protected void tryDropExperience(ServerLevel serverLevel, BlockPos p_220824_, ItemStack p_220825_, IntProvider vak) {
      RandomSource source = RandomSource.create();
      super.tryDropExperience(serverLevel, p_220824_, p_220825_, vak);
      this.popExperience(serverLevel, p_220824_, source.nextInt(6));
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, level);
      cloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 160, 1));
      cloud.setDuration(160);
      cloud.setRadius(2.0F);
      cloud.setParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get());
      cloud.moveTo((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      level.addFreshEntity(cloud);
      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState groundState = worldIn.getBlockState(blockpos);
      return this.mayPlaceOn(groundState, worldIn, blockpos);
   }

   protected boolean mayPlaceOn(BlockState blockState, BlockGetter p_51043_, BlockPos p_51044_) {
      return blockState.canOcclude();
   }

   public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
      super.spawnAfterBreak(state, level, pos, stack, dropExperience);
      int xp = 3 + level.random.nextInt(3);
      this.popExperience(level, pos, xp);
   }
}
