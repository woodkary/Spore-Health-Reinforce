package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.entityStorages.SporeEntitySection;
import com.Harbinger.Spore.Core.entityStorages.SporeEntitySectionStorage;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeClientLevel;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeTransientEntitySectionManager;
import com.Harbinger.Spore.Core.entityStorages.serverSide.SporePersistentEntitySectionManager;
import com.Harbinger.Spore.Core.entityStorages.serverSide.SporeServerLevel;
import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.network.DespawnPacket;
import com.Harbinger.Spore.network.DespawnPacketHandler;
import com.Harbinger.Spore.sEvents.SporeEventBus;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SimpleRemoveUtil implements ISimpleRemoval, BiConsumer<DynamicGameEventListener<?>, ServerLevel> {
    public static final ISimpleRemoval INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            ISimpleRemoval.class,
            SimpleRemoveUtil.class
    );
    private final Map<Class<?>,Integer> serverNotSpawning=ProtectedConcurrentHashMap.newInstance();
    private final Map<Integer,Integer> serverIdNotSpawning=ProtectedConcurrentHashMap.newInstance();
    private final Map<UUID,Integer> serverUuidNotSpawning=ProtectedConcurrentHashMap.newInstance();

    private final Map<Class<?>,Integer> clientNotSpawning=ProtectedConcurrentHashMap.newInstance();
    private final Map<Integer,Integer> clientIdNotSpawning=ProtectedConcurrentHashMap.newInstance();
    private final Map<UUID,Integer> clientUuidNotSpawning=ProtectedConcurrentHashMap.newInstance();
    private final AABB NaNAABB=NaNAABBClass.INSTANCE;
    private final Vec3 NaN=NaNVec3.INSTANCE;
    private final BlockPos INF_BLOCK_POS=new BlockPos(Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE);
    private final ChunkPos INF_CHUNK_POS=new ChunkPos(Integer.MAX_VALUE,Integer.MIN_VALUE);
    private final Map<Class<?>,Class<?>> wrapperToOriginal=new ConcurrentHashMap<>();
    @Override
    public void tickServer() {
        Iterator<Map.Entry<Class<?>, Integer>> iterator = serverNotSpawning.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, Integer> entry = iterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                iterator.remove();
            }else{
                entry.setValue(timeLeft-1);
            }
        }

        Iterator<Map.Entry<Integer, Integer>> idIterator = serverIdNotSpawning.entrySet().iterator();
        while (idIterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = idIterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                idIterator.remove();
            }else{
                entry.setValue(timeLeft-1);
            }
        }

        Iterator<Map.Entry<UUID, Integer>> uuidIterator = serverUuidNotSpawning.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = uuidIterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                iterator.remove();
            }else{
                entry.setValue(timeLeft-1);
            }
        }
    }
    @Override
    public void tickClient() {
        Iterator<Map.Entry<Class<?>, Integer>> iterator = clientNotSpawning.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, Integer> entry = iterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                iterator.remove();
            }else{
                entry.setValue(timeLeft-1);
            }
        }

        //迭代clientIdNotSpawning和ClientUuidNotSpawning
        Iterator<Map.Entry<Integer, Integer>> idIterator = clientIdNotSpawning.entrySet().iterator();
        while (idIterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = idIterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                idIterator.remove();
            }else{
                entry.setValue(timeLeft-1);
            }
        }

        Iterator<Map.Entry<UUID, Integer>> uuidIterator = clientUuidNotSpawning.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = uuidIterator.next();
            Integer timeLeft=entry.getValue();
            if(timeLeft==null||timeLeft<=0) {
                iterator.remove();
            }else {
                entry.setValue(timeLeft-1);
            }
        }
    }
    private void setPosRawNaN(Entity target){
        target.position = NaN;
        target.blockPosition = INF_BLOCK_POS;
        target.feetBlockState = null;
        target.chunkPosition = INF_CHUNK_POS;
        try {
            target.levelCallback.onMove();
        }catch (Exception e) {}

        if (target.isAddedToWorld && !target.level().isClientSide && !target.isRemoved()) {
            target.level().getChunk(Integer.MAX_VALUE, Integer.MIN_VALUE);
        }
    }
    private void setPosRaw(Entity target, double x, double y, double z) {
        if (target.position.x != x || target.position.y != y|| target.position.z != z) {
            target.position = new Vec3(x, y, z);
            int i = Mth.floor(x);
            int j = Mth.floor(y);
            int k = Mth.floor(z);
            if (i != target.blockPosition.getX() || j != target.blockPosition.getY() || k != target.blockPosition.getZ()) {
                target.blockPosition = new BlockPos(i, j, k);
                target.feetBlockState = null;
                if (SectionPos.blockToSectionCoord(i) != target.chunkPosition.x || SectionPos.blockToSectionCoord(k) != target.chunkPosition.z) {
                    target.chunkPosition = new ChunkPos(target.blockPosition);
                }
            }
            try {
                target.levelCallback.onMove();
            }catch (Exception e) {}
        }
        if (target.isAddedToWorld && !target.level().isClientSide && !target.isRemoved()) {
            target.level().getChunk((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4);
        }

    }
    private void setPosNaN(Entity target){
        setPosRawNaN(target);
        target.bb=makeBoundingBoxNaN();
        for(Class<?> current=getOrginalClass(target.getClass());current!=null&&current!=Object.class;current=current.getSuperclass()){
            for (Field field : current.getDeclaredFields()) {
                if(Vec3.class.isAssignableFrom(field.getType())) {
                    ClassUtil.setFieldValue(field,target,NaNVec3.INSTANCE);
                }
            }
        }
    }
    private void setPos(Entity target, double x, double y, double z) {
        setPosRaw(target,x,y,z);
        target.bb=makeBoundingBox(target);
    }
    private void setPosField(Entity target, double x, double y, double z) {

    }
    private AABB makeBoundingBoxNaN() {
        return NaNAABB;
    }
    private AABB makeBoundingBox(Entity target) {
        float $$3 = target.dimensions.width / 2.0F;
        float $$4 = target.dimensions.height;
        return new AABB(target.position.x - (double)$$3, target.position.y, target.position.z - (double)$$3, target.position.x + (double)$$3, target.position.y + (double)$$4, target.position.z + (double)$$3);
    }
    @Override
    public Vec3 getNaNPosition() {
        return NaN;
    }
    private Class<?> getOrginalClass(Class<?> wrapperValue){
        //通过value找回第一个key
        return wrapperToOriginal.getOrDefault(wrapperValue, wrapperValue);
    }
    private boolean containsKey(Map<Class<?>, Integer> map, Class<?> clazz) {
        return map.containsKey(clazz)||map.containsKey(getOrginalClass(clazz));
    }
    @Override
    public boolean checkIsRemovedAndUpdate(Object target){
        if(target instanceof ChunkMap.TrackedEntity entity){
            return checkIsRemovedAndUpdate(entity);
        }
        if(target instanceof Entity entity){
            return checkIsRemovedAndUpdate(entity);
        }
        if(target instanceof Integer id){
            return checkIsRemovedAndUpdate(id);
        }
        if(target instanceof UUID uuid){
            return checkIsRemovedAndUpdate(uuid);
        }
        return false;
    }
    @Override
    public <T extends EntityAccess> Collection<T> getAllEntities(Level level, Predicate<T> filter){
        Set<T> entities=new HashSet<>();
        EntityTickList tickList = EntityHeealuthManager.INSTANCE.getEntityTickList(level);
        if(tickList!=null){
            tickList.forEach(EntityListConsumer.newInstance(filter,entities));
        }
        Long2ObjectMap<EntitySection<Entity>> sections = EntityHeealuthManager.INSTANCE.getEntitySections(level);
        if(sections!=null) {
            for (EntitySection<Entity> section : sections.values()) {
                section.getEntities().forEach(EntityListConsumer.newInstance(filter,entities));
            }
        }
        EntityLookup<?> lookup = EntityHeealuthManager.INSTANCE.getEntityLookup(level);
        if(lookup!=null){
            for (EntityAccess e : lookup.byId.values()) {
                if(!filter.test((T) e)){
                    entities.add((T) e);
                }
            }
            for (EntityAccess e : lookup.byUuid.values()) {
                if(!filter.test((T) e)){
                    entities.add((T) e);
                }
            }
        }
        if(level instanceof ServerLevel serverLevel){
            for (ChunkMap.TrackedEntity trackEnt : serverLevel.getChunkSource().chunkMap.entityMap.values()) {
                Entity entity= trackEnt.serverEntity.entity;
                if(!filter.test((T) entity)){
                    entities.add((T) entity);
                }
            }
        }
        return Collections.unmodifiableCollection(entities);
    }
    @Override
    public boolean checkIsRemovedAndUpdate(ChunkMap.TrackedEntity entity){
        if(isRemoved(entity)){
            updateNotSpawning(entity);
            return true;
        }
        return false;
    }
    @Override
    public boolean checkIsRemovedAndUpdate(Entity entity){
        if(isRemoved(entity)){
            updateNotSpawning(entity);
            return true;
        }
        return false;
    }
    @Override
    public boolean checkIsRemovedAndUpdate(Integer id){
        if(isRemoved(id)){
            updateIdNotSpawning(id);
            return true;
        }
        return false;
    }
    @Override
    public boolean checkIsRemovedAndUpdate(UUID uuid){
        if(isRemoved(uuid)){
            updateUuidNotSpawning(uuid);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRemoved(Object key){
        if(key instanceof Entity entity){
            return isRemoved(entity);
        }
        if(key instanceof Integer id){
            return isRemoved(id);
        }
        if(key instanceof UUID uuid){
            return isRemoved(uuid);
        }
        return false;
    }
    @Override
    public boolean isRemoved(ChunkMap.TrackedEntity entity){
        return isRemoved(entity.serverEntity.entity);
    }
    @Override
    public boolean isRemoved(Entity entity){
        return containsKey(entity.level.isClientSide?clientNotSpawning:serverNotSpawning,entity.getClass());
    }
    @Override
    public boolean isRemoved(Integer id){
        return (StackTraceUtil.isClientThread()?clientIdNotSpawning:serverIdNotSpawning).containsKey(id);
    }
    @Override
    public boolean isRemoved(UUID uuid){
        return (StackTraceUtil.isClientThread()?clientUuidNotSpawning:serverUuidNotSpawning).containsKey(uuid);
    }

    @Override
    public boolean remove(Entity entity, Entity.RemovalReason removalReason) {
        Entity res=removeLocal(entity, removalReason);
        DespawnPacketHandler.sendToClient(new DespawnPacket(entity.id,removalReason));
        return res.level.getEntity(res.id) == null;
    }
    @Override
    public Entity removeLocal(Entity entity, Entity.RemovalReason removalReason) {
        if(SporeJudge.isSporeEntity(entity)){
            if(entity instanceof LivingEntity liv) {
                SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            }
            entity.remove(removalReason);
            return entity;
        }
        if (entity.removalReason == null) {
            entity.removalReason = removalReason;
        }

        if (entity.removalReason.shouldDestroy()) {
            entity.stopRiding();
        }

        for (Entity passenger : entity.getPassengers()) {
            passenger.stopRiding();
        }
        onRemove(entity,removalReason);
        try {
            entity.invalidateCaps();
        } catch (Throwable ignored) {}
        SporeEntityHeeaafastthManager.INSTANCE.replaceEntityMap(entity);
        SporeEventBus.tick();
        setPosNaN(entity);
        createWrapppper(entity);
        return entity;
    }
    private void updateIdNotSpawning(Integer id){
        (StackTraceUtil.isClientThread()?clientIdNotSpawning:serverIdNotSpawning).put(id,100);
    }
    private void updateUuidNotSpawning(UUID uuid){
        (StackTraceUtil.isClientThread()?clientUuidNotSpawning:serverUuidNotSpawning).put(uuid,100);
    }
    private void updateNotSpawning(ChunkMap.TrackedEntity entity){
        updateNotSpawning(entity.serverEntity.entity);
    }
    private void updateNotSpawning(Entity entity){
        Map<Class<?>,Integer> map=entity.level.isClientSide?clientNotSpawning:serverNotSpawning;
        map.put(getOrginalClass(entity.getClass()),100);
        map.put(entity.getClass(),100);
    }
    private <T extends EntityAccess> void replaceSectionStorage(PersistentEntitySectionManager<T> manager){
        for (Long2ObjectMap.Entry<EntitySection<T>> entry : manager.sectionStorage.sections.long2ObjectEntrySet()) {
            EntitySection<T> value = entry.getValue();
            if(value.getClass()!=SporeEntitySection.class){
                entry.setValue(new SporeEntitySection<>(value));
            }
        }
        KlassPointerUtil.INSTANCE.replaceClass(manager.sectionStorage, SporeEntitySectionStorage.entitySectionStorageClass,"",0,0.0f);
    }
    private <T extends EntityAccess> void replaceSectionStorage(TransientEntitySectionManager<T> manager){
        for (Long2ObjectMap.Entry<EntitySection<T>> entry : manager.sectionStorage.sections.long2ObjectEntrySet()) {
            EntitySection<T> value = entry.getValue();
            if(value.getClass()!=SporeEntitySection.class){
                entry.setValue(new SporeEntitySection<>(value));
            }
        }
        KlassPointerUtil.INSTANCE.replaceClass(manager.sectionStorage, SporeEntitySectionStorage.entitySectionStorageClass,"",0,0.0f);
    }
    private void onRemove(Entity entity, Entity.RemovalReason reason){
        if(entity.level instanceof ServerLevel serverlevel){
            onRemoveServer(entity,reason,serverlevel,serverlevel.entityManager);
            serverNotSpawning.put(entity.getClass(),100);
            serverIdNotSpawning.put(entity.id,100);
            serverUuidNotSpawning.put(entity.uuid,100);
            KlassPointerUtil.INSTANCE.replaceClass(serverlevel, SporeServerLevel.levelClass,"",0,0.0f);
            KlassPointerUtil.INSTANCE.replaceClass(serverlevel.entityManager, SporePersistentEntitySectionManager.managerClass,"",0,0.0f);
            replaceSectionStorage(serverlevel.entityManager);
        }else if(entity.level instanceof ClientLevel clientlevel){
            onRemoveClient(entity,reason,clientlevel,clientlevel.entityStorage);
            clientNotSpawning.put(entity.getClass(),100);
            clientIdNotSpawning.put(entity.id,100);
            clientUuidNotSpawning.put(entity.uuid,100);
            KlassPointerUtil.INSTANCE.replaceClass(clientlevel, SporeClientLevel.clientLevelClass,"",0,0.0f);
            KlassPointerUtil.INSTANCE.replaceClass(clientlevel.entityStorage, SporeTransientEntitySectionManager.transientEntitySectionManagerClass,"",0,0.0f);
            replaceSectionStorage(clientlevel.entityStorage);
        }
    }
    private void createWrapppper(Object entity){
        Class<?> wrapper = ClassLoaderUtil.INSTANCE.creeateveWrapperHidden(
                LivingEntityHealthLifecycleWrapperUtil.INSTANCE.getOrginalClass(entity.getClass())
        );
        if (wrapper != null) {
            wrapperToOriginal.putIfAbsent(wrapper,entity.getClass());
            KlassPointerUtil.INSTANCE.replaceClass(entity, wrapper, "", 0, 0.0f);
        }
    }
    private <T extends EntityAccess> boolean removeEntitySection(EntitySection<T> section, Entity entity){
        if(section==null){
            return false;
        }
        ClassInstanceMultiMap<? extends EntityAccess> storage = section.storage;
        boolean $$1 = false;

        for(Map.Entry<Class<?>, ? extends List<? extends EntityAccess>> $$2 : storage.byClass.entrySet()) {
            if (($$2.getKey()).isInstance(entity)) {
                List<T> $$3 = (List<T>) $$2.getValue();
                $$1 |= MethodHandleUtil.INSTANCE.javaCollectionRemove($$3,entity);
                $$1 |= $$3.remove(entity);
            }
        }

        return $$1;
    }
    private <T extends EntityAccess> EntitySection<T> getEntitySection(Entity entity){
        EntityInLevelCallback levelCallback = entity.levelCallback;
        for(Class<?> current=levelCallback.getClass();current!=null&&current!=EntityInLevelCallback.class;current=current.getSuperclass()){
            for (Field field : current.getDeclaredFields()) {
                if(EntitySection.class.isAssignableFrom(field.getType())) {
                    return (EntitySection<T>) ClassUtil.getFieldValue(field,levelCallback);
                }
            }
        }
        return null;
    }
    private <T extends EntityAccess> long getCurrentSectionKey(Entity entity){
        EntityInLevelCallback levelCallback = entity.levelCallback;
        for(Class<?> current=levelCallback.getClass();current!=null&&current!=EntityInLevelCallback.class;current=current.getSuperclass()){
            for (Field field : current.getDeclaredFields()) {
                if((field.getType()==Long.TYPE||field.getType()==Long.class)&&ClassUtil.getFieldValue(field,levelCallback) instanceof Long res) {
                    return res;
                }
            }
        }
        return -1L;
    }
    private void ensureActiveIsNotIterated(EntityTickList tickList) {
        if (tickList.iterated == tickList.active) {
            tickList.passive.clear();

            for (Int2ObjectMap.Entry<Entity> objectEntry : Int2ObjectMaps.fastIterable(tickList.active)) {
                tickList.passive.put(objectEntry.getIntKey(), objectEntry.getValue());
            }

            Int2ObjectMap<Entity> $$1 = tickList.active;
            tickList.active = tickList.passive;
            tickList.passive = $$1;
        }

    }
    private void onTrackingEnd(ClientLevel level,Entity entity) {
        entity.unRide();
        level.players.remove(entity);
        entity.onRemovedFromWorld();
        MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, level));
        if (entity.isMultipartEntity()) {
            for(PartEntity<?> part : entity.getParts()) {
                if (part != null) {
                    level.partEntities.remove(part.id);
                }
            }
        }
    }
    private void onTrackingEnd(ServerLevel level,Entity entity) {
        level.getChunkSource().removeEntity(entity);
        if (entity instanceof ServerPlayer serverplayer) {
            level.players.remove(serverplayer);
            level.updateSleepingPlayerList();
        }

        if (entity instanceof Mob mob) {
            if (level.isUpdatingNavigations) {
                Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
            }

            level.navigatingMobs.remove(mob);
        }

        if (entity.isMultipartEntity()) {
            for(PartEntity<?> enderdragonpart : entity.getParts()) {
                if (enderdragonpart != null) {
                    level.dragonParts.remove(enderdragonpart.id);
                }
            }
        }

        entity.updateDynamicGameEventListener(this);
        entity.isAddedToWorld=false;
    }
    private <T extends EntityAccess> void onRemoveServer(Entity entity,Entity.RemovalReason reason,ServerLevel serverLevel,PersistentEntitySectionManager<T> manager) {
        EntitySection<EntityAccess> section = getEntitySection(entity);
        long currentSectionKey = getCurrentSectionKey(entity);
        if (!removeEntitySection(section,entity)) {
            PersistentEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})",entity, SectionPos.of(currentSectionKey), reason);
        }

        EntityTickList entityTickList = serverLevel.entityTickList;
        ensureActiveIsNotIterated(entityTickList);
        MethodHandleUtil.INSTANCE.fastRemove(entityTickList.active,entity.id);
        //entityTickList.active.remove(entity.id);

        onTrackingEnd(serverLevel,entity);
        MethodHandleUtil.INSTANCE.fastRemove(manager.visibleEntityStorage.byId,entity.id);
        //manager.visibleEntityStorage.byId.remove(entity.id);
        MethodHandleUtil.INSTANCE.javaMapRemove(manager.visibleEntityStorage.byUuid,entity.uuid);
        //manager.visibleEntityStorage.byUuid.remove(entity.uuid);

        serverLevel.getScoreboard().entityRemoved(entity);

        MethodHandleUtil.INSTANCE.javaCollectionRemove(manager.knownUuids,entity.uuid);
        //manager.knownUuids.remove(entity.uuid);
        entity.levelCallback=EntityInLevelCallback.NULL;
        if (section!=null&&currentSectionKey!=-1&&section.isEmpty()) {
            manager.sectionStorage.sections.remove(currentSectionKey);
            manager.sectionStorage.sectionIds.remove(currentSectionKey);
        }
    }
    private <T extends EntityAccess> void onRemoveClient(Entity entity,Entity.RemovalReason reason,ClientLevel clientLevel,TransientEntitySectionManager<T> manager) {
        EntitySection<EntityAccess> section = getEntitySection(entity);
        long currentSectionKey = getCurrentSectionKey(entity);
        if (!removeEntitySection(section,entity)) {
            TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", entity, SectionPos.of(currentSectionKey), reason);
        }

        EntityTickList entityTickList = clientLevel.tickingEntities;
        ensureActiveIsNotIterated(entityTickList);
        MethodHandleUtil.INSTANCE.fastRemove(entityTickList.active,entity.id);
        

        onTrackingEnd(clientLevel,entity);
        manager.callbacks.onDestroyed((T) entity);
        MethodHandleUtil.INSTANCE.fastRemove(manager.entityStorage.byId,entity.id);
        //manager.entityStorage.byId.remove(entity.id);
        MethodHandleUtil.INSTANCE.javaMapRemove(manager.entityStorage.byUuid,entity.uuid);
        //manager.entityStorage.byUuid.remove(entity.uuid);
        entity.setLevelCallback(EntityInLevelCallback.NULL);
        if (section!=null&&currentSectionKey!=-1&&section.isEmpty()) {
            manager.sectionStorage.sections.remove(currentSectionKey);
            manager.sectionStorage.sectionIds.remove(currentSectionKey);
        }
    }

    @Override
    public void accept(DynamicGameEventListener dynamicGameEventListener, ServerLevel serverLevel) {
        dynamicGameEventListener.remove(serverLevel);
    }
    private static final class EntityListConsumer<T extends EntityAccess> implements Consumer<Entity> {
        private static final Class<? extends Consumer<Entity>> consumerClass= (Class<? extends Consumer<Entity>>) BytecodeUtil.resolveHiddenClassOrSelf(
                EntityListConsumer.class,
                Predicate.class,
                Set.class
        );
        private static MethodHandle constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                consumerClass,
                EntityListConsumer.class,
                Predicate.class,
                Set.class
        );

        private static <T extends EntityAccess> Consumer<Entity> newInstance(Predicate<T> filter,Set<T> entities){
            constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    consumerClass,
                    EntityListConsumer.class,
                    Predicate.class,
                    Set.class
            );
            if(constructor!=null){
                try{
                    return (Consumer<Entity>) constructor.invoke(filter,entities);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new EntityListConsumer",e.getMessage());
                }
            }
            return new EntityListConsumer<>(filter,entities);
        }
        private final Predicate<T> filter;
        private final Set<T> entities;
        private EntityListConsumer(Predicate<T> filter, Set<T> entities) {
            this.filter = filter;
            this.entities = entities;
        }
        @Override
        public void accept(Entity entity) {
            T t = (T) entity;
            if(filter.test(t)){
                return;
            }
            entities.add(t);
        }
    }
}
