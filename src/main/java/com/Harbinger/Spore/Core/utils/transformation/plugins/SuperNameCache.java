package com.Harbinger.Spore.Core.utils.transformation.plugins;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SuperNameCache implements ISuperName {
    public static final ISuperName INSTANCE = new SuperNameCache();
    private final Map<String, String> superNameCache = new ConcurrentHashMap<>();
    @Override
    public String get(String current) {
        return superNameCache.get(current);
    }
    @Override
    public void put(String className, String superName) {
        if (className != null && superName != null) {
            superNameCache.put(className, superName);
        }
    }
    private SuperNameCache() {
    }
}
