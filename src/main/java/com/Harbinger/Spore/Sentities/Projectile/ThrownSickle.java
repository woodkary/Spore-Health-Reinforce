package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.InfectedSickle;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownSickle extends AbstractArrow {
   private static final EntityDataAccessor ID_FOIL;
   private static final EntityDataAccessor COLOR;
   private ItemStack spearItem;
   private boolean dealtDamage;
   private SickelState state;
   private Entity hookedEntity;
   private Vec3 hookedBlockPos;

   public ThrownSickle(Level level, LivingEntity livingEntity, ItemStack stack, int color) {
      super((EntityType)Sentities.THROWN_SICKEL.get(), livingEntity, level);
      this.spearItem = new ItemStack((ItemLike)Sitems.SICKLE.get());
      this.state = SickelState.FLYING;
      this.hookedEntity = null;
      this.hookedBlockPos = null;
      this.setOwner(livingEntity);
      this.spearItem = stack.copy();
      this.entityData.set(ID_FOIL, stack.hasFoil());
      this.entityData.set(COLOR, color);
   }

   public ThrownSickle(Level level) {
      super((EntityType)Sentities.THROWN_SICKEL.get(), level);
      this.spearItem = new ItemStack((ItemLike)Sitems.SICKLE.get());
      this.state = SickelState.FLYING;
      this.hookedEntity = null;
      this.hookedBlockPos = null;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ID_FOIL, false);
      this.entityData.define(COLOR, 0);
   }

   public ItemStack getSpearItem() {
      return this.spearItem.copy();
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   public void tick() {
      if (this.state == SickelState.HOOKED_IN_ENTITY && this.hookedEntity != null && this.hookedEntity.isAlive()) {
         this.setPos(this.hookedEntity.getX(), this.hookedEntity.getY() + (double)this.hookedEntity.getBbHeight() * (double)0.5F, this.hookedEntity.getZ());
      }

      if (this.inGroundTime > 4) {
         this.dealtDamage = true;
      }

      Entity owner = this.getOwner();
      if (owner instanceof LivingEntity living) {
         ItemStack stack = living.getMainHandItem();
         if (this.distanceTo(living) > 30.0F || !(stack.getItem() instanceof InfectedSickle)) {
            Item var5 = stack.getItem();
            if (var5 instanceof InfectedSickle) {
               InfectedSickle sickle = (InfectedSickle)var5;
               sickle.setThrownSickle(stack, false);
            }

            this.discard();
         }
      } else {
         this.discard();
      }

      super.tick();
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   public boolean isFoil() {
      return (Boolean)this.entityData.get(ID_FOIL);
   }

   @Nullable
   protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec31) {
      return this.dealtDamage ? null : super.findHitEntity(vec3, vec31);
   }

   protected void onHitEntity(EntityHitResult hit) {
      Entity entity = hit.getEntity();
      float f = (float)(Integer)SConfig.SERVER.sickle_damage.get() + 0.5F * (float)EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, this.spearItem);
      if (entity instanceof LivingEntity livingentity) {
         f += EnchantmentHelper.getDamageBonus(this.spearItem, livingentity.getMobType());
      }

      Entity entity1 = this.getOwner();
      DamageSource damagesource = this.damageSources().trident(this, (Entity)(entity1 == null ? this : entity1));
      this.dealtDamage = true;
      SoundEvent soundevent = (SoundEvent)Ssounds.INFECTED_WEAPON_HIT_ENTITY.get();
      if (entity.hurt(damagesource, f)) {
         if (entity.getType() == EntityType.ENDERMAN) {
            return;
         }

         if (entity instanceof LivingEntity target) {
             if (entity1 instanceof LivingEntity ownerLiving) {
                EnchantmentHelper.doPostHurtEffects(target, ownerLiving);
                EnchantmentHelper.doPostDamageEffects(ownerLiving, target);
                Item var10 = this.spearItem.getItem();
                if (var10 instanceof SporeWeaponData data) {
                   if(data.getVariant(spearItem) == SporeToolsMutations.BEZERK) {
                      SporeAttackUtil.INSTANCE.dealDamage(target, ownerLiving, damagesource, f);
                   }
                   data.abstractMutationBuffs(target, ownerLiving, this.spearItem, data);
                }
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, this.spearItem) > 0) {
               int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, this.spearItem);
               entity.setSecondsOnFire(4 * j);
            }

            abstractEffects(this.spearItem, target);
            this.doPostHurtEffects(target);
         }
      }

      this.hookedEntity = entity;
      this.state = SickelState.HOOKED_IN_ENTITY;
      this.playSound(soundevent, 1.0F, 1.0F);
   }

   protected boolean tryPickup(Player p_150121_) {
      return false;
   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      this.hookedBlockPos = result.getLocation();
      this.state = SickelState.HOOKED_BLOCK;
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return (SoundEvent)Ssounds.INFECTED_WEAPON_HIT_BLOCK.get();
   }

   public SickelState getHookState() {
      return this.state;
   }

   public Entity getHookedEntity() {
      return this.hookedEntity;
   }

   public Vec3 getHookedBlockPos() {
      return this.hookedBlockPos;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("Sickle", 10)) {
         this.spearItem = ItemStack.of(tag.getCompound("Sickle"));
      }

      this.dealtDamage = tag.getBoolean("DealtDamage");
      if (this.getOwner() != null) {
         tag.putUUID("OwnerUUID", this.getOwner().getUUID());
      }

      this.entityData.set(COLOR, tag.getInt("color"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put("Sickle", this.spearItem.save(new CompoundTag()));
      tag.putBoolean("DealtDamage", this.dealtDamage);
      tag.putInt("color", (Integer)this.entityData.get(COLOR));
   }

   public void onSyncedDataUpdated(EntityDataAccessor key) {
      super.onSyncedDataUpdated(key);
      Entity entity = this.getOwner();
      if (entity != null) {
         UUID uuid1 = entity.getUUID();
         if (!this.level().isClientSide) {
            Entity entity1 = ((ServerLevel)this.level()).getEntity(uuid1);
            if (entity1 instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity1;
               this.setOwner(livingEntity);
            }
         }
      }

   }

   public void tickDespawn() {
      if (this.pickup != Pickup.ALLOWED) {
         super.tickDespawn();
      }

   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   public boolean shouldRender(double p_37588_, double p_37589_, double p_37590_) {
      return true;
   }

   public static void abstractEffects(ItemStack stack, LivingEntity livingEntity) {
      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CORROSIVE_POTENCY.get()) > 0) {
         livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.GASTRIC_SPEWAGE.get()) > 0) {
         for(MobEffectInstance instance : BileLiquid.bileEffects()) {
            livingEntity.addEffect(instance);
         }
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CRYOGENIC_ASPECT.get()) > 0) {
         livingEntity.setTicksFrozen(livingEntity.getTicksFrozen() + 300);
      }

   }

   static {
      ID_FOIL = SynchedEntityData.defineId(ThrownSickle.class, EntityDataSerializers.BOOLEAN);
      COLOR = SynchedEntityData.defineId(ThrownSickle.class, EntityDataSerializers.INT);
   }

   public static enum SickelState {
      FLYING,
      HOOKED_IN_ENTITY,
      HOOKED_BLOCK;

      // $FF: synthetic method
      private static SickelState[] $values() {
         return new SickelState[]{FLYING, HOOKED_IN_ENTITY, HOOKED_BLOCK};
      }
   }
}
