package com.Harbinger.Spore.Sentities.Projectile.GunProjectiles;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.Projectile.AbstractGunProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class AssassinBullet extends AbstractGunProjectile implements ArmorPersentageBypass {
   public AssassinBullet(EntityType entityType, Level level) {
      super(entityType, level);
   }

   public SoundEvent blockImpactSound() {
      return (SoundEvent)Ssounds.ASSASSIN_BULLET_BLOCK.get();
   }

   public SoundEvent entityImpactSound() {
      return (SoundEvent)Ssounds.ASSASSIN_BULLET_ENTITY.get();
   }

   public float getMaxBlockRange() {
      return 200.0F;
   }

   public float getProDamage() {
      return 0.2F;
   }

   public void doHitAfterEffects(LivingEntity living, LivingEntity owner) {
      living.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
   }

   public ParticleOptions getParticle() {
      return (ParticleOptions)Sparticles.ACID_BULLET.get();
   }

   public float amountOfDamage(float value) {
      return value * 0.5F;
   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      if (!this.level().isClientSide) {
         BlockPos pos = result.getBlockPos().above();
         if (this.level().getBlockState(pos).isAir()) {
            this.level().setBlock(pos, ((Block)Sblocks.ACID.get()).defaultBlockState(), 3);
         }

      }
   }
}
