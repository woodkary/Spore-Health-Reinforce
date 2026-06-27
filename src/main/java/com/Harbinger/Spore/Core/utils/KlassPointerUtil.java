package com.Harbinger.Spore.Core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.Function;

public final class KlassPointerUtil implements IKlassPointer {
    public static final IKlassPointer INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IKlassPointer.class,
            KlassPointerUtil.class
    );
    private final ConcurrentMap<Class<?>, Integer> KLASS_PTR_CACHE = new ConcurrentHashMap<>();
    private MethodHandle addressSize=null;
    private MethodHandle putIntVolatile;
    private int addressSize(){
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
    public <T> T replaceClass(T o, Class<? extends T> tc, String s1, int i2, float f3) {
        try{
            Object internal = ClassUtil.getInternalUnsafe();
            if (internal == null) {
                LogUtil.error("internal unsafe is null when replace class");
                return o;
            }
            Class<?> maybeSubClass=internal.getClass();
            Class<?> internalClass = Class.forName("jdk.internal.misc.Unsafe");
            int addressSize = addressSize();
            if(addressSize==-10){
                LogUtil.error("failed to find address size when replace class");
                return o;
            }
            if (o!=null&&tc!=null){
                MethodHandles.Lookup lookup = ClassUtil.getLookup();
                int klassPtr = KLASS_PTR_CACHE.computeIfAbsent(tc, KlassPointerComputeFunction.newInstance(lookup,internalClass,maybeSubClass,internal,addressSize));
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
        return o;
    }
    private static final class KlassPointerComputeFunction implements Function<Class<?>,Integer> {
        private static Class<? extends Function<Class<?>,Integer>> funcClass= (Class<? extends Function<Class<?>, Integer>>) BytecodeUtil.resolveHiddenClassOrSelf(
                KlassPointerComputeFunction.class,
                MethodHandles.Lookup.class,
                Class.class,
                Class.class,
                Object.class
        );
        private static MethodHandle constructor;
        static {
            constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    funcClass,
                    KlassPointerComputeFunction.class,
                    MethodHandles.Lookup.class,
                    Class.class,
                    Class.class,
                    Object.class,
                    int.class
            );
        }
        private static Function<Class<?>,Integer> newInstance(MethodHandles.Lookup lookup, Class<?> internalClass, Class<?> maybeSubClass, Object internal, int addressSize){
            constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    funcClass,
                    KlassPointerComputeFunction.class,
                    MethodHandles.Lookup.class,
                    Class.class,
                    Class.class,
                    Object.class,
                    int.class
            );
            if(constructor!=null){
                try{
                    return (Function<Class<?>, Integer>) constructor.invoke(lookup, internalClass, maybeSubClass, internal, addressSize);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new KlassPointerComputeFunction: %s", e.getMessage());
                }
            }
            return new KlassPointerComputeFunction(lookup, internalClass, maybeSubClass, internal, addressSize);
        }
        private final MethodHandles.Lookup lookup;
        private final Class<?> internalClass;
        private final Class<?> maybeSubClass;
        private final Object internal;
        private final int addressSize;

        private KlassPointerComputeFunction(MethodHandles.Lookup lookup, Class<?> internalClass, Class<?> maybeSubClass, Object internal, int addressSize) {
            this.lookup = lookup;
            this.internalClass = internalClass;
            this.maybeSubClass = maybeSubClass;
            this.internal = internal;
            this.addressSize = addressSize;
        }

        @Override
        public Integer apply(Class<?> clazz) {
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
        }
    }
}
