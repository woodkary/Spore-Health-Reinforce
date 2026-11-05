package com.Harbinger.Spore.Core.utils.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.*;
import java.util.function.Consumer;

/** Shared named helpers: hidden implementations only cross the stable map interface. */
public final class NoSRPEffectViews {
    private NoSRPEffectViews() {}
    public static final class CheckedEntry implements Map.Entry<MobEffect,MobEffectInstance> {
        private final NoSRPEffectMap map;
        private final MobEffect key;
        public CheckedEntry(NoSRPEffectMap map,MobEffect key) { this.map=map; this.key=key; }
        private void check() { if(!map.containsKey(key)) throw new IllegalStateException("Effect entry is no longer visible"); }
        @Override public MobEffect getKey() { check(); return key; }
        @Override public MobEffectInstance getValue() { check(); return map.get(key); }
        @Override public MobEffectInstance setValue(MobEffectInstance value) { check(); return map.put(key,value); }
        @Override public boolean equals(Object o) { check(); return o instanceof Map.Entry<?,?> e && Objects.equals(key,e.getKey()) && Objects.equals(getValue(),e.getValue()); }
        @Override public int hashCode() { check(); return Objects.hashCode(key)^Objects.hashCode(getValue()); }
        @Override public String toString() { check(); return key+"="+getValue(); }
    }
    public static final class Cursor<T> implements Iterator<T> {
        private final NoSRPEffectMap map;
        private final Map<MobEffect,MobEffectInstance> owner;
        private final Iterator<MobEffect> keys;
        private final int kind;
        private MobEffect candidate,last;
        private boolean pending,removable;
        public Cursor(NoSRPEffectMap map,Map<MobEffect,MobEffectInstance> owner,int kind) {
            this.map=map; this.owner=owner; this.kind=kind; keys=new ArrayList<>(owner.keySet()).iterator();
        }
        @Override public boolean hasNext() {
            if(pending && map.containsKey(candidate)) return true;
            pending=false;
            while(keys.hasNext()) { candidate=keys.next(); if(map.containsKey(candidate)) { pending=true; return true; } }
            return false;
        }
        @SuppressWarnings("unchecked")
        @Override public T next() {
            if(!hasNext()) throw new NoSuchElementException();
            last=candidate; pending=false; removable=true;
            return (T)(kind==0 ? new CheckedEntry(map,last) : kind==1 ? last : map.get(last));
        }
        @Override public void remove() {
            if(!removable) throw new IllegalStateException();
            // Use the owner's iterator, preserving its mutation permissions. Pre-reading
            // this cursor never changes the entry targeted for removal.
            Iterator<Map.Entry<MobEffect,MobEffectInstance>> it=owner.entrySet().iterator();
            while(it.hasNext()) { Map.Entry<MobEffect,MobEffectInstance> e=it.next();
                if(Objects.equals(last,e.getKey())) { if(map.containsKey(last)) it.remove(); break; }
            }
            removable=false;
        }
    }
    public static final class Unsplit<T> implements Spliterator<T> {
        private final Iterator<T> iterator;
        public Unsplit(Iterator<T> iterator) { this.iterator=iterator; }
        @Override public boolean tryAdvance(Consumer<? super T> action) { Objects.requireNonNull(action); if(!iterator.hasNext()) return false; action.accept(iterator.next()); return true; }
        @Override public void forEachRemaining(Consumer<? super T> action) { iterator.forEachRemaining(action); }
        @Override public Spliterator<T> trySplit() { return null; }
        @Override public long estimateSize() { return Long.MAX_VALUE; }
        @Override public int characteristics() { return 0; }
    }
    public abstract static class ViewSet<T> extends AbstractSet<T> {
        protected final NoSRPEffectMap map;
        protected final Map<MobEffect,MobEffectInstance> owner;
        private final int kind;
        public ViewSet(NoSRPEffectMap map,Map<MobEffect,MobEffectInstance> owner,int kind) { this.map=map; this.owner=owner; this.kind=kind; }
        @Override public Iterator<T> iterator() { return new Cursor<>(map,owner,kind); }
        @Override public Spliterator<T> spliterator() { return new Unsplit<>(iterator()); }
        @Override public int size() { int n=0; Iterator<T> it=iterator(); while(it.hasNext()) { it.next(); n++; } return n; }
        @Override public void clear() { map.clear(); }
        @Override public boolean add(T value) { throw new UnsupportedOperationException(); }
        @Override public boolean addAll(Collection<? extends T> values) { throw new UnsupportedOperationException(); }
        @Override public boolean removeAll(Collection<?> values) {
            Objects.requireNonNull(values); boolean changed=false; Iterator<T> it=iterator();
            while(it.hasNext()) if(values.contains(it.next())) { it.remove(); changed=true; } return changed;
        }
    }
    public static final class Entries extends ViewSet<Map.Entry<MobEffect,MobEffectInstance>> {
        public Entries(NoSRPEffectMap map,Map<MobEffect,MobEffectInstance> owner) { super(map,owner,0); }
    }
    public static final class Keys extends ViewSet<MobEffect> {
        public Keys(NoSRPEffectMap map,Map<MobEffect,MobEffectInstance> owner) { super(map,owner,1); }
    }
    public static final class Values extends AbstractCollection<MobEffectInstance> {
        private final NoSRPEffectMap map;
        private final Map<MobEffect,MobEffectInstance> owner;
        public Values(NoSRPEffectMap map,Map<MobEffect,MobEffectInstance> owner) { this.map=map; this.owner=owner; }
        @Override public Iterator<MobEffectInstance> iterator() { return new Cursor<>(map,owner,2); }
        @Override public Spliterator<MobEffectInstance> spliterator() { return new Unsplit<>(iterator()); }
        @Override public int size() { return map.size(); }
        @Override public void clear() { map.clear(); }
        @Override public boolean add(MobEffectInstance value) { throw new UnsupportedOperationException(); }
        @Override public boolean addAll(Collection<? extends MobEffectInstance> values) { throw new UnsupportedOperationException(); }
    }
}
