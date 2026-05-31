package com.Harbinger.Spore.Core.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author karywoodOyo
 */
public final class ProtectedConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
    public static final Class<? extends ConcurrentMap<?,?>> concurrentMapClass;
    public static MethodHandle constructorNoArgs;
    public static MethodHandle constructorWithCapacity;
    static {
        concurrentMapClass=(Class<? extends ConcurrentMap<?, ?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedConcurrentHashMap.class
        );
        constructorNoArgs= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorNoArgs,
                concurrentMapClass,
                ProtectedConcurrentHashMap.class
        );
        constructorWithCapacity=MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorWithCapacity,
                concurrentMapClass,
                ProtectedConcurrentHashMap.class,
                int.class
        );
    }
    public static <K,V> ConcurrentMap<K,V> newInstance(){
        MethodHandle ctor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorNoArgs,
                concurrentMapClass,
                ProtectedConcurrentHashMap.class
        );
        try{
            if (ctor != null) {
                return (ConcurrentMap<K,V>) ctor.invoke();
            }
        }catch (Throwable t){
            LogUtil.errorf("failed to init ProtectedConcurrentHashMap", t);
        }
        return new ProtectedConcurrentHashMap<>();
    }
    public static <K,V> ConcurrentMap<K,V> newInstance(int initialCapacity){
        MethodHandle ctor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorWithCapacity,
                concurrentMapClass,
                ProtectedConcurrentHashMap.class,
                int.class
        );
        try{
            if (ctor != null) {
                return (ConcurrentMap<K,V>) ctor.invoke(initialCapacity);
            }
        }catch (Throwable t){
            LogUtil.errorf("failed to init ProtectedConcurrentHashMap", t);
        }
        return new ProtectedConcurrentHashMap<>(initialCapacity);
    }

    public ProtectedConcurrentHashMap() {
        super();
    }
    public ProtectedConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void clear() {
        if(StackTraceUtil.isCallFromOther()){
            return;
        }
        super.clear();
    }

    @Override
    public V put(@NotNull K key, @NotNull V value) {
        return StackTraceUtil.isCallFromOther()? value : super.put(key, value);
    }
    @Override
    public V putIfAbsent(@NotNull K key, @NotNull V value) {
        return StackTraceUtil.isCallFromOther()? value : super.putIfAbsent(key, value);
    }
    @Override
    public V remove(@NotNull Object key) {
        return StackTraceUtil.isCallFromOther()? this.get(key) : super.remove(key);
    }
    @Override
    public boolean remove(@NotNull Object key, @NotNull Object value) {
        return !StackTraceUtil.isCallFromOther() && super.remove(key, value);
    }
    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        return StackTraceUtil.isCallFromOther()? value : super.replace(key, value);
    }
    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        return !StackTraceUtil.isCallFromOther() && super.replace(key, oldValue, newValue);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return StackTraceUtil.isCallFromOther()? mappingFunction.apply(key) : super.computeIfAbsent(key, mappingFunction);
    }
}
