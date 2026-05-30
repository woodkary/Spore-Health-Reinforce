package com.Harbinger.Spore.Core.utils.inventory;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class SporeEmptyInventory extends Inventory {
    @SuppressWarnings("unchecked")
    public static Class<? extends Inventory> inventoryClass = (Class<? extends Inventory>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEmptyInventory.class,
            Player.class
    );
    private static Inventory inst;
    private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            inventoryClass,
            SporeEmptyInventory.class,
            Player.class
    );

    public static Inventory newInstance(Player player){
        if(inst!=null&&inst.getClass()==inventoryClass){
            inst.player=player;
            return inst;
        }
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                inventoryClass,
                SporeEmptyInventory.class,
                Player.class
        );
        if(constructor!=null){
            try{
                return (Inventory) constructor.invoke(player);
            } catch (Throwable e) {
                LogUtil.error("failed to create SporeEmptyInventory constructor");
            }
        }
        return new SporeEmptyInventory(player);
    }

    private SporeEmptyInventory(Player player) {
        super(player);
        SporeEmptyInventory.inst=this;
    }

    @Override
    public int getFreeSlot() {
        return 0;
    }

    @Override
    public ItemStack getSelected() {
        return ItemStack.EMPTY;
    }

    @Override
    public void setPickedItem(ItemStack stack) {
    }

    @Override
    public void pickSlot(int slot) {
    }

    @Override
    public int findSlotMatchingItem(ItemStack stack) {
        return 0;
    }

    @Override
    public int findSlotMatchingUnusedItem(ItemStack stack) {
        return 0;
    }

    @Override
    public int getSuitableHotbarSlot() {
        return 0;
    }

    @Override
    public void swapPaint(double direction) {
    }

    @Override
    public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int maxItems, Container inventory) {
        return 0;
    }

    @Override
    public int getSlotWithRemainingSpace(ItemStack stack) {
        return 0;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean add(ItemStack stack) {
        return false;
    }

    @Override
    public boolean add(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void placeItemBackInInventory(ItemStack stack) {
    }

    @Override
    public void placeItemBackInInventory(ItemStack stack, boolean sendPacket) {
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        this.clearContent();
        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(ItemStack stack) {
        this.clearContent();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        this.clearContent();
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.clearContent();
    }

    @Override
    public float getDestroySpeed(BlockState state) {
        return 0.0f;
    }

    @Override
    public ListTag save(ListTag tag) {
        return super.save(tag);
    }

    @Override
    public void load(ListTag tag) {
        super.load(tag);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Component getName() {
        return Component.empty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getArmor(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void hurtArmor(DamageSource source, float amount, int[] slots) {
        super.hurtArmor(source, Float.MAX_VALUE, new int[]{0, 1, 2, 3});
    }

    @Override
    public void dropAll() {
        this.clearContent();
    }

    @Override
    public int getTimesChanged() {
        return 0;
    }

    @Override
    public void setChanged() {
        this.clearContent();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean contains(ItemStack stack) {
        return false;
    }

    @Override
    public boolean contains(TagKey<Item> tag) {
        return false;
    }

    @Override
    public void replaceWith(Inventory inventory) {
        super.replaceWith(newInstance(inventory.player));
    }

    @Override
    public void clearContent() {
        this.compartments=List.of();
        this.items=SporeEmptyItemStackNonNullList.newInstance(List.of(),ItemStack.EMPTY);
        this.armor=SporeEmptyItemStackNonNullList.newInstance(List.of(),ItemStack.EMPTY);
        this.offhand=SporeEmptyItemStackNonNullList.newInstance(List.of(),ItemStack.EMPTY);
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        this.items=SporeEmptyItemStackNonNullList.newInstance(List.of(),ItemStack.EMPTY);
        super.fillStackedContents(contents);
    }

    @Override
    public ItemStack removeFromSelected(boolean removeStack) {
        this.clearContent();
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull Component getCustomName() {
        return Component.empty();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        return false;
    }

    @Override
    public boolean hasAnyOf(Set<Item> items) {
        return false;
    }

    @Override
    public int countItem(Item item) {
        return 0;
    }

    @Override
    public boolean canTakeItem(Container container, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }
}
