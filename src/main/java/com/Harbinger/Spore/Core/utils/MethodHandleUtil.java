package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class MethodHandleUtil implements IMethodHandle {
    public static IMethodHandle INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IMethodHandle.class,
            MethodHandleUtil.class
    );
    private MethodHandle initConstructor(Class<?> hiddenClass, Class<?> origianlClass, Class<?>... ctorTypes) {
        MethodType ctorType = MethodType.methodType(void.class, ctorTypes);
        try {
            return ClassUtil.getLookup().findConstructor(hiddenClass, ctorType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LogUtil.errorf("can't find %s constructor, fallback to %s", hiddenClass, origianlClass);
            try {
                return ClassUtil.getLookup().findConstructor(origianlClass, ctorType);
            } catch (NoSuchMethodException | IllegalAccessException e2) {
                LogUtil.errorf("can't find %s constructor", origianlClass);
            }
        }
        return null;
    }
    public MethodHandle ensureConstructor(MethodHandle constructor,
                                                 Class<?> hiddenClass,
                                                 Class<?> originalClass,
                                                 Class<?>... ctorTypes) {
        if (constructor != null) {
            return constructor;
        }
        try {
            Class<?> targetHiddenClass = hiddenClass;
            if (targetHiddenClass == null && originalClass != null) {
                try {
                    targetHiddenClass = BytecodeUtil.resolveHiddenClassOrSelf(originalClass);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to resolve hidden class for %s: %s",
                            originalClass.getName(),
                            t.getMessage());
                }
            }
            if (targetHiddenClass == null) {
                targetHiddenClass = originalClass;
            }
            return initConstructor(targetHiddenClass, originalClass, ctorTypes);
        } catch (Throwable t) {
            LogUtil.errorf("failed to ensure constructor for %s: %s",
                    originalClass != null ? originalClass.getName() : "unknown",
                    t.getMessage());
            return null;
        }
    }
}
