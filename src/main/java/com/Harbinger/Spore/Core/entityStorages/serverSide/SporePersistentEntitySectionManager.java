package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.entityStorages.ISporeEntityStorage;
import com.Harbinger.Spore.Core.entityStorages.SporeEntityGetter;
import com.Harbinger.Spore.Core.entityStorages.SporeKnownUuidsHashSet;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.*;

import java.util.UUID;

public final class SporePersistentEntitySectionManager<T extends EntityAccess> extends PersistentEntitySectionManager<T> {
    public static final Class<? extends PersistentEntitySectionManager<? extends EntityAccess>> managerClass= (Class<? extends PersistentEntitySectionManager<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporePersistentEntitySectionManager.class,
            Class.class,
            LevelCallback.class,
            EntityPersistentStorage.class
    );
    public SporePersistentEntitySectionManager(Class<T> p_157503_, LevelCallback<T> p_157504_, EntityPersistentStorage<T> p_157505_) {
        super(p_157503_, p_157504_, p_157505_);
    }
    public boolean addEntityUuid(T p_157558_) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_157558_)){
            return false;
        }
        if (!this.knownUuids.add(p_157558_.getUUID())) {
            LOGGER.warn("UUID of added entity already exists: {}", p_157558_);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isLoaded(UUID uuid) {
        if(!(this.knownUuids instanceof ISporeEntityStorage)){
            this.knownUuids= SporeKnownUuidsHashSet.newInstance(this.knownUuids);
        }
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(uuid)&&super.isLoaded(uuid);
    }

    public boolean addNewEntity(T entity) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return false;
        }
        return super.addEntity(entity, false);
    }
    public void unloadEntity(EntityAccess entityAccess) {
        if(entityAccess instanceof Entity entity&&entity instanceof IDieWithDiscardEntity){
            SimpleRemoveUtil.INSTANCE.setRemoved(entity,Entity.RemovalReason.UNLOADED_TO_CHUNK);
        }else{
            entityAccess.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        }
        entityAccess.setLevelCallback(EntityInLevelCallback.NULL);
    }

    public boolean addNewEntityWithoutEvent(T entity) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return false;
        }
        return super.addEntityWithoutEvent(entity, false);
    }

    public boolean addEntity(T entity, boolean p_157540_) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return false;
        }
        return super.addEntity(entity, p_157540_);
    }

    public boolean addEntityWithoutEvent(T entity, boolean p_157540_) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return false;
        }
        return super.addEntityWithoutEvent(entity, p_157540_);
    }
    public LevelEntityGetter<T> getEntityGetter() {
        if(this.entityGetter.getClass()!=SporeEntityGetter.entityGetterClass){
            this.entityGetter=SporeEntityGetter.newInstance(this.entityGetter,this.visibleEntityStorage,this.sectionStorage);
        }
        return this.entityGetter;
    }
}
