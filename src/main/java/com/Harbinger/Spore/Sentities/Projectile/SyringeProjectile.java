package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Recipes.EntityContainer;
import com.Harbinger.Spore.Recipes.InjectionRecipe;
import com.Harbinger.Spore.Sitems.Agents.AbstractSyringe;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SyringeProjectile extends AbstractArrow {
   private ItemStack itemStack;

   public SyringeProjectile(Level level) {
      super((EntityType)Sentities.THROWN_SYRINGE.get(), level);
      this.itemStack = new ItemStack((ItemLike)Sitems.SYRINGE.get());
   }

   public SyringeProjectile(Level level, LivingEntity living, float damage, ItemStack stack) {
      super((EntityType)Sentities.STINGER.get(), level);
      this.setOwner(living);
      this.setBaseDamage((double)damage);
      this.setItemStack(stack);
   }

   public void setItemStack(ItemStack stack) {
      this.itemStack = stack;
   }

   protected ItemStack getPickupItem() {
      return this.itemStack;
   }

   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("Syringe", 10)) {
         this.itemStack = ItemStack.of(tag.getCompound("Syringe"));
      }

   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put("Syringe", this.itemStack.save(new CompoundTag()));
   }

   public Optional getRecipe(Level level, Entity entity) {
      EntityContainer container = new EntityContainer(entity);
      return level.getRecipeManager().getRecipeFor(InjectionRecipe.InjectionRecipeType.INSTANCE, container, level);
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (entity instanceof LivingEntity living) {
         if (!this.level().isClientSide && this.canHitEntity(living)) {
            if (this.itemStack.getItem().equals(Sitems.SYRINGE.get())) {
               Optional<InjectionRecipe> match = this.getRecipe(this.level(), living);
               if (match.isPresent() && Math.random() < (double)0.5F) {
                  ItemStack stack = ((InjectionRecipe)match.get()).getResultItem((RegistryAccess)null);
                  if (stack == null) {
                     return;
                  }

                  ItemEntity itemEntity = new ItemEntity(this.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
                  this.level().addFreshEntity(itemEntity);
               }

               this.playSound((SoundEvent)Ssounds.SYRINGE_SUCK.get());
            }

            Item var8 = this.itemStack.getItem();
            if (var8 instanceof AbstractSyringe) {
               AbstractSyringe syringe = (AbstractSyringe)var8;
               syringe.useSyringe(this.itemStack, living);
               this.playSound((SoundEvent)Ssounds.SYRINGE_GUN_INJECT.get());
            }

            living.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity)this.getOwner()), (float)this.getBaseDamage());
         }
      }

   }

   protected void onHitBlock(BlockHitResult p_36755_) {
      super.onHitBlock(p_36755_);
      this.discard();
   }

   protected boolean canHitEntity(Entity entity) {
      return !entity.equals(this.getOwner()) && super.canHitEntity(entity);
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return (SoundEvent)Ssounds.SYRINGE_GUN_INJECT.get();
   }
}
