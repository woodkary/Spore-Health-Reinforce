package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.sEvents.SporeEventBus;

public interface IEventTickable {
    default void tickEventBus() {
        SporeEventBus.tick();
    }
}
