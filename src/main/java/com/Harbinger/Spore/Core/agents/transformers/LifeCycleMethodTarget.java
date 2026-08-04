package com.Harbinger.Spore.Core.agents.transformers;

import java.util.Arrays;

public record LifeCycleMethodTarget(EntitySource entitySource,
                                    int[] entityArgumentIndexes,
                                    LifeCycleMethodCategory category) {
    public LifeCycleMethodTarget {
        if (entitySource == null) {
            throw new IllegalArgumentException("entitySource must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        entityArgumentIndexes = entitySource == EntitySource.INSTANCE_THIS
                ? new int[0]
                : normalize(entityArgumentIndexes);
    }

    public static LifeCycleMethodTarget staticArguments(LifeCycleMethodCategory category,
                                                        int[] entityArgumentIndexes) {
        return new LifeCycleMethodTarget(EntitySource.STATIC_ARGUMENTS, entityArgumentIndexes, category);
    }

    public static LifeCycleMethodTarget instanceThis(LifeCycleMethodCategory category) {
        return new LifeCycleMethodTarget(EntitySource.INSTANCE_THIS, new int[0], category);
    }

    @Override
    public int[] entityArgumentIndexes() {
        return entityArgumentIndexes.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LifeCycleMethodTarget target
                && entitySource == target.entitySource
                && category == target.category
                && Arrays.equals(entityArgumentIndexes, target.entityArgumentIndexes);
    }

    @Override
    public int hashCode() {
        int result = entitySource.hashCode();
        result = 31 * result + Arrays.hashCode(entityArgumentIndexes);
        return 31 * result + category.hashCode();
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
