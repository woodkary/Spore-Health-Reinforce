package com.Harbinger.Spore.Core.utils.transformation.plugins;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    static boolean isSameOrSubclass(String owner, String baseClass) {
        if (baseClass.equals(owner)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        while (owner != null && !"java/lang/Object".equals(owner) && visited.add(owner)) {
            String parent = INSTANCE.get(owner);
            if (parent == null) {
                parent = readSuperName(owner);
            }
            if (baseClass.equals(parent)) {
                return true;
            }
            owner = parent;
        }
        return false;
    }

    private static String readSuperName(String owner) {
        try (InputStream stream = openClassResource(owner + ".class")) {
            if (stream == null) {
                return null;
            }
            ClassReader reader = new ClassReader(stream);
            if (!owner.equals(reader.getClassName())) {
                return null;
            }
            String parent = reader.getSuperName();
            INSTANCE.put(owner, parent);
            return parent;
        } catch (IOException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static InputStream openClassResource(String name) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        InputStream stream = context == null ? null : context.getResourceAsStream(name);
        ClassLoader defining = SuperNameCache.class.getClassLoader();
        if (stream == null && defining != null && defining != context) {
            stream = defining.getResourceAsStream(name);
        }
        return stream != null ? stream : ClassLoader.getSystemResourceAsStream(name);
    }

    private SuperNameCache() {
    }
}
