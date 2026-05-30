package com.Harbinger.Spore.Sentities.Projectile;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class FallenAcidSack extends ThrowableItemProjectile {
   public FallenAcidSack(EntityType type, Level level) {
      super(type, level);
   }

   public FallenAcidSack(Level level, LivingEntity entity) {
      super((EntityType)Sentities.FALLEN_ACID_BULB.get(), entity, level);
   }

   public FallenAcidSack(Level level) {
      super((EntityType)Sentities.FALLEN_ACID_BULB.get(), level);
   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      Level var3 = this.level();
      if (var3 instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 3, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         AABB aabb = this.getBoundingBox().inflate((double)3.0F);
         List<Entity> entities = this.level().getEntities(this, aabb);
         this.poisonTargets(entities);
      }

      this.playSound((SoundEvent)Ssounds.FUNGAL_BOOM.get());
      this.discard();
   }

   public void poisonTargets(List<Entity> entityList) {
      for(Entity entity : entityList) {
         if (entity instanceof LivingEntity livingEntity) {
            if (Utilities.TARGET_SELECTOR.Test(livingEntity)) {
               livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 200, 1));
               livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            }
         }
      }

   }

   protected boolean canHitEntity(Entity target) {
      return false;
   }

   protected Item getDefaultItem() {
      return (Item)Sitems.ACID_BALL.get();
   }
}
