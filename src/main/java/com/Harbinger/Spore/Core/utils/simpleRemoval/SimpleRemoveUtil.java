package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassLoaderUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.network.DespawnPacket;
import com.Harbinger.Spore.network.DespawnPacketHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class SimpleRemoveUtil implements ISimpleRemoval {
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
}
