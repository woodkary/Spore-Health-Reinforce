package com.Harbinger.Spore.Core.agents.transformers;

public interface ICommonBootStrap {
    void retransformMaybeHiddenClasses(Class<?>... classes);

    void retransformMaybeHiddenClassesInstOnly(Class<?>... classes);

    void retransformMaybeHiddenClassesJVMTIOnly(Class<?>... classes);

    void installAndRetransform();
}
