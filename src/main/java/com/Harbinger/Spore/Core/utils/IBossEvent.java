package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.BossEvent;

public interface IBossEvent {
    BossEvent findBossEvent(Object entity);
    void disableBossEvent(Object entity);
}
