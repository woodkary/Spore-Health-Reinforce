package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

public final class SporeEntityGetter<T extends EntityAccess> extends LevelEntityGetterAdapter<T> {
    public static final Class<? extends LevelEntityGetter<?>> entityGetterClass =
            (Class<? extends LevelEntityGetter<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeEntityGetter.class,
                    LevelEntityGetter.class,
                    EntityLookup.class,
                    EntitySectionStorage.class
            );
    private static MethodHandle constructor;
    static {
        try {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    null,
                    entityGetterClass,
                    SporeEntityGetter.class,
                    LevelEntityGetter.class,
                    EntityLookup.class,
                    EntitySectionStorage.class
            );
        } catch (Throwable t) {
            constructor = null;
            LogUtil.errorf("failed to eager init hidden SporeEntityGetter constructor, %s", t.getMessage());
        }
    }

    public static <T extends EntityAccess> LevelEntityGetter<T> newInstance(LevelEntityGetter<T> owner,EntityLookup<T> lookup,EntitySectionStorage<T> sectionStorage) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entityGetterClass,
                SporeEntityGetter.class,
                LevelEntityGetter.class,
                EntityLookup.class,
                EntitySectionStorage.class
        );
        if (constructor != null) {
            try {
                return (LevelEntityGetter<T>) constructor.invoke(owner,lookup,sectionStorage);
            } catch (Throwable t) {
                LogUtil.errorf("failed to create hidden SporeEntityGetter, %s", t.getMessage());
            }
        }
        return new SporeEntityGetter<>(owner,lookup,sectionStorage);
    }

    private final LevelEntityGetter<T> owner;
    public SporeEntityGetter(LevelEntityGetter<T> owner,EntityLookup<T> lookup,EntitySectionStorage<T> sectionStorage) {
        super(lookup,sectionStorage);
        this.owner = owner;
    }
    @Override
    public @Nullable T get(int id) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(id)){
            return null;
        }
        T entity;
        try {
            entity=owner.get(id);
        }catch (Throwable ignored){
            entity=super.get(id);
        }
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return null;
        }
        return entity;
    }

    @Override
    public @Nullable T get(UUID uuid) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(uuid)){
            return null;
        }
        T entity;
        try {
            entity=owner.get(uuid);
        }catch (Throwable ignored){
            entity=super.get(uuid);
        }
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return null;
        }
        return entity;
    }

    @Override
    public Iterable<T> getAll() {
        try {
            return FilteredIterable.newInstance(owner.getAll());
        }catch (Throwable ignored){}
        return FilteredIterable.newInstance(super.getAll());
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
        try {
            owner.get(entityTypeTest, abortableIterationConsumer);
        }catch (Throwable ignored){}
        super.get(entityTypeTest, abortableIterationConsumer);
    }

    @Override
    public void get(AABB aabb, Consumer<T> consumer) {
        try {
            owner.get(aabb, consumer);
        }catch (Throwable ignored){}
        super.get(aabb, consumer);
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aabb, AbortableIterationConsumer<U> abortableIterationConsumer) {
        try {
            owner.get(entityTypeTest, aabb, abortableIterationConsumer);
        }catch (Throwable ignored){}
        super.get(entityTypeTest, aabb, abortableIterationConsumer);
    }

    private static final class FilteredIterable<E extends EntityAccess> implements Iterable<E> {
        private static final Class<? extends Iterable<?>> iterableClass =
                (Class<? extends Iterable<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        FilteredIterable.class,
                        Iterable.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iterableClass,
                        FilteredIterable.class,
                        Iterable.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden FilteredIterable constructor, %s", t.getMessage());
            }
        }

        public static <E extends EntityAccess> Iterable<E> newInstance(Iterable<E> source) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iterableClass,
                    FilteredIterable.class,
                    Iterable.class
            );
            if (constructor != null) {
                try {
                    return (Iterable<E>) constructor.invoke(source);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden FilteredIterable, %s", t.getMessage());
                }
            }
            return new FilteredIterable<>(source);
        }

        private final boolean isEmpty;
        private final Iterable<E> source;
        private FilteredIterable(Iterable<E> source) {
            this.isEmpty=source==null;
            this.source = source;
        }

        @Override
        public Iterator<E> iterator() {
            return !isEmpty ? FilteredIterator.newInstance(source.iterator()) : EmptyIterator.newInstance();
        }
    }

    private static final class FilteredIterator<E extends EntityAccess> implements Iterator<E> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        FilteredIterator.class,
                        Iterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        FilteredIterator.class,
                        Iterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden FilteredIterator constructor, %s", t.getMessage());
            }
        }

        public static <E extends EntityAccess> Iterator<E> newInstance(Iterator<E> delegate) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    FilteredIterator.class,
                    Iterator.class
            );

            if (constructor != null) {
                try {
                    return (Iterator<E>) constructor.invoke(delegate);
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden FilteredIterator, %s", t.getMessage());
                }
            }
            return new FilteredIterator<>(delegate);
        }

        private final Iterator<E> delegate;
        private E next;
        private boolean nextReady;

        private FilteredIterator(Iterator<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            if (nextReady) {
                return true;
            }
            while (delegate.hasNext()) {
                E candidate = delegate.next();
                if (candidate == null || SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(candidate)) {
                    continue;
                }
                next = candidate;
                nextReady = true;
                return true;
            }
            return false;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E result = next;
            next = null;
            nextReady = false;
            return result;
        }
    }

    private static final class EmptyIterator<E> implements Iterator<E> {
        private static final Class<? extends Iterator<?>> emptyIteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        EmptyIterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        constructor,
                        emptyIteratorClass,
                        EmptyIterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden EmptyIterator constructor, %s", t.getMessage());
            }
        }

        public static <E> Iterator<E> newInstance() {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    emptyIteratorClass,
                    EmptyIterator.class
            );

            if (constructor != null) {
                try {
                    return (Iterator<E>) constructor.invoke();
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden EmptyIterator, %s", t.getMessage());
                }
            }
            return new EmptyIterator<>();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }
    }
}