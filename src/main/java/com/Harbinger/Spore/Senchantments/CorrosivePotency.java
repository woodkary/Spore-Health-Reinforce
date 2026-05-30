package com.Harbinger.Spore.Senchantments;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class CorrosivePotency extends BaseSporeEnchantment {
   public CorrosivePotency(EquipmentSlot... slots) {
      super(Rarity.COMMON, Senchantments.FUNGAL_ITEMS, slots);
   }

   public void doPostAttack(LivingEntity livingEntity, Entity entity, int value) {
      super.doPostAttack(livingEntity, entity, value);
      if (entity instanceof LivingEntity livingEntity1) {
         livingEntity1.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 60, 1));
      }

   }
}
