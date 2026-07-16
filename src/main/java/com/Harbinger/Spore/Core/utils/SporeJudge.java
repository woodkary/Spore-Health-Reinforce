package com.Harbinger.Spore.Core.utils;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

import java.util.Optional;

/**
 * @author karywoodOyo
 */
public class SporeJudge {
    public static boolean isSporeEntity(Entity entity) {
        if(entity == null) return false;
        return isSporeClass(entity.getClass());
    }
    public static boolean isSporeClass(Class<?> clazz) {
        return clazz.getName().startsWith("com.Harbinger.Spore");
    }
    public static boolean isSporeItem(ItemStack item) {
        return isSporeItem(item.getItem());
    }
    public static boolean isSporeTieredItem(ItemStack item) {
        return isSporeTieredItem(item.getItem());
    }
    public static boolean isSporeTieredItem(Item item) {
        return item instanceof TieredItem &&isSporeClass(item.getClass());
    }
    public static boolean isSporeItem(Item item){
        return isSporeClass(item.getClass());
    }
    public static boolean isSporeDamageSource(DamageSource source){
        Holder<DamageType> holder = source.typeHolder();
        Optional<ResourceKey<DamageType>> keyOpt = holder.unwrapKey();
        return keyOpt.map(damageTypeResourceKey -> damageTypeResourceKey.location().toString().contains("Spore")).orElse(false);

    }
}
