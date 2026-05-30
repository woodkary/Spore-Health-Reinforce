package com.Harbinger.Spore.Senchantments;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Senchantments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class CorrosiveThorns extends BaseSporeEnchantment {
   public CorrosiveThorns() {
      super(Rarity.COMMON, Senchantments.FUNGAL_ITEMS, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
   }

   public boolean isCurse() {
      return false;
   }

   public void doPostHurt(LivingEntity livingEntity, Entity entity, int value) {
      if (entity instanceof LivingEntity attacker) {
         if (attacker.isAlive()) {
            int totalLevel = 0;

            for(ItemStack armor : livingEntity.getArmorSlots()) {
               totalLevel += EnchantmentHelper.getItemEnchantmentLevel(this, armor);
            }

            int duration = 40 + totalLevel * 40;
            if (totalLevel > 0) {
               attacker.hurt(livingEntity.damageSources().thorns(livingEntity), 3.5F * (float)totalLevel);
               if (Math.random() < (double)0.5F) {
                  attacker.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0, false, true));
               } else {
                  attacker.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), duration, 0, false, true));
               }
            }
         }
      }

   }
}
