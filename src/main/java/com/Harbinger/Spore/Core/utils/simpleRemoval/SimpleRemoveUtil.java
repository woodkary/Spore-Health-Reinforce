package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.network.DespawnPacket;
import com.Harbinger.Spore.network.DespawnPacketHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class SimpleRemoveUtil implements ISimpleRemoval, BiConsumer<DynamicGameEventListener<?>, ServerLevel> {
    public static final ISimpleRemoval INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            ISimpleRemoval.class,
            SimpleRemoveUtil.class
    );
    @Override
    public boolean remove(Entity entity, Entity.RemovalReason removalReason) {
        Entity res=removeLocal(entity, removalReason);
        DespawnPacketHandler.sendToClient(new DespawnPacket(entity.id,removalReason));
        return res.level.getEntity(res.id) == null;
    }
    @Override
    public Entity removeLocal(Entity entity, Entity.RemovalReason removalReason) {
        if (entity.removalReason == null) {
            entity.removalReason = removalReason;
        }

        if (entity.removalReason.shouldDestroy()) {
            entity.stopRiding();
        }

        for (Entity passenger : entity.getPassengers()) {
            passenger.stopRiding();
        }
        entity.levelCallback.onRemove(removalReason);
        if(!entity.level.isClientSide){
            entity.invalidateCaps();
        }
        createWrapppper(entity);
        return entity;
    }
    private void createWrapppper(Object entity){
        Class<?> wrapper = ClassLoaderUtil.INSTANCE.creeateveWrapperHidden(
                LivingEntityHealthLifecycleWrapperUtil.INSTANCE.getOrginalClass(entity.getClass())
        );
        if (wrapper != null) {
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
    private <T extends EntityAccess> void onRemoveServer(Entity entity,Entity.RemovalReason reason,PersistentEntitySectionManager<T> manager) {
        if(!(entity.level instanceof ServerLevel serverLevel)){
            return;//todo： 改为onRemoveClient
        }
        EntitySection<EntityAccess> section = getEntitySection(entity);
        if(section==null){
            return;
        }
        long currentSectionKey = getCurrentSectionKey(entity);
        if(currentSectionKey==-1){
            return;
        }
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
        manager.visibleEntityStorage.byUuid.remove(entity.uuid);

        serverLevel.getScoreboard().entityRemoved(entity);


        manager.knownUuids.remove(entity.uuid);
        entity.levelCallback=EntityInLevelCallback.NULL;
        if (section.isEmpty()) {
            manager.sectionStorage.sections.remove(currentSectionKey);
            manager.sectionStorage.sectionIds.remove(currentSectionKey);
        }
    }

    @Override
    public void accept(DynamicGameEventListener dynamicGameEventListener, ServerLevel serverLevel) {
        dynamicGameEventListener.remove(serverLevel);
    }
}
