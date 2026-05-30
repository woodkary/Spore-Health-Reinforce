package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Fluids.BileLiquid;
import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class BileProjectile extends Projectile implements ItemSupplier {
   private float setBaseDamage;
   private Predicate target = (livingEntityx) -> true;

   public BileProjectile(Level level) {
      super((EntityType)Sentities.BILE.get(), level);
   }

   public BileProjectile(Level level, LivingEntity livingEntity, Predicate predicate) {
      super((EntityType)Sentities.BILE.get(), level);
      this.setOwner(livingEntity);
      this.target = predicate;
   }

   protected void defineSynchedData() {
   }

   public float getDamage() {
      return this.setBaseDamage;
   }

   public void setDamage(float value) {
      this.setBaseDamage = value;
   }

   public @NotNull Packet getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   protected boolean canHitEntity(Entity entity) {
      return entity != this.getOwner();
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      if (!this.level().isClientSide()) {
         Entity entity = entityHitResult.getEntity();
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (this.target.test(livingEntity)) {
               entity.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity)this.getOwner()), this.getDamage());

               for(MobEffectInstance instance : BileLiquid.bileEffects()) {
                  livingEntity.addEffect(instance);
               }
            }
         }

         if (entity instanceof Boat) {
            Boat boat = (Boat)entity;
            boat.setDamage(50.0F);
         }
      } else {
         super.onHitEntity(entityHitResult);
      }

   }

   protected void onHitBlock(BlockHitResult hitResult) {
      super.onHitBlock(hitResult);
      if (this.level().getBlockState(hitResult.getBlockPos()).isSolidRender(this.level(), hitResult.getBlockPos())) {
         this.discard();
      }

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

   public ItemStack getItem() {
      return new ItemStack((ItemLike)Sitems.BILE.get());
   }
}
