package com.Harbinger.Spore.Sentities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public interface ArmedInfected {
   default Enchantment meleeEnchants(LivingEntity living) {
      List<Enchantment> values = new ArrayList();
      values.add(Enchantments.SHARPNESS);
      values.add(Enchantments.FIRE_ASPECT);
      values.add(Enchantments.KNOCKBACK);
      return (Enchantment)values.get(living.getRandom().nextInt(values.size()));
   }

   default Enchantment bowEnchantments(LivingEntity living) {
      List<Enchantment> values = new ArrayList();
      values.add(Enchantments.FLAMING_ARROWS);
      values.add(Enchantments.POWER_ARROWS);
      values.add(Enchantments.PUNCH_ARROWS);
      return (Enchantment)values.get(living.getRandom().nextInt(values.size()));
   }

   default Enchantment crossbowEnchantments(LivingEntity living) {
      List<Enchantment> values = new ArrayList();
      values.add(Enchantments.MULTISHOT);
      values.add(Enchantments.PIERCING);
      return (Enchantment)values.get(living.getRandom().nextInt(values.size()));
   }

   default Enchantment armorEnchantments(LivingEntity living) {
      List<Enchantment> values = new ArrayList();
      values.add(Enchantments.ALL_DAMAGE_PROTECTION);
      values.add(Enchantments.THORNS);
      values.add(Enchantments.UNBREAKING);
      return (Enchantment)values.get(living.getRandom().nextInt(values.size()));
   }

   default int getRandomLevel(Enchantment enchantment, LivingEntity living) {
      return enchantment.getMaxLevel() == 1 ? 1 : living.getRandom().nextInt(1, enchantment.getMaxLevel());
   }

   default void EnchantBasedOnItem(ItemStack stack, LivingEntity living) {
      Item item = stack.getItem();
      Enchantment melee = this.meleeEnchants(living);
      Enchantment bow = this.bowEnchantments(living);
      Enchantment crossbow = this.crossbowEnchantments(living);
      if (item instanceof TieredItem) {
         stack.enchant(melee, this.getRandomLevel(melee, living));
      }

      if (item instanceof BowItem) {
         stack.enchant(bow, this.getRandomLevel(bow, living));
      }

      if (item instanceof CrossbowItem) {
         stack.enchant(crossbow, this.getRandomLevel(crossbow, living));
      }

   }

   default void enchantItems(LivingEntity living) {
      Enchantment armor = this.armorEnchantments(living);
      living.getArmorSlots().forEach((item) -> item.enchant(armor, this.getRandomLevel(armor, living)));
      this.EnchantBasedOnItem(living.getMainHandItem(), living);
      this.EnchantBasedOnItem(living.getOffhandItem(), living);
   }
}
