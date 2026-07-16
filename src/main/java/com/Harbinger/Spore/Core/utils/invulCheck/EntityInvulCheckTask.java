package com.Harbinger.Spore.Core.utils.invulCheck;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;

final class EntityInvulCheckTask implements IEntityInvulCheckTask {
    private static final Class<? extends IEntityInvulCheckTask> taskClass = (Class<? extends IEntityInvulCheckTask>) BytecodeUtil.resolveHiddenClassOrSelf(
            EntityInvulCheckTask.class,
            LivingEntity.class
    );
    private static MethodHandle constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            taskClass,
            EntityInvulCheckTask.class,
            LivingEntity.class
    );
    public static IEntityInvulCheckTask newInstance(LivingEntity entity){
        constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                taskClass,
                EntityInvulCheckTask.class,
                LivingEntity.class
        );
        if(constructor!=null){
            try{
                return (IEntityInvulCheckTask) constructor.invoke(entity);
            } catch (Throwable e) {
                LogUtil.errorf("faied to new instance of EntityInvulCheckTask. %s", e.getMessage());
            }
        }
        return new EntityInvulCheckTask(entity);
    }
    private final LivingEntity entity;
    private int maxDeathTime=0;
    private int removeCounter=20;
    public EntityInvulCheckTask(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean preEntityTick() {
        if(entity.deathScore<maxDeathTime){
            --removeCounter;
        }
        return removeCounter<=0&&SimpleRemoveUtil.INSTANCE.remove(entity, Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void postEntityTick() {
        maxDeathTime=Math.max(maxDeathTime,entity.deathTime);
    }
}
