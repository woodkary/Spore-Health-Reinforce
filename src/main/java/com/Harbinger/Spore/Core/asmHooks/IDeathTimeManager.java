package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.function.Consumer;

public interface IDeathTimeManager extends Consumer<LivingEvent.LivingTickEvent> {
    /** A positive time selects the custom death render for this dispatch. */
    int getDeathTimeForRender(LivingEntity entity);

    int deathTimeGetFieldHook(LivingEntity entity, int initialDeathTime);

    void enterSpecialRender(LivingEntity entity);

    void exitSpecialRender(LivingEntity entity);
}
