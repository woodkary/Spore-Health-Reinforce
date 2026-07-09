package com.Harbinger.Spore.Core.agents.transformers;

public interface SelfTransformer {
    byte[] transformClassByte(ClassLoader loader, String className, byte[] classfileBuffer);
}
