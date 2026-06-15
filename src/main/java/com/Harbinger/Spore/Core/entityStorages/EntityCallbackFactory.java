package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.*;

public final class EntityCallbackFactory implements ICallbackFactory {
    public static final ICallbackFactory INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            ICallbackFactory.class,
            EntityCallbackFactory.class
    );
    public EntityInLevelCallback newInstance(LivingEntity entity,EntityInLevelCallback callback){
        if(entity.level instanceof ServerLevel sl&&callback instanceof PersistentEntitySectionManager.Callback sCallback){
            return server(entity,sl,sCallback);
        }else if(entity.level instanceof ClientLevel client&&callback instanceof TransientEntitySectionManager.Callback cCallback){
            return client(entity,client,cCallback);
        }
        return SporeEntityInLevelCallback.newInstance(entity,callback);
    }
    private <T extends EntityAccess> EntityInLevelCallback server(T entity,ServerLevel level,PersistentEntitySectionManager<T>.Callback callback){
        SporeServerEntityCallback.constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                SporeServerEntityCallback.callbackClass,
                SporeServerEntityCallback.class,
                PersistentEntitySectionManager.class,
                EntityAccess.class,
                long.class,
                EntitySection.class
        );
        if(SporeServerEntityCallback.constructor!=null){
            try{
                return (EntityInLevelCallback) SporeServerEntityCallback.constructor.invoke(level.entityManager,entity,callback.currentSectionKey,callback.currentSection);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new ServerEntityCallback,%s",e.getMessage());
            }
        }
        return new SporeServerEntityCallback<>((PersistentEntitySectionManager<T>)level.entityManager,entity,callback.currentSectionKey,callback.currentSection);
    }
    private <T extends EntityAccess> EntityInLevelCallback client(T entity, ClientLevel level, TransientEntitySectionManager<T>.Callback callback){
        SporeClientEntityCallback.constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                SporeClientEntityCallback.constructor,
                SporeClientEntityCallback.callbackClass,
                SporeClientEntityCallback.class,
                TransientEntitySectionManager.class,
                EntityAccess.class,
                long.class,
                EntitySection.class
        );
        if(SporeClientEntityCallback.constructor!=null){
            try{
                return (EntityInLevelCallback) SporeClientEntityCallback.constructor.invoke(level.entityStorage,entity,callback.currentSectionKey,callback.currentSection);
            }catch (Throwable e) {
                LogUtil.errorf("failed to new ClientEntityCallback,%s",e.getMessage());
            }
        }
        return new SporeClientEntityCallback<>((TransientEntitySectionManager<T>)level.entityStorage,entity,callback.currentSectionKey,callback.currentSection);
    }
}
