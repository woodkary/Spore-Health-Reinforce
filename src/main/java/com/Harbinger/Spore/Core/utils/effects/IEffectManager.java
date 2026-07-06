package com.Harbinger.Spore.Core.utils.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IEffectManager extends Consumer<LivingEvent.LivingTickEvent> {
    void forceAddEffect(LivingEntity target, MobEffectInstance p_147216_, @Nullable Entity p_147217_);

    boolean removeEffect(LivingEntity target, MobEffect effect);
}
