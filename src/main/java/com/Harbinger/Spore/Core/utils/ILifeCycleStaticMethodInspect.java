package com.Harbinger.Spore.Core.utils;

public interface ILifeCycleStaticMethodInspect {
    void inspectAndCacheLifeCycleStaticMethods(Class<?> livingEntityClass);

    void inspectAndRetransformStatic();
}
