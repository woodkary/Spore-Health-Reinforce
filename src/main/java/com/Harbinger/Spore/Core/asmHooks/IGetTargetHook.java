package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;

public interface IGetTargetHook extends Consumer<TickEvent.ServerTickEvent> {
    LivingEntity getTargetCallSiteHook(Mob mob, LivingEntity original);

    boolean isSelfTarrget(Mob mob);

    void putSelfTarrget(Mob mob);

    void serverTick(TickEvent.ServerTickEvent event);
}
