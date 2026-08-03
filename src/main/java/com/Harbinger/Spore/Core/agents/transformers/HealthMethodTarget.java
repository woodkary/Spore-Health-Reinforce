package com.Harbinger.Spore.Core.agents.transformers;

import java.util.Arrays;

public record HealthMethodTarget(EntitySource entitySource, int[] entityArgumentIndexes) {
    public HealthMethodTarget {
        if (entitySource == null) {
            throw new IllegalArgumentException("entitySource must not be null");
        }
        entityArgumentIndexes = entitySource == EntitySource.INSTANCE_THIS
                ? new int[0]
                : normalize(entityArgumentIndexes);
    }

    public static HealthMethodTarget staticArguments(int[] entityArgumentIndexes) {
        return new HealthMethodTarget(EntitySource.STATIC_ARGUMENTS, entityArgumentIndexes);
    }

    public static HealthMethodTarget instanceThis() {
        return new HealthMethodTarget(EntitySource.INSTANCE_THIS, new int[0]);
    }

    @Override
    public int[] entityArgumentIndexes() {
        return entityArgumentIndexes.clone();
    }

    private static int[] normalize(int[] indexes) {
        if (indexes == null || indexes.length == 0) {
            return new int[0];
        }
        return Arrays.stream(indexes)
                .filter(index -> index >= 0)
                .distinct()
                .sorted()
                .toArray();
    }
}
