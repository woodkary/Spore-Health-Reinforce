package com.Harbinger.Spore.Sentities.AI;

/** Bounded, allocation-free observation of one task's local movement. */
final class LocalNavigationProgress {
    private static final int WINDOW = 40;
    private static final double SPAN = 1.0D;
    private static final double PROGRESS = 0.25D;
    private final double[] xs = new double[WINDOW];
    private final double[] zs = new double[WINDOW];
    private int samples;
    private int cursor;
    private boolean initialized;
    private double originX, originZ;
    private double targetX, targetY, targetZ;
    private double bestDistance, creditedDistance;
    private double minY, maxY, creditedMinY, creditedMaxY;

    void reset() {
        initialized = false;
        suspend();
    }

    // A pause discards elapsed observation ticks, but not historical achievements.
    void suspend() {
        samples = 0;
        cursor = 0;
    }

    boolean sample(double x, double y, double z, double wantedX, double wantedY, double wantedZ) {
        if (!initialized || Math.abs(x - originX) > SPAN || Math.abs(z - originZ) > SPAN) {
            initialized = true;
            originX = x;
            originZ = z;
            // Freeze the local reference across path replacements and node oscillations.
            // Only a new spatial episode may select another reference point.
            targetX = wantedX;
            targetY = wantedY;
            targetZ = wantedZ;
            bestDistance = creditedDistance = distance(x, y, z);
            minY = maxY = creditedMinY = creditedMaxY = y;
            suspend();
            return false;
        }

        bestDistance = Math.min(bestDistance, distance(x, y, z));
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
        // Height frontiers allow stairs and sustained swimming/flying even after
        // passing the frozen node. Revisiting a jump apex earns no new progress.
        if (creditedDistance - bestDistance >= PROGRESS
                || creditedMinY - minY >= PROGRESS || maxY - creditedMaxY >= PROGRESS) {
            creditedDistance = bestDistance;
            creditedMinY = minY;
            creditedMaxY = maxY;
            suspend();
            return false;
        }

        xs[cursor] = x;
        zs[cursor] = z;
        cursor = (cursor + 1) % WINDOW;
        samples = Math.min(samples + 1, WINDOW);
        if (samples < WINDOW) {
            return false;
        }
        double lowX = x, highX = x, lowZ = z, highZ = z;
        for (int i = 0; i < WINDOW; i++) {
            lowX = Math.min(lowX, xs[i]);
            highX = Math.max(highX, xs[i]);
            lowZ = Math.min(lowZ, zs[i]);
            highZ = Math.max(highZ, zs[i]);
        }
        return highX - lowX <= SPAN && highZ - lowZ <= SPAN;
    }

    private double distance(double x, double y, double z) {
        double dx = x - targetX, dy = y - targetY, dz = z - targetZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
