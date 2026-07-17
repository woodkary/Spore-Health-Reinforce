package com.Harbinger.Spore.Core.utils;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public final class KlassPointerUtil implements IKlassPointer {
    public static final IKlassPointer INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IKlassPointer.class,
            KlassPointerUtil.class
    );
    private final ConcurrentMap<Class<?>, Number> KLASS_PTR_CACHE = new ConcurrentHashMap<>();
    private MethodHandle addressSize=null;
    private volatile MethodHandle putKlassPointerVolatile;
    private volatile Function<Class<?>,Number> klassPointerComputeFunction;
    private final boolean compressedClassPointers;
    public KlassPointerUtil() {
        HotSpotDiagnosticMXBean bean =
                ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        compressedClassPointers = Boolean.parseBoolean(
                bean.getVMOption("UseCompressedClassPointers").getValue()
        );
    }

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
                Function<Class<?>,Number> computeFunction = getKlassPointerComputeFunction(
                        lookup,
                        internalClass,
                        maybeSubClass,
                        internal,
                        addressSize
                );
                Number klassPtr = KLASS_PTR_CACHE.computeIfAbsent(tc, computeFunction);
                // 替换对象 klass_ptr
                MethodHandle putKlassPointer = getPutKlassPointerVolatile(
                        lookup,
                        internalClass,
                        maybeSubClass,
                        internal
                );
                if (compressedClassPointers) {
                    putKlassPointer.invoke(o, (long) addressSize, klassPtr.intValue());
                } else {
                    putKlassPointer.invoke(o, (long) addressSize, klassPtr.longValue());
                }
            }
        }catch (Throwable e){
            LogUtil.errorf("error when replaceClass: %s,target: %s,class: %s", e.getMessage(),o,tc.getName());
            LogUtil.printStackTrace(e);
        }
        return o;
    }

    private Function<Class<?>,Number> getKlassPointerComputeFunction(MethodHandles.Lookup lookup,
                                                                      Class<?> internalClass,
                                                                      Class<?> maybeSubClass,
                                                                      Object internal,
                                                                      int addressSize) {
        Function<Class<?>,Number> result = klassPointerComputeFunction;
        if (result != null) {
            return result;
        }
        synchronized (this) {
            result = klassPointerComputeFunction;
            if (result == null) {
                result = KlassPointerComputeFunction.newInstance(
                        lookup,
                        internalClass,
                        maybeSubClass,
                        internal,
                        addressSize,
                        compressedClassPointers
                );
                klassPointerComputeFunction = result;
            }
        }
        return result;
    }

    private MethodHandle getPutKlassPointerVolatile(MethodHandles.Lookup lookup,
                                                     Class<?> internalClass,
                                                     Class<?> maybeSubClass,
                                                     Object internal) throws Throwable {
        MethodHandle result = putKlassPointerVolatile;
        if (result != null) {
            return result;
        }
        synchronized (this) {
            result = putKlassPointerVolatile;
            if (result == null) {
                String methodName = compressedClassPointers ? "putIntVolatile" : "putLongVolatile";
                Class<?> valueType = compressedClassPointers ? int.class : long.class;
                result = lookup.findSpecial(
                        internalClass,
                        methodName,
                        MethodType.methodType(void.class,Object.class,long.class,valueType),
                        maybeSubClass
                ).bindTo(internal);
                putKlassPointerVolatile = result;
            }
        }
        return result;
    }

    private static final class KlassPointerComputeFunction implements Function<Class<?>,Number> {
        private static Class<? extends Function<Class<?>,Number>> funcClass= (Class<? extends Function<Class<?>, Number>>) BytecodeUtil.resolveHiddenClassOrSelf(
                KlassPointerComputeFunction.class,
                MethodHandles.Lookup.class,
                Class.class,
                Class.class,
                Object.class,
                int.class,
                boolean.class
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
                    int.class,
                    boolean.class
            );
        }
        private static Function<Class<?>,Number> newInstance(MethodHandles.Lookup lookup, Class<?> internalClass, Class<?> maybeSubClass, Object internal, int addressSize,boolean compressed){
            constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    funcClass,
                    KlassPointerComputeFunction.class,
                    MethodHandles.Lookup.class,
                    Class.class,
                    Class.class,
                    Object.class,
                    int.class,
                    boolean.class
            );
            if(constructor!=null){
                try{
                    return (Function<Class<?>, Number>) constructor.invoke(lookup, internalClass, maybeSubClass, internal, addressSize,compressed);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new KlassPointerComputeFunction: %s", e.getMessage());
                }
            }
            return new KlassPointerComputeFunction(lookup, internalClass, maybeSubClass, internal, addressSize,compressed);
        }
        private final MethodHandles.Lookup lookup;
        private final Class<?> internalClass;
        private final Class<?> maybeSubClass;
        private final Object internal;
        private final int addressSize;
        private final boolean compressed;
        private volatile MethodHandle allocateInstance;
        private volatile MethodHandle getKlassPointerVolatile;

        private KlassPointerComputeFunction(MethodHandles.Lookup lookup, Class<?> internalClass, Class<?> maybeSubClass, Object internal, int addressSize,boolean compressed) {
            this.lookup = lookup;
            this.internalClass = internalClass;
            this.maybeSubClass = maybeSubClass;
            this.internal = internal;
            this.addressSize = addressSize;
            this.compressed = compressed;
        }

        @Override
        public Number apply(Class<?> clazz) {
            try {
                MethodHandle allocate = getAllocateInstance();
                MethodHandle getKlassPointer = getKlassPointerVolatile();
                // 分配未初始化对象并获取 klass_ptr
                Object tmp = allocate.invoke(clazz);
                if (compressed) {
                    return (int) getKlassPointer.invoke(tmp, (long) addressSize);
                }
                return (long) getKlassPointer.invoke(tmp, (long) addressSize);
            } catch (Throwable e) {
                LogUtil.errorf("error when allocateInstance: %s", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        private MethodHandle getAllocateInstance() throws Throwable {
            MethodHandle result = allocateInstance;
            if (result != null) {
                return result;
            }
            synchronized (this) {
                result = allocateInstance;
                if (result == null) {
                    result = lookup.findSpecial(
                            internalClass,
                            "allocateInstance",
                            MethodType.methodType(Object.class, Class.class),
                            maybeSubClass
                    ).bindTo(internal);
                    allocateInstance = result;
                }
            }
            return result;
        }

        private MethodHandle getKlassPointerVolatile() throws Throwable {
            MethodHandle result = getKlassPointerVolatile;
            if (result != null) {
                return result;
            }
            synchronized (this) {
                result = getKlassPointerVolatile;
                if (result == null) {
                    String methodName = compressed ? "getIntVolatile" : "getLongVolatile";
                    Class<?> returnType = compressed ? int.class : long.class;
                    result = lookup.findSpecial(
                            internalClass,
                            methodName,
                            MethodType.methodType(returnType, Object.class, long.class),
                            maybeSubClass
                    ).bindTo(internal);
                    getKlassPointerVolatile = result;
                }
            }
            return result;
        }
    }
}
