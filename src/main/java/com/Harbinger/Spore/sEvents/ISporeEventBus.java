package com.Harbinger.Spore.sEvents;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

public interface ISporeEventBus extends IEventBus, Consumer<TickEvent> {
    void tickMinecraftForgeEventBus();
    void addSelfListener();
}
