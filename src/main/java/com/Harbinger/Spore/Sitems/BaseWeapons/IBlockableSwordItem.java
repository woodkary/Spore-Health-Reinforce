package com.Harbinger.Spore.Sitems.BaseWeapons;

import com.Harbinger.Spore.Client.Renderers.SwordBlockingItemInHandRenderer;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IBlockableSwordItem {
    default ItemStack replaceRender(Player player, ItemStack itemStack) {
        if(!player.level.isClientSide){
            return itemStack;
        }
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        if(itemInHandRenderer.getClass()!= SwordBlockingItemInHandRenderer.renderClass){
            KlassPointerUtil.INSTANCE.replaceClass(itemInHandRenderer,SwordBlockingItemInHandRenderer.renderClass,"",0,0.0f);
        }
        return itemStack;
    }
}
