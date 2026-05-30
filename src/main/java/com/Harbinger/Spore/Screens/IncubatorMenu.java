package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.SMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class IncubatorMenu extends AbstractContainerMenu {
   public IncubatorMenu(int id, Inventory inventory) {
      super((MenuType)SMenu.INCUBATOR_MENU.get(), id);
   }

   public IncubatorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
      this(id, inventory);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return true;
   }
}
