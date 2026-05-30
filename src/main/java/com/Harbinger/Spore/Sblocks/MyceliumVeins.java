package com.Harbinger.Spore.Sblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MyceliumVeins extends GenericFoliageBlock {
   public MyceliumVeins() {
      super(Properties.of().strength(0.0F, 0.0F).noCollission().noOcclusion().sound(SoundType.CROP));
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(world, pos);
      return box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, 0.2, (double)16.0F).move(offset.x, offset.y, offset.z);
   }
}
