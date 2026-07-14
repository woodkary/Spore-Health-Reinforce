package com.Harbinger.Spore.Core.agents.transformers;

import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SporeClassFileTransformer0 implements ISporeClassFileTransformer {
    protected final ConcurrentMap<String, String> superNameCache = new ConcurrentHashMap<>();

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
