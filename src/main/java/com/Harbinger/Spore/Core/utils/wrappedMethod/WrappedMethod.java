package com.Harbinger.Spore.Core.utils.wrappedMethod;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class WrappedMethod implements IWrappedMethod{
    private static final Class<? extends IWrappedMethod> clazz= (Class<? extends IWrappedMethod>) BytecodeUtil.resolveHiddenClassOrSelf(
            WrappedMethod.class,
            MethodHandle.class,
            String.class,
            Class.class,
            Class[].class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            clazz,
            WrappedMethod.class,
            MethodHandle.class,
            String.class,
            Class.class,
            Class[].class
    );
    public static IWrappedMethod of(Method method){
        return of(method.getDeclaringClass(), method,method.getReturnType(), method.getParameterTypes());
    }
    public static IWrappedMethod of(Class<?> declaringClass,Method method){
        return of(declaringClass, method, method.getReturnType(), method.getParameterTypes());
    }
    public static IWrappedMethod of(Class<?> declaringClass,Method method,Class<?> rType){
        return of(declaringClass, method, rType, method.getParameterTypes());
    }
    public static IWrappedMethod of(Class<?> declaringClass,Method method,Class<?> rType,Class<?>[] paraTypes){
        return of(declaringClass,method,method.getName(), rType, paraTypes);
    }
    public static IWrappedMethod of(Class<?> declaringClass,Method method,String methodName,Class<?> rType,Class<?>[] paraTypes){
        try {
            MethodHandle handle = ClassUtil.getLookup().findVirtual(
                    declaringClass,
                    methodName,
                    MethodType.methodType(rType, paraTypes)
            );
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    clazz,
                    WrappedMethod.class,
                    MethodHandle.class,
                    String.class,
                    Class.class,
                    Class[].class
            );
            if(constructor!=null){
                try{
                    return (IWrappedMethod) constructor.invoke(handle,methodName,rType,paraTypes);
                }catch(Throwable e){
                    LogUtil.errorf("failed to invoke WrappedMethod constructor %s",e.getMessage());
                }
            }
            return new WrappedMethod(handle, methodName,rType, paraTypes);
        }catch (Throwable t){
            LogUtil.errorf("failed to build MethodHandle of Method %s,%s", methodName,t.getMessage());
        }
        return null;
    }

    private final MethodHandle handle;
    private final String methodName;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;

    public WrappedMethod(MethodHandle handle, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        this.handle = handle;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public MethodHandle getMethod() {
        return handle;
    }
    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "WrappedMethod{" +
                "handle=" + handle +
                ", methodName='" + methodName + '\'' +
                ", returnType=" + returnType +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    @Override
    public Object invoke(Object... args) throws Throwable {
        return handle.invokeWithArguments(args);
    }
}
