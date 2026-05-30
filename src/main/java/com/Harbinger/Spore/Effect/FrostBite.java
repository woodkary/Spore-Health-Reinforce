package com.Harbinger.Spore.Effect;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FrostBite extends MobEffect {
   public static final TagKey coldWeakness;

   public FrostBite() {
      super(MobEffectCategory.BENEFICIAL, 8991416);
   }

   public void applyEffectTick(LivingEntity entity, int intense) {
      if (entity.getType().is(FrostBite.coldWeakness)) {
         int level = 0;
         double modifier = 0.1;
         if (entity instanceof ColdWeakness) {
            ColdWeakness coldWeakness = (ColdWeakness)entity;
            level = coldWeakness.getEndurance().getLevel();
            modifier = coldWeakness.getEndurance().getHealthModifier();
         }

         if (intense >= level && this == Seffects.FROSTBITE.get() && !entity.level().isClientSide) {
            float damage = (float)((double)entity.getMaxHealth() * modifier + (double)intense);
            entity.hurt(entity.damageSources().freeze(), damage);
            entity.setTicksFrozen(entity.getTicksFrozen() + 100);
         }
      }

   }

   public boolean isDurationEffectTick(int duration, int intensity) {
      if (this == Seffects.FROSTBITE.get()) {
         return duration % 80 == 0;
      } else {
         return false;
      }
   }

   static {
      coldWeakness = EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES;
   }
}
