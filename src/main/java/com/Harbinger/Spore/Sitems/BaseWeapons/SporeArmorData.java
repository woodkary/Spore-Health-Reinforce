package com.Harbinger.Spore.Sitems.BaseWeapons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface SporeArmorData {
   String BASE_TAG = "agent";
   String PROTECTION_TAG = "mutant_protection";
   String TOUGHNESS_TAG = "mutant_toughness";
   String MAX_DURABILITY = "mutant_max_durability";
   String EXTRA_DURABILITY = "mutant_extra_durability";
   String ENCHANTING = "mutant_enchanting";
   String MUTATION = "mutation";

   default boolean tooHurt(ItemStack stack) {
      return stack.getDamageValue() < stack.getMaxDamage() - 10;
   }

   default double calculateTrueDefense(ItemStack stack, double defense) {
      double value = this.getAdditionalProtection(stack) * 0.01;
      return value > (double)0.0F ? defense + defense * value : defense;
   }

   default void setAdditionalProtection(double value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putDouble("mutant_protection", value);
   }

   default double getAdditionalProtection(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTagElement("agent");
      return tag.getDouble("mutant_protection");
   }

   default double calculateTrueToughness(ItemStack stack, double defense) {
      double value = this.getAdditionalProtection(stack) * 0.01;
      return value > (double)0.0F ? defense + defense * value : defense;
   }

   default void setAdditionalToughness(double value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putDouble("mutant_toughness", value);
   }

   default double getAdditionalToughness(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTagElement("agent");
      return tag.getDouble("mutant_toughness");
   }

   default void setLuck(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putDouble("mutant_enchanting", (double)value);
   }

   default int getLuck(ItemStack itemStack) {
      CompoundTag tag = itemStack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_enchanting");
   }

   default SporeArmorMutations getVariant(ItemStack stack) {
      return SporeArmorMutations.byId(this.getTypeVariant(stack) & 255);
   }

   default int getTypeVariant(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutation");
   }

   default void setVariant(SporeArmorMutations variant, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutation", variant.getId() & 255);
   }

   default void healTool(ItemStack stack, int value) {
      if (stack.getDamageValue() < stack.getMaxDamage()) {
         stack.setDamageValue(stack.getDamageValue() - value);
      }

      if (this.getMaxTrueAdditionalDurability(stack) > this.getAdditionalDurability(stack)) {
         this.setAdditionalDurability(this.getAdditionalDurability(stack) + value, stack);
      }

   }

   default int getMaxTrueAdditionalDurability(ItemStack stack) {
      return (int)((double)stack.getMaxDamage() * (double)this.getMaxAdditionalDurability(stack) * 0.01);
   }

   default int getMaxAdditionalDurability(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_max_durability");
   }

   default void setMaxAdditionalDurability(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutant_max_durability", value);
   }

   default int getAdditionalDurability(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      return tag.getInt("mutant_extra_durability");
   }

   default void setAdditionalDurability(int value, ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTagElement("agent");
      tag.putInt("mutant_extra_durability", value);
   }

   default void hurtExtraDurability(ItemStack stack, int value, @Nullable LivingEntity living) {
      this.setAdditionalDurability(this.getAdditionalDurability(stack) - value, stack);
   }
}
