package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.Random;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AcidBall extends AbstractArrow implements ItemSupplier {
   public AcidBall(Level level) {
      super((EntityType)Sentities.ACID_BALL.get(), level);
   }

   public AcidBall(EntityType acidBallEntityType, Level level) {
      super(acidBallEntityType, level);
   }

   public AcidBall(EntityType acidBallEntityType, LivingEntity entity, Level world) {
      super(acidBallEntityType, entity, world);
   }

   public ItemStack getItem() {
      return new ItemStack((ItemLike)Sitems.ACID_BALL.get());
   }

   public void tick() {
      super.tick();
      if (this.inGround || this.isInFluidType()) {
         this.discard();
      }

   }

   public static AcidBall shoot(Level world, LivingEntity entity, Random random, float power, double damage, int knockback) {
      AcidBall entityarrow = new AcidBall((EntityType)Sentities.ACID_BALL.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      world.addFreshEntity(entityarrow);
      return entityarrow;
   }

   public static AcidBall shoot(LivingEntity entity, LivingEntity target, float damage) {
      AcidBall entityarrow = new AcidBall((EntityType)Sentities.ACID_BALL.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + (double)target.getEyeHeight() - (double)2.0F;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * (double)0.2F, dz, 2.0F, 12.0F);
      entityarrow.setBaseDamage((double)damage);
      entityarrow.setKnockback(1);
      entity.level().addFreshEntity(entityarrow);
      return entityarrow;
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      place_acid(this.level(), (double)blockHitResult.getBlockPos().getX(), (double)blockHitResult.getBlockPos().getY(), (double)blockHitResult.getBlockPos().getZ());
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   protected void onHitEntity(EntityHitResult hitResult) {
      Entity var3 = hitResult.getEntity();
      if (var3 instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            super.onHitEntity(hitResult);
            this.levels(living);
         }
      }

   }

   public static void place_acid(LevelAccessor world, double x, double y, double z) {
      if (world.getBlockState(new BlockPos((int)x, (int)y + 1, (int)z)).isAir() && world.getBlockState(new BlockPos((int)x, (int)y, (int)z)).canOcclude()) {
         world.setBlock(new BlockPos((int)x, (int)y + 1, (int)z), ((Block)Sblocks.ACID.get()).defaultBlockState(), 3);
      }

   }

   private void levels(LivingEntity living) {
      int level = 0;
      MobEffectInstance instance = living.getEffect((MobEffect)Seffects.CORROSION.get());
      if (instance != null) {
         level = instance.getAmplifier() + 1;
      }

      living.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 300, level));
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.SLIME_JUMP_SMALL;
   }

   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }
}
