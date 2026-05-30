package com.Harbinger.Spore.ExtremelySusThings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FungalExplosion extends Explosion {
   private final SoundEvent customSound;

   public FungalExplosion(Level level, Entity source, double x, double y, double z, float radius, boolean causesFire, BlockInteraction blockInteraction, SoundEvent soundEvent) {
      super(level, source, (DamageSource)null, (ExplosionDamageCalculator)null, x, y, z, radius, causesFire, blockInteraction);
      this.customSound = soundEvent;
   }

   public static void create(Level level, Entity source, Vec3 position, float radius, boolean causesFire, BlockInteraction blockInteraction, SoundEvent sound) {
      FungalExplosion explosion = new FungalExplosion(level, source, position.x, position.y, position.z, radius, causesFire, blockInteraction, sound);
      explosion.explode();
      explosion.finalizeExplosion(false);
      level.playSound(source, new BlockPos((int)position.x, (int)position.y, (int)position.z), explosion.customSound, SoundSource.MASTER, 1.0F, 1.0F);
      source.level().addParticle(ParticleTypes.EXPLOSION, position.x, position.y, position.z, (double)1.0F, (double)0.0F, (double)0.0F);
   }
}
