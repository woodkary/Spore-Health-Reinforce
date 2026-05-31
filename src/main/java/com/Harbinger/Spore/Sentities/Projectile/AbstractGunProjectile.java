package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeToolsMutations;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public abstract class AbstractGunProjectile extends AbstractArrow implements SporeWeaponData {
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor TRAVEL;
   private static final EntityDataAccessor VARIANT;

   protected AbstractGunProjectile(EntityType entityType, Level level) {
      super(entityType, level);
   }

   protected ItemStack getPickupItem() {
      return ItemStack.EMPTY;
   }

   public Float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   public void setDamage(Float value) {
      this.entityData.set(DAMAGE, value);
   }

   public Float getTravel() {
      return (Float)this.entityData.get(TRAVEL);
   }

   public void setTravel(Float value) {
      this.entityData.set(TRAVEL, value);
   }

   public int getVariant() {
      return (Integer)this.entityData.get(VARIANT);
   }

   public void setVariant(Integer value) {
      this.entityData.set(VARIANT, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE, 0.0F);
      this.entityData.define(TRAVEL, 0.0F);
      this.entityData.define(VARIANT, 0);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setDamage(tag.getFloat("damage"));
      this.setTravel(tag.getFloat("travel"));
      this.setVariant(tag.getInt("variant"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", this.getDamage());
      tag.putFloat("travel", this.getTravel());
      tag.putInt("variant", this.getVariant());
   }

   protected void onHitEntity(EntityHitResult result) {
      if (this.level().isClientSide) {
         return;
      }

      Entity target = result.getEntity();
      CalamityMultipart severedPart = null;
      if (target instanceof CalamityMultipart multipart) {
         target = multipart.getParent();
         severedPart = multipart;
      }

      if (target instanceof LivingEntity living) {
         Entity var6 = this.getOwner();
         if (var6 instanceof LivingEntity owner) {
            float calculations = living.getMaxHealth() * this.getProDamage();
            float damage = this.getDamage();
            if (calculations > damage) {
               damage = calculations;
            }

            if (severedPart == null) {
               SporeAttackUtil.INSTANCE.dealDamage(living,owner,this.level().damageSources().mobProjectile(this, owner), damage);
            } else {
               severedPart.hurt(this.level().damageSources().mobProjectile(this, owner), damage);
            }

            this.doHitAfterEffects(living, owner);
            if (living instanceof Player && owner instanceof Player player) {
                player.playNotifySound((SoundEvent)Ssounds.BIOGUN_HIT_PLAYER.get(), SoundSource.MASTER, 1.0F, 1.0F);
            } else {
               this.playSound(this.entityImpactSound());
            }

            this.mutationBuffs(living, owner);
         }
      }

   }

   protected void mutationBuffs(LivingEntity victim, LivingEntity owner) {
      SporeToolsMutations mutations = this.getMutationVariant();
      if (mutations == SporeToolsMutations.TOXIC) {
         victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
      }

      if (mutations == SporeToolsMutations.ROTTEN) {
         victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
      }

      if (mutations == SporeToolsMutations.VAMPIRIC && owner.getHealth() < owner.getMaxHealth()) {
         owner.heal(2.0F);
      }

      if (mutations == SporeToolsMutations.CALCIFIED) {
         AABB aabb = victim.getBoundingBox().inflate((double)2.0F);

         for(Entity entity : this.level().getEntities(this, aabb)) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (living.hurtTime == 0 && !living.equals(owner)) {
                  living.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage() * 0.5F);
               }
            }
         }
      }

      if (mutations == SporeToolsMutations.BEZERK && Math.random() < 0.3) {
         if (Math.random() < (double)0.5F) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
         } else if (Math.random() < (double)0.5F) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0));
         } else {
            owner.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
         }
      }

   }

   public SporeToolsMutations getMutationVariant() {
      return SporeToolsMutations.byId(this.getVariant() & 255);
   }

   public void setMutationVariant(SporeToolsMutations variant) {
      this.setVariant(variant.getId() & 255);
   }

   public abstract SoundEvent blockImpactSound();

   public abstract SoundEvent entityImpactSound();

   public abstract float getMaxBlockRange();

   public abstract float getProDamage();

   public abstract void doHitAfterEffects(LivingEntity var1, LivingEntity var2);

   public abstract ParticleOptions getParticle();

   protected void onHitBlock(BlockHitResult result) {
      this.playSound(this.blockImpactSound());
      this.discard();
   }

   protected float getWaterInertia() {
      return 0.99F;
   }

   public void shootFrom(LivingEntity shooter, float velocity, float inaccuracy, float damage) {
      this.setOwner(shooter);
      float xRot = shooter.getXRot();
      float yRot = shooter.getYRot();
      double x = -Math.sin(Math.toRadians((double)yRot)) * Math.cos(Math.toRadians((double)xRot));
      double y = -Math.sin(Math.toRadians((double)xRot));
      double z = Math.cos(Math.toRadians((double)yRot)) * Math.cos(Math.toRadians((double)xRot));
      this.shoot(x, y, z, velocity, inaccuracy);
      this.setDamage(damage);
   }

   public void tick() {
      super.tick();
      double dx = this.getDeltaMovement().x;
      double dy = this.getDeltaMovement().y;
      double dz = this.getDeltaMovement().z;
      float distanceThisTick = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      float newTravel = this.getTravel() + distanceThisTick;
      this.setTravel(newTravel);
      if (newTravel >= this.getMaxBlockRange()) {
         this.discard();
      }

      if (this.level().isClientSide) {
         int i = this.getMutationVariant().getColor();
         float r = (float)(i >> 16 & 255) / 255.0F;
         float g = (float)(i >> 8 & 255) / 255.0F;
         float b = (float)(i & 255) / 255.0F;
         int tries = this.random.nextInt(4);

         for(int u = 0; u < tries; ++u) {
            float x = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
            float z = (this.random.nextFloat() - this.random.nextFloat()) * 0.5F;
            this.level().addParticle(this.getParticle(), this.getX() + (double)x, this.getY(), this.getZ() + (double)z, (double)r, (double)g, (double)b);
         }
      }

   }

   static {
      DAMAGE = SynchedEntityData.defineId(AbstractGunProjectile.class, EntityDataSerializers.FLOAT);
      TRAVEL = SynchedEntityData.defineId(AbstractGunProjectile.class, EntityDataSerializers.FLOAT);
      VARIANT = SynchedEntityData.defineId(AbstractGunProjectile.class, EntityDataSerializers.INT);
   }
}
