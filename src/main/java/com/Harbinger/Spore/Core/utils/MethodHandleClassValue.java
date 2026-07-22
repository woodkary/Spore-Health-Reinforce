package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Optional;

final class MethodHandleClassValue extends ClassValue<Optional<MethodHandle>> {
    private final String methodName;
    private final MethodType methodType;

    MethodHandleClassValue(String methodName, MethodType methodType) {
        this.methodName = methodName;
        this.methodType = methodType;
    }

    @Override
    protected Optional<MethodHandle> computeValue(Class<?> type) {
        try {
            return Optional.of(MethodHandleUtil.INSTANCE.getLookup().findVirtual(type, methodName, methodType));
        } catch (Throwable throwable) {
            LogUtil.errorf("failed to find method %s of %s. %s",
                    methodName, type.getName(), throwable.getMessage());
            return Optional.empty();
        }
    }
}
