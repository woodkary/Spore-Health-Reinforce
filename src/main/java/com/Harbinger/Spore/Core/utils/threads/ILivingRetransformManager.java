package com.Harbinger.Spore.Core.utils.threads;

import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;

public interface ILivingRetransformManager extends Consumer<TickEvent> {
    void add(LivingEntityRetransformationTask.Strategy strategy, Class<?>... livingClasses);
}
