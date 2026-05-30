package com.Harbinger.Spore.Screens;

import com.Harbinger.Spore.Core.SMenu;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
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
import org.jetbrains.annotations.NotNull;

public class SurgeryMenu extends AbstractContainerMenu {
   public final SurgeryTableBlockEntity blockEntity;
   private final Level level;
   private static final int HOTBAR_SLOT_COUNT = 9;
   private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
   private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
   private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
   private static final int VANILLA_SLOT_COUNT = 36;
   private static final int VANILLA_FIRST_SLOT_INDEX = 0;
   private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
   private static final int TE_INVENTORY_SLOT_COUNT = 20;

   public SurgeryMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
      this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(25));
   }

   public SurgeryMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
      super((MenuType)SMenu.SURGERY_MENU.get(), pContainerId);
      checkContainerSize(inv, 25);
      this.blockEntity = (SurgeryTableBlockEntity)entity;
      this.level = inv.player.level();
      this.addPlayerInventory(inv);
      this.addPlayerHotbar(inv);
      this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((iItemHandler) -> {
         this.addSlot(new SlotItemHandler(iItemHandler, 0, 7, 8));
         this.addSlot(new SlotItemHandler(iItemHandler, 1, 7, 26));
         this.addSlot(new SlotItemHandler(iItemHandler, 2, 7, 44));
         this.addSlot(new SlotItemHandler(iItemHandler, 3, 7, 62));
         this.addSlot(new SlotItemHandler(iItemHandler, 4, 25, 8));
         this.addSlot(new SlotItemHandler(iItemHandler, 5, 25, 26));
         this.addSlot(new SlotItemHandler(iItemHandler, 6, 25, 44));
         this.addSlot(new SlotItemHandler(iItemHandler, 7, 25, 62));
         this.addSlot(new SlotItemHandler(iItemHandler, 8, 43, 8));
         this.addSlot(new SlotItemHandler(iItemHandler, 9, 43, 26));
         this.addSlot(new SlotItemHandler(iItemHandler, 10, 43, 44));
         this.addSlot(new SlotItemHandler(iItemHandler, 11, 43, 62));
         this.addSlot(new SlotItemHandler(iItemHandler, 12, 61, 8));
         this.addSlot(new SlotItemHandler(iItemHandler, 13, 61, 26));
         this.addSlot(new SlotItemHandler(iItemHandler, 14, 61, 44));
         this.addSlot(new SlotItemHandler(iItemHandler, 15, 61, 62));
         this.addSlot(new SlotItemHandler(iItemHandler, 16, 97, 8) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return stack.is(SurgeryMenu.this.blockEntity.stringLikeItem);
            }
         });
         this.addSlot(new SlotItemHandler(iItemHandler, 17, 115, 8) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return stack.getItem() == Sitems.HARDENING_AGENT.get();
            }
         });
         this.addSlot(new SlotItemHandler(iItemHandler, 18, 133, 8) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return stack.getItem() == Sitems.SHARPENING_AGENT.get();
            }
         });
         this.addSlot(new SlotItemHandler(iItemHandler, 19, 151, 8) {
            public boolean mayPlace(@NotNull ItemStack stack) {
               return stack.getItem() == Sitems.INTEGRATING_AGENT.get();
            }
         });
         this.addSlot(new SlotItemHandler(iItemHandler, 20, 124, 53) {
            public boolean mayPlace(ItemStack stack) {
               return false;
            }

            public void onTake(Player player, ItemStack stack) {
               super.onTake(player, stack);
               SurgeryMenu.this.blockEntity.consumeItems();
               SurgeryMenu.this.blockEntity.assembleWeapon(player, stack);
            }
         });
      });
      this.addDataSlots(data);
   }

   public ItemStack quickMoveStack(Player playerIn, int pIndex) {
      if (pIndex == 20) {
         return ItemStack.EMPTY;
      } else {
         Slot sourceSlot = (Slot)this.slots.get(pIndex);
         if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack sourceStack = sourceSlot.getItem();
            ItemStack copyOfSourceStack = sourceStack.copy();
            if (pIndex < 36) {
               if (!this.moveItemStackTo(sourceStack, 36, 56, false)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (pIndex >= 56) {
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

   public boolean stillValid(Player pPlayer) {
      return stillValid(ContainerLevelAccess.create(this.level, this.blockEntity.getBlockPos()), pPlayer, (Block)Sblocks.SURGERY_TABLE.get());
   }

   private void addPlayerInventory(Inventory playerInventory) {
      for(int i = 0; i < 3; ++i) {
         for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
         }
      }

   }

   private void addPlayerHotbar(Inventory playerInventory) {
      for(int i = 0; i < 9; ++i) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }

   }

   public void removed(Player playerIn) {
      super.removed(playerIn);
      if (playerIn instanceof ServerPlayer serverPlayer) {
         if (serverPlayer.isAlive() && !serverPlayer.hasDisconnected()) {
            for(int i = 0; i < this.blockEntity.itemHandler.getSlots(); ++i) {
               if (i != 20) {
                  playerIn.getInventory().placeItemBackInInventory(this.blockEntity.itemHandler.extractItem(i, this.blockEntity.itemHandler.getStackInSlot(i).getCount(), false));
               }
            }
         } else {
            for(int j = 0; j < this.blockEntity.itemHandler.getSlots(); ++j) {
               if (j != 20) {
                  playerIn.drop(this.blockEntity.itemHandler.extractItem(j, this.blockEntity.itemHandler.getStackInSlot(j).getCount(), false), false);
               }
            }
         }
      }

   }
}
