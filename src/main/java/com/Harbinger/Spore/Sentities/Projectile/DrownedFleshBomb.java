package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

public class DrownedFleshBomb extends AbstractArrow {
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor BOMB_TIME;
   private static final EntityDataAccessor EFFECT;
   private static final EntityDataAccessor FLOAT;

   public DrownedFleshBomb(Level level) {
      super((EntityType)Sentities.DROWNED_FLESH_BOMB.get(), level);
   }

   public DrownedFleshBomb(EntityType vomitEntityType, Level level) {
      super(vomitEntityType, level);
   }

   public void tick() {
      super.tick();
      if (this.isInWater() && (Boolean)this.entityData.get(FLOAT)) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
      }

      if (this.getBombTime() < 80) {
         this.setBombTime(this.getBombTime() + 1);
      } else {
         this.explodeBomb();
         this.discard();
      }

   }

   public void explodeBomb() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 3, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         this.playSound((SoundEvent)Ssounds.FUNGAL_BOOM.get());
         AABB aabb = this.getBoundingBox().inflate((double)3.0F);
         List<Entity> entityList = this.level().getEntities(this, aabb);
         if (entityList.isEmpty()) {
            return;
         }

         for(Entity entity : entityList) {
            if (entity instanceof LivingEntity living) {
               if (Utilities.TARGET_SELECTOR.Test(living)) {
                  MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(this.getEffect()));
                  if (effect == null) {
                     effect = (MobEffect)Seffects.MYCELIUM.get();
                  }

                  living.addEffect(new MobEffectInstance(effect, 200, 0));
               }
            }
         }
      }

   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.SLIME_JUMP_SMALL;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE, 2.0F);
      this.entityData.define(BOMB_TIME, 0);
      this.entityData.define(EFFECT, "spore:mycelium_ef");
      this.entityData.define(FLOAT, Math.random() <= (double)0.5F);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
      this.setBombTime(tag.getInt("bomb_time"));
      this.setEffect(tag.getString("effect"));
      this.entityData.set(FLOAT, tag.getBoolean("float"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
      tag.putInt("bomb_time", this.getBombTime());
      tag.putString("effect", this.getEffect());
      tag.putBoolean("float", (Boolean)this.entityData.get(FLOAT));
   }

   public float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   public void setDamage(float value) {
      this.entityData.set(DAMAGE, value);
   }

   public int getBombTime() {
      return (Integer)this.entityData.get(BOMB_TIME);
   }

   public void setBombTime(int value) {
      this.entityData.set(BOMB_TIME, value);
   }

   public String getEffect() {
      return (String)this.entityData.get(EFFECT);
   }

   public void setEffect(String value) {
      this.entityData.set(EFFECT, value);
   }

   protected boolean canHitEntity(Entity target) {
      return false;
   }

   protected void onHitBlock(BlockHitResult result) {
   }

   static {
      DAMAGE = SynchedEntityData.defineId(DrownedFleshBomb.class, EntityDataSerializers.FLOAT);
      BOMB_TIME = SynchedEntityData.defineId(DrownedFleshBomb.class, EntityDataSerializers.INT);
      EFFECT = SynchedEntityData.defineId(DrownedFleshBomb.class, EntityDataSerializers.STRING);
      FLOAT = SynchedEntityData.defineId(DrownedFleshBomb.class, EntityDataSerializers.BOOLEAN);
   }
}
