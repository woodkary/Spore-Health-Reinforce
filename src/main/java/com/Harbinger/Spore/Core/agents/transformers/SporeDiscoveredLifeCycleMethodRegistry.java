package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.LogUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Shared discovered targets for normal, JVMTI, and hidden-definition transformer instances. */
public final class SporeDiscoveredLifeCycleMethodRegistry {
    private static final ConcurrentMap<String, ConcurrentMap<String, LifeCycleMethodTarget>> TARGETS =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<String>> INVALID_TARGETS = new ConcurrentHashMap<>();

    private SporeDiscoveredLifeCycleMethodRegistry() {
    }

    public static boolean registerStatic(String owner,
                                         String name,
                                         String descriptor,
                                         LifeCycleMethodCategory category,
                                         Collection<Integer> entityArgumentIndexes) {
        if (entityArgumentIndexes == null) {
            return false;
        }
        int[] indexes = entityArgumentIndexes.stream()
                .filter(index -> index != null && index >= 0)
                .mapToInt(Integer::intValue)
                .toArray();
        return register(
                owner,
                name,
                descriptor,
                LifeCycleMethodTarget.staticArguments(category, indexes)
        );
    }

    public static boolean register(String owner,
                                   String name,
                                   String descriptor,
                                   Collection<Integer> entityArgumentIndexes) {
        return registerStatic(
                owner,
                name,
                descriptor,
                LifeCycleMethodCategory.HEALTH,
                entityArgumentIndexes
        );
    }

    public static boolean registerInstance(String owner,
                                           String name,
                                           String descriptor,
                                           LifeCycleMethodCategory category) {
        return register(owner, name, descriptor, LifeCycleMethodTarget.instanceThis(category));
    }

    public static boolean registerInstance(String owner, String name, String descriptor) {
        return registerInstance(owner, name, descriptor, LifeCycleMethodCategory.HEALTH);
    }

    public static boolean register(String owner,
                                   String name,
                                   String descriptor,
                                   LifeCycleMethodTarget requested) {
        if (owner == null || name == null || descriptor == null || requested == null
                || (requested.entitySource() == EntitySource.STATIC_ARGUMENTS
                && requested.entityArgumentIndexes().length == 0)) {
            return false;
        }

        String normalizedOwner = normalizeOwner(owner);
        String methodKey = methodKey(name, descriptor);
        Set<String> invalidMethods = INVALID_TARGETS.computeIfAbsent(
                normalizedOwner,
                ignored -> ConcurrentHashMap.newKeySet()
        );
        if (invalidMethods.contains(methodKey)) {
            LogUtil.errorf("Rejected invalid discovered lifecycle target %s.%s", normalizedOwner, methodKey);
            return false;
        }

        ConcurrentMap<String, LifeCycleMethodTarget> ownerTargets =
                TARGETS.computeIfAbsent(normalizedOwner, ignored -> new ConcurrentHashMap<>());
        boolean[] changed = new boolean[1];
        ownerTargets.compute(methodKey, (ignored, existing) -> {
            if (invalidMethods.contains(methodKey)) {
                changed[0] = existing != null;
                return null;
            }
            if (existing == null) {
                changed[0] = true;
                return requested;
            }
            if (existing.entitySource() != requested.entitySource()
                    || existing.category() != requested.category()) {
                invalidMethods.add(methodKey);
                changed[0] = true;
                LogUtil.errorf(
                        "Invalidated conflicting discovered lifecycle target %s.%s: %s/%s vs %s/%s",
                        normalizedOwner,
                        methodKey,
                        existing.entitySource(),
                        existing.category(),
                        requested.entitySource(),
                        requested.category()
                );
                return null;
            }
            if (existing.entitySource() == EntitySource.INSTANCE_THIS) {
                return existing;
            }
            int[] existingIndexes = existing.entityArgumentIndexes();
            int[] merged = merge(existingIndexes, requested.entityArgumentIndexes());
            changed[0] = !Arrays.equals(existingIndexes, merged);
            return changed[0]
                    ? LifeCycleMethodTarget.staticArguments(existing.category(), merged)
                    : existing;
        });
        return changed[0];
    }

    public static Map<String, LifeCycleMethodTarget> targetsForOwner(String owner) {
        Map<String, LifeCycleMethodTarget> targets = TARGETS.get(normalizeOwner(owner));
        return targets == null ? Collections.emptyMap() : targets;
    }

    public static boolean isInvalid(String owner, String name, String descriptor) {
        Set<String> invalidMethods = INVALID_TARGETS.get(normalizeOwner(owner));
        return invalidMethods != null && invalidMethods.contains(methodKey(name, descriptor));
    }

    public static boolean invalidate(String owner,
                                     String name,
                                     String descriptor,
                                     String reason) {
        String normalizedOwner = normalizeOwner(owner);
        String methodKey = methodKey(name, descriptor);
        Set<String> invalidMethods = INVALID_TARGETS.computeIfAbsent(
                normalizedOwner,
                ignored -> ConcurrentHashMap.newKeySet()
        );
        boolean newlyInvalid = invalidMethods.add(methodKey);
        ConcurrentMap<String, LifeCycleMethodTarget> ownerTargets = TARGETS.computeIfAbsent(
                normalizedOwner,
                ignored -> new ConcurrentHashMap<>()
        );
        LifeCycleMethodTarget removed = ownerTargets.remove(methodKey);
        if (newlyInvalid || removed != null) {
            LogUtil.errorf("Invalidated discovered lifecycle target %s.%s: %s",
                    normalizedOwner,
                    methodKey,
                    reason == null ? "conflicting evidence" : reason);
        }
        return newlyInvalid || removed != null;
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
