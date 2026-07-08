package com.Harbinger.Spore.Core.utils.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface IEffectManager extends Consumer<LivingEvent.LivingTickEvent> {
    boolean checkEffect(MobEffectInstance effect);

    boolean checkAndAddEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source);

    void forceAddEffect(LivingEntity target, MobEffectInstance p_147216_, @Nullable Entity p_147217_);

    Collection<MobEffectInstance> getActiveEffectsHook(LivingEntity target, Collection<MobEffectInstance> initial);

    Map<MobEffect, MobEffectInstance> getActiveEffectsMapHook(LivingEntity target, Map<MobEffect, MobEffectInstance> initial);

    boolean hasEffectHook(LivingEntity target, MobEffect effect, boolean initial);

    MobEffectInstance getEffectHook(LivingEntity target, MobEffect effect, MobEffectInstance initial);

    boolean canBeAffectedHook(LivingEntity target, MobEffectInstance effect, boolean initial);

    boolean removeEffect(LivingEntity target, MobEffect effect);

    void tryApplyHealInhibit(LivingEntity entity);

    void tryApplyHealInhibit(LivingEntity entity, float expectedHealth);
}
