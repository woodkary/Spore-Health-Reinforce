package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.PlayMessages;

public class ThrownBoomerang extends AbstractArrow {
   private static final EntityDataAccessor ID_FOIL;
   private static final EntityDataAccessor COLOR;
   private ItemStack boomerang;
   private boolean dealtDamage;
   private int returnTick;

   public ThrownBoomerang(Level level, LivingEntity owner, ItemStack stack, int color) {
      super((EntityType)Sentities.THROWN_BOOMERANG.get(), owner, level);
      this.boomerang = new ItemStack((ItemLike)Sitems.BOOMERANG.get());
      this.boomerang = stack.copy();
      this.entityData.set(ID_FOIL, stack.hasFoil());
      this.entityData.set(COLOR, color);
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   public ItemStack getBoomerang() {
      return this.boomerang;
   }

   public ThrownBoomerang(PlayMessages.SpawnEntity spawnEntity, Level level) {
      this((EntityType)Sentities.THROWN_BOOMERANG.get(), level);
   }

   public ThrownBoomerang(EntityType type, Level level) {
      super(type, level);
      this.boomerang = new ItemStack((ItemLike)Sitems.BOOMERANG.get());
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ID_FOIL, false);
      this.entityData.define(COLOR, 0);
   }

   public void tick() {
      if (this.inGroundTime > 4 || this.returnTick++ > 35) {
         this.dealtDamage = true;
      }

      Entity owner = this.getOwner();
      if ((this.dealtDamage || this.isNoPhysics()) && owner != null) {
         if (!this.isAcceptibleReturnOwner()) {
            if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
         } else {
            this.setNoPhysics(true);
            Vec3 direction = owner.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + direction.y * 0.045, this.getZ());
            if (this.level().isClientSide) {
               this.yOld = this.getY();
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(direction.normalize().scale(0.15)));
         }
      }

      super.tick();
   }

   private boolean isAcceptibleReturnOwner() {
      Entity owner = this.getOwner();
      return owner != null && owner.isAlive() && (!(owner instanceof ServerPlayer) || !owner.isSpectator());
   }

   protected ItemStack getPickupItem() {
      return this.boomerang.copy();
   }

   public boolean isFoil() {
      return (Boolean)this.entityData.get(ID_FOIL);
   }

   @Nullable
   protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
      return this.dealtDamage ? null : super.findHitEntity(from, to);
   }

   protected void onHitBlock(BlockHitResult p_36755_) {
      this.dealtDamage = true;
      super.onHitBlock(p_36755_);
   }

   protected void onHitEntity(EntityHitResult result) {
      Entity target = result.getEntity();
      float baseDamage = (float)(Integer)SConfig.SERVER.boomerang_damage.get() + 0.5F * (float)EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, this.boomerang);
      if (target instanceof LivingEntity livingTarget) {
         baseDamage += EnchantmentHelper.getDamageBonus(this.boomerang, livingTarget.getMobType());
      }

      Entity owner = this.getOwner();
      DamageSource source = this.damageSources().trident(this, (Entity)(owner == null ? this : owner));
      this.dealtDamage = true;
      if (target.hurt(source, baseDamage)) {
         if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)target;
            if (owner instanceof LivingEntity) {
               LivingEntity ownerLiving = (LivingEntity)owner;
               EnchantmentHelper.doPostHurtEffects(living, ownerLiving);
               EnchantmentHelper.doPostDamageEffects(ownerLiving, living);
               Item var9 = this.boomerang.getItem();
               if (var9 instanceof SporeWeaponData) {
                  SporeWeaponData data = (SporeWeaponData)var9;
                  data.abstractMutationBuffs(living, ownerLiving, this.boomerang, data);
               }
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, this.boomerang) > 0) {
               target.setSecondsOnFire(4 * EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, this.boomerang));
            }

            abstractEffects(this.boomerang, living);
            this.doPostHurtEffects(living);
         }

         this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
         this.playSound((SoundEvent)Ssounds.INFECTED_WEAPON_HIT_ENTITY.get(), 1.0F, 1.0F);
      }

   }

   protected boolean tryPickup(Player player) {
      return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
   }

   public boolean isNoGravity() {
      return true;
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return (SoundEvent)Ssounds.INFECTED_WEAPON_HIT_BLOCK.get();
   }

   public void playerTouch(Player player) {
      if (this.ownedBy(player) || this.getOwner() == null) {
         super.playerTouch(player);
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("Boomerang", 10)) {
         this.boomerang = ItemStack.of(tag.getCompound("Boomerang"));
      }

      this.dealtDamage = tag.getBoolean("DealtDamage");
      this.entityData.set(COLOR, tag.getInt("color"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put("Boomerang", this.boomerang.save(new CompoundTag()));
      tag.putBoolean("DealtDamage", this.dealtDamage);
      tag.putInt("color", (Integer)this.entityData.get(COLOR));
   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   public boolean shouldRender(double x, double y, double z) {
      return true;
   }

   public static void abstractEffects(ItemStack stack, LivingEntity target) {
      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CORROSIVE_POTENCY.get()) > 0) {
         target.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.GASTRIC_SPEWAGE.get()) > 0) {
         for(MobEffectInstance effect : BileLiquid.bileEffects()) {
            target.addEffect(effect);
         }
      }

      if (stack.getEnchantmentLevel((Enchantment)Senchantments.CRYOGENIC_ASPECT.get()) > 0) {
         target.setTicksFrozen(target.getTicksFrozen() + 300);
      }

   }

   static {
      ID_FOIL = SynchedEntityData.defineId(ThrownBoomerang.class, EntityDataSerializers.BOOLEAN);
      COLOR = SynchedEntityData.defineId(ThrownBoomerang.class, EntityDataSerializers.INT);
   }
}
