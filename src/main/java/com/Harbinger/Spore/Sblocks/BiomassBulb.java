package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BiomassBulb extends GenericFoliageBlock {
   public BiomassBulb() {
      super(Properties.of().strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)4.0F, (double)0.0F, (double)4.0F, (double)12.0F, (double)8.0F, (double)12.0F).move(offset.x, offset.y, offset.z);
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      if (fluid.is(Fluids.WATER)) {
         AreaEffectCloud cloud = new AreaEffectCloud(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         cloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 200, 0));
         level.addFreshEntity(cloud);
      } else {
         RandomSource randomSource = RandomSource.create();
         ItemStack stack = new ItemStack((ItemLike)Sitems.BIOMASS.get());
         stack.setCount(randomSource.nextInt(1, 5));
         ItemEntity entity = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
         level.addFreshEntity(entity);
      }

      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
   }
}
