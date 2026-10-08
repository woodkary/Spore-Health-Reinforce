package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.network.AdaptableHurtColor;

/** Per-entity, transient feedback. Time comes from the owning client's world, without a tick hook. */
public final class AdaptableHurtFeedbackState {
    public static final int MIN_DURATION_TICKS = 1;
    public static final int MAX_DURATION_TICKS = 40;

    private Snapshot feedback = Snapshot.NONE;
    private long endsAt = Long.MIN_VALUE;

    public void apply(long gameTime, AdaptableHurtColor color, int durationTicks) {
        feedback = new Snapshot(true, color);
        endsAt = gameTime + clampDuration(durationTicks);
    }

    public Snapshot snapshot(long gameTime) {
        return gameTime < endsAt ? feedback : Snapshot.NONE;
    }

    public static int clampDuration(int durationTicks) {
        return Math.max(MIN_DURATION_TICKS, Math.min(MAX_DURATION_TICKS, durationTicks));
    }

    /** Immutable capture for one render; deferred vertices must not read the live state. */
    public record Snapshot(boolean active, AdaptableHurtColor color) {
        public static final Snapshot NONE = new Snapshot(false, AdaptableHurtColor.RED);
    }
}
