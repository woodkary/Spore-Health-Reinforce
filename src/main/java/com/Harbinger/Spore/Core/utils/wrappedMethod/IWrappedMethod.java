package com.Harbinger.Spore.Core.utils.wrappedMethod;

import java.lang.invoke.MethodHandle;

public interface IWrappedMethod {
    MethodHandle getMethod();

    String getName();

    Class<?> getReturnType();
    Class<?>[] getParameterTypes();

    Object invoke(Object... args) throws Throwable;
}
