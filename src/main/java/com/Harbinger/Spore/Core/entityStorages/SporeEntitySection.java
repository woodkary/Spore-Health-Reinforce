package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author karywoodOyo
 */
public final class SporeEntitySection<T extends EntityAccess> extends EntitySection<T> {
    public SporeEntitySection(EntitySection<T> owner){
        super(owner.storage.baseClass, owner.getStatus());
        this.storage = owner.storage;
        updateChunkStatus(owner.getStatus());
    }
    public SporeEntitySection(Class<T> clazz, Visibility p_156832_) {
        super(clazz, p_156832_);
        this.storage=new ProtectedClassInstanceMultiMap<>(clazz);
    }
    @Override
    public void add(T entity) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return;
        }
        this.storage.add(entity);
    }
    @Override
    public boolean remove(T entity) {
        return this.storage.remove(entity);
    }
    @Override
    public AbortableIterationConsumer.Continuation getEntities(
            AABB box,
            AbortableIterationConsumer<T> consumer
    ) {
        Predicate<T> filter= e -> e.getBoundingBox().intersects(box);
        // -------- phase 1: storage --------
        for (T entity : this.storage) {
            if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) continue;
            if (!filter.test(entity)) continue;

            AbortableIterationConsumer.Continuation res=AbortableIterationConsumer.Continuation.ABORT;
            try{
                res=consumer.accept(entity);
            }catch (Throwable t){
                LogUtil.errorf("error in calling consumer for entity %s, error: %s",entity,t.getMessage());
            }
            if (res.shouldAbort()) {
                return AbortableIterationConsumer.Continuation.ABORT;
            }
        }

        return AbortableIterationConsumer.Continuation.CONTINUE;
    }

    @Override
    public <U extends T> AbortableIterationConsumer.Continuation getEntities(
            EntityTypeTest<T, U> test,
            AABB box,
            AbortableIterationConsumer<? super U> consumer0
    ){
        Predicate<T> filter= e -> {
            if (!test.getBaseClass().isInstance(e)) return false;
            U casted = test.tryCast(e);
            return casted != null && e.getBoundingBox().intersects(box);
        };
        AbortableIterationConsumer<T> consumer= e -> consumer0.accept(test.tryCast(e));
        // -------- phase 1: storage --------
        for (T entity : this.storage) {
            if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) continue;
            if (!filter.test(entity)) continue;
            AbortableIterationConsumer.Continuation res=AbortableIterationConsumer.Continuation.ABORT;
            try{
                res=consumer.accept(entity);
            }catch (Throwable t){
                LogUtil.errorf("error in calling consumer for entity %s, error: %s",entity,t.getMessage());
            }
            if (res.shouldAbort()) {
                return AbortableIterationConsumer.Continuation.ABORT;
            }
        }

        return AbortableIterationConsumer.Continuation.CONTINUE;
    }
    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }
    @Override
    public Stream<T> getEntities() {
        Iterable<T> iterable = () -> new Iterator<>() {

            private final Iterator<T> storageIt = storage.iterator();

            private T next;

            @Override
            public boolean hasNext() {
                if (next != null) return true;

                while (storageIt.hasNext()) {
                    T e = storageIt.next();
                    if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(e)) {
                        next = e;
                        return true;
                    }

            }
                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T r = next;
                next = null;
                return r;
            }
        };

        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @VisibleForDebug
    public int size() {
        int count = 0;
        for (T e : this.storage) {
            if (!SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(e)) {
                count++;
            }
        }

        return count;
    }
}

