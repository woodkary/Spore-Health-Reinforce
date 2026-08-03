package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.LogUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Shared discovered targets for normal, JVMTI, and hidden-definition transformer instances. */
public final class SporeDiscoveredHealthMethodRegistry {
    private static final ConcurrentMap<String, ConcurrentMap<String, HealthMethodTarget>> TARGETS =
            new ConcurrentHashMap<>();

    private SporeDiscoveredHealthMethodRegistry() {
    }

    public static boolean registerStatic(String owner,
                                         String name,
                                         String descriptor,
                                         Collection<Integer> entityArgumentIndexes) {
        if (entityArgumentIndexes == null) {
            return false;
        }
        int[] indexes = entityArgumentIndexes.stream()
                .filter(index -> index != null && index >= 0)
                .mapToInt(Integer::intValue)
                .toArray();
        return register(owner, name, descriptor, HealthMethodTarget.staticArguments(indexes));
    }

    public static boolean register(String owner,
                                   String name,
                                   String descriptor,
                                   Collection<Integer> entityArgumentIndexes) {
        return registerStatic(owner, name, descriptor, entityArgumentIndexes);
    }

    public static boolean registerInstance(String owner, String name, String descriptor) {
        return register(owner, name, descriptor, HealthMethodTarget.instanceThis());
    }

    public static boolean register(String owner,
                                   String name,
                                   String descriptor,
                                   HealthMethodTarget requested) {
        if (owner == null || name == null || descriptor == null || requested == null
                || (requested.entitySource() == EntitySource.STATIC_ARGUMENTS
                && requested.entityArgumentIndexes().length == 0)) {
            return false;
        }

        String normalizedOwner = normalizeOwner(owner);
        ConcurrentMap<String, HealthMethodTarget> ownerTargets =
                TARGETS.computeIfAbsent(normalizedOwner, ignored -> new ConcurrentHashMap<>());
        boolean[] changed = new boolean[1];
        String methodKey = methodKey(name, descriptor);
        ownerTargets.compute(methodKey, (ignored, existing) -> {
            if (existing == null) {
                changed[0] = true;
                return requested;
            }
            if (existing.entitySource() != requested.entitySource()) {
                HealthMethodTarget selected = existing.entitySource().ordinal() < requested.entitySource().ordinal()
                        ? existing
                        : requested;
                changed[0] = selected != existing;
                LogUtil.errorf(
                        "Conflicting discovered health target modes for %s.%s: %s vs %s; selected %s",
                        normalizedOwner,
                        methodKey,
                        existing.entitySource(),
                        requested.entitySource(),
                        selected.entitySource()
                );
                return selected;
            }
            if (existing.entitySource() == EntitySource.INSTANCE_THIS) {
                return existing;
            }
            int[] merged = merge(existing.entityArgumentIndexes(), requested.entityArgumentIndexes());
            changed[0] = !Arrays.equals(existing.entityArgumentIndexes(), merged);
            return changed[0] ? HealthMethodTarget.staticArguments(merged) : existing;
        });
        return changed[0];
    }

    static Map<String, HealthMethodTarget> targetsForOwner(String owner) {
        Map<String, HealthMethodTarget> targets = TARGETS.get(normalizeOwner(owner));
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
        return java.util.stream.IntStream.concat(Arrays.stream(existing), Arrays.stream(requested))
                .distinct()
                .sorted()
                .toArray();
    }
}
