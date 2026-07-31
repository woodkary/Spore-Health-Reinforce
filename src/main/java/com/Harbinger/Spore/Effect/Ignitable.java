package com.Harbinger.Spore.Effect;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class Ignitable extends MobEffect {
    public Ignitable() {
        super(MobEffectCategory.NEUTRAL, 1908001);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
