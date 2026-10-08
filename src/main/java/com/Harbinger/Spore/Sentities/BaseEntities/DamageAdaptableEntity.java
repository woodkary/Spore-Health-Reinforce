package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.network.AdaptableHurtColor;
import com.Harbinger.Spore.network.AdaptableHurtFeedbackPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

/**
 * Shared hit feedback; each implementation owns its adaptation algorithm, memory and feedback state.
 * UtilityEntity's actualHurt already calls adaptDamage. Other damage pipelines must call it once and
 * use its return value. Implementations must also wire their own adaptation NBT callbacks.
 */
public interface DamageAdaptableEntity {
    LivingEntity entity();

    /** Return the same, per-instance state on every call; do not persist it or sync it as entity data. */
    AdaptableHurtFeedbackState getHurtFeedbackState();

    float adaptDamage(DamageSource source, float damage);

    default UUID getAdaptableUUID() {
        return entity().getUUID();
    }

    /** Call once after this hit's adaptation result is known, before subsequent zero-damage returns. */
    default void sendHurtFeedback(AdaptableHurtColor color) {
        sendHurtFeedback(color, AdaptableHurtFeedbackPacket.DEFAULT_DURATION_TICKS);
    }

    default void sendHurtFeedback(AdaptableHurtColor color, int durationTicks) {
        LivingEntity living = entity();
        if (!living.level().isClientSide) {
            SporePacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> living),
                    new AdaptableHurtFeedbackPacket(living.getId(), living.getUUID(), color,
                            AdaptableHurtFeedbackState.clampDuration(durationTicks)));
        }
    }

    default void applyClientHurtFeedback(AdaptableHurtColor color, int durationTicks) {
        LivingEntity living = entity();
        if (living.level().isClientSide) {
            getHurtFeedbackState().apply(living.level().getGameTime(), color, durationTicks);
        }
    }

    default AdaptableHurtFeedbackState.Snapshot getClientHurtFeedbackSnapshot() {
        LivingEntity living = entity();
        return living.level().isClientSide
                ? getHurtFeedbackState().snapshot(living.level().getGameTime())
                : AdaptableHurtFeedbackState.Snapshot.NONE;
    }

    default AdaptableHurtColor getClientHurtColor() {
        return getClientHurtFeedbackSnapshot().color();
    }

    default boolean hasClientHurtFeedback() {
        return getClientHurtFeedbackSnapshot().active();
    }

    void readAdaptData(CompoundTag tag);
    void addAdaptData(CompoundTag tag);
}
