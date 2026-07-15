package com.Harbinger.Spore.Sitems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public final class EquipmentSlotBreakConsumer implements Consumer<LivingEntity> {
    private final EquipmentSlot slot;

    public EquipmentSlotBreakConsumer(EquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public void accept(LivingEntity entity) {
        entity.broadcastBreakEvent(slot);
    }
}
