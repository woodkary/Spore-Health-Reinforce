package com.Harbinger.Spore.Core.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MethodHandleUtil implements IMethodHandle {
    public static IMethodHandle INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IMethodHandle.class,
            MethodHandleUtil.class
    );
    // ===============================
    // FastUtil 缓存
    // ===============================
    private final Map<Class<?>, MethodHandle> FASTUTIL_REMOVE_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodHandle> FASTUTIL_PUT_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodHandle> FASTUTIL_CLEAR_CACHE = new ConcurrentHashMap<>();

    // ===============================
    // Java Map 缓存
    // ===============================
    private final Map<Class<?>, MethodHandle> JAVA_MAP_REMOVE_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodHandle> JAVA_MAP_PUT_CACHE = new ConcurrentHashMap<>();
    private final Map<Class<?>, MethodHandle> JAVA_MAP_CLEAR_CACHE = new ConcurrentHashMap<>();

    // ===============================
    // Java Collection 缓存
    // ===============================
    private final Map<Class<?>, MethodHandle> JAVA_COLLECTION_REMOVE_CACHE = new ConcurrentHashMap<>();

    // ==============================================================
    // 懒加载 Lookup
    // ==============================================================

    private volatile MethodHandles.Lookup cachedLookup;

    public MethodHandles.Lookup getLookup() {
        MethodHandles.Lookup lookup = cachedLookup;
        if (lookup == null) {
            synchronized (MethodHandleUtil.class) {
                lookup = cachedLookup;
                if (lookup == null) {
                    try {
                        lookup = ClassUtil.getLookup();
                    } catch (Throwable e) {
                        LogUtil.errorf("Failed to get Unsafe.IMPL_LOOKUP: %s", e.toString());
                        lookup = MethodHandles.lookup();
                    }
                    cachedLookup = lookup;
                }
            }
        }
        return lookup;
    }


    // ==============================================================
    // FastUtil Map 操作
    // ==============================================================

    public <V> Object fastRemove(Int2ObjectMap<V> map, int key) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = FASTUTIL_REMOVE_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("remove",MethodType.methodType(Object.class, int.class)));
            if (mh != null) {
                return mh.bindTo(map).invokeWithArguments(key);
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to remove key %d from %s using FastUtil: %s", key, map.getClass().getName(), ignored.toString());
        }
        return map.remove(key);
    }

    public <V> Object fastPut(Int2ObjectMap<V> map, int key, V value) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = FASTUTIL_PUT_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("put",MethodType.methodType(Object.class, int.class, Object.class)));
            if (mh != null) {
                return mh.bindTo(map).invokeWithArguments(key, value);
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to put key %d and value %s to %s using FastUtil: %s", key, value, map.getClass().getName(), ignored.toString());
        }
        return map.put(key, value);
    }

    public void fastClear(Int2ObjectMap<?> map) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = FASTUTIL_CLEAR_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("clear",MethodType.methodType(void.class)));
            if (mh != null) {
                mh.bindTo(map).invokeWithArguments();
                return;
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to clear %s using FastUtil: %s", map.getClass().getName(), ignored.toString());
        }
        map.clear();
    }

    // ==============================================================
    // Java Map 操作
    // ==============================================================

    public <K, V> Object javaMapRemove(Map<K, V> map, Object key) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = JAVA_MAP_REMOVE_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("remove",MethodType.methodType(Object.class, Object.class)));
            if (mh != null) {
                return mh.bindTo(map).invokeWithArguments(key);
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to remove key %s from %s using Java Map: %s", key, map.getClass().getName(), ignored.toString());
        }
        return map.remove(key);
    }

    public <K, V> Object javaMapPut(Map<K, V> map, K key, V value) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = JAVA_MAP_PUT_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("put",MethodType.methodType(Object.class, Object.class, Object.class)));
            if (mh != null) {
                return mh.bindTo(map).invokeWithArguments(key, value);
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to put key %s and value %s to %s using Java Map: %s", key, value, map.getClass().getName(), ignored.toString());
        }
        return map.put(key, value);
    }

    public void javaMapClear(Map<?, ?> map) {
        Class<?> cls = map.getClass();
        try {
            MethodHandle mh = JAVA_MAP_CLEAR_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("clear",MethodType.methodType(void.class)));
            if (mh != null) {
                mh.bindTo(map).invokeWithArguments();
                return;
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to clear %s using Java Map: %s", map.getClass().getName(), ignored.toString());
        }
        map.clear();
    }

    // ==============================================================
    // Java Collection 操作
    // ==============================================================

    public <T> boolean javaCollectionRemove(Collection<T> collection, Object element) {
        Class<?> cls = collection.getClass();
        try {
            MethodHandle mh = JAVA_COLLECTION_REMOVE_CACHE.computeIfAbsent(cls,MethodHandleJundingFunction.newInstance("remove",MethodType.methodType(boolean.class, Object.class)));
            if (mh != null) {
                return (boolean) mh.bindTo(collection).invokeWithArguments(element);
            }
        } catch (Throwable ignored) {
            LogUtil.errorf("Failed to remove element %s from %s using Java Collection: %s", element, collection.getClass().getName(), ignored.toString());
        }
        return collection.remove(element);
    }
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
    private static final class MethodHandleJundingFunction implements Function<Class<?>, MethodHandle> {
        private static final Class<? extends Function<Class<?>, MethodHandle>> funcClass= (Class<? extends Function<Class<?>, MethodHandle>>) BytecodeUtil.resolveHiddenClassOrSelf(
                MethodHandleJundingFunction.class,
                String.class,
                MethodType.class
        );
        private static MethodHandle constructor=INSTANCE.ensureConstructor(
                null,
                funcClass,
                MethodHandleJundingFunction.class,
                String.class,
                MethodType.class
        );
        private static Function<Class<?>, MethodHandle> newInstance(String methodName, MethodType methodType){
            constructor=INSTANCE.ensureConstructor(
                    constructor,
                    funcClass,
                    MethodHandleJundingFunction.class,
                    String.class,
                    MethodType.class
            );
            if(constructor!=null){
                try{
                    return (Function<Class<?>, MethodHandle>) constructor.invoke(methodName,methodType);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new MappingFunction. %s",e.getMessage());
                }
            }
            return new MethodHandleJundingFunction(methodName,methodType);
        }
        private final String methodName;
        private final MethodType methodType;

        private MethodHandleJundingFunction(String methodName, MethodType methodType) {
            this.methodName = methodName;
            this.methodType = methodType;
        }

        @Override
        public MethodHandle apply(Class<?> c) {
            try {
                return INSTANCE.getLookup().findVirtual(
                        c,methodName,methodType
                );
            } catch (Throwable e) {
                LogUtil.errorf("failed to find method %s of %s. %s", methodName, c.getName(),e.getMessage());
            }
            return null;
        }
    }
}
