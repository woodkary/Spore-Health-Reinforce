package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EvolvedInfected extends Infected {
   public EvolvedInfected(EntityType type, Level level) {
      super(type, level);
   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      return super.blockBreakingParameter(blockstate, blockpos) || blockstate.is(Utilities.biomass);
   }

   protected boolean canRide(Entity entity) {
      return !(entity instanceof Infected) && !(entity instanceof UtilityEntity) ? false : super.canRide(entity);
   }

   public boolean hurt(DamageSource source, float amount) {
      return this.level().getDifficulty() == Difficulty.HARD && (double)amount > this.getDamageCap() && (Boolean)SConfig.SERVER.damagecap.get() ? super.hurt(source, (float)this.getDamageCap()) : super.hurt(source, amount);
   }

   public double getDamageCap() {
      return (double)(this.getMaxHealth() / 3.0F);
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.EVOLVED;
   }

   public boolean canStarve() {
      return false;
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.EVOLVE_HURT.get();
   }

   public boolean removeWhenFarAway(double p_21542_) {
      return this.getLinked() && !(this instanceof Scamper);
   }
}
