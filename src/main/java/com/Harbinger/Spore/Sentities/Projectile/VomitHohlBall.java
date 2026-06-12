package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.utils.ParentUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Fluids.BileLiquid;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class VomitHohlBall extends AbstractArrow {
   private static final EntityDataAccessor ORES;
   private static final EntityDataAccessor MEAT;

   public VomitHohlBall(Level level) {
      super((EntityType)Sentities.VOMIT_BALL.get(), level);
   }

   public VomitHohlBall(EntityType acidBallEntityType, Level level) {
      super(acidBallEntityType, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ORES, false);
      this.entityData.define(MEAT, false);
   }

   public VomitHohlBall(EntityType acidBallEntityType, LivingEntity entity, Level world) {
      super(acidBallEntityType, entity, world);
   }

   public ItemStack getItem() {
      return ItemStack.EMPTY;
   }

   public void setOres(boolean val) {
      this.entityData.set(ORES, val);
   }

   public void setMeat(boolean val) {
      this.entityData.set(MEAT, val);
   }

   public void tick() {
      super.tick();
      this.makeBile((Boolean)this.entityData.get(MEAT));
      if ((Boolean)this.entityData.get(ORES)) {
         this.makeOre();
      }

      if (this.inGround || this.isInFluidType()) {
         this.discard();
      }

   }

   private void makeBile(boolean val) {
      for(int i = 0; i < 8; ++i) {
         float movement1 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement2 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement3 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         this.level().addParticle(val ? (ParticleOptions)Sparticles.VOMIT_BONE.get() : (ParticleOptions)Sparticles.VOMIT.get(), this.getX() + (double)movement1, this.getY() + (double)movement2, this.getZ() + (double)movement3, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   private void makeOre() {
      for(int i = 0; i < 4; ++i) {
         float movement1 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement2 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         float movement3 = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
         this.level().addParticle((ParticleOptions)Sparticles.VOMIT_ORES.get(), this.getX() + (double)movement1, this.getY() + (double)movement2, this.getZ() + (double)movement3, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   public static VomitHohlBall shoot(Level world, LivingEntity entity, float power, double damage, int knockback, boolean ore, boolean meat) {
      VomitHohlBall entityarrow = new VomitHohlBall((EntityType)Sentities.VOMIT_BALL.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      entityarrow.setOres(ore);
      entityarrow.setMeat(meat);
      world.addFreshEntity(entityarrow);
      return entityarrow;
   }

   public static VomitHohlBall shoot(LivingEntity entity, LivingEntity target, float damage, boolean ore, boolean meat) {
      VomitHohlBall entityarrow = new VomitHohlBall((EntityType)Sentities.VOMIT_BALL.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + (double)target.getEyeHeight() - (double)2.0F;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * (double)0.2F, dz, 2.0F, 4.0F);
      entityarrow.setBaseDamage((double)damage);
      entityarrow.setKnockback(1);
      entityarrow.setOres(ore);
      entityarrow.setMeat(meat);
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
      Entity var3 = ParentUtil.INSTANCE.getUltimateParent(hitResult.getEntity());
      if (var3 instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            super.onHitEntity(hitResult);
            this.addStuff(living);
            if(!(living instanceof Player player&& EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(player))){
               Entity owner = this.getOwner();
               SporeAttackUtil.INSTANCE.dealDamage(living,
                       owner instanceof LivingEntity livOwner?livOwner:null,
                       this.damageSources().arrow(this, owner!=null?owner:this),
                       (float)this.getBaseDamage()
               );
            }
         }
      }

   }

   void addStuff(LivingEntity living) {
      if (Math.random() < 0.1) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 100, 1));
      }

      if (Math.random() < 0.1) {
         for(MobEffectInstance instance : BileLiquid.bileEffects()) {
            living.addEffect(instance);
         }
      }

   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.SLIME_JUMP_SMALL;
   }

   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }

   static {
      ORES = SynchedEntityData.defineId(VomitHohlBall.class, EntityDataSerializers.BOOLEAN);
      MEAT = SynchedEntityData.defineId(VomitHohlBall.class, EntityDataSerializers.BOOLEAN);
   }
}
