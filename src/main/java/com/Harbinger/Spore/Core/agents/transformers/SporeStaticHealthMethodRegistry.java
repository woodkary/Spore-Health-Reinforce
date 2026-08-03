package com.Harbinger.Spore.Core.agents.transformers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Shared targets for normal, JVMTI, and hidden-definition transformer instances. */
public final class SporeStaticHealthMethodRegistry {
    private static final ConcurrentMap<String, ConcurrentMap<String, int[]>> TARGETS =
            new ConcurrentHashMap<>();

    private SporeStaticHealthMethodRegistry() {
    }

    public static boolean register(String owner,
                                   String name,
                                   String descriptor,
                                   Collection<Integer> entityArgumentIndexes) {
        if (owner == null || name == null || descriptor == null
                || entityArgumentIndexes == null || entityArgumentIndexes.isEmpty()) {
            return false;
        }
        int[] requested = entityArgumentIndexes.stream()
                .filter(index -> index != null && index >= 0)
                .mapToInt(Integer::intValue)
                .distinct()
                .sorted()
                .toArray();
        if (requested.length == 0) {
            return false;
        }

        String normalizedOwner = normalizeOwner(owner);
        ConcurrentMap<String, int[]> ownerTargets =
                TARGETS.computeIfAbsent(normalizedOwner, ignored -> new ConcurrentHashMap<>());
        boolean[] changed = new boolean[1];
        ownerTargets.compute(methodKey(name, descriptor), (ignored, existing) -> {
            int[] merged = merge(existing, requested);
            changed[0] = !Arrays.equals(existing, merged);
            return merged;
        });
        return changed[0];
    }

    static Map<String, int[]> targetsForOwner(String owner) {
        Map<String, int[]> targets = TARGETS.get(normalizeOwner(owner));
        return targets == null ? Collections.emptyMap() : targets;
    }

    public static Collection<String> owners() {
        return Collections.unmodifiableSet(TARGETS.keySet());
    }

    static String methodKey(String name, String descriptor) {
        return name + descriptor;
    }

    public static String normalizeOwner(String owner) {
        if (owner == null) {
            return "";
        }
        String normalized = owner.replace('.', '/');
        int slashHidden = normalized.indexOf("/0x");
        int plusHidden = normalized.indexOf("+0x");
        int hiddenIndex;
        if (slashHidden < 0) {
            hiddenIndex = plusHidden;
        } else if (plusHidden < 0) {
            hiddenIndex = slashHidden;
        } else {
            hiddenIndex = Math.min(slashHidden, plusHidden);
        }
        return hiddenIndex < 0 ? normalized : normalized.substring(0, hiddenIndex);
    }

    private static int[] merge(int[] existing, int[] requested) {
        if (existing == null || existing.length == 0) {
            return requested.clone();
        }
        return java.util.stream.IntStream.concat(Arrays.stream(existing), Arrays.stream(requested))
                .distinct()
                .sorted()
                .toArray();
    }
}
