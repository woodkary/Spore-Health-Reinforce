package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class HarpoonProjectile extends AbstractArrow {
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor ID;
   private static final EntityDataAccessor VICTIM_ID;
   private static final EntityDataAccessor SHOT;

   public HarpoonProjectile(Level level) {
      super((EntityType)Sentities.HARPOON.get(), level);
   }

   public HarpoonProjectile(Level level, LivingEntity living, float damage) {
      super((EntityType)Sentities.HARPOON.get(), level);
      this.entityData.set(ID, living.getId());
      this.setDamage(damage);
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
      this.entityData.define(ID, -1);
      this.entityData.define(VICTIM_ID, -1);
      this.entityData.define(SHOT, false);
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   public Entity getOwnerById() {
      int i = (Integer)this.entityData.get(ID);
      return i == -1 ? null : this.level().getEntity(i);
   }

   public int getOwnerId() {
      return (Integer)this.entityData.get(ID);
   }

   public Entity getVictimById() {
      int i = (Integer)this.entityData.get(VICTIM_ID);
      return i == -1 ? null : this.level().getEntity(i);
   }

   public void setHarpoonBackInPlace() {
      Entity entity = this.getOwnerById();
      if (entity instanceof Grakensenker grakensenker) {
         if (this.distanceTo(grakensenker) < 10.0F) {
            grakensenker.shootHook(true);
            this.discard();
         }
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
      this.entityData.set(SHOT, tag.getBoolean("shot"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
      tag.putBoolean("shot", (Boolean)this.entityData.get(SHOT));
   }

   protected void onHitEntity(EntityHitResult result) {
      Entity target = result.getEntity();
      if (target instanceof LivingEntity living) {
         living.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity)this.getOwner()), this.getDamage());
         living.setArrowCount(living.getArrowCount() - 1);
         this.entityData.set(VICTIM_ID, living.getId());
         this.setDeltaMovement(Vec3.ZERO);
         this.setNoGravity(true);
      }

      this.entityData.set(SHOT, true);
      this.playSound(SoundEvents.ANVIL_HIT);
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      SoundType soundtype = state.getSoundType(this.level(), pos, this);
      this.playSound(SoundEvents.CHAIN_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      this.entityData.set(SHOT, true);
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ANVIL_HIT;
   }

   protected boolean canHitEntity(Entity entity) {
      boolean var10000;
      if (entity instanceof LivingEntity living) {
         if (Utilities.TARGET_SELECTOR.Test(living)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public void tick() {
      super.tick();
      Entity owner = this.getOwnerById();
      Entity victim = this.getVictimById();
      if (victim == null) {
         AABB aabb = this.getBoundingBox();

         for(Entity entity : this.level().getEntities(this, aabb)) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (Utilities.TARGET_SELECTOR.Test(living)) {
                  this.entityData.set(VICTIM_ID, living.getId());
                  break;
               }
            }
         }
      }

      if (owner != null) {
         if ((Boolean)this.entityData.get(SHOT)) {
            Vec3 ownerPos = owner.position();
            Vec3 direction = ownerPos.subtract(this.position());
            double distance = direction.length();
            if (distance > (double)1.0F) {
               Vec3 motion = direction.normalize().scale((double)0.5F);
               this.setDeltaMovement(motion);
               if (victim instanceof LivingEntity) {
                  LivingEntity living = (LivingEntity)victim;
                  living.hurtMarked = true;
                  living.setDeltaMovement(motion);
                  this.moveTo(living.getX(), living.getY() + (double)(living.getBbHeight() / 2.0F), living.getZ());
               } else {
                  this.moveTo(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
               }

               this.setHarpoonBackInPlace();
            }
         }

      }
   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   static {
      DAMAGE = SynchedEntityData.defineId(HarpoonProjectile.class, EntityDataSerializers.FLOAT);
      ID = SynchedEntityData.defineId(HarpoonProjectile.class, EntityDataSerializers.INT);
      VICTIM_ID = SynchedEntityData.defineId(HarpoonProjectile.class, EntityDataSerializers.INT);
      SHOT = SynchedEntityData.defineId(HarpoonProjectile.class, EntityDataSerializers.BOOLEAN);
   }
}
