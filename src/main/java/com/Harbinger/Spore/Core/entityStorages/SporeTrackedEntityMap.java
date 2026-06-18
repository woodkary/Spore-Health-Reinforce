package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.level.ChunkMap;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class SporeTrackedEntityMap extends ProtectedTrackedEntityMapBase implements ISporeEntityStorage {
    private static final Class<? extends Int2ObjectMap<ChunkMap.TrackedEntity>> mapClass =
            (Class<? extends Int2ObjectMap<ChunkMap.TrackedEntity>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeTrackedEntityMap.class
            );
    private static MethodHandle noArg = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeTrackedEntityMap.class
    );
    private static MethodHandle mapArg = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeTrackedEntityMap.class,
            Map.class
    );
    private static MethodHandle i2oMapArg = MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeTrackedEntityMap.class,
            Int2ObjectMap.class
    );

    private transient FastEntrySet<ChunkMap.TrackedEntity> protectedEntries;
    private transient ObjectCollection<ChunkMap.TrackedEntity> protectedValues;
    private transient IntSet protectedKeys;

    public static Int2ObjectMap<ChunkMap.TrackedEntity> newInstance() {
        noArg = MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                mapClass,
                SporeTrackedEntityMap.class
        );
        if (noArg != null) {
            try {
                return (Int2ObjectMap<ChunkMap.TrackedEntity>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeTrackedEntityMap instance", t.getMessage());
            }
        }
        return new SporeTrackedEntityMap();
    }

    public static Int2ObjectMap<ChunkMap.TrackedEntity> newInstance(Map<? extends Integer, ? extends ChunkMap.TrackedEntity> m) {
        mapArg = MethodHandleUtil.INSTANCE.ensureConstructor(
                mapArg,
                mapClass,
                SporeTrackedEntityMap.class,
                Map.class
        );
        if (mapArg != null) {
            try {
                return (Int2ObjectMap<ChunkMap.TrackedEntity>) mapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeTrackedEntityMap instance", t.getMessage());
            }
        }
        return new SporeTrackedEntityMap(m);
    }

    public static Int2ObjectMap<ChunkMap.TrackedEntity> newInstance(Int2ObjectMap<ChunkMap.TrackedEntity> m) {
        i2oMapArg = MethodHandleUtil.INSTANCE.ensureConstructor(
                i2oMapArg,
                mapClass,
                SporeTrackedEntityMap.class,
                Int2ObjectMap.class
        );
        if (i2oMapArg != null) {
            try {
                return (Int2ObjectMap<ChunkMap.TrackedEntity>) i2oMapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeTrackedEntityMap instance", t.getMessage());
            }
        }
        return new SporeTrackedEntityMap(m);
    }

    public SporeTrackedEntityMap() {
    }

    public SporeTrackedEntityMap(Map<? extends Integer, ? extends ChunkMap.TrackedEntity> m) {
        super(m);
    }

    public SporeTrackedEntityMap(Int2ObjectMap<ChunkMap.TrackedEntity> m) {
        super(m);
    }

    @Override
    public ChunkMap.TrackedEntity put(int k, ChunkMap.TrackedEntity value) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k) || !shouldExposeValue(value)) {
            return value;
        }
        return super.put(k, value);
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends ChunkMap.TrackedEntity> m) {
        if (m instanceof ProtectedTrackedEntityMapBase trackedEntityMap) {
            ObjectIterator<Entry<ChunkMap.TrackedEntity>> it = trackedEntityMap.superEntryIterator();
            while (it.hasNext()) {
                Entry<ChunkMap.TrackedEntity> entry = it.next();
                int key = entry.getIntKey();
                ChunkMap.TrackedEntity value = entry.getValue();
                if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key) && shouldExposeValue(value)) {
                    super.put(key, value);
                }
            }
            return;
        }
        for (Map.Entry<? extends Integer, ? extends ChunkMap.TrackedEntity> entry : m.entrySet()) {
            int key = entry.getKey();
            ChunkMap.TrackedEntity value = entry.getValue();
            if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(key) && shouldExposeValue(value)) {
                super.put(key, value);
            }
        }
    }

    @Override
    public ChunkMap.TrackedEntity get(int k) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)) {
            return null;
        }
        ChunkMap.TrackedEntity res = super.get(k);
        return shouldExposeValue(res) ? res : null;
    }

    @Override
    public ChunkMap.TrackedEntity getOrDefault(int k, ChunkMap.TrackedEntity defaultValue) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)) {
            return defaultValue;
        }
        ChunkMap.TrackedEntity res = super.get(k);
        return shouldExposeValue(res) ? res : defaultValue;
    }

    @Override
    public boolean containsKey(int k) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(k)) {
            return false;
        }
        ChunkMap.TrackedEntity res = super.get(k);
        return shouldExposeValue(res);
    }

    @Override
    public boolean containsValue(Object value) {
        return value instanceof ChunkMap.TrackedEntity trackedEntity
                && shouldExposeValue(trackedEntity)
                && super.containsValue(value);
    }

    @Override
    public FastEntrySet<ChunkMap.TrackedEntity> int2ObjectEntrySet() {
        if (protectedEntries == null) {
            protectedEntries = new ProtectedEntrySet(this);
        }
        return protectedEntries;
    }

    @Override
    public ObjectCollection<ChunkMap.TrackedEntity> values() {
        if (protectedValues == null) {
            protectedValues = new ProtectedValuesView(this);
        }
        return protectedValues;
    }

    @Override
    public IntSet keySet() {
        if (protectedKeys == null) {
            protectedKeys = new ProtectedKeySet(this);
        }
        return protectedKeys;
    }

    private static final class ProtectedEntrySet
            extends AbstractObjectSet<Entry<ChunkMap.TrackedEntity>>
            implements FastEntrySet<ChunkMap.TrackedEntity> {
        private final ProtectedTrackedEntityMapBase owner;

        private ProtectedEntrySet(ProtectedTrackedEntityMapBase owner) {
            this.owner = owner;
        }

        @Override
        public ObjectIterator<Entry<ChunkMap.TrackedEntity>> iterator() {
            return ProtectedEntryIterator.newInstance(owner, owner.superEntryIterator());
        }

        @Override
        public ObjectIterator<Entry<ChunkMap.TrackedEntity>> fastIterator() {
            return iterator();
        }

        @Override
        public int size() {
            int count = 0;
            ObjectIterator<Entry<ChunkMap.TrackedEntity>> it = owner.superEntryIterator();
            while (it.hasNext()) {
                Entry<ChunkMap.TrackedEntity> entry = it.next();
                if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entry.getIntKey())
                        && owner.shouldExposeValue(entry.getValue())) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public void clear() {
            ObjectIterator<Entry<ChunkMap.TrackedEntity>> it = iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?> entry)) {
                return false;
            }
            return owner.remove(entry.getIntKey()) != null;
        }

        @Override
        public boolean removeIf(Predicate<? super Entry<ChunkMap.TrackedEntity>> filter) {
            boolean changed = false;
            ObjectIterator<Entry<ChunkMap.TrackedEntity>> it = iterator();
            while (it.hasNext()) {
                Entry<ChunkMap.TrackedEntity> entry = it.next();
                if (filter.test(entry)) {
                    it.remove();
                    changed = true;
                }
            }
            return changed;
        }
    }

    private static final class ProtectedEntryIterator implements ObjectIterator<Entry<ChunkMap.TrackedEntity>> {
        private static final Class<? extends ObjectIterator<?>> iteratorClass =
                (Class<? extends ObjectIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedEntryIterator.class,
                        ProtectedTrackedEntityMapBase.class,
                        ObjectIterator.class
                );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedEntryIterator.class,
                ProtectedTrackedEntityMapBase.class,
                ObjectIterator.class
        );

        public static ObjectIterator<Entry<ChunkMap.TrackedEntity>> newInstance(
                ProtectedTrackedEntityMapBase owner,
                ObjectIterator<Entry<ChunkMap.TrackedEntity>> delegate
        ) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedEntryIterator.class,
                    ProtectedTrackedEntityMapBase.class,
                    ObjectIterator.class
            );
            if (constructor != null) {
                try {
                    return (ObjectIterator<Entry<ChunkMap.TrackedEntity>>) constructor.invoke(owner, delegate);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedEntryIterator, %s", t.getMessage());
                }
            }
            return new ProtectedEntryIterator(owner, delegate);
        }

        private final ProtectedTrackedEntityMapBase owner;
        private final ObjectIterator<Entry<ChunkMap.TrackedEntity>> delegate;
        private Entry<ChunkMap.TrackedEntity> lastReturned;
        private Entry<ChunkMap.TrackedEntity> nextCandidate;
        private boolean nextReady;

        private ProtectedEntryIterator(ProtectedTrackedEntityMapBase owner, ObjectIterator<Entry<ChunkMap.TrackedEntity>> delegate) {
            this.owner = owner;
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            if (nextReady) {
                return true;
            }
            while (delegate.hasNext()) {
                Entry<ChunkMap.TrackedEntity> entry = delegate.next();
                if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entry.getIntKey())
                        && owner.shouldExposeValue(entry.getValue())) {
                    nextCandidate = entry;
                    nextReady = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Entry<ChunkMap.TrackedEntity> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = nextCandidate;
            nextCandidate = null;
            nextReady = false;
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            delegate.remove();
            lastReturned = null;
        }
    }

    private static final class ProtectedValuesView extends AbstractObjectCollection<ChunkMap.TrackedEntity> {
        private final ProtectedTrackedEntityMapBase owner;

        private ProtectedValuesView(ProtectedTrackedEntityMapBase owner) {
            this.owner = owner;
        }

        @Override
        public ObjectIterator<ChunkMap.TrackedEntity> iterator() {
            return ProtectedValuesIterator.newInstance(owner.int2ObjectEntrySet().iterator());
        }

        @Override
        public int size() {
            int count = 0;
            ObjectIterator<ChunkMap.TrackedEntity> it = iterator();
            while (it.hasNext()) {
                it.next();
                count++;
            }
            return count;
        }
    }

    private static final class ProtectedValuesIterator implements ObjectIterator<ChunkMap.TrackedEntity> {
        private static final Class<? extends ObjectIterator<?>> iteratorClass =
                (Class<? extends ObjectIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedValuesIterator.class,
                        ObjectIterator.class
                );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedValuesIterator.class,
                ObjectIterator.class
        );

        public static ObjectIterator<ChunkMap.TrackedEntity> newInstance(
                ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt
        ) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedValuesIterator.class,
                    ObjectIterator.class
            );
            if (constructor != null) {
                try {
                    return (ObjectIterator<ChunkMap.TrackedEntity>) constructor.invoke(entryIt);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedValuesIterator, %s", t.getMessage());
                }
            }
            return new ProtectedValuesIterator(entryIt);
        }

        private final ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt;

        private ProtectedValuesIterator(ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt) {
            this.entryIt = entryIt;
        }

        @Override
        public boolean hasNext() {
            return entryIt.hasNext();
        }

        @Override
        public ChunkMap.TrackedEntity next() {
            return entryIt.next().getValue();
        }

        @Override
        public void remove() {
            entryIt.remove();
        }
    }

    private static final class ProtectedKeySet extends AbstractIntSet {
        private final ProtectedTrackedEntityMapBase owner;

        private ProtectedKeySet(ProtectedTrackedEntityMapBase owner) {
            this.owner = owner;
        }

        @Override
        public IntIterator iterator() {
            return ProtectedKeyIterator.newInstance(owner.int2ObjectEntrySet().iterator());
        }

        @Override
        public boolean contains(int k) {
            return owner.containsKey(k);
        }

        @Override
        public boolean remove(int k) {
            return owner.remove(k) != null;
        }

        @Override
        public int size() {
            int count = 0;
            IntIterator it = iterator();
            while (it.hasNext()) {
                it.nextInt();
                count++;
            }
            return count;
        }
    }

    private static final class ProtectedKeyIterator implements IntIterator {
        private static final Class<? extends IntIterator> iteratorClass =
                (Class<? extends IntIterator>) BytecodeUtil.resolveHiddenClassOrSelf(
                        ProtectedKeyIterator.class,
                        ObjectIterator.class
                );
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                iteratorClass,
                ProtectedKeyIterator.class,
                ObjectIterator.class
        );

        public static IntIterator newInstance(ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    ProtectedKeyIterator.class,
                    ObjectIterator.class
            );
            if (constructor != null) {
                try {
                    return (IntIterator) constructor.invoke(entryIt);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden ProtectedKeyIterator, %s", t.getMessage());
                }
            }
            return new ProtectedKeyIterator(entryIt);
        }

        private final ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt;

        private ProtectedKeyIterator(ObjectIterator<Entry<ChunkMap.TrackedEntity>> entryIt) {
            this.entryIt = entryIt;
        }

        @Override
        public boolean hasNext() {
            return entryIt.hasNext();
        }

        @Override
        public int nextInt() {
            return entryIt.next().getIntKey();
        }

        @Override
        public void remove() {
            entryIt.remove();
        }
    }
}
