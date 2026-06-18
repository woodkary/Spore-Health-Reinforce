package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.HeasdalthUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class SporeEntityByIdMap<V extends EntityAccess> extends ProtectedEntityMapBase<V> implements ISporeEntityStorage {
    private static final Class<? extends Int2ObjectMap<? extends EntityAccess>> mapClass= (Class<? extends Int2ObjectMap<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityByIdMap.class
    );
    private static MethodHandle noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class
    );
    private static MethodHandle mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class,
            Map.class
    );
    private static MethodHandle i2oMapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class,
            Int2ObjectMap.class
    );
    private transient ProtectedEntrySet<V> protectedEntries;
    private transient ObjectCollection<V> protectedValues;
    public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(Int2ObjectMap<V> m){
        i2oMapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                i2oMapArg,
                mapClass,
                SporeEntityByIdMap.class,
                Int2ObjectMap.class
        );
        if(i2oMapArg!=null){
            try{
                return (Int2ObjectMap<V>) i2oMapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>(m);
    }

    public SporeEntityByIdMap() {
    }public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(){
        noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                mapClass,
                SporeEntityByIdMap.class
        );
        if(noArg!=null){
            try{
                return (Int2ObjectMap<V>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>();
    }
    public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(Map<? extends Integer, ? extends V> m){
        mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                mapArg,
                mapClass,
                SporeEntityByIdMap.class,
                Map.class
        );
        if(mapArg!=null){
            try{
                return (Int2ObjectMap<V>) mapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>(m);
    }

    public SporeEntityByIdMap(Map<? extends Integer, ? extends V> m) {
        super(m);
    }

    public SporeEntityByIdMap(Int2ObjectMap<V> m) {
        super(m);
    }

    @Override
    public V remove(int k) {
        V res=super.remove(k);
        if(res instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special&&!special.isSpecialDead()){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            DamageSource source = liv.lastDamageSource != null ? liv.lastDamageSource : liv.damageSources().cactus();
            special.specialDie(source);
            HeasdalthUtil.INSTANCE.genericDie(liv, source);
        }
        return res;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> m) {
        if (m instanceof ProtectedEntityMapBase protectedMap) {
            ObjectBidirectionalIterator<Entry<V>> it = protectedMap.superEntryIterator();
            while (it.hasNext()) {
                Int2ObjectMap.Entry<V> entry = it.next();
                int key1 = entry.getIntKey();
                V value1 = entry.getValue();
                if(!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key1)&&!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(value1)){
                    super.put(key1, value1);
                }
            }
            return;
        }
        for (Map.Entry<? extends Integer, ? extends V> entry : m.entrySet()) {
            int key1 = entry.getKey();
            V value1 = entry.getValue();
            if(!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key1)&&!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(value1)){
                super.put(key1, value1);
            }
        }
    }
    @Override
    public V put(int k, V t) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k) || SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(t)) {
            return t;
        }
        return super.put(k, t);
    }
    @Override
    public FastSortedEntrySet<V> int2ObjectEntrySet() {
        if (protectedEntries == null) {
            protectedEntries = new ProtectedEntrySet<>(this);
        }
        return protectedEntries;
    }

    // ============================================================
    //                 ProtectedEntrySet
    // ============================================================
    private static final class ProtectedEntrySet<V>
            extends AbstractObjectSet<Entry<V>>
            implements FastSortedEntrySet<V> {
        private final ProtectedEntityMapBase<V> owner;

        private ProtectedEntrySet(ProtectedEntityMapBase<V> owner) {
            this.owner = owner;
        }

        @Override
        public ObjectBidirectionalIterator<Entry<V>> iterator(Entry<V> entry) {
            // LinkedOpenHashMap 不支持从指定 entry 开始的迭代
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectBidirectionalIterator<Entry<V>> iterator() {
            return ProtectedIterator.newInstance(owner, owner.superEntryIterator());
        }

        @Override
        public int size() {
            int count = 0;
            ObjectBidirectionalIterator<Entry<V>> it = owner.superEntryIterator();
            while (it.hasNext()) {
                Entry<V> e = it.next();
                if (owner.shouldExposeValue(e.getValue())) {
                    count++;
                }
            }
            return count;
        }

        @Nullable
        @Override
        public Comparator<? super Entry<V>> comparator() {
            // LinkedOpenHashMap 是插入顺序，不支持排序
            return null;
        }

        @Override
        public ObjectSortedSet<Entry<V>> subSet(Entry<V> from, Entry<V> to) {
            throw new UnsupportedOperationException("subSet not supported.");
        }

        @Override
        public ObjectSortedSet<Entry<V>> headSet(Entry<V> to) {
            throw new UnsupportedOperationException("headSet not supported.");
        }

        @Override
        public ObjectSortedSet<Entry<V>> tailSet(Entry<V> from) {
            throw new UnsupportedOperationException("tailSet not supported.");
        }

        @Override
        public Entry<V> first() {
            if (owner.isEmpty()) {
                throw new NoSuchElementException();
            }
            int first = owner.firstIntKey();
            return new BasicEntry<>(first, owner.get(first));
        }

        @Override
        public Entry<V> last() {
            if (owner.isEmpty()) {
                throw new NoSuchElementException();
            }
            int last = owner.lastIntKey();
            return new BasicEntry<>(last, owner.get(last));
        }

        @Override
        public ObjectBidirectionalIterator<Entry<V>> fastIterator() {
            return iterator();
        }

        @Override
        public ObjectBidirectionalIterator<Entry<V>> fastIterator(Entry<V> entry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?> e)) return false;
            int key = e.getIntKey();
            return owner.remove(key) != null;
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<V>> filter) {
            boolean changed = false;
            ObjectIterator<Entry<V>> it = iterator();

            while (it.hasNext()) {
                Entry<V> entry = it.next();
                if (filter.test(entry)) {
                    it.remove();
                    changed = true;
                }
            }
            return changed;
        }
    }



    // ============================================================
    //                 ProtectedIterator
    // ============================================================
    private static final class ProtectedIterator<V> implements ObjectBidirectionalIterator<Entry<V>> {
        private static final Class<? extends ObjectBidirectionalIterator<?>> iteratorClass =
                (Class<? extends ObjectBidirectionalIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedIterator.class,
                        ProtectedEntityMapBase.class,
                        ObjectBidirectionalIterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        ProtectedIterator.class,
                        ProtectedEntityMapBase.class,
                        ObjectBidirectionalIterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden ProtectedIterator constructor, %s", t.getMessage());
            }
        }

        public static <V> ObjectBidirectionalIterator<Entry<V>> newInstance(
                ProtectedEntityMapBase<V> owner,
                ObjectBidirectionalIterator<Entry<V>> delegate
        ) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedIterator.class,
                    ProtectedEntityMapBase.class,
                    ObjectBidirectionalIterator.class
            );
            if (constructor != null) {
                try {
                    return (ObjectBidirectionalIterator<Entry<V>>) constructor.invoke(owner, delegate);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedIterator, %s", t.getMessage());
                }
            }
            return new ProtectedIterator<>(owner, delegate);
        }

        private final ProtectedEntityMapBase<V> owner;

        private final ObjectBidirectionalIterator<Entry<V>> delegate;

        private Entry<V> lastReturned;
        private Entry<V> nextCandidate;
        private Entry<V> previousCandidate;
        private boolean nextReady;
        private boolean previousReady;

        ProtectedIterator(ProtectedEntityMapBase<V> owner, ObjectBidirectionalIterator<Entry<V>> delegate) {
            this.owner = owner;
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            if (nextReady) {
                return true;
            }
            while (delegate.hasNext()) {
                Entry<V> e = delegate.next();
                if (owner.shouldExposeValue(e.getValue())) {
                    nextCandidate = e;
                    nextReady = true;
                    previousReady = false;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Entry<V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = nextCandidate;
            nextCandidate = null;
            nextReady = false;
            return lastReturned;
        }

        @Override
        public boolean hasPrevious() {
            if (previousReady) {
                return true;
            }
            while (delegate.hasPrevious()) {
                Entry<V> e = delegate.previous();
                if (owner.shouldExposeValue(e.getValue())) {
                    previousCandidate = e;
                    previousReady = true;
                    nextReady = false;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Entry<V> previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            lastReturned = previousCandidate;
            previousCandidate = null;
            previousReady = false;
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            delegate.remove();
            lastReturned = null;
        }
    }
}
