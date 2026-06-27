package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.Predicate;

public final class SporeEntityByUuidMap<T extends EntityAccess> extends ProtectedUUIDHashMapBase<T> implements ISporeEntityStorage {
    private static final Class<? extends HashMap<UUID,? extends EntityAccess>> mapClass= (Class<? extends HashMap<UUID,? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityByUuidMap.class
    );
    private static MethodHandle noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByUuidMap.class
    );
    private static MethodHandle mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByUuidMap.class,
            Map.class
    );
    public static <V extends EntityAccess> Map<UUID,V> newInstance(){
        noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                mapClass,
                SporeEntityByUuidMap.class
        );
        if(noArg!=null){
            try{
                return (Map<UUID,V>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByUuidMap instance. %s", t.getMessage());
            }
        }
        return new SporeEntityByUuidMap<>();
    }
    public static <V extends EntityAccess> Map<UUID,V> newInstance(Map<? extends UUID, ? extends V> m){
        mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                mapArg,
                mapClass,
                SporeEntityByUuidMap.class,
                Map.class
        );
        if(mapArg!=null){
            try{
                return (Map<UUID,V>) mapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByUuidMap instance. %s", t.getMessage());
            }
        }
        return new SporeEntityByUuidMap<>(m);
    }
    private transient ProtectedEntrySet<T> protectedEntries;
    private transient Set<UUID> protectedKeys;
    private transient Collection<T> protectedValues;
    public SporeEntityByUuidMap() {
    }

    public SporeEntityByUuidMap(Map<? extends UUID, ? extends T> m) {
        super(m);
    }

    @Override
    public T remove(Object key) {
        T res=super.remove(key);
        if(res instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special&&!special.isSpecialDefasd()&&StackTraceUtil.isCallFromOther()){
            //SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            DamageSource source = liv.lastDamageSource != null ? liv.lastDamageSource : liv.damageSources().cactus();
            special.specialDie(source);
            HeasdalthUtil.INSTANCE.genericDie(liv, source);
        }
        return res;
    }
    @Override
    public T getOrDefault(Object k, @Nullable T defaultValue) {
        return SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)?
                defaultValue:
                super.getOrDefault(k, defaultValue);
    }

    @Override
    public T get(Object k) {
        return SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)?
                null:
                super.get(k);
    }

    @Override
    public boolean containsKey(Object k) {
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)&&
                super.containsKey(k);
    }

    @Override
    public Set<Entry<UUID, T>> entrySet() {
        if (protectedEntries == null) {
            protectedEntries = new ProtectedEntrySet<>(this, super.entrySet());
        }
        return protectedEntries;
    }

    @Override
    public boolean containsValue(Object v) {
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(v)&&super.containsValue(v);
    }

    @Override
    public void putAll(Map<? extends UUID, ? extends T> m) {
        if (m instanceof ProtectedUUIDHashMapBase<?> protectedMap) {
            for (Entry<UUID, ?> entry : protectedMap.entrySet()) {
                UUID key1 = entry.getKey();
                Object value1 = entry.getValue();
                if(!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key1)&&!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(value1)){
                    super.put(key1, (T) value1);
                }
            }
            return;
        }
        for (Entry<? extends UUID, ? extends T> entry : m.entrySet()) {
            UUID key1 = entry.getKey();
            T value1 = entry.getValue();
            if(!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key1)&&!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(value1)){
                super.put(key1, value1);
            }
        }

    }
    @Override
    public T put(UUID key, T value) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key)){
            return value;
        }
        return super.put(key, value);
    }
    private static final class ProtectedEntrySet<T extends EntityAccess> extends AbstractSet<Entry<UUID, T>> {
        private final ProtectedUUIDHashMapBase<T> owner;
        private final Set<Entry<UUID, T>> delegate;

        ProtectedEntrySet(ProtectedUUIDHashMapBase<T> owner, Set<Entry<UUID, T>> delegate) {
            this.owner = owner;
            this.delegate = delegate;
        }

        @Override
        public Iterator<Entry<UUID, T>> iterator() {
            return ProtectedEntryIterator.newInstance(owner, delegate.iterator());
        }

        @Override
        public int size() {
            int count = 0;
            Iterator<Entry<UUID, T>> it = iterator();
            while (it.hasNext()) {
                it.next();
                count++;
            }
            return count;
        }

        @Override
        public void clear() {
            Iterator<Entry<UUID, T>> it = iterator();
            while (it.hasNext()) {
                it.next();
                it.remove(); // 走我们自己的 remove
            }
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?, ?> e)) return false;
            return delegate.remove(o);
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<UUID, T>> filter) {
            boolean modified = false;
            Iterator<Entry<UUID, T>> it = iterator();
            while (it.hasNext()) {
                Entry<UUID, T> e = it.next();
                if (filter.test(e)) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }
    }



    private static final class ProtectedEntryIterator<T extends EntityAccess> implements Iterator<Entry<UUID, T>> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedEntryIterator.class,
                        ProtectedUUIDHashMapBase.class,
                        Iterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        ProtectedEntryIterator.class,
                        ProtectedUUIDHashMapBase.class,
                        Iterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden ProtectedEntryIterator constructor, %s", t.getMessage());
            }
        }

        public static <T extends EntityAccess> Iterator<Entry<UUID, T>> newInstance(
                ProtectedUUIDHashMapBase<T> owner,
                Iterator<Entry<UUID, T>> delegate
        ) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    null,
                    iteratorClass,
                    ProtectedEntryIterator.class,
                    ProtectedUUIDHashMapBase.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (Iterator<Entry<UUID, T>>) constructor.invoke(owner, delegate);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedEntryIterator, %s", t.getMessage());
                }
            }
            return new ProtectedEntryIterator<>(owner, delegate);
        }

        private final ProtectedUUIDHashMapBase<T> owner;
        private final Iterator<Entry<UUID, T>> delegate;
        private Entry<UUID, T> last;
        private Entry<UUID, T> nextCandidate;
        private boolean nextReady;

        ProtectedEntryIterator(ProtectedUUIDHashMapBase<T> owner, Iterator<Entry<UUID, T>> delegate) {
            this.owner = owner;
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            if (nextReady) {
                return true;
            }
            while (delegate.hasNext()) {
                Entry<UUID, T> e = delegate.next();
                if (owner.shouldExposeValue(e.getValue())) {
                    nextCandidate = e;
                    nextReady = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Entry<UUID, T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            last = nextCandidate;
            nextCandidate = null;
            nextReady = false;
            return last;
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
            delegate.remove();
            last = null;
        }
    }

    @Override
    public Set<UUID> keySet() {
        if (protectedKeys == null) {
            protectedKeys = new ProtectedKeySet<>(this);
        }
        return protectedKeys;
    }


    private static final class ProtectedKeySet<V extends EntityAccess> extends AbstractSet<UUID> {
        private final ProtectedUUIDHashMapBase<V> owner;

        private ProtectedKeySet(ProtectedUUIDHashMapBase<V> owner) {
            this.owner = owner;
        }

        @Override
        public Iterator<UUID> iterator() {
            return ProtectedKeyIterator.newInstance(owner.entrySet().iterator());
        }

        @Override
        public int size() {
            int count = 0;
            Iterator<UUID> it = iterator();
            while (it.hasNext()) {
                it.next();
                count++;
            }
            return count;
        }
    }

    private static final class ProtectedKeyIterator<T extends EntityAccess> implements Iterator<UUID> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedKeyIterator.class,
                        Iterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        ProtectedKeyIterator.class,
                        Iterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden ProtectedKeyIterator constructor, %s", t.getMessage());
            }
        }

        public static <T extends EntityAccess> Iterator<UUID> newInstance(Iterator<Entry<UUID, T>> entryIt) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedKeyIterator.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (Iterator<UUID>) constructor.invoke(entryIt);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedKeyIterator, %s", t.getMessage());
                }
            }
            return new ProtectedKeyIterator<>(entryIt);
        }

        private final Iterator<Entry<UUID, T>> entryIt;
        private Entry<UUID, T> last;

        private ProtectedKeyIterator(Iterator<Entry<UUID, T>> entryIt) {
            this.entryIt = entryIt;
        }

        @Override
        public boolean hasNext() {
            return entryIt.hasNext();
        }

        @Override
        public UUID next() {
            last = entryIt.next();
            return last.getKey();
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
            entryIt.remove();
            last = null;
        }
    }

    @Override
    public Collection<T> values() {
        if (protectedValues == null) {
            protectedValues = new ProtectedValuesView<>(this);
        }
        return protectedValues;
    }

    private static final class ProtectedValuesView<T extends EntityAccess> extends AbstractCollection<T> {
        private final ProtectedUUIDHashMapBase<T> owner;

        private ProtectedValuesView(ProtectedUUIDHashMapBase<T> owner) {
            this.owner = owner;
        }

        @Override
        public Iterator<T> iterator() {
            return ProtectedValuesIterator.newInstance(owner.entrySet().iterator());
        }

        @Override
        public int size() {
            int count = 0;
            Iterator<T> it = iterator();
            while (it.hasNext()) {
                it.next();
                count++;
            }
            return count;
        }
    }

    private static final class ProtectedValuesIterator<T extends EntityAccess> implements Iterator<T> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedValuesIterator.class,
                        Iterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        ProtectedValuesIterator.class,
                        Iterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden ProtectedValuesIterator constructor, %s", t.getMessage());
            }
        }

        public static <T extends EntityAccess> Iterator<T> newInstance(Iterator<Entry<UUID, T>> entryIt) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedValuesIterator.class,
                    Iterator.class
            );
            if (constructor != null) {
                try {
                    return (Iterator<T>) constructor.invoke(entryIt);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedValuesIterator, %s", t.getMessage());
                }
            }
            return new ProtectedValuesIterator<>(entryIt);
        }

        private final Iterator<Entry<UUID, T>> entryIt;

        private ProtectedValuesIterator(Iterator<Entry<UUID, T>> entryIt) {
            this.entryIt = entryIt;
        }

        @Override
        public boolean hasNext() {
            return entryIt.hasNext();
        }

        @Override
        public T next() {
            return entryIt.next().getValue();
        }

        @Override
        public void remove() {
            entryIt.remove();
        }
    }
}
