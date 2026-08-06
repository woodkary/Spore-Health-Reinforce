package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class HealthTargetInvocationHandler implements InvocationHandler {
    private static final Class<? extends InvocationHandler> handlerClass= (Class<? extends InvocationHandler>) BytecodeUtil.resolveHiddenClassOrSelf(
            HealthTargetInvocationHandler.class,
            Object.class,
            Object.class
    );
    private static MethodHandle constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            handlerClass,
            HealthTargetInvocationHandler.class,
            Object.class,
            Object.class
    );
    public static InvocationHandler newInstance(Object target,Object entity){
        constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                handlerClass,
                HealthTargetInvocationHandler.class,
                Object.class,
                Object.class
        );
        if(constructor!=null){
            try{
                return (InvocationHandler) constructor.invoke(target,entity);
            } catch (Throwable e) {
                LogUtil.errorf("failed to create health target handler", e.getMessage());
            }
        }
        return new HealthTargetInvocationHandler(target,entity);
    }
    private final Object target;
    private final Object entity;
    HealthTargetInvocationHandler(Object target, Object entity) {
        this.target = target;
        this.entity = entity;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object res = method.invoke(target, args);
        if(res instanceof Float fHealth) {
            return EntityHeealuthManager.INSTANCE.getHeealth(fHealth, entity);
        }
        if(res instanceof Double dHealth) {
            return EntityHeealuthManager.INSTANCE.getHeealth(dHealth, entity);
        }
        return res;
    }
}
