package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungalClamp extends GenericFoliageBlock {
   public static final BooleanProperty OPEN = BooleanProperty.create("open");

   public FungalClamp() {
      super(Properties.of().sound(SoundType.CROP).strength(1.0F, 1.0F).randomTicks().noCollission().noOcclusion().sound(SoundType.CROP));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box((double)2.0F, (double)0.0F, (double)2.0F, (double)14.0F, (double)12.0F, (double)14.0F);
   }

   public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
      BlockState blockState = levelReader.getBlockState(pos.below());
      return blockState.canOcclude();
   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      super.createBlockStateDefinition(stateBuilder);
      stateBuilder.add(new Property[]{OPEN});
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      super.entityInside(state, level, pos, entity);
      if (entity instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            if ((Boolean)state.getValue(OPEN)) {
               level.setBlock(pos, (BlockState)level.getBlockState(pos).setValue(OPEN, false), 3);
               AABB aabb = new AABB((double)(pos.getX() - 5), (double)(pos.getY() - 1), (double)(pos.getZ() - 5), (double)(pos.getX() + 5), (double)(pos.getY() + 3), (double)(pos.getZ() + 5));
               living.hurt(level.damageSources().cactus(), 10.0F);
               this.spreadInfection(level, aabb);
            } else if (living.tickCount % 200 == 0 && !level.isClientSide()) {
               this.injectSpores(living);
            }

            entity.makeStuckInBlock(state, new Vec3((double)0.1F, 0.2, (double)0.1F));
         }
      }

   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.randomTick(state, level, pos, random);
      if (!(Boolean)state.getValue(OPEN) && this.isEmptyWithin(level, new AABB(pos))) {
         level.setBlock(pos, (BlockState)level.getBlockState(pos).setValue(OPEN, true), 3);
      }

   }

   public void injectSpores(LivingEntity living) {
      MobEffectInstance instance = living.getEffect((MobEffect)Seffects.MYCELIUM.get());
      if (instance == null) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 300, 0));
      } else {
         int i = instance.getAmplifier();
         i = i < 4 ? i + 1 : i;
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 300, i));
      }
   }

   public boolean isRandomlyTicking(BlockState state) {
      return !(Boolean)state.getValue(OPEN);
   }

   public boolean isEmptyWithin(Level level, AABB aabb) {
      return level.getEntities((Entity)null, aabb).isEmpty();
   }

   public void spreadInfection(Level level, AABB aabb) {
      List<Entity> entities = level.getEntities((Entity)null, aabb);
      if (!entities.isEmpty()) {
         for(Entity entity : entities) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (Utilities.TARGET_SELECTOR.Test(living)) {
                  living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 0));
               }
            }
         }

      }
   }
}
