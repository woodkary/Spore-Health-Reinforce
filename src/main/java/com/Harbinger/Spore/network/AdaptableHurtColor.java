package com.Harbinger.Spore.network;

/** The adaptation multiplier for this hit, independent of armor and actual health loss. */
public enum AdaptableHurtColor {
    RED, GREEN, PURPLE;

    public static AdaptableHurtColor fromMultiplier(float multiplier) {
        if (multiplier >= 1.0F) {
            return RED;
        }
        return multiplier > 0.0F ? GREEN : PURPLE;
    }
}
