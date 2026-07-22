package com.Harbinger.Spore.Core.utils.transformation.plugins;

import org.objectweb.asm.Type;

import java.util.Locale;

public final class LifeCycleCallSiteHookResolver {
    private static final String LIVING_ENTITY_DESC = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final LifeCycleCallSiteHookSpec HEALTH_FLOAT =
            new LifeCycleCallSiteHookSpec("getHeealth", "(" + LIVING_ENTITY_DESC + "F)F");
    private static final LifeCycleCallSiteHookSpec HEALTH_DOUBLE =
            new LifeCycleCallSiteHookSpec("getHeealth", "(" + LIVING_ENTITY_DESC + "D)D");
    private static final LifeCycleCallSiteHookSpec MAX_HEALTH_FLOAT =
            new LifeCycleCallSiteHookSpec("getMaaxxHeaaltsh", "(" + LIVING_ENTITY_DESC + "F)F");
    private static final LifeCycleCallSiteHookSpec MAX_HEALTH_DOUBLE =
            new LifeCycleCallSiteHookSpec("getMaaxxHeaaltsh", "(" + LIVING_ENTITY_DESC + "D)D");
    private static final LifeCycleCallSiteHookSpec DEAD_OR_DYING =
            new LifeCycleCallSiteHookSpec("isDeeadfOrDyaging", "(" + LIVING_ENTITY_DESC + "Z)Z");
    private static final LifeCycleCallSiteHookSpec ALIVE =
            new LifeCycleCallSiteHookSpec("isAlliive", "(" + LIVING_ENTITY_DESC + "Z)Z");

    private LifeCycleCallSiteHookResolver() {
    }

    static LifeCycleCallSiteHookSpec resolve(String methodName, String methodDescriptor) {
        if (methodName == null || methodDescriptor == null) {
            return null;
        }

        Type returnType;
        try {
            returnType = Type.getReturnType(methodDescriptor);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        if (returnType.getSort() == Type.BOOLEAN) {
            if (nameLooksLikeIsDeadOrDying(methodName)) {
                return DEAD_OR_DYING;
            }
            if (nameLooksLikeIsAlive(methodName)) {
                return ALIVE;
            }
            return null;
        }

        if (returnType.getSort() != Type.FLOAT && returnType.getSort() != Type.DOUBLE) {
            return null;
        }
        if (nameLooksLikeMaxHealth(methodName)) {
            return returnType.getSort() == Type.FLOAT ? MAX_HEALTH_FLOAT : MAX_HEALTH_DOUBLE;
        }
        if (nameLooksLikeHealth(methodName)) {
            return returnType.getSort() == Type.FLOAT ? HEALTH_FLOAT : HEALTH_DOUBLE;
        }
        return null;
    }

    private static boolean nameLooksLikeHealth(String name) {
        if ("haveDiexv".equals(name)) {
            return true;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return (normalized.contains("heal") && !normalized.contains("max"))
                || "m_21223_".equals(name);
    }

    private static boolean nameLooksLikeMaxHealth(String name) {
        if ("haveBigDiexv".equals(name)) {
            return true;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return (normalized.contains("max") && normalized.contains("heal"))
                || "m_21233_".equals(name);
    }

    private static boolean nameLooksLikeIsDeadOrDying(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("dead")
                || normalized.contains("die")
                || normalized.contains("death")
                || normalized.contains("away")
                || normalized.contains("died")
                || (normalized.contains("kill") && !normalized.contains("skill"))
                || normalized.contains("weak")
                || (normalized.contains("end")
                && !normalized.contains("render")
                && !normalized.contains("legend"))
                || "m_21224_".equals(name);
    }

    private static boolean nameLooksLikeIsAlive(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("alive")
                || normalized.contains("living")
                || "m_6084_".equals(name);
    }
}
