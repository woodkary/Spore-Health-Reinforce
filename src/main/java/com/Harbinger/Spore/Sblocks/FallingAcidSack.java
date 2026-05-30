package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Projectile.FallenAcidSack;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FallingAcidSack extends HangingRootsBlock {
   protected static final VoxelShape SHAPE = Block.box((double)2.0F, (double)4.0F, (double)2.0F, (double)14.0F, (double)16.0F, (double)14.0F);

   public FallingAcidSack() {
      super(Properties.of().strength(4.0F, 2.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK).randomTicks().lightLevel((s) -> 1).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true));
   }

   public VoxelShape getShape(BlockState p_153342_, BlockGetter p_153343_, BlockPos p_153344_, CollisionContext p_153345_) {
      return SHAPE;
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource randomSource) {
      super.randomTick(state, serverLevel, pos, randomSource);
      AABB searchArea = new AABB((double)(pos.getX() - 3), (double)(pos.getY() - 8), (double)(pos.getZ() - 3), (double)(pos.getX() + 3), (double)pos.getY(), (double)(pos.getZ() + 3));
      List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, searchArea, (living) -> Utilities.TARGET_SELECTOR.Test(living));
      if (!entities.isEmpty()) {
         serverLevel.destroyBlock(pos, false);
         FallenAcidSack sack = new FallenAcidSack(serverLevel);
         sack.moveTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F);
         sack.setDeltaMovement((randomSource.nextDouble() - (double)0.5F) * 0.1, (double)-0.5F, (randomSource.nextDouble() - (double)0.5F) * 0.1);
         serverLevel.addFreshEntity(sack);
      }

   }
}
