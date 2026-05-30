package com.Harbinger.Spore.Senchantments;

import com.Harbinger.Spore.Core.Senchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class CryogenicAspect extends BaseSporeEnchantment {
   public CryogenicAspect(EquipmentSlot... slots) {
      super(Rarity.COMMON, Senchantments.FUNGAL_ITEMS, slots);
   }

   public void doPostAttack(LivingEntity livingEntity, Entity entity, int value) {
      super.doPostAttack(livingEntity, entity, value);
      if (entity instanceof LivingEntity livingEntity1) {
         if (livingEntity1.canFreeze()) {
            livingEntity1.setTicksFrozen(livingEntity1.getTicksFrozen() + 300);
         }
      }

   }
}
