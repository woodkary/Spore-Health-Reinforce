package com.Harbinger.Spore.Core.utils;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtil {
    private static volatile MethodHandle DEFINE_CLASS_MH;
    private static volatile Unsafe uns=null;
    private static volatile Object internalUns=null;
    private static volatile ReflectionFactory factory=null;
    private static volatile MethodHandles.Lookup lookup=null;
    private static MethodHandle staticFieldBaseInternal;
    private static MethodHandle staticFieldOffsetInternal;
    private static MethodHandle objectFieldOffsetInternal;
    private static MethodHandle getIntInternal;
    private static MethodHandle getBooleanInternal;
    private static MethodHandle getLongInternal;
    private static MethodHandle getFloatInternal;
    private static MethodHandle getDoubleInternal;
    private static MethodHandle getShortInternal;
    private static MethodHandle getByteInternal;
    private static MethodHandle getCharInternal;
    private static MethodHandle getObjectInternal;
    private static MethodHandle putIntInternal;
    private static MethodHandle putBooleanInternal;
    private static MethodHandle putLongInternal;
    private static MethodHandle putFloatInternal;
    private static MethodHandle putDoubleInternal;
    private static MethodHandle putShortInternal;
    private static MethodHandle putByteInternal;
    private static MethodHandle putCharInternal;
    private static MethodHandle putObjectInternal;
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private ClassUtil() {
    }
    public static synchronized MethodHandles.Lookup getLookup() {
        if(lookup!=null) {
            return lookup;
        }
        try {
            lookup = _new(MethodHandles.Lookup.class,
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class),
                    Object.class, null, -1);
            if(lookup!=null){
                return lookup;
            }
        }catch (NoSuchMethodException e){
            LogUtil.log("failed to instantiate IMPL_LOOKUP,fallback to getFieldValue");
        }
        // fallback 1: use sun.misc.Unsafe directly (no getFieldValue to avoid init cycle)
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe != null) {
                Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                Object base = unsafe.staticFieldBase(f);
                long off = unsafe.staticFieldOffset(f);
                Object v = unsafe.getObject(base, off);
                if (v instanceof MethodHandles.Lookup lk) {
                    lookup=lk;
                    return lookup;
                }
            }
        } catch (Throwable t) {
            LogUtil.logf("failed to read IMPL_LOOKUP via Unsafe: %s", t.getMessage());
        }
        // fallback 2: reflective access
        try {
            Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            f.setAccessible(true);
            Object v = f.get(null);
            if (v instanceof MethodHandles.Lookup lk) {
                lookup=lk;
                return lookup;
            }
        } catch (Throwable t) {
            LogUtil.logf("failed to read IMPL_LOOKUP reflectively: %s", t.getMessage());
        }

        // fallback 3: never return null
        lookup=MethodHandles.lookup();
        return lookup;
    }
    public static synchronized Unsafe getUnsafe() {
        if(uns!=null) {
            return uns;
        }
        try{
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            uns=unsafeConstructor.newInstance();
            if(uns!=null) {
                return uns;
            }
        }catch(Throwable e){
            LogUtil.errorf("Failed to new Unsafe instance: %s", e.getMessage());
        }

        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            uns = (Unsafe) theUnsafe.get(null);
            if(uns!=null) {
                return uns;
            }
        } catch (Throwable var1) {
            LogUtil.errorf("Failed to get Unsafe.theUnsafe: %s", var1.getMessage());
        }

        try {
            uns = Unsafe.getUnsafe();
            if(uns!=null) {
                return uns;
            }
        } catch (Throwable var1) {
            LogUtil.errorf("Failed to invoke Unsafe.getUnsafe: %s", var1.getMessage());
        }
        return uns;
    }
    static Object getInternalUnsafe() {
        if(internalUns!=null) {
            return internalUns;
        }
        try {
            MethodHandles.Lookup lk = getLookup();
            Class<?> c = lk.findClass("jdk.internal.misc.Unsafe");
            // 1) private ctor
            try {
                MethodHandle ctor = lk.findConstructor(c, MethodType.methodType(void.class));
                internalUns=ctor.invoke();
                if(internalUns!=null) {
                    return internalUns;
                }
            } catch (Throwable t) {
                LogUtil.errorf("Failed to new internal unsafe instance: %s", t.getMessage());
            }

            // 2) sun.misc.Unsafe 读 theUnsafe（不 setAccessible）
            try {
                Field f = c.getDeclaredField("theUnsafe");
                Unsafe unsafe = getUnsafe();
                if (unsafe != null) {
                    Object base = unsafe.staticFieldBase(f);
                    long off = unsafe.staticFieldOffset(f);
                    internalUns=unsafe.getObject(base, off);
                    if (internalUns!=null) {
                        return internalUns;
                    }
                }
            }catch (Throwable t) {
                LogUtil.errorf("Failed to get internal theUnsafe: %s", t.getMessage());
            }

            // 1) getUnsafe()
            try {
                internalUns=lk.findStatic(c, "getUnsafe", MethodType.methodType(c)).invoke();
                if(internalUns!=null) {
                    return internalUns;
                }
            } catch (Throwable t) {
                LogUtil.errorf("Failed to invoke getUnsafe: %s", t.getMessage());
            }
        } catch (Throwable t) {
            LogUtil.errorf("Failed to get internal unsafe: %s", t.getMessage());
        }
        return internalUns;
    }
    public static synchronized ReflectionFactory getReflectionFactory(){
        if(factory!=null){
            return factory;
        }
        try {
            Field soleInstanceField = ReflectionFactory.class.getDeclaredField("soleInstance");
            Unsafe unsafe = getUnsafe();
            if (unsafe != null) {
                Object target = unsafe.staticFieldBase(soleInstanceField);
                long offset = unsafe.staticFieldOffset(soleInstanceField);
                Object soleInstance = unsafe.getObject(target, offset);
                if (soleInstance instanceof ReflectionFactory rf) {
                    factory= rf;
                    return factory;
                }
            }
        }catch (Exception e){
            LogUtil.error("failed to get ReflectionFactory field");
        }
        factory=ReflectionFactory.getReflectionFactory();
        return factory;
    }
    public static <T> T _new(Class<T> clazz, Constructor<T> constructor, Object... args) {

        try{
            Constructor<?> ctr= getReflectionFactory().newConstructorForSerialization(clazz,constructor);
            return (T) ctr.newInstance(args);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LogUtil.logf("failed to instantiate class %s", clazz.getName());
        }
        return null;
    }
    public static Class<?> deffineneClazz(ClassLoader loader, String name, byte[] data){
        return deffineneClazz(loader, name, data, null);
    }
    public static Class<?> deffineneHiddenClazz(Class<?> hostClass, byte[] data) {
        return deffineneHiddenClazz(hostClass, data, true);
    }
    public static Class<?> deffineneHiddenClazz(Class<?> hostClass, byte[] data, boolean initialize) {
        if (hostClass == null || data == null || data.length == 0) {
            return null;
        }
        MethodHandles.Lookup.ClassOption nestmate = MethodHandles.Lookup.ClassOption.NESTMATE;
        if (!hostClass.isHidden()) {
            try{
                Class<?> res=HiddenClassDefiner.defaneClazz0(hostClass,data,initialize,nestmate);
                if(res!=null&&res.isHidden()){
                    return res;
                }
            }catch (Throwable t){
                LogUtil.errorf("failed to invoke defineClazz0 for %s,%s" , hostClass,t);
            }
        }
        try {
            MethodHandles.Lookup rootLookup = getLookup();
            MethodHandles.Lookup hostLookup = null;

            // 优先：直接构造 hostClass 的 TRUSTED lookup，尽量绕开 SecurityManager defineClass 权限检查路径
            try {
                hostLookup = _new(
                        MethodHandles.Lookup.class,
                        MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class),
                        hostClass,
                        null,
                        -1
                );
            } catch (Throwable t) {
                LogUtil.logf("failed to instantiate trusted host class %s", hostClass.getName());
            }

            if (hostLookup == null) {
                try {
                    hostLookup = MethodHandles.privateLookupIn(hostClass, rootLookup);
                } catch (Throwable t) {
                    LogUtil.logf("failed to instantiate privateLookup host class %s", hostClass.getName());
                    hostLookup = rootLookup.in(hostClass);
                }
            }
            MethodHandles.Lookup hiddenLookup = HiddenClassDefiner.defaneHeddenClazz(
                    hostLookup,
                    data,
                    initialize,
                    nestmate
            );
            return hiddenLookup.lookupClass();
        } catch (Throwable t) {
            LogUtil.errorf("Failed to define hidden class for host %s: %s", hostClass.getName(), t.getMessage());
            LogUtil.printStackTrace(t);
            return null;
        }
    }
    public static Class<?> deffineneClazz(ClassLoader loader, String name, byte[] data, ProtectionDomain domain) {
        if (data == null || data.length == 0) {
            LogUtil.errorf("Class byte data is empty");
            return null;
        }

        try {
            // 懒加载并缓存 defineClass 的 MethodHandle
            if (DEFINE_CLASS_MH == null) {
                synchronized (ClassUtil.class) {
                    if (DEFINE_CLASS_MH == null) {
                        // 1. 获取 jdk.internal.misc.Unsafe 的 Class 对象
                        Class<?> unsafeClazz = getLookup().findClass("jdk.internal.misc.Unsafe");
                        // 2. 定义 defineClass 的方法签名（和 Unsafe 的方法完全匹配）
                        MethodType mt = MethodType.methodType(
                                Class.class,          // 返回值：Class<?>
                                String.class,         // 参数1：类名
                                byte[].class,         // 参数2：字节码数组
                                int.class,            // 参数3：起始偏移
                                int.class,            // 参数4：长度
                                ClassLoader.class,    // 参数5：类加载器
                                ProtectionDomain.class// 参数6：保护域
                        );
                        // 3. 通过 IMPL_LOOKUP 获取 Unsafe.defineClass 的方法句柄（突破访问限制）
                        DEFINE_CLASS_MH = getLookup().findVirtual(
                                unsafeClazz,          // 目标类：jdk.internal.misc.Unsafe
                                "defineClass0",        // 方法名
                                mt                    // 方法签名
                        );
                    }
                }
            }

            // 4. 调用 MethodHandle 执行 deffineneClazz（替代反射 invoke）
            // 入参顺序：Unsafe实例, 类名, 字节码数组, 偏移0, 数组长度, 类加载器, 保护域null
            Object internalUnsafe = getInternalUnsafe();
            if (internalUnsafe == null) {
                throw new IllegalStateException("internalUNSAFE is null");
            }

            return (Class<?>) DEFINE_CLASS_MH.invoke(
                    internalUnsafe,
                    name,
                    data,
                    0,
                    data.length,
                    loader,
                    domain
            );
        } catch (Throwable var5) {
            LogUtil.errorf("Failed to define class: %s, error: %s", name, var5.getMessage());
            throw new RuntimeException(var5);
        }
    }
    private static void ensureInternalUnsafeMethodHandles() {
        if (staticFieldBaseInternal != null && staticFieldOffsetInternal != null
                && objectFieldOffsetInternal != null && getObjectInternal != null && putObjectInternal != null) {
            return;
        }
        synchronized (ClassUtil.class) {
            if (staticFieldBaseInternal != null && staticFieldOffsetInternal != null
                    && objectFieldOffsetInternal != null && getObjectInternal != null && putObjectInternal != null) {
                return;
            }
            try {
                MethodHandles.Lookup lk = getLookup();
                Object internal = getInternalUnsafe();
                if (internal == null || lk == null) {
                    return;
                }
                Class<?> internalUNSAFEClass = lk.findClass("jdk.internal.misc.Unsafe");
                staticFieldBaseInternal = lk.findVirtual(
                        internalUNSAFEClass,
                        "staticFieldBase",
                        MethodType.methodType(Object.class, Field.class)
                ).bindTo(internal);
                staticFieldOffsetInternal = lk.findVirtual(
                        internalUNSAFEClass,
                        "staticFieldOffset",
                        MethodType.methodType(Long.TYPE, Field.class)
                ).bindTo(internal);
                objectFieldOffsetInternal = lk.findVirtual(internalUNSAFEClass, "objectFieldOffset", MethodType.methodType(Long.TYPE, Field.class)).bindTo(internal);
                getIntInternal = lk.findVirtual(internalUNSAFEClass, "getInt", MethodType.methodType(int.class, Object.class, long.class)).bindTo(internal);
                getBooleanInternal = lk.findVirtual(internalUNSAFEClass, "getBoolean", MethodType.methodType(boolean.class, Object.class, long.class)).bindTo(internal);
                getLongInternal = lk.findVirtual(internalUNSAFEClass, "getLong", MethodType.methodType(long.class, Object.class, long.class)).bindTo(internal);
                getFloatInternal = lk.findVirtual(internalUNSAFEClass, "getFloat", MethodType.methodType(float.class, Object.class, long.class)).bindTo(internal);
                getDoubleInternal = lk.findVirtual(internalUNSAFEClass, "getDouble", MethodType.methodType(double.class, Object.class, long.class)).bindTo(internal);
                getShortInternal = lk.findVirtual(internalUNSAFEClass, "getShort", MethodType.methodType(short.class, Object.class, long.class)).bindTo(internal);
                getByteInternal = lk.findVirtual(internalUNSAFEClass, "getByte", MethodType.methodType(byte.class, Object.class, long.class)).bindTo(internal);
                getCharInternal = lk.findVirtual(internalUNSAFEClass, "getChar", MethodType.methodType(char.class, Object.class, long.class)).bindTo(internal);
                try {
                    getObjectInternal = lk.findVirtual(internalUNSAFEClass, "getReference", MethodType.methodType(Object.class, Object.class, long.class)).bindTo(internal);
                } catch (Throwable t) {
                    getObjectInternal = lk.findVirtual(internalUNSAFEClass, "getObject", MethodType.methodType(Object.class, Object.class, long.class)).bindTo(internal);
                }

                putIntInternal = lk.findVirtual(internalUNSAFEClass, "putInt", MethodType.methodType(void.class, Object.class, long.class, int.class)).bindTo(internal);
                putBooleanInternal = lk.findVirtual(internalUNSAFEClass, "putBoolean", MethodType.methodType(void.class, Object.class, long.class, boolean.class)).bindTo(internal);
                putLongInternal = lk.findVirtual(internalUNSAFEClass, "putLong", MethodType.methodType(void.class, Object.class, long.class, long.class)).bindTo(internal);
                putFloatInternal = lk.findVirtual(internalUNSAFEClass, "putFloat", MethodType.methodType(void.class, Object.class, long.class, float.class)).bindTo(internal);
                putDoubleInternal = lk.findVirtual(internalUNSAFEClass, "putDouble", MethodType.methodType(void.class, Object.class, long.class, double.class)).bindTo(internal);
                putShortInternal = lk.findVirtual(internalUNSAFEClass, "putShort", MethodType.methodType(void.class, Object.class, long.class, short.class)).bindTo(internal);
                putByteInternal = lk.findVirtual(internalUNSAFEClass, "putByte", MethodType.methodType(void.class, Object.class, long.class, byte.class)).bindTo(internal);
                putCharInternal = lk.findVirtual(internalUNSAFEClass, "putChar", MethodType.methodType(void.class, Object.class, long.class, char.class)).bindTo(internal);
                try {
                    putObjectInternal = lk.findVirtual(internalUNSAFEClass, "putReference", MethodType.methodType(void.class, Object.class, long.class, Object.class)).bindTo(internal);
                } catch (Throwable t) {
                    putObjectInternal = lk.findVirtual(internalUNSAFEClass, "putObject", MethodType.methodType(void.class, Object.class, long.class, Object.class)).bindTo(internal);
                }
            } catch (Throwable var1) {
                LogUtil.errorf("Failed to initialize Unsafe handles: %s", var1.getMessage());
            }
        }
    }

    public static Object getFieldValue(Field f, Object target) {
        ensureInternalUnsafeMethodHandles();
        long offset = 0L;
        Object base = target;
        Class<?> type = f.getType();
        try {
            if (Modifier.isStatic(f.getModifiers())) {
                base = staticFieldBase(f);
                offset = staticFieldOffset(f);
            } else {
                offset = objectFieldOffset(f);
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to resolve field base/offset: %s", e.getMessage());
        }

        // 1) jdk.internal.misc.Unsafe
        try {
            if (getInternalUnsafe() != null) {
                if (type == int.class && getIntInternal != null) return (int) getIntInternal.invoke(base, offset);
                if (type == boolean.class && getBooleanInternal != null) return (boolean) getBooleanInternal.invoke(base, offset);
                if (type == long.class && getLongInternal != null) return (long) getLongInternal.invoke(base, offset);
                if (type == float.class && getFloatInternal != null) return (float) getFloatInternal.invoke(base, offset);
                if (type == double.class && getDoubleInternal != null) return (double) getDoubleInternal.invoke(base, offset);
                if (type == short.class && getShortInternal != null) return (short) getShortInternal.invoke(base, offset);
                if (type == byte.class && getByteInternal != null) return (byte) getByteInternal.invoke(base, offset);
                if (type == char.class && getCharInternal != null) return (char) getCharInternal.invoke(base, offset);
                if (!type.isPrimitive() && getObjectInternal != null) return getObjectInternal.invoke(base, offset);
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to get field value via jdk.internal.misc.Unsafe: %s", e.getMessage());
        }

        // 2) sun.misc.Unsafe
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe != null) {
                if (type == int.class) return unsafe.getInt(base, offset);
                if (type == boolean.class) return unsafe.getBoolean(base, offset);
                if (type == long.class) return unsafe.getLong(base, offset);
                if (type == float.class) return unsafe.getFloat(base, offset);
                if (type == double.class) return unsafe.getDouble(base, offset);
                if (type == short.class) return unsafe.getShort(base, offset);
                if (type == byte.class) return unsafe.getByte(base, offset);
                if (type == char.class) return unsafe.getChar(base, offset);
                return unsafe.getObject(base, offset);
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to get field value via sun.misc.Unsafe: %s", e.getMessage());
        }

        // 3) Reflection fallback
        try {
            f.setAccessible(true);
            return f.get(target);
        } catch (Throwable e) {
            LogUtil.errorf("Failed to get field value via reflection: %s", e.getMessage());
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public static long objectFieldOffset(Field f) {
        ensureInternalUnsafeMethodHandles();
        try {
            if (objectFieldOffsetInternal != null && getInternalUnsafe() != null) {
                return (long) objectFieldOffsetInternal.invoke(f);
            }
        } catch (Throwable ignored) {
        }
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe != null) {
                return unsafe.objectFieldOffset(f);
            }
        } catch (Throwable var4) {
            // ignore
        }
        try {
            if (objectFieldOffsetInternal != null) {
                return (long) objectFieldOffsetInternal.invoke(f);
            }
        } catch (Throwable var3) {
            LogUtil.errorf("Failed to get field offset: %s", var3.getMessage());
        }
        return 0L;
    }

    public static Object staticFieldBase(Field f) {
        ensureInternalUnsafeMethodHandles();
        try {
            if (staticFieldBaseInternal != null && getInternalUnsafe() != null) {
                return staticFieldBaseInternal.invoke(f);
            }
        } catch (Throwable ignored) {
        }
        Unsafe unsafe = getUnsafe();
        if (unsafe != null) {
            try {
                return unsafe.staticFieldBase(f);
            } catch (Throwable ignored) {
            }
        }
        try {
            if (staticFieldBaseInternal != null) {
                return staticFieldBaseInternal.invoke(f);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static long staticFieldOffset(Field f) {
        ensureInternalUnsafeMethodHandles();
        try {
            if (staticFieldOffsetInternal != null && getInternalUnsafe() != null) {
                return (long) staticFieldOffsetInternal.invoke(f);
            }
        } catch (Throwable ignored) {
        }
        Unsafe unsafe = getUnsafe();
        if (unsafe != null) {
            try {
                return unsafe.staticFieldOffset(f);
            } catch (Throwable ignored) {
            }
        }
        try {
            if (staticFieldOffsetInternal != null) {
                return (long) staticFieldOffsetInternal.invoke(f);
            }
        } catch (Throwable ignored) {
        }
        return 0L;
    }
    public static Object getFieldValue(Class<?> specialFieldClass,Object target, String fieldName) {
        try {
            return getFieldValue(specialFieldClass.getDeclaredField(fieldName), target);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to get field value: %s", var4.getMessage());
            return null;
        }
    }
    public static Object getFieldValue(Object target, String fieldName) {
        try {
            return getFieldValue(target.getClass().getDeclaredField(fieldName), target);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to get field value: %s", var4.getMessage());
            return null;
        }
    }

    public static Object getFieldValue(Class<?> target, String fieldName) {
        try {
            return getFieldValue(target.getDeclaredField(fieldName), (Object)null);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to get field value: %s", var4.getMessage());
            return null;
        }
    }
    private static Field findFieldInHierarchy(Class<?> cls, String fieldName) {
        return FIELD_CACHE
                .computeIfAbsent(cls, ClassUtil::scanClassFields)
                .get(fieldName);
    }

    private static Map<String, Field> scanClassFields(Class<?> cls) {
        Map<String, Field> map = new HashMap<>();
        Class<?> current = cls;
        while (current != null) {
            for (Field f : current.getDeclaredFields()) {
                //f.setAccessible(true);
                map.putIfAbsent(f.getName(), f);
            }
            current = current.getSuperclass();
        }
        return map;
    }
    public static Object getFieldValueFromHierarchy(Object object, String fieldName) {
        Field f = findFieldInHierarchy(object.getClass(), fieldName);
        return getFieldValue(f, object);
    }
    public static void setFieldValue(Class<?> target, String fieldName, Object value) {
        try {
            setFieldValue(target.getDeclaredField(fieldName), target, value);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to set field value: %s", var4.getMessage());
        }
    }
    public static void setFieldValue(Class<?> specialFieldClass, String fieldName, Object target,Object value) {
        try {
            setFieldValue(specialFieldClass.getDeclaredField(fieldName), target, value);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to set field value: %s", var4.getMessage());
        }
    }
    public static void setFieldValue(Object target, String fieldName, Object value) {
        try {
            setFieldValue(target.getClass().getDeclaredField(fieldName), target, value);
        } catch (Throwable var4) {
            LogUtil.errorf("Failed to set field value: %s", var4.getMessage());
        }

    }

    public static void setFieldValue(Field f, Object target, Object value) {
        ensureInternalUnsafeMethodHandles();
        long offset = 0L;
        Object base = target;
        Class<?> type = f.getType();
        try {
            if (Modifier.isStatic(f.getModifiers())) {
                base = staticFieldBase(f);
                offset = staticFieldOffset(f);
            } else {
                offset = objectFieldOffset(f);
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to resolve field base/offset for set: %s", e.getMessage());
        }

        // 1) jdk.internal.misc.Unsafe
        try {
            if (getInternalUnsafe() != null) {
                if (type == int.class && putIntInternal != null) { putIntInternal.invoke(base, offset, (int) value); return; }
                if (type == boolean.class && putBooleanInternal != null) { putBooleanInternal.invoke(base, offset, (boolean) value); return; }
                if (type == long.class && putLongInternal != null) { putLongInternal.invoke(base, offset, (long) value); return; }
                if (type == float.class && putFloatInternal != null) { putFloatInternal.invoke(base, offset, (float) value); return; }
                if (type == double.class && putDoubleInternal != null) { putDoubleInternal.invoke(base, offset, (double) value); return; }
                if (type == short.class && putShortInternal != null) { putShortInternal.invoke(base, offset, (short) value); return; }
                if (type == byte.class && putByteInternal != null) { putByteInternal.invoke(base, offset, (byte) value); return; }
                if (type == char.class && putCharInternal != null) { putCharInternal.invoke(base, offset, (char) value); return; }
                if (!type.isPrimitive() && putObjectInternal != null) { putObjectInternal.invoke(base, offset, value); return; }
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to set field value via jdk.internal.misc.Unsafe: %s", e.getMessage());
        }

        // 2) sun.misc.Unsafe
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe != null) {
                if (type == int.class) { unsafe.putInt(base, offset, (int) value); return; }
                if (type == boolean.class) { unsafe.putBoolean(base, offset, (boolean) value); return; }
                if (type == long.class) { unsafe.putLong(base, offset, (long) value); return; }
                if (type == float.class) { unsafe.putFloat(base, offset, (float) value); return; }
                if (type == double.class) { unsafe.putDouble(base, offset, (double) value); return; }
                if (type == short.class) { unsafe.putShort(base, offset, (short) value); return; }
                if (type == byte.class) { unsafe.putByte(base, offset, (byte) value); return; }
                if (type == char.class) { unsafe.putChar(base, offset, (char) value); return; }
                unsafe.putObject(base, offset, value);
                return;
            }
        } catch (Throwable e) {
            LogUtil.errorf("Failed to set field value via sun.misc.Unsafe: %s", e.getMessage());
        }

        // 3) Reflection fallback
        try {
            f.setAccessible(true);
            f.set(target, value);
        } catch (Throwable ex) {
            LogUtil.errorf("Failed to set field value via reflection: %s", ex.getMessage());
            LogUtil.printStackTrace(ex);
        }
    }
}
