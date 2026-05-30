package com.Harbinger.Spore.Sblocks;

import com.Harbinger.Spore.Core.Ssounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallVocalsBlock extends LadderBlock {
   protected static final VoxelShape MOD_EAST_AABB = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)6.0F, (double)16.0F, (double)16.0F);
   protected static final VoxelShape MOD_WEST_AABB = Block.box((double)10.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
   protected static final VoxelShape MOD_SOUTH_AABB = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)6.0F);
   protected static final VoxelShape MOD_NORTH_AABB = Block.box((double)0.0F, (double)0.0F, (double)10.0F, (double)16.0F, (double)16.0F, (double)16.0F);
   private static final List LIST = new ArrayList() {
      {
         this.add((SoundEvent)Ssounds.INF_GROWL.get());
         this.add((SoundEvent)Ssounds.INF_VILLAGER_AMBIENT.get());
         this.add((SoundEvent)Ssounds.INF_PILLAGER_AMBIENT.get());
         this.add((SoundEvent)Ssounds.WITCH_AMBIENT.get());
         this.add((SoundEvent)Ssounds.HUSK_AMBIENT.get());
      }
   };

   public WallVocalsBlock() {
      super(Properties.of().sound(SoundType.SLIME_BLOCK).noOcclusion().strength(4.0F, 2.0F).noCollission().randomTicks());
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      VoxelShape var10000;
      switch ((Direction)state.getValue(FACING)) {
         case NORTH -> var10000 = MOD_NORTH_AABB;
         case SOUTH -> var10000 = MOD_SOUTH_AABB;
         case WEST -> var10000 = MOD_WEST_AABB;
         default -> var10000 = MOD_EAST_AABB;
      }

      return var10000;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.randomTick(state, level, pos, random);
      if (Math.random() < 0.1) {
         int listSize = LIST.size();
         level.playSound((Player)null, pos, (SoundEvent)LIST.get(random.nextInt(listSize)), SoundSource.HOSTILE, 1.0F, 1.0F);
      }

   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }
}
