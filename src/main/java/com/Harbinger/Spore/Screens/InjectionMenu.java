package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.SMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class InjectionMenu extends AbstractContainerMenu {
   public InjectionMenu(int id, Inventory inventory) {
      super((MenuType)SMenu.INJECTION_MENU.get(), id);
   }

   public InjectionMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
      this(id, inventory);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return true;
   }
}
