package com.Harbinger.Spore.Core.agents.transformers;

import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SporeClassFileTransformer0 implements ISporeClassFileTransformer {
    private static final int MAX_SUPER_NAME_CACHE_SIZE = Math.max(
            1024,
            Integer.getInteger("spore.transformer.superNameCacheLimit", 8192)
    );
    protected static final ConcurrentMap<String, String> superNameCache = new ConcurrentHashMap<>();

    protected static void cacheSuperName(String className, String superName) {
        if (className == null || superName == null) {
            return;
        }
        if (!superNameCache.containsKey(className)
                && superNameCache.size() >= MAX_SUPER_NAME_CACHE_SIZE) {
            synchronized (superNameCache) {
                if (superNameCache.size() >= MAX_SUPER_NAME_CACHE_SIZE) {
                    int removeCount = Math.max(1, MAX_SUPER_NAME_CACHE_SIZE / 4);
                    var iterator = superNameCache.keySet().iterator();
                    while (iterator.hasNext() && removeCount-- > 0) {
                        superNameCache.remove(iterator.next());
                    }
                }
            }
        }
        superNameCache.putIfAbsent(className, superName);
    }

    static {
        SporeNativeBridge.INSTANCE.load();
    }

    @Override
    public native byte[] transform(Module module,
                                   ClassLoader loader,
                                   String className,
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain,
                                   byte[] classfileBuffer);

    @Override
    public native byte[] transform(ClassLoader loader,
                                   String className,
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain,
                                   byte[] classfileBuffer);

    protected abstract byte[] transformInternal(ClassLoader loader, String className, byte[] classfileBuffer);
}
