package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.function.Consumer;

public interface IDeathTimeManager extends Consumer<LivingEvent.LivingTickEvent> {
    int deathTimeGetFieldHook(LivingEntity entity, int initialDeathTime);
}
