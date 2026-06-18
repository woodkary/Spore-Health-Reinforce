package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author karywoodOyo
 */
public final class SporeEntityLookup<T extends EntityAccess> extends EntityLookup<T> implements ISporeEntityStorage {
    public static final Class<? extends EntityLookup<? extends EntityAccess>> entityLookupClass= (Class<? extends EntityLookup<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityLookup.class
    );
    private static MethodHandle constructor;
    static {
        try {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    null,
                    entityLookupClass,
                    SporeEntityLookup.class
            );
        } catch (Throwable t) {
            constructor = null;
            LogUtil.errorf("failed to eager init hidden EntityLookup constructor, %s", t.getMessage());
        }
    }
    public static <T extends EntityAccess> EntityLookup<T> newInstance(){
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                entityLookupClass,
                SporeEntityLookup.class
        );
        try{
            if (constructor != null) {
                return (EntityLookup<T>) constructor.invoke();
            }
        }catch (Throwable t){
            LogUtil.errorf("failed to init EntityLookup, %s",t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return new SporeEntityLookup<>();
    }
    public static <T extends EntityAccess> Future<EntityLookup<T>> replaceEntityLookup(EntityLookup<T> original){
        if (original == null) {
            return null;
        }

        // Do not replace third-party subclasses: call sites may resolve owners to that
        // subclass (e.g. add(Entity)/remove(Entity) descriptors), and swapping receiver
        // type can break invokevirtual resolution.
        if (original.getClass() == EntityLookup.class) {
            KlassPointerUtil.INSTANCE.replaceClass(original,entityLookupClass,"",0,0.0f);
        }
        if (!(original.byId instanceof SporeEntityByIdMap)) {
            original.byId = SporeEntityByIdMap.newInstance(original.byId);
        }
        if (!(original.byUuid instanceof SporeEntityByUuidMap)) {
            original.byUuid = SporeEntityByUuidMap.newInstance(original.byUuid);
        }
        return null;
    }
    public SporeEntityLookup() {
        this.byId = SporeEntityByIdMap.newInstance();
        this.byUuid = SporeEntityByUuidMap.newInstance();
    }
    public static <T extends EntityAccess> EntityLookup<T> copy(EntityLookup<T> lookup){
        if (lookup == null) {
            return null;
        }

        // Always harden backing maps in-place. Entity managers keep a separate
        // LevelEntityGetterAdapter that points at this lookup object; replacing the
        // lookup instance leaves that getter reading the stale storage.
        try {
            if (!(lookup.byId instanceof SporeEntityByIdMap)) {
                lookup.byId = SporeEntityByIdMap.newInstance(lookup.byId);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to harden EntityLookup.byId for %s: %s",
                    lookup.getClass().getName(), t.getMessage());
        }
        try {
            if (!(lookup.byUuid instanceof SporeEntityByUuidMap)) {
                lookup.byUuid = SporeEntityByUuidMap.newInstance(lookup.byUuid);
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to harden EntityLookup.byUuid for %s: %s",
                    lookup.getClass().getName(), t.getMessage());
        }

        if (lookup.getClass() == entityLookupClass || lookup instanceof SporeEntityLookup) {
            return lookup;
        }

        // Only replace vanilla base EntityLookup in-place. For custom subclasses
        // we keep receiver type, otherwise invokevirtual sites whose owner is
        // that subclass can fail with AbstractMethodError.
        if (lookup.getClass() != EntityLookup.class) {
            return lookup;
        }

        KlassPointerUtil.INSTANCE.replaceClass(lookup, entityLookupClass, "", 0, 0.0f);
        return lookup;
    }
    @Override
    public void remove(EntityAccess entity) {
        this.byUuid.remove(entity.getUUID());
        this.byId.remove(entity.getId());
    }
    @Override
    public Iterable<T> getAllEntities() {

        // 保持你现有的保护逻辑（按你的要求不提前）
        if (!(this.byId instanceof SporeEntityByIdMap)) {
            this.byId = SporeEntityByIdMap.newInstance(this.byId);
        }
        if (!(this.byUuid instanceof SporeEntityByUuidMap)) {
            this.byUuid = SporeEntityByUuidMap.newInstance(this.byUuid);
        }

        return EntityIterable.newInstance(this.byId);
    }

    @Override
    public void add(T entity) {
        if (!(this.byId instanceof SporeEntityByIdMap)) {
            this.byId = SporeEntityByIdMap.newInstance(this.byId);
        }
        if (!(this.byUuid instanceof SporeEntityByUuidMap)) {
            this.byUuid = SporeEntityByUuidMap.newInstance(this.byUuid);
        }
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return;
        }
        super.add(entity);
    }

    @Override
    public T getEntity(int id) {
        if(!(this.byId instanceof SporeEntityByIdMap)){
            this.byId = SporeEntityByIdMap.newInstance(this.byId);
        }
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(id)){
            return null;
        }

        T entity = this.byId.get(id);
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return null;
        }
        return entity;
    }
    @Override
    public T getEntity(UUID uuid) {
        if(!(this.byUuid instanceof SporeEntityByUuidMap)){
            this.byUuid = SporeEntityByUuidMap.newInstance(this.byUuid);
        }
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(uuid)){
            return null;
        }

        T entity = this.byUuid.get(uuid);
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)) {
            return null;
        }
        return entity;
    }
    private static final class EntityIterable<T extends EntityAccess> implements Iterable<T> {
        private static final Class<? extends Iterable<EntityAccess>> iterableClass= (Class<? extends Iterable<EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
                EntityIterable.class,
                Int2ObjectMap.class
        );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iterableClass,
                        EntityIterable.class,
                        Int2ObjectMap.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden EntityIterable constructor, %s", t.getMessage());
            }
        }
        public static <T extends EntityAccess> Iterable<T> newInstance(Int2ObjectMap<T> byId){
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iterableClass,
                    EntityIterable.class,
                    Int2ObjectMap.class
            );
            try{
                if (constructor != null) {
                    return (Iterable<T>) constructor.invoke(byId);
                }
            }catch (Throwable t){
                LogUtil.errorf("failed to init EntityIterable, %s",t.getMessage());
                LogUtil.printStackTrace(t);
            }
            return new EntityIterable<>(byId);
        }
        private final Int2ObjectMap<T> byId;
        public EntityIterable(Int2ObjectMap<T> byId) {
            this.byId = byId;
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return EntityIterator.newInstance(this.byId.values().iterator());
        }
    }

    private static final class EntityIterator<T extends EntityAccess> implements Iterator<T> {
        private static final Class<? extends Iterator<?>> iteratorClass =
                (Class<? extends Iterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
                        EntityIterator.class,
                        Iterator.class
                );
        private static MethodHandle constructor;
        static {
            try {
                constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                        null,
                        iteratorClass,
                        EntityIterator.class,
                        Iterator.class
                );
            } catch (Throwable t) {
                constructor = null;
                LogUtil.errorf("failed to eager init hidden EntityIterator constructor, %s", t.getMessage());
            }
        }

        public static <T extends EntityAccess> Iterator<T> newInstance(Iterator<T> byIdIt) {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    iteratorClass,
                    EntityIterator.class,
                    Iterator.class
            );
            try {
                if (constructor != null) {
                    return (Iterator<T>) constructor.invoke(byIdIt);
                }
            } catch (Throwable t) {
                LogUtil.errorf("failed to init EntityIterator, %s", t.getMessage());
                LogUtil.printStackTrace(t);
            }
            return new EntityIterator<>(byIdIt);
        }

        private final Iterator<T> byIdIt;
        private T next;

        private EntityIterator(Iterator<T> byIdIt) {
            this.byIdIt = byIdIt;
        }

        @Override
        public boolean hasNext() {
            if (next != null) return true;

            while (byIdIt.hasNext()) {
                T e = byIdIt.next();
                if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(e)) {
                    continue;
                }
                next = e;
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T r = next;
            next = null;
            return r;
        }
    }
}

