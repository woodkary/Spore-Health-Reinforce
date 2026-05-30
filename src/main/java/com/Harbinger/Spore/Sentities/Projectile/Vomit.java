package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import java.util.Random;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.PlayMessages;

public class Vomit extends AbstractArrow implements ItemSupplier {
   public Vomit(PlayMessages.SpawnEntity spawnEntity, Level level) {
      super((EntityType)Sentities.ACID.get(), level);
   }

   public Vomit(EntityType vomitEntityType, Level level) {
      super(vomitEntityType, level);
   }

   public Vomit(EntityType vomitEntityType, LivingEntity entity, Level world) {
      super(vomitEntityType, entity, world);
   }

   public ItemStack getItem() {
      return new ItemStack((ItemLike)Sitems.ACID.get());
   }

   public void tick() {
      super.tick();
      if (this.inGround || this.isInFluidType()) {
         this.discard();
      }

   }

   public static Vomit shoot(Level world, LivingEntity entity, Random random, float power, double damage, int knockback) {
      Vomit entityarrow = new Vomit((EntityType)Sentities.ACID.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 0.1F, 0.0F);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      world.addFreshEntity(entityarrow);
      return entityarrow;
   }

   public static Vomit shoot(LivingEntity entity, LivingEntity target, float damage) {
      Vomit entityarrow = new Vomit((EntityType)Sentities.ACID.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + (double)target.getEyeHeight() - (double)2.0F;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * (double)0.1F, dz, 2.0F, 12.0F);
      entityarrow.setBaseDamage((double)damage);
      entityarrow.setKnockback(0);
      entity.level().addFreshEntity(entityarrow);
      return entityarrow;
   }

   protected void onHitEntity(EntityHitResult hitResult) {
      super.onHitEntity(hitResult);
      this.levels(hitResult.getEntity());
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   private void levels(Entity entity) {
      if (entity instanceof LivingEntity _livEnt) {
         _livEnt.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 300, 1 + (_livEnt.hasEffect((MobEffect)Seffects.CORROSION.get()) ? _livEnt.getEffect((MobEffect)Seffects.CORROSION.get()).getAmplifier() : 0)));
      }

   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.SLIME_JUMP_SMALL;
   }

   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }
}
