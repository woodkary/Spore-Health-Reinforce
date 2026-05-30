package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class InfectedConsumeFromRemains extends Goal {
   public Infected infected;

   public InfectedConsumeFromRemains(Infected infected) {
      this.infected = infected;
   }

   public boolean canUse() {
      return this.infected.isStarving() && this.infected.getRandom().nextInt(0, 10) == 0 && this.isCorpse(this.infected);
   }

   boolean isCorpse(Entity entity) {
      AABB aabb = entity.getBoundingBox().inflate((double)2.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.infected.level().getBlockState(blockpos);
         if (blockstate.is((Block)Sblocks.REMAINS.get()) || blockstate.is((Block)Sblocks.WALL_REMAINS.get())) {
            if (Math.random() < 0.1) {
               entity.level().removeBlock(blockpos, false);
               this.infected.setHunger(0);
               this.infected.setEvoPoints(this.infected.getEvoPoints() + 1);
               this.infected.removeEffect((MobEffect)Seffects.STARVATION.get());
            }

            return true;
         }

         if (blockstate.is((Block)Sblocks.BIOMASS_BULB.get())) {
            if (Math.random() < 0.1) {
               entity.level().removeBlock(blockpos, false);
               this.infected.setHunger(0);
               this.infected.setKills(this.infected.getKills() + 1);
               this.infected.removeEffect((MobEffect)Seffects.STARVATION.get());
            }

            return true;
         }
      }

      return false;
   }

   public void start() {
      super.start();
      this.infected.playSound(SoundEvents.GENERIC_EAT);
   }
}
