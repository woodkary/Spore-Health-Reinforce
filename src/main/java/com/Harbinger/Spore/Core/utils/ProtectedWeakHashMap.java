package com.Harbinger.Spore.Core.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * @author karywoodOyo
 */
public final class ProtectedWeakHashMap<K, V> extends WeakHashMap<K, V> {
    public static final Class<? extends Map<?,?>> weakHashMapClass;
    public static MethodHandle constructorNoArgs;
    public static MethodHandle constructorWithCapacity;
    static {
        weakHashMapClass=(Class<? extends Map<?, ?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                ProtectedWeakHashMap.class
        );
        constructorNoArgs= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorNoArgs,
                weakHashMapClass,
                ProtectedWeakHashMap.class
        );
        constructorWithCapacity=MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorWithCapacity,
                weakHashMapClass,
                ProtectedWeakHashMap.class,
                int.class
        );
    }
    public static <K,V> Map<K,V> newInstance(){
        MethodHandle ctor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorNoArgs,
                weakHashMapClass,
                ProtectedWeakHashMap.class
        );
        try{
            if (ctor != null) {
                return (Map<K,V>) ctor.invoke();
            }
        }catch (Throwable t){
            LogUtil.errorf("failed to init ProtectedWeakHashMap", t);
        }
        return new ProtectedWeakHashMap<>();
    }
    public static <K,V> Map<K,V> newInstance(int initialCapacity){
        MethodHandle ctor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorWithCapacity,
                weakHashMapClass,
                ProtectedWeakHashMap.class,
                int.class
        );
        try{
            if (ctor != null) {
                return (Map<K,V>) ctor.invoke(initialCapacity);
            }
        }catch (Throwable t){
            LogUtil.errorf("failed to init ProtectedWeakHashMap", t);
        }
        return new ProtectedWeakHashMap<>(initialCapacity);
    }
    public ProtectedWeakHashMap() {
    }

    public ProtectedWeakHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ProtectedWeakHashMap(Map<? extends K, ? extends V> m) {
        super(m);
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
