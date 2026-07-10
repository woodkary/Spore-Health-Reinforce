package com.Harbinger.Spore.Core.agents.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class SporeFrameClassWriter extends MixinClassWriter {
    private static final String OBJECT = "java/lang/Object";
    private final ClassLoader loader;
    private final Map<String, ClassInfo> classInfoCache = new ConcurrentHashMap<>();
    private final Set<String> missingClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    SporeFrameClassWriter(ClassLoader loader, ClassNode currentClass, int flags) {
        super(flags);
        this.loader = loader;
        remember(currentClass);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (type1 == null || type2 == null || type1.equals(type2)) {
            return type1 == null ? OBJECT : type1;
        }
        if (isArray(type1) || isArray(type2)) {
            return getArrayCommonSuperClass(type1, type2);
        }
        if (isAssignableFrom(type1, type2)) {
            return type1;
        }
        if (isAssignableFrom(type2, type1)) {
            return type2;
        }
        ClassInfo info1 = resolve(type1);
        ClassInfo info2 = resolve(type2);
        if (info1 == null || info2 == null) {
            return fallbackCommonSuperClass(type1, type2);
        }
        if (info1.isInterface() || info2.isInterface()) {
            return OBJECT;
        }
        String cursor = info1.superName();
        Set<String> visited = new HashSet<>();
        while (cursor != null && visited.add(cursor)) {
            if (isAssignableFrom(cursor, type2)) {
                return cursor;
            }
            ClassInfo cursorInfo = resolve(cursor);
            cursor = cursorInfo == null ? null : cursorInfo.superName();
        }
        return OBJECT;
    }

    private String getArrayCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        } catch (Throwable ignored) {
            return OBJECT;
        }
    }

    private String fallbackCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        } catch (Throwable ignored) {
            return OBJECT;
        }
    }

    private boolean isAssignableFrom(String target, String source) {
        if (target == null || source == null) {
            return false;
        }
        if (target.equals(source) || OBJECT.equals(target)) {
            return true;
        }
        ClassInfo sourceInfo = resolve(source);
        if (sourceInfo == null) {
            return false;
        }
        if (sourceInfo.interfaces().contains(target) || implementsInterface(sourceInfo, target, new HashSet<>())) {
            return true;
        }
        String cursor = sourceInfo.superName();
        Set<String> visited = new HashSet<>();
        while (cursor != null && visited.add(cursor)) {
            if (target.equals(cursor)) {
                return true;
            }
            ClassInfo cursorInfo = resolve(cursor);
            if (cursorInfo == null) {
                return false;
            }
            if (cursorInfo.interfaces().contains(target) || implementsInterface(cursorInfo, target, new HashSet<>())) {
                return true;
            }
            cursor = cursorInfo.superName();
        }
        return false;
    }

    private boolean implementsInterface(ClassInfo sourceInfo, String target, Set<String> visited) {
        for (String interfaceName : sourceInfo.interfaces()) {
            if (!visited.add(interfaceName)) {
                continue;
            }
            if (target.equals(interfaceName)) {
                return true;
            }
            ClassInfo interfaceInfo = resolve(interfaceName);
            if (interfaceInfo != null && implementsInterface(interfaceInfo, target, visited)) {
                return true;
            }
        }
        return false;
    }

    private ClassInfo resolve(String internalName) {
        if (internalName == null || internalName.isBlank()) {
            return null;
        }
        String normalized = normalizeInternalName(internalName);
        ClassInfo cached = classInfoCache.get(normalized);
        if (cached != null) {
            return cached;
        }
        if (OBJECT.equals(normalized)) {
            ClassInfo objectInfo = new ClassInfo(null, Collections.emptyList(), false);
            classInfoCache.put(OBJECT, objectInfo);
            return objectInfo;
        }
        ClassInfo loaded = readClassInfo(normalized);
        if (loaded != null) {
            classInfoCache.put(normalized, loaded);
            return loaded;
        }
        String stable = stripHiddenSuffix(normalized);
        if (!stable.equals(normalized)) {
            loaded = readClassInfo(stable);
            if (loaded != null) {
                classInfoCache.put(normalized, loaded);
                return loaded;
            }
        }
        missingClasses.add(normalized);
        return null;
    }

    private ClassInfo readClassInfo(String internalName) {
        if (missingClasses.contains(internalName)) {
            return null;
        }
        String resource = internalName + ".class";
        InputStream input = null;
        try {
            if (loader != null) {
                input = loader.getResourceAsStream(resource);
            }
            if (input == null) {
                input = Thread.currentThread().getContextClassLoader() == null
                        ? null
                        : Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            }
            if (input == null) {
                input = SporeFrameClassWriter.class.getClassLoader().getResourceAsStream(resource);
            }
            if (input == null) {
                input = ClassLoader.getSystemResourceAsStream(resource);
            }
            if (input == null) {
                return null;
            }
            try (InputStream closeable = input) {
                ClassReader reader = new ClassReader(closeable);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                remember(node);
                return classInfoCache.get(normalizeInternalName(node.name));
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void remember(ClassNode node) {
        if (node == null || node.name == null) {
            return;
        }
        String normalizedName = normalizeInternalName(node.name);
        ClassInfo info = new ClassInfo(
                normalizeNullable(node.superName),
                normalizeInterfaces(node.interfaces),
                (node.access & Opcodes.ACC_INTERFACE) != 0
        );
        classInfoCache.put(normalizedName, info);
        String alternateHiddenName = alternateHiddenSuffix(normalizedName);
        if (!alternateHiddenName.equals(normalizedName)) {
            classInfoCache.put(alternateHiddenName, info);
        }
    }

    private List<String> normalizeInterfaces(List<String> interfaces) {
        if (interfaces == null || interfaces.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>(interfaces.size());
        for (String interfaceName : interfaces) {
            normalized.add(normalizeInternalName(interfaceName));
        }
        return normalized;
    }

    private String normalizeNullable(String name) {
        return name == null ? null : normalizeInternalName(name);
    }

    private String normalizeInternalName(String name) {
        return name == null ? null : name.replace('.', '/');
    }

    private String alternateHiddenSuffix(String name) {
        int plus = name.indexOf("+0x");
        if (plus >= 0) {
            return name.substring(0, plus) + "/0x" + name.substring(plus + 3);
        }
        int slash = name.indexOf("/0x");
        if (slash >= 0) {
            return name.substring(0, slash) + "+0x" + name.substring(slash + 3);
        }
        return name;
    }

    private String stripHiddenSuffix(String name) {
        int plus = name.indexOf("+0x");
        int slash = name.indexOf("/0x");
        int index;
        if (plus < 0) {
            index = slash;
        } else if (slash < 0) {
            index = plus;
        } else {
            index = Math.min(plus, slash);
        }
        return index < 0 ? name : name.substring(0, index);
    }

    private boolean isArray(String name) {
        return name.startsWith("[");
    }

    private record ClassInfo(String superName, List<String> interfaces, boolean isInterface) {
    }
}
