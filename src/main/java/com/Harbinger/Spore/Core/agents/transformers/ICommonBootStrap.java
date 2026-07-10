package com.Harbinger.Spore.Core.agents.transformers;

public interface ICommonBootStrap {
    void retransformMaybeHiddenClasses(Class<?>... classes);

    void installAndRetransform();
}
