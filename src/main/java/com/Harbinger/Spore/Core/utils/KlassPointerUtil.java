package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class KlassPointerUtil {
    private static final ConcurrentMap<Class<?>, Integer> KLASS_PTR_CACHE = new ConcurrentHashMap<>();
    private static MethodHandle addressSize=null;
    private static MethodHandle putIntVolatile;
    private static int addressSize(){
        Object internal = ClassUtil.getInternalUnsafe();
        if (internal == null) {
            return -10;
        }
        if(addressSize==null){
            try {
                Class<?> maybeSubClass = internal.getClass();
                Class<?> internalClass = Class.forName("jdk.internal.misc.Unsafe");
                addressSize = ClassUtil.getLookup().findSpecial(
                        internalClass,
                        "addressSize",
                        MethodType.methodType(int.class),
                        maybeSubClass
                );
                addressSize=addressSize.bindTo(internal);
            }catch (Throwable t){
                LogUtil.errorf("failed to find address size method: %s", t.getMessage());
            }
        }
        if(addressSize!=null){
            try {
                return (int) addressSize.invoke();
            }catch (Throwable t){
                LogUtil.errorf("failed to invoke address size method: %s", t.getMessage());
            }
        }
        return -10;
    }
    public static Future<?> replaceClass(Object o, Class<?> tc, String s1, int i2, float f3) {
        try{
            Object internal = ClassUtil.getInternalUnsafe();
            if (internal == null) {
                LogUtil.error("internal unsafe is null when replace class");
                return null;
            }
            Class<?> maybeSubClass=internal.getClass();
            Class<?> internalClass = Class.forName("jdk.internal.misc.Unsafe");
            int addressSize = addressSize();
            if(addressSize==-10){
                LogUtil.error("failed to find address size when replace class");
                return null;
            }
            if (o!=null&&tc!=null){
                MethodHandles.Lookup lookup = ClassUtil.getLookup();
                int klassPtr = KLASS_PTR_CACHE.computeIfAbsent(tc, clazz -> {
                    try {
                        MethodHandle allocateInstance = lookup.findSpecial(internalClass,
                                "allocateInstance",
                                MethodType.methodType(Object.class, Class.class),
                                maybeSubClass);
                        MethodHandle getIntVolatile = lookup.findSpecial(internalClass,
                                "getIntVolatile",
                                MethodType.methodType(int.class, Object.class, long.class),
                                maybeSubClass);
                        // 分配未初始化对象并获取 klass_ptr
                        Object tmp = allocateInstance.bindTo(internal).invoke(clazz);
                        return (Integer) getIntVolatile.bindTo(internal).invoke(tmp, addressSize);
                    } catch (Throwable e) {
                        LogUtil.errorf("error when allocateInstance: %s", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

                // 替换对象 klass_ptr
                if (putIntVolatile == null) {
                    putIntVolatile=lookup.findSpecial(
                            internalClass,
                            "putIntVolatile",
                            MethodType.methodType(void.class,Object.class,long.class,int.class),
                            maybeSubClass
                    );
                    putIntVolatile=putIntVolatile.bindTo(internal);
                }
                putIntVolatile.invoke(o, addressSize, klassPtr);
                //unsafe.putIntVolatile(o, 8L, klassPtr);
            }
        }catch (Throwable e){
            LogUtil.errorf("error when replaceClass: %s,target: %s,class: %s", e.getMessage(),o,tc.getName());
            LogUtil.printStackTrace(e);
        }
        return null;
    }
}
