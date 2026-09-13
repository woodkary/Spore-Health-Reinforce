package com.Harbinger.Spore.Core.asmHooks;

import java.lang.invoke.MethodHandle;

public interface IUnsafePutHook {
    int isUnsafeRelatedMethodHandle(MethodHandle mh);

    boolean isSporeModTarget(Object target);
}
