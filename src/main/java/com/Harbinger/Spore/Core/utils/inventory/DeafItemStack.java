package com.Harbinger.Spore.Core.utils.inventory;

import com.Harbinger.Spore.Core.Sitems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class DeafItemStack extends ItemStack {
    private final Item deafItem=Sitems.KILL_SELF.get();
    private final Holder<Item> holder=deafItem.builtInRegistryHolder();
    public DeafItemStack() {
        super(Sitems.KILL_SELF.get());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isItemEnabled(FeatureFlagSet p_250869_) {
        return true;
    }

    @Override
    public ItemStack split(int p_41621_) {
        return this;
    }

    @Override
    public ItemStack copyAndClear() {
        return this;
    }

    @Override
    public Item getItem() {
        return deafItem;
    }

    @Override
    public Holder<Item> getItemHolder() {
        return holder;
    }

    @Override
    public boolean is(TagKey<Item> key) {
        return holder.is(key);
    }

    @Override
    public boolean is(Item p_150931_) {
        return deafItem==p_150931_;
    }

    @Override
    public boolean is(Predicate<Holder<Item>> p_220168_) {
        return p_220168_.test(holder);
    }

    @Override
    public boolean is(Holder<Item> p_220166_) {
        return holder == p_220166_;
    }

    @Override
    public Stream<TagKey<Item>> getTags() {
        return holder.tags();
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41662_) {
        return deafItem.useOn(p_41662_);
    }

    @Override
    public InteractionResult onItemUseFirst(UseOnContext p_41662_) {
        return deafItem.onItemUseFirst(this, p_41662_);
    }

    @Override
    public InteractionResult onItemUse(UseOnContext p_41662_, Function<UseOnContext, InteractionResult> callback) {
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(BlockState p_41692_) {
        return deafItem.getDestroySpeed(this, p_41692_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41683_, Player p_41684_, InteractionHand p_41685_) {
        return deafItem.use(p_41683_, p_41684_, p_41685_);
    }

    @Override
    public ItemStack finishUsingItem(Level p_41672_, LivingEntity p_41673_) {
        return deafItem.finishUsingItem(this,p_41672_, p_41673_);
    }

    @Override
    public int getMaxStackSize() {
        return deafItem.getMaxStackSize();
    }

    @Override
    public boolean isStackable() {
        return false;
    }

    @Override
    public boolean isDamageableItem() {
        return deafItem.isDamageable(this);
    }

    @Override
    public boolean isDamaged() {
        return deafItem.isDamaged(this);
    }

    @Override
    public int getDamageValue() {
        return deafItem.getDamage(this);
    }

    @Override
    public void setDamageValue(int p_41722_) {

    }

    @Override
    public int getMaxDamage() {
        return deafItem.getMaxDamage(this);
    }

    @Override
    public boolean hurt(int p_220158_, RandomSource p_220159_, @Nullable ServerPlayer p_220160_) {
        return false;
    }

    @Override
    public <T extends LivingEntity> void hurtAndBreak(int p_41623_, T p_41624_, Consumer<T> p_41625_) {

    }

    @Override
    public boolean overrideStackedOnOther(Slot p_150927_, ClickAction p_150928_, Player p_150929_) {
        return deafItem.overrideStackedOnOther(this,p_150927_, p_150928_, p_150929_);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack p_150933_, Slot p_150934_, ClickAction p_150935_, Player p_150936_, SlotAccess p_150937_) {
        return deafItem.overrideOtherStackedOnMe(this, p_150933_, p_150934_, p_150935_, p_150936_, p_150937_);
    }

    @Override
    public void hurtEnemy(LivingEntity p_41641_, Player p_41642_) {

    }

    @Override
    public void mineBlock(Level p_41687_, BlockState p_41688_, BlockPos p_41689_, Player p_41690_) {

    }

    @Override
    public boolean isCorrectToolForDrops(BlockState p_41736_) {
        return false;
    }

    @Override
    public ItemStack copy() {
        return this;
    }

    @Override
    public InteractionResult interactLivingEntity(Player p_41648_, LivingEntity p_41649_, InteractionHand p_41650_) {
        return InteractionResult.FAIL;
    }

    @Override
    public ItemStack copyWithCount(int p_256354_) {
        return this;
    }

    @Override
    public void inventoryTick(Level p_41667_, Entity p_41668_, int p_41669_, boolean p_41670_) {
        deafItem.inventoryTick(this,p_41667_, p_41668_, p_41669_, p_41670_);
    }

    @Override
    public int getUseDuration() {
        return deafItem.getUseDuration(this);
    }

    @Override
    public UseAnim getUseAnimation() {
        return deafItem.getUseAnimation(this);
    }

    @Override
    public void releaseUsing(Level p_41675_, LivingEntity p_41676_, int p_41677_) {
        deafItem.releaseUsing(this,p_41675_, p_41676_, p_41677_);
    }

    @Override
    public boolean useOnRelease() {
        return deafItem.useOnRelease(this);
    }

    @Override
    public ItemStack setHoverName(@Nullable Component p_41715_) {
        return this;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public void onUseTick(Level p_41732_, LivingEntity p_41733_, int p_41734_) {
        deafItem.onUseTick(p_41732_, p_41733_, this, p_41734_);
    }

    @Override
    public boolean onEntitySwing(LivingEntity entity) {
        return deafItem.onEntitySwing(this,entity);
    }

    @Override
    public void onStopUsing(LivingEntity entity, int count) {
        deafItem.onStopUsing(this,entity,count);
    }

    @Override
    public boolean canDisableShield(ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        deafItem.onDestroyed(itemEntity, damageSource);
    }
}
