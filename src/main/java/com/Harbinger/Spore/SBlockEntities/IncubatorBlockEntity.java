package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Screens.IncubatorMenu;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeArmorData;
import com.Harbinger.Spore.Sitems.BaseWeapons.SporeWeaponData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class IncubatorBlockEntity extends BlockEntity implements AnimatedEntity, WorldlyContainer, MenuProvider {
   private static final int[] slotsTop = new int[]{0};
   private static final int[] slotsBottom = new int[]{0};
   private NonNullList<ItemStack> stacks;
   public int fuel;
   private int tick;
   private int side;

   public IncubatorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.INCUBATOR.get(), pos, state);
      this.stacks = NonNullList.withSize(1, ItemStack.EMPTY);
      this.side = this.setSide(state);
   }

   private int setSide(BlockState state) {
      Property var3 = state.getBlock().getStateDefinition().getProperty("facing");
      if (var3 instanceof DirectionProperty directionProperty) {
         return ((Direction)state.getValue(directionProperty)).get3DDataValue();
      } else {
         return 2;
      }
   }

   public void setSide(int i) {
      this.side = i;
   }

   public int getSide() {
      return this.side;
   }

   public NonNullList<ItemStack> getStacks() {
      return this.stacks;
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("fuel", this.getFuel());
      tag.putInt("side", this.getSide());
      ContainerHelper.saveAllItems(tag, this.stacks);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.fuel = tag.getInt("fuel");
      this.side = tag.getInt("side");
      this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(tag, this.stacks);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      this.setChanged();
      return this.saveWithFullMetadata();
   }

   public void setFuel(int i) {
      this.fuel = i;
   }

   public int getFuel() {
      return this.fuel;
   }

   public void addTick() {
      if (this.tick < 720) {
         ++this.tick;
      } else {
         this.tick = 0;
      }

   }

   public void HealItemStack() {
      for(ItemStack stack : this.stacks) {
         if (stack != ItemStack.EMPTY && this.getFuel() > 0 && stack.isDamaged()) {
            Item var5 = stack.getItem();
            if (var5 instanceof SporeWeaponData) {
               SporeWeaponData data = (SporeWeaponData)var5;
               data.healTool(stack, 1);
            } else {
               var5 = stack.getItem();
               if (var5 instanceof SporeArmorData) {
                  SporeArmorData data = (SporeArmorData)var5;
                  data.healTool(stack, 1);
               } else {
                  int l = stack.getDamageValue() - 1;
                  stack.setDamageValue(l);
               }
            }

            this.setFuel(this.getFuel() - 1);
         }
      }

   }

   public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, IncubatorBlockEntity e) {
      e.HealItemStack();
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity e) {
      e.addTick();
   }

   public boolean isActive() {
      return this.getFuel() > 0 && this.stacks.get(0) != ItemStack.EMPTY;
   }

   public int getTicks() {
      return this.tick;
   }

   public int[] getSlotsForFace(Direction direction) {
      return direction == Direction.UP ? slotsTop : slotsBottom;
   }

   public boolean canPlaceItemThroughFace(int p_19235_, ItemStack item, @Nullable Direction direction) {
      if ((direction == Direction.NORTH || direction == Direction.SOUTH || direction == Direction.EAST || direction == Direction.WEST) && this.getFuel() <= 750) {
         this.setFuel(this.getFuel() + 250);
         item.shrink(1);
      }

      return direction == Direction.UP && item.is(ItemTags.create(new ResourceLocation("spore:weapons")));
   }

   public boolean canTakeItemThroughFace(int p_19239_, ItemStack stack, Direction direction) {
      return direction == Direction.DOWN && !stack.isDamaged();
   }

   public int getContainerSize() {
      return this.stacks.size();
   }

   public boolean isEmpty() {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         if (!this.getItem(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int index) {
      return (ItemStack)this.stacks.get(index);
   }

   public ItemStack removeItem(int index, int count) {
      if (!((ItemStack)this.stacks.get(index)).isEmpty()) {
         ItemStack itemstack;
         if (((ItemStack)this.stacks.get(index)).getCount() <= count) {
            itemstack = (ItemStack)this.stacks.get(index);
            this.stacks.set(index, ItemStack.EMPTY);
         } else {
            itemstack = ((ItemStack)this.stacks.get(index)).split(count);
            if (((ItemStack)this.stacks.get(index)).isEmpty()) {
               this.stacks.set(index, ItemStack.EMPTY);
            }
         }

         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeItemNoUpdate(int index) {
      ItemStack stack = (ItemStack)this.stacks.get(index);
      this.stacks.set(index, ItemStack.EMPTY);
      return stack;
   }

   public void setItem(int index, ItemStack stack) {
      this.stacks.set(index, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.saveAdditional(this.getUpdateTag());
   }

   public boolean stillValid(Player p_18946_) {
      return true;
   }

   public void clearContent() {
      this.stacks.clear();
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
      if (packet != null && packet.getTag() != null) {
         this.load(packet.getTag());
      }

   }

   public Component getDisplayName() {
      return Component.translatable("block.spore.incubator");
   }

   public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return new IncubatorMenu(i, inventory);
   }
}
