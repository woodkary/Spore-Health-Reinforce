package com.Harbinger.Spore.Core.utils.effects;

import com.Harbinger.Spore.Compat.srps.SRPJudge;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.*;

/** Live, pair-wise filtering; deliberately does not promise concurrent atomicity. */
public final class NoSRPEffectMapProxy extends AbstractMap<MobEffect, MobEffectInstance> implements NoSRPEffectMap {
    private static final Class<?> HIDDEN_CLASS;
    private static final MethodHandle CONSTRUCTOR;
    static {
        try {
            HIDDEN_CLASS = BytecodeUtil.resolveHiddenClassOrSelf(NoSRPEffectMapProxy.class, Map.class);
            if (!HIDDEN_CLASS.isHidden() || !NoSRPEffectMap.class.isAssignableFrom(HIDDEN_CLASS))
                throw new IllegalStateException("NoSRPEffectMap hidden definition failed: " + HIDDEN_CLASS);
            CONSTRUCTOR = ClassUtil.getLookup().findConstructor(HIDDEN_CLASS, MethodType.methodType(void.class, Map.class));
        } catch (Throwable failure) { throw new ExceptionInInitializerError(failure); }
    }
    public static NoSRPEffectMap newInstance(Map<MobEffect, MobEffectInstance> owner) {
        try { return (NoSRPEffectMap) CONSTRUCTOR.invoke(owner); }
        catch (Throwable failure) { throw new IllegalStateException("Cannot construct hidden NoSRPEffectMap", failure); }
    }
    private final Map<MobEffect, MobEffectInstance> owner;
    public NoSRPEffectMapProxy(Map<MobEffect, MobEffectInstance> owner) { this.owner = Objects.requireNonNull(owner); }
    private static boolean forbidden(Object key, MobEffectInstance value) {
        return key instanceof MobEffect && SRPJudge.isSRPEffect((MobEffect) key)
            || value != null && value.getEffect() != null && SRPJudge.isSRPEffect(value.getEffect());
    }
    @Override public boolean containsKey(Object key) {
        MobEffectInstance value = owner.get(key);
        return !forbidden(key, value) && (value != null || owner.containsKey(key));
    }
    @Override public MobEffectInstance get(Object key) { MobEffectInstance v = owner.get(key); return forbidden(key,v) ? null : v; }
    @Override public MobEffectInstance getOrDefault(Object key, MobEffectInstance fallback) { return containsKey(key) ? get(key) : fallback; }
    @Override public MobEffectInstance put(MobEffect key, MobEffectInstance value) {
        MobEffectInstance old = get(key);
        if (!forbidden(key,value)) owner.put(key,value);
        return old;
    }
    @Override public void putAll(Map<? extends MobEffect, ? extends MobEffectInstance> map) {
        for (Entry<? extends MobEffect, ? extends MobEffectInstance> e : map.entrySet()) put(e.getKey(),e.getValue());
    }
    @Override public MobEffectInstance remove(Object key) {
        if (!containsKey(key)) return null;
        return owner.remove(key);
    }
    @Override public boolean remove(Object key,Object value) {
        return containsKey(key) && Objects.equals(get(key),value) && owner.remove(key,value);
    }
    @Override public void clear() { owner.clear(); }
    @Override public MobEffectInstance putIfAbsent(MobEffect key,MobEffectInstance value) {
        MobEffectInstance old=get(key); if(old==null) put(key,value); return old;
    }
    @Override public MobEffectInstance replace(MobEffect key,MobEffectInstance value) {
        return containsKey(key) ? put(key,value) : null;
    }
    @Override public boolean replace(MobEffect key,MobEffectInstance old,MobEffectInstance value) {
        if(forbidden(key,value) || !containsKey(key) || !Objects.equals(get(key),old)) return false;
        return owner.replace(key,old,value);
    }
    @Override public void replaceAll(BiFunction<? super MobEffect,? super MobEffectInstance,? extends MobEffectInstance> f) {
        Objects.requireNonNull(f);
        for(Entry<MobEffect,MobEffectInstance> e:entrySet()) e.setValue(f.apply(e.getKey(),e.getValue()));
    }
    @Override public MobEffectInstance computeIfAbsent(MobEffect key,Function<? super MobEffect,? extends MobEffectInstance> f) {
        Objects.requireNonNull(f); MobEffectInstance old=get(key); if(old!=null) return old;
        MobEffectInstance value=f.apply(key); if(value==null || forbidden(key,value)) return old;
        put(key,value); return value;
    }
    @Override public MobEffectInstance computeIfPresent(MobEffect key,BiFunction<? super MobEffect,? super MobEffectInstance,? extends MobEffectInstance> f) {
        Objects.requireNonNull(f); MobEffectInstance old=get(key); if(old==null) return null;
        MobEffectInstance value=f.apply(key,old); if(forbidden(key,value)) return old;
        if(value==null) remove(key); else put(key,value); return value;
    }
    @Override public MobEffectInstance compute(MobEffect key,BiFunction<? super MobEffect,? super MobEffectInstance,? extends MobEffectInstance> f) {
        Objects.requireNonNull(f); MobEffectInstance old=get(key), value=f.apply(key,old);
        if(forbidden(key,value)) return old;
        if(value==null) remove(key); else put(key,value); return value;
    }
    @Override public MobEffectInstance merge(MobEffect key,MobEffectInstance value,BiFunction<? super MobEffectInstance,? super MobEffectInstance,? extends MobEffectInstance> f) {
        Objects.requireNonNull(f); Objects.requireNonNull(value); MobEffectInstance old=get(key);
        if(forbidden(key,value)) return old;
        MobEffectInstance result=old==null ? value : f.apply(old,value);
        if(forbidden(key,result)) return old;
        if(result==null) remove(key); else put(key,result); return result;
    }
    @Override public Set<Entry<MobEffect,MobEffectInstance>> entrySet() { return new NoSRPEffectViews.Entries(this,owner); }
    @Override public Set<MobEffect> keySet() { return new NoSRPEffectViews.Keys(this,owner); }
    @Override public Collection<MobEffectInstance> values() { return new NoSRPEffectViews.Values(this,owner); }
}
