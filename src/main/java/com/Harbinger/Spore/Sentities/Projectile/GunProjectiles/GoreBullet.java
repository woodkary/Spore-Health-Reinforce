package com.Harbinger.Spore.Sentities.Projectile.GunProjectiles;

import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.AbstractGunProjectile;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;

public class GoreBullet extends AbstractGunProjectile {
   public GoreBullet(EntityType entityType, Level level) {
      super(entityType, level);
   }

   public SoundEvent blockImpactSound() {
      return (SoundEvent)Ssounds.MISTMAKER_BULLET_BLOCK.get();
   }

   public SoundEvent entityImpactSound() {
      return (SoundEvent)Ssounds.MISTMAKER_BULLET_ENTITY.get();
   }

   public float getMaxBlockRange() {
      return 8.0F;
   }

   public float getProDamage() {
      return 0.05F;
   }

   public void doHitAfterEffects(LivingEntity living, LivingEntity owner) {
      living.hurtTime = 0;
      living.invulnerableTime = 0;
   }

   public ParticleOptions getParticle() {
      return (ParticleOptions)Sparticles.GORE_BULLET.get();
   }
}
