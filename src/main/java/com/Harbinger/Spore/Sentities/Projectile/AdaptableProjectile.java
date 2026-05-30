package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Variants.BulletParameters;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class AdaptableProjectile extends Projectile {
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor TYPE;
   private static final EntityDataAccessor PARTICLES;

   public AdaptableProjectile(Level level) {
      super((EntityType)Sentities.SPIT.get(), level);
   }

   public AdaptableProjectile(BulletParameters parameters, Level level, LivingEntity livingEntity) {
      super((EntityType)Sentities.SPIT.get(), level);
      this.setOwner(livingEntity);
      this.setType(parameters.getId());
      this.setDamage(parameters.getDamage());
   }

   protected void defineSynchedData() {
      this.entityData.define(DAMAGE, 0.0F);
      this.entityData.define(TYPE, 0);
      this.entityData.define(PARTICLES, 0);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DAMAGE, tag.getFloat("damage"));
      this.entityData.set(TYPE, tag.getInt("type"));
      this.entityData.set(PARTICLES, tag.getInt("particles"));
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", (Float)this.entityData.get(DAMAGE));
      tag.putInt("type", (Integer)this.entityData.get(TYPE));
      tag.putInt("particles", (Integer)this.entityData.get(PARTICLES));
   }

   public void setType(int i) {
      this.entityData.set(TYPE, i);
   }

   public void setDamage(float i) {
      this.entityData.set(DAMAGE, i);
   }

   public void setParticles(int i) {
      this.entityData.set(PARTICLES, i);
   }

   public int getParticles() {
      return (Integer)this.entityData.get(PARTICLES);
   }

   public @NotNull Packet getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   protected boolean canHitEntity(Entity entity) {
      return entity != this.getOwner();
   }

   public void tick() {
      super.tick();
      if (this.tickCount >= 300) {
         this.remove(RemovalReason.DISCARDED);
      }

      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      Vec3 vec3 = this.getDeltaMovement();
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      this.setPos(d0, d1, d2);
      if (hitresult.getType() != Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
         this.onHit(hitresult);
      }

   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      if (!this.level().isClientSide()) {
         Entity entity = entityHitResult.getEntity();
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (livingEntity instanceof Infected || livingEntity instanceof UtilityEntity || ((List)SConfig.SERVER.blacklist.get()).contains(livingEntity.getEncodeId())) {
               return;
            }

            int type = (Integer)this.entityData.get(TYPE);
            livingEntity.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity)this.getOwner()), (Float)this.entityData.get(DAMAGE));
            if (type == 0) {
               livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 400, 3));
            } else if (type == 1) {
               livingEntity.level().explode(this.getOwner(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 1.0F, ExplosionInteraction.NONE);
            } else if (type == 2) {
               livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 600, 1));
            } else if (type == 3) {
               livingEntity.setSecondsOnFire(6);
            }

            this.discard();
         }
      } else {
         super.onHitEntity(entityHitResult);
      }

   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      if (!this.level().isClientSide) {
         int type = (Integer)this.entityData.get(TYPE);
         Level level = this.level();
         if (type == 3 && level.getBlockState(blockHitResult.getBlockPos()).isFlammable(level, blockHitResult.getBlockPos(), blockHitResult.getDirection())) {
            this.level().setBlock(blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), Blocks.FIRE.defaultBlockState(), 3);
         }

         this.discard();
      }

      super.onHitBlock(blockHitResult);
   }

   static {
      DAMAGE = SynchedEntityData.defineId(AdaptableProjectile.class, EntityDataSerializers.FLOAT);
      TYPE = SynchedEntityData.defineId(AdaptableProjectile.class, EntityDataSerializers.INT);
      PARTICLES = SynchedEntityData.defineId(AdaptableProjectile.class, EntityDataSerializers.INT);
   }
}
