package com.Harbinger.Spore.Senchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class BaseSporeEnchantment extends Enchantment {
   protected BaseSporeEnchantment(Rarity p_44676_, EnchantmentCategory p_44677_, EquipmentSlot[] p_44678_) {
      super(p_44676_, p_44677_, p_44678_);
   }

   public boolean isDiscoverable() {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack) {
      return false;
   }

   public boolean isAllowedOnBooks() {
      return false;
   }

   public boolean isTradeable() {
      return false;
   }
}
