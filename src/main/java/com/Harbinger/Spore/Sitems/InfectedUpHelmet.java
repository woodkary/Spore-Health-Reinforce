package com.Harbinger.Spore.Sitems;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class InfectedUpHelmet extends UpgradedInfectedExoskeleton {
    public InfectedUpHelmet() {
        super(Type.HELMET);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        super.onArmorTick(stack, level, player);
        if (player.tickCount % 10 == 0 && player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
        }
        if (player.tickCount % 20 == 0 && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false));
        }
    }
}
