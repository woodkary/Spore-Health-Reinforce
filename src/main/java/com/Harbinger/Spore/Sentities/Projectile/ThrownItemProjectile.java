package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sentities;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ThrownItemProjectile extends AbstractArrow {
   private static final EntityDataAccessor DAMAGE;
   private final ItemStack stack;
   private Predicate livingEntityPredicate = (entity) -> true;

   public ThrownItemProjectile(Level level) {
      super((EntityType)Sentities.THROWN_TOOL.get(), level);
      this.stack = new ItemStack(Items.IRON_AXE);
   }

   public ThrownItemProjectile(Level level, LivingEntity living, float damage, ItemStack stack) {
      super((EntityType)Sentities.THROWN_TOOL.get(), level);
      this.setOwner(living);
      this.moveTo(living.getX(), living.getY() + (double)1.2F, living.getZ());
      this.setDamage(damage);
      this.stack = stack;
   }

   public void setLivingEntityPredicate(Predicate value) {
      this.livingEntityPredicate = value;
   }

   public Float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   public void setDamage(Float value) {
      this.entityData.set(DAMAGE, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE, 0.0F);
   }

   public ItemStack getItem() {
      return this.stack;
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
   }

   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      Entity var3 = result.getEntity();
      if (var3 instanceof LivingEntity living) {
         living.setArrowCount(living.getArrowCount() - 1);
      }

      this.discard();
   }

   protected void onHitBlock(BlockHitResult p_36755_) {
      super.onHitBlock(p_36755_);
      this.discard();
   }

   protected boolean canHitEntity(Entity entity) {
      boolean var10000;
      if (entity instanceof LivingEntity living) {
         if (this.livingEntityPredicate.test(living)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 200 == 0) {
         this.discard();
      }

   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   static {
      DAMAGE = SynchedEntityData.defineId(ThrownItemProjectile.class, EntityDataSerializers.FLOAT);
   }
}
