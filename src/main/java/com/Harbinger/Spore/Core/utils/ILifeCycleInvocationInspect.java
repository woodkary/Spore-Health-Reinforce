package com.Harbinger.Spore.Core.utils;

public interface ILifeCycleInvocationInspect {
    void inspectAndCacheLifeCycleInvocations(Class<?> livingEntityClass);

    void inspectAndRetransformInvocations();
}
