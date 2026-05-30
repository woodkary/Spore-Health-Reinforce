package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Screens.ContainerMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ContainerBlockEntity extends RandomizableContainerBlockEntity {
   private NonNullList stacks;
   public NonNullList Stacks;

   public ContainerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.CONTAINER.get(), pos, state);
      this.stacks = NonNullList.withSize(32, ItemStack.EMPTY);
      this.Stacks = this.stacks;
   }

   public Component getDisplayName() {
      return Component.translatable("container.biohazard");
   }

   protected Component getDefaultName() {
      return Component.translatable("container.biohazard");
   }

   public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return new ContainerMenu(id, inventory, (new FriendlyByteBuf(Unpooled.buffer())).writeBlockPos(this.worldPosition));
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
      if (packet != null && packet.getTag() != null) {
         this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
         ContainerHelper.loadAllItems(packet.getTag(), this.stacks);
      }

   }

   public int getContainerSize() {
      return this.stacks.size();
   }

   protected void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (!this.trySaveLootTable(compound)) {
         ContainerHelper.saveAllItems(compound, this.stacks);
      }

   }

   public void load(CompoundTag compound) {
      super.load(compound);
      this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(compound)) {
         ContainerHelper.loadAllItems(compound, this.stacks);
      }

   }

   protected NonNullList getItems() {
      this.unpackLootTable((Player)null);
      return this.stacks;
   }

   protected void setItems(NonNullList nonNullList) {
      this.stacks = nonNullList;
   }
}
