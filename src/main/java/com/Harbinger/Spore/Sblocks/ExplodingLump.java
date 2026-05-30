package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExplodingLump extends GenericFoliageBlock {
   public ExplodingLump() {
      super(Properties.of().sound(SoundType.SLIME_BLOCK).noOcclusion().strength(4.0F, 2.0F).noCollission());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)3.0F, (double)0.0F, (double)3.0F, (double)13.0F, (double)10.0F, (double)13.0F).move(offset.x, offset.y, offset.z);
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            level.explode((Entity)null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 1.0F, ExplosionInteraction.NONE);
            level.destroyBlock(pos, false, living);
         }
      }

   }
}
