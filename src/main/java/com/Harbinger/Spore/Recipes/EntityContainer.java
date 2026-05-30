package com.Harbinger.Spore.Recipes;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record EntityContainer(Entity entity) implements Container {
   public int getContainerSize() {
      return 0;
   }

   public boolean isEmpty() {
      return true;
   }

   public ItemStack getItem(int i) {
      return ItemStack.EMPTY;
   }

   public ItemStack removeItem(int i, int i1) {
      return ItemStack.EMPTY;
   }

   public ItemStack removeItemNoUpdate(int i) {
      return ItemStack.EMPTY;
   }

   public void setItem(int i, ItemStack itemStack) {
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public void clearContent() {
   }
}
