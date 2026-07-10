package com.Harbinger.Spore.Core.agents.transformers;

import com.Harbinger.Spore.Core.utils.LogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SporeTransformerDebugDump {
    private static final int MAX_CACHE_ENTRIES = Integer.getInteger("spore.transformer.debugDump.cacheLimit", 256);
    private static final String DUMP_DIR_PROPERTY = "spore.transformer.debugDumpDir";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final ConcurrentMap<String, DumpEntry> LAST_TRANSFORMED = new ConcurrentHashMap<>();

    private SporeTransformerDebugDump() {
    }

    public static void rememberTransformed(String transformerName,
                                           String requestedClassName,
                                           String internalClassName,
                                           byte[] inputBytes,
                                           byte[] transformedBytes) {
        if (transformedBytes == null || transformedBytes.length == 0) {
            return;
        }
        if (LAST_TRANSFORMED.size() > MAX_CACHE_ENTRIES) {
            LAST_TRANSFORMED.clear();
        }
        DumpEntry entry = new DumpEntry(
                transformerName,
                requestedClassName,
                internalClassName,
                copy(inputBytes),
                copy(transformedBytes),
                System.currentTimeMillis()
        );
        putEntry(requestedClassName, entry);
        putEntry(internalClassName, entry);
        putEntry(normalizeName(requestedClassName), entry);
        putEntry(normalizeName(internalClassName), entry);
    }

    public static void dumpFailedTransform(String backend, Class<?> target, Throwable failure) {
        String className = target == null ? null : target.getName();
        DumpEntry entry = findEntry(className);
        if (entry == null) {
            LogUtil.errorf("No cached transformed bytes found for failed %s retransform target %s.",
                    backend,
                    className);
            return;
        }
        try {
            Path dumpDir = Paths.get(System.getProperty(DUMP_DIR_PROPERTY, "spore-transformer-dumps"));
            Files.createDirectories(dumpDir);
            String prefix = FILE_TIME.format(LocalDateTime.now())
                    + "-"
                    + sanitize(backend)
                    + "-"
                    + sanitize(normalizeName(className))
                    + "-"
                    + sanitize(entry.transformerName());
            Path transformedPath = dumpDir.resolve(prefix + "-transformed.class");
            Files.write(transformedPath, entry.transformedBytes());
            Path inputPath = null;
            if (entry.inputBytes() != null && entry.inputBytes().length > 0) {
                inputPath = dumpDir.resolve(prefix + "-input.class");
                Files.write(inputPath, entry.inputBytes());
            }
            Path metaPath = dumpDir.resolve(prefix + ".txt");
            Files.writeString(metaPath, buildMeta(backend, target, failure, entry, transformedPath, inputPath));
            LogUtil.errorf("Dumped failed %s transformed bytes for %s to %s",
                    backend,
                    className,
                    transformedPath.toAbsolutePath());
        } catch (Throwable dumpFailure) {
            LogUtil.errorf("Failed to dump transformed bytes for %s: %s",
                    className,
                    dumpFailure.getMessage());
            LogUtil.printStackTrace(dumpFailure);
        }
    }

    private static DumpEntry findEntry(String className) {
        DumpEntry entry = LAST_TRANSFORMED.get(className);
        if (entry != null) {
            return entry;
        }
        return LAST_TRANSFORMED.get(normalizeName(className));
    }

    private static void putEntry(String key, DumpEntry entry) {
        if (key != null && !key.isBlank()) {
            LAST_TRANSFORMED.put(key, entry);
        }
    }

    private static byte[] copy(byte[] bytes) {
        return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        String normalized = name.replace('.', '/');
        int hiddenSlash = normalized.indexOf("/0x");
        if (hiddenSlash >= 0) {
            normalized = normalized.substring(0, hiddenSlash);
        }
        int hiddenPlus = normalized.indexOf("+0x");
        if (hiddenPlus >= 0) {
            normalized = normalized.substring(0, hiddenPlus);
        }
        return normalized;
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (sanitized.length() > 160) {
            return sanitized.substring(sanitized.length() - 160);
        }
        return sanitized;
    }

    private static String buildMeta(String backend,
                                    Class<?> target,
                                    Throwable failure,
                                    DumpEntry entry,
                                    Path transformedPath,
                                    Path inputPath) {
        StringBuilder builder = new StringBuilder();
        builder.append("backend=").append(backend).append('\n');
        builder.append("target=").append(target == null ? "null" : target.getName()).append('\n');
        builder.append("targetHidden=").append(target != null && target.isHidden()).append('\n');
        builder.append("targetLoader=").append(target == null ? "null" : target.getClassLoader()).append('\n');
        builder.append("transformer=").append(entry.transformerName()).append('\n');
        builder.append("requestedClassName=").append(entry.requestedClassName()).append('\n');
        builder.append("internalClassName=").append(entry.internalClassName()).append('\n');
        builder.append("cachedAtMillis=").append(entry.cachedAtMillis()).append('\n');
        builder.append("inputBytes=").append(entry.inputBytes() == null ? 0 : entry.inputBytes().length).append('\n');
        builder.append("transformedBytes=").append(entry.transformedBytes().length).append('\n');
        builder.append("inputPath=").append(inputPath == null ? "" : inputPath.toAbsolutePath()).append('\n');
        builder.append("transformedPath=").append(transformedPath.toAbsolutePath()).append('\n');
        if (failure != null) {
            builder.append("failure=").append(failure).append('\n');
            StringWriter stack = new StringWriter();
            failure.printStackTrace(new PrintWriter(stack));
            builder.append(stack);
        }
        return builder.toString();
    }

    private record DumpEntry(String transformerName,
                             String requestedClassName,
                             String internalClassName,
                             byte[] inputBytes,
                             byte[] transformedBytes,
                             long cachedAtMillis) {
    }
}
