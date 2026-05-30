package com.Harbinger.Spore.Sitems.BaseWeapons;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface DeathRewardingWeapon {
   void computeAfterEffect(LivingEntity var1, LivingEntity var2, ItemStack var3);
}
