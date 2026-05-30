package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.SMenu;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.SBlockEntities.SurgeryTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class GraftingMenu extends AbstractContainerMenu {
   public final SurgeryTableBlockEntity blockEntity;
   private final Level level;
   private static final int HOTBAR_SLOT_COUNT = 9;
   private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
   private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
   private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
   private static final int VANILLA_SLOT_COUNT = 36;
   private static final int VANILLA_FIRST_SLOT_INDEX = 0;
   private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
   private static final int TE_INVENTORY_SLOT_COUNT = 4;

   public GraftingMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
      this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(25));
   }

   public GraftingMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
      super((MenuType)SMenu.GRAFTING_MENU.get(), containerId);
      this.blockEntity = (SurgeryTableBlockEntity)entity;
      this.level = inv.player.level();
      this.addPlayerInventory(inv);
      this.addPlayerHotbar(inv);
      this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((iItemHandler) -> {
         this.addSlot(new SlotItemHandler(iItemHandler, 21, 25, 8));
         this.addSlot(new SlotItemHandler(iItemHandler, 22, 25, 35));
         this.addSlot(new SlotItemHandler(iItemHandler, 23, 25, 62));
         this.addSlot(new SlotItemHandler(iItemHandler, 24, 88, 35) {
            public boolean mayPlace(ItemStack stack) {
               return false;
            }

            public void onTake(Player player, ItemStack stack) {
               super.onTake(player, GraftingMenu.this.blockEntity.assembleGraft(stack));
               GraftingMenu.this.blockEntity.consumeItemsGrafting();
            }
         });
      });
      this.addDataSlots(data);
   }

   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(this.level, this.blockEntity.getBlockPos()), player, (Block)Sblocks.SURGERY_TABLE.get());
   }

   public ItemStack quickMoveStack(Player playerIn, int pIndex) {
      if (pIndex == 24) {
         return ItemStack.EMPTY;
      } else {
         Slot sourceSlot = (Slot)this.slots.get(pIndex);
         if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack sourceStack = sourceSlot.getItem();
            sourceSlot.onTake(playerIn, sourceStack);
            ItemStack copyOfSourceStack = sourceStack.copy();
            if (pIndex < 36) {
               if (!this.moveItemStackTo(sourceStack, 36, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (pIndex >= 40) {
                  System.out.println("Invalid slotIndex:" + pIndex);
                  return ItemStack.EMPTY;
               }

               if (!this.moveItemStackTo(sourceStack, 0, 36, false)) {
                  return ItemStack.EMPTY;
               }
            }

            if (sourceStack.getCount() == 0) {
               sourceSlot.set(ItemStack.EMPTY);
            } else {
               sourceSlot.setChanged();
            }

            sourceSlot.onTake(playerIn, sourceStack);
            return copyOfSourceStack;
         }
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (player instanceof ServerPlayer serverPlayer) {
         if (serverPlayer.isAlive() && !serverPlayer.hasDisconnected()) {
            for(int j = 21; j <= 23; ++j) {
               player.getInventory().placeItemBackInInventory(this.blockEntity.itemHandler.extractItem(j, this.blockEntity.itemHandler.getStackInSlot(j).getCount(), false));
            }
         } else {
            for(int j = 21; j <= 23; ++j) {
               player.drop(this.blockEntity.itemHandler.extractItem(j, this.blockEntity.itemHandler.getStackInSlot(j).getCount(), false), false);
            }
         }
      }

   }

   private void addPlayerInventory(Inventory playerInventory) {
      for(int row = 0; row < 3; ++row) {
         for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

   }

   private void addPlayerHotbar(Inventory playerInventory) {
      for(int col = 0; col < 9; ++col) {
         this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
      }

   }
}
