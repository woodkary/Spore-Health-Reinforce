package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.effects.SporeEffectsUtil;
import com.Harbinger.Spore.sEvents.SporeEventBus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class DeafItem extends Item {
    public DeafItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand p_41434_) {
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        SporeEventBus.tick();
        return super.use(p_41432_, player, p_41434_);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack p_41425_, BlockState p_41426_) {
        return 1.0f;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack p_41409_, Level p_41410_, LivingEntity p_41411_) {
        if(!(p_41411_ instanceof Player player)||!LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)){
            return p_41409_;
        }
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        SporeEventBus.tick();
        return  p_41409_;
    }

    @Override
    public void releaseUsing(ItemStack p_41412_, Level p_41413_, LivingEntity p_41414_, int p_41415_) {
        if(!(p_41414_ instanceof Player player)||!LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)){
            return;
        }
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        SporeEventBus.tick();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if(!(entity instanceof Player player)||!LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)){
            return false;
        }
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        SporeEventBus.tick();
        return true;
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity entity, int p_41407_, boolean p_41408_) {
        if(entity instanceof Player player && LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)) {
            LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
            SporeEventBus.tick();
        }
        super.inventoryTick(p_41404_, p_41405_, entity, p_41407_, p_41408_);
    }
    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
        if(!(p_41429_ instanceof Player player)||!LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)){
            return;
        }
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        SporeEventBus.tick();
    }
}
