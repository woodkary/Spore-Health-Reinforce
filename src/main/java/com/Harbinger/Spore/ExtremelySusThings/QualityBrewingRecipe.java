package com.Harbinger.Spore.ExtremelySusThings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class QualityBrewingRecipe implements IBrewingRecipe {
   private final Potion input;
   private final Item ingredient;
   private final Potion output;

   public QualityBrewingRecipe(Potion input, Item ingredient, Potion output) {
      this.input = input;
      this.ingredient = ingredient;
      this.output = output;
   }

   public boolean isInput(ItemStack input) {
      return PotionUtils.getPotion(input) == this.input;
   }

   public boolean isIngredient(ItemStack ingredient) {
      return ingredient.getItem() == this.ingredient;
   }

   public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
      if (this.isInput(input) && this.isIngredient(ingredient)) {
         ItemStack itemStack = new ItemStack(input.getItem());
         itemStack.setTag(new CompoundTag());
         PotionUtils.setPotion(itemStack, this.output);
         return itemStack;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
