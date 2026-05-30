package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sentities;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.ForgeEventFactory;

public class ThrownBlockProjectile extends Projectile {
   private Predicate victim = (livingEntityx) -> true;
   private static final EntityDataAccessor DAMAGE;
   private static final EntityDataAccessor STATE;

   public ThrownBlockProjectile(Level level) {
      super((EntityType)Sentities.THROWN_BLOCK.get(), level);
      this.entityData.set(STATE, Blocks.GRASS_BLOCK.defaultBlockState());
   }

   public ThrownBlockProjectile(Level level, LivingEntity livingEntity, Float damage, BlockState state, Predicate livingEntityPredicate) {
      super((EntityType)Sentities.THROWN_BLOCK.get(), level);
      this.setOwner(livingEntity);
      this.entityData.set(DAMAGE, damage);
      this.entityData.set(STATE, state);
      this.victim = livingEntityPredicate;
   }

   public BlockState state() {
      return (BlockState)this.entityData.get(STATE);
   }

   protected void defineSynchedData() {
      this.entityData.define(DAMAGE, 5.0F);
      this.entityData.define(STATE, Blocks.GRASS_BLOCK.defaultBlockState());
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DAMAGE, tag.getFloat("damage"));
      this.entityData.set(STATE, NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("state")));
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("damage", (Float)this.entityData.get(DAMAGE));
      tag.put("state", NbtUtils.writeBlockState((BlockState)this.entityData.get(STATE)));
   }

   protected boolean canHitEntity(Entity entity) {
      boolean var10000;
      if (entity == this.getOwner()) {
         label26: {
            if (entity instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity;
               if (this.victim.test(livingEntity)) {
                  break label26;
               }
            }

            var10000 = false;
            return var10000;
         }
      }

      var10000 = true;
      return var10000;
   }

   public void tick() {
      super.tick();
      if (this.tickCount >= 300) {
         this.remove(RemovalReason.DISCARDED);
         FallingBlockEntity.fall(this.level(), new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ()), (BlockState)this.entityData.get(STATE));
      }

      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      Vec3 vec3 = this.getDeltaMovement().add((double)0.0F, -0.1, (double)0.0F);
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      this.setPos(d0, d1, d2);
      if (hitresult.getType() != Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
         this.onHit(hitresult);
      }

   }

   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      if (!this.level().isClientSide) {
         Entity var3 = result.getEntity();
         if (var3 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)var3;
            BlockPos pos = new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ());
            DamageSources var10001 = this.level().damageSources();
            Entity var5 = this.getOwner();
            LivingEntity var10003;
            if (var5 instanceof LivingEntity) {
               LivingEntity livingEntity1 = (LivingEntity)var5;
               var10003 = livingEntity1;
            } else {
               var10003 = null;
            }

            livingEntity.hurt(var10001.mobProjectile(this, var10003), (Float)this.entityData.get(DAMAGE) * ((BlockState)this.entityData.get(STATE)).getDestroySpeed(this.level(), pos));
            FallingBlockEntity.fall(this.level(), pos, (BlockState)this.entityData.get(STATE));
            this.discard();
         }
      }

   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      BlockPos pos = result.getBlockPos().relative(result.getDirection()).above();
      FallingBlockEntity.fall(this.level(), pos, (BlockState)this.entityData.get(STATE));
      this.discard();
   }

   static {
      DAMAGE = SynchedEntityData.defineId(ThrownBlockProjectile.class, EntityDataSerializers.FLOAT);
      STATE = SynchedEntityData.defineId(ThrownBlockProjectile.class, EntityDataSerializers.BLOCK_STATE);
   }
}
