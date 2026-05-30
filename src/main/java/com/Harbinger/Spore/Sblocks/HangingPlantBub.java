package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class HangingPlantBub extends HangingPlant {
   public static final BooleanProperty PERSISTENT;

   public HangingPlantBub() {
      this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HANGING, Boolean.FALSE)).setValue(WATERLOGGED, Boolean.FALSE)).setValue(PERSISTENT, Boolean.TRUE));
   }

   public void entityInside(BlockState state, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide && !(entity instanceof Infected) && !(entity instanceof UtilityEntity) && !((List)SConfig.SERVER.blacklist.get()).contains(entity.getEncodeId()) && !((List)SConfig.SERVER.mycelium.get()).contains(entity.getEncodeId())) {
         BlockState block2 = ((Block)Sblocks.BLOOM_G.get()).defaultBlockState();
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(level, (double)blockpos.getX() + 0.4, (double)blockpos.getY(), (double)blockpos.getZ() + 0.4);
         areaeffectcloud.setRadius(2.5F);
         areaeffectcloud.setRadiusOnUse(-0.5F);
         areaeffectcloud.setWaitTime(10);
         areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 100, 2));
         areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
         areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
         Property var8 = state.getBlock().getStateDefinition().getProperty("hanging");
         if (var8 instanceof BooleanProperty) {
            BooleanProperty property = (BooleanProperty)var8;
            if ((Boolean)state.getValue(property)) {
               level.setBlock(blockpos, (BlockState)block2.setValue(property, true), 3);
            } else {
               level.setBlock(blockpos, block2, 3);
            }
         }

         level.addFreshEntity(areaeffectcloud);
         level.playSound((Player)null, blockpos, (SoundEvent)Ssounds.FUNGAL_BURST.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Boolean)state.getValue(HANGING) && (Boolean)state.getValue(PERSISTENT);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos blockpos, RandomSource randomSource) {
      super.randomTick(state, level, blockpos, randomSource);
      if (Math.random() < (double)0.4F) {
         BlockState blockState = level.getBlockState(blockpos.below());
         BlockState blockState1 = level.getBlockState(blockpos.below(2));
         BlockState blockState2 = level.getBlockState(blockpos.below(3));
         if (blockState.getBlock() instanceof AirBlock && blockState1.getBlock() instanceof AirBlock && blockState2.getBlock() instanceof AirBlock) {
            BlockState block = ((Block)Sblocks.BLOOM_GG.get()).defaultBlockState();
            level.setBlock(blockpos, ((Block)Sblocks.HANGING_FUNGAL_STEM.get()).defaultBlockState(), 2);
            level.setBlock(blockpos.below(), (BlockState)((BlockState)block.setValue(HANGING, true)).setValue(PERSISTENT, !(Math.random() < 0.2)), 2);
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      stateBuilder.add(new Property[]{HANGING, WATERLOGGED, PERSISTENT});
   }

   static {
      PERSISTENT = BlockStateProperties.PERSISTENT;
   }
}
