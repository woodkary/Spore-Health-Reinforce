package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.effects.SporeEffectsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public final class DeafItem extends Item {
    public DeafItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player player, InteractionHand p_41434_) {
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        return super.use(p_41432_, player, p_41434_);
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity entity, int p_41407_, boolean p_41408_) {
        if(entity instanceof Player player && LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)) {
            LivingEntityHealthLifecycleWrapperUtil.INSTANCE.slayPlayer(player);
        }
        super.inventoryTick(p_41404_, p_41405_, entity, p_41407_, p_41408_);
    }
}
