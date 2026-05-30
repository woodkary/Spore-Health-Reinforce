package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Sparticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CancerLungs extends HangingRootsBlock {
   protected static final VoxelShape SHAPE = Block.box((double)2.0F, (double)4.0F, (double)2.0F, (double)14.0F, (double)16.0F, (double)14.0F);

   public CancerLungs() {
      super(Properties.of().strength(4.0F, 2.0F).noCollission().noOcclusion().sound(SoundType.SLIME_BLOCK));
   }

   public VoxelShape getShape(BlockState p_153342_, BlockGetter p_153343_, BlockPos p_153344_, CollisionContext p_153345_) {
      return SHAPE;
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      Entity entity = Minecraft.getInstance().cameraEntity;
      if (entity != null) {
         double maxDistance = (double)12.0F;
         double maxDistanceSq = maxDistance * maxDistance;
         if (!(entity.distanceToSqr((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F) > maxDistanceSq)) {
            if (random.nextFloat() < 0.6F) {
               double radius = (double)3.0F;
               double px = (double)pos.getX() + (double)0.5F + (random.nextDouble() - (double)0.5F) * radius;
               double py = (double)pos.getY() + (double)0.5F + random.nextDouble() * (double)1.5F * -radius;
               double pz = (double)pos.getZ() + (double)0.5F + (random.nextDouble() - (double)0.5F) * radius;
               double centerX = (double)pos.getX() + (double)0.5F;
               double centerY = (double)pos.getY() + (double)0.75F;
               double centerZ = (double)pos.getZ() + (double)0.5F;
               double dx = centerX - px;
               double dy = centerY - py;
               double dz = centerZ - pz;
               double speed = (double)0.25F;
               double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
               dx = dx / length * speed;
               dy = dy / length * speed;
               dz = dz / length * speed;
               level.addParticle((ParticleOptions)Sparticles.SPORE_PARTICLE.get(), px, py, pz, dx, dy, dz);
            }

         }
      }
   }
}
