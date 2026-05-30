package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;

public interface IMethodHandle {
    MethodHandle ensureConstructor(MethodHandle constructor,
                                   Class<?> hiddenClass,
                                   Class<?> originalClass,
                                   Class<?>... ctorTypes);
}
