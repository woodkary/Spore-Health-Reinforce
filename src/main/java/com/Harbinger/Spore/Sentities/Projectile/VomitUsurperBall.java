package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class VomitUsurperBall extends AbstractArrow {
   public VomitUsurperBall(Level level) {
      super((EntityType)Sentities.USURPER_VOMIT_BALL.get(), level);
   }

   public VomitUsurperBall(EntityType acidBallEntityType, Level level) {
      super(acidBallEntityType, level);
   }

   public ItemStack getItem() {
      return ItemStack.EMPTY;
   }

   public void tick() {
      super.tick();
      this.makeBile();
      if (this.inGround || this.isInFluidType()) {
         this.discard();
      }

   }

   private void makeBile() {
      for(int i = 0; i < 8; ++i) {
         float movement1 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement2 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement3 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         this.level().addParticle((ParticleOptions)Sparticles.VOMIT.get(), this.getX() + (double)movement1, this.getY() + (double)movement2, this.getZ() + (double)movement3, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   public static VomitUsurperBall shoot(Level world, LivingEntity entity, float power, double damage) {
      VomitUsurperBall entityarrow = new VomitUsurperBall((EntityType)Sentities.USURPER_VOMIT_BALL.get(), world);
      entityarrow.setOwner(entity);
      entityarrow.moveTo(entity.getX(), entity.getY() + 1.2, entity.getZ());
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setBaseDamage(damage);
      world.addFreshEntity(entityarrow);
      return entityarrow;
   }

   public static VomitUsurperBall shoot(LivingEntity entity, LivingEntity target, float damage) {
      VomitUsurperBall entityarrow = new VomitUsurperBall((EntityType)Sentities.USURPER_VOMIT_BALL.get(), entity.level());
      entityarrow.setOwner(entity);
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + (double)target.getEyeHeight() - (double)2.0F;
      double dz = target.getZ() - entity.getZ();
      entityarrow.moveTo(entity.getX(), entity.getY() + 1.2, entity.getZ());
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * (double)0.2F, dz, 2.0F, 4.0F);
      entityarrow.setBaseDamage((double)damage);
      entity.level().addFreshEntity(entityarrow);
      return entityarrow;
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.discard();
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   protected void onHitEntity(EntityHitResult hitResult) {
      Entity var3 = hitResult.getEntity();
      if (var3 instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            super.onHitEntity(hitResult);
            living.hurtTime = 0;
            living.invulnerableTime = 0;
            this.addStuff(living);
         }
      }

   }

   void addStuff(LivingEntity living) {
      MobEffectInstance instance = living.getEffect((MobEffect)Seffects.CORROSION.get());
      int amplifier = instance == null ? 0 : instance.getAmplifier() + 1;
      living.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, amplifier));
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.SLIME_JUMP_SMALL;
   }

   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }
}
