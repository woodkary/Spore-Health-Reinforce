package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class UnderWaterFungusTop extends GenericFoliageBlock {
   public static final BooleanProperty PERSISTENT;

   public UnderWaterFungusTop() {
      super(Properties.of().sound(SoundType.CROP).strength(0.0F, 0.0F).noCollission().sound(SoundType.CROP).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, Boolean.TRUE)).setValue(PERSISTENT, Boolean.TRUE));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)4.0F, (double)0.0F, (double)4.0F, (double)12.0F, (double)10.0F, (double)12.0F).move(offset.x, offset.y, offset.z);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState groundState = worldIn.getBlockState(blockpos);
      return this.mayPlaceOn(groundState, worldIn, blockpos);
   }

   protected boolean mayPlaceOn(BlockState blockState, BlockGetter p_51043_, BlockPos pos) {
      return blockState.canOcclude() || blockState.getBlock() == Sblocks.UNDERWATER_FUNGAL_STEM.get();
   }

   public void entityInside(BlockState state, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide) {
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(level, (double)blockpos.getX() + 0.4, (double)blockpos.getY(), (double)blockpos.getZ() + 0.4);
         areaeffectcloud.setRadius(3.5F);
         areaeffectcloud.setRadiusOnUse(-0.5F);
         areaeffectcloud.setWaitTime(10);
         areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 100, 0));
         areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
         areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
         level.addFreshEntity(areaeffectcloud);
         level.removeBlock(blockpos, false);
         level.playSound((Player)null, blockpos, (SoundEvent)Ssounds.FUNGAL_BURST.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
      super.randomTick(state, level, pos, source);
      BlockState blockState = level.getBlockState(pos.above());
      if (blockState.getFluidState().is(Fluids.WATER)) {
         level.setBlock(pos, ((Block)Sblocks.UNDERWATER_FUNGAL_STEM.get()).defaultBlockState(), 3);
         BlockState block = ((Block)Sblocks.UNDERWATER_FUNGAL_STEM_TOP.get()).defaultBlockState();
         level.setBlock(pos.above(), (BlockState)block.setValue(PERSISTENT, !(Math.random() < 0.3)), 3);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder stateBuilder) {
      stateBuilder.add(new Property[]{WATERLOGGED, PERSISTENT});
   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Boolean)state.getValue(PERSISTENT) && (Boolean)state.getValue(WATERLOGGED);
   }

   static {
      PERSISTENT = BlockStateProperties.PERSISTENT;
   }
}
