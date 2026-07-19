package com.Harbinger.Spore.Core.utils.invulCheck;

import com.Harbinger.Spore.Core.utils.BossEventUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.Harbinger.Spore.Spore;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

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
    private static MethodHandle constructorWithMaxDelay=MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            taskClass,
            EntityInvulCheckTask.class,
            LivingEntity.class,
            int.class
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
    public static IEntityInvulCheckTask newInstance(LivingEntity entity,int maxDelay){
        constructorWithMaxDelay=MethodHandleUtil.INSTANCE.ensureConstructor(
                constructorWithMaxDelay,
                taskClass,
                EntityInvulCheckTask.class,
                LivingEntity.class,
                int.class
        );
        if(constructorWithMaxDelay!=null){
            try{
                return (IEntityInvulCheckTask) constructorWithMaxDelay.invoke(entity,maxDelay);
            } catch (Throwable e) {
                LogUtil.errorf("faied to new instance of EntityInvulCheckTask. %s", e.getMessage());
            }
        }
        return new EntityInvulCheckTask(entity,maxDelay);
    }
    private final LivingEntity entity;
    private final int startCountingTime;
    private int maxDeathTime=0;
    private int removeCounter=50;
    private final int maxDelay;
    public EntityInvulCheckTask(LivingEntity entity) {
        this.entity = entity;
        startCountingTime=entity.tickCount;
        maxDelay=400;
    }
    public EntityInvulCheckTask(LivingEntity entity,int maxDelay) {
        this.entity = entity;
        startCountingTime=entity.tickCount;
        this.maxDelay=maxDelay;
    }

    @Override
    public boolean preEntityTick() {
        if(entity.isRemoved()&&entity.level.getEntity(entity.id)==null){
            return true;
        }
        if(entity.deathTime<maxDeathTime){
            --removeCounter;
        }
        return (removeCounter<=0||entity.tickCount-startCountingTime>maxDelay)&&remove();
    }
    private boolean remove(){
        double x=entity.getX();
        double y=entity.getY();
        double z=entity.getZ();
        SporeAttackUtil.INSTANCE.playSound(entity.level,null, x,y,z,
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (entity.getDeathSound() != null) {
            SporeAttackUtil.INSTANCE.playSound(entity.level, null, entity.blockPosition, entity.getDeathSound(), entity.getSoundSource(), 1.0f, entity.getVoicePitch());
        }
        if(SimpleRemoveUtil.INSTANCE.remove(entity, Entity.RemovalReason.DISCARDED)){
            BossEventUtil.INSTANCE.disableBossEvent(entity);
            return true;
        }
        return false;
    }


    @Override
    public void postEntityTick() {
        maxDeathTime=Math.max(maxDeathTime,entity.deathTime);
    }
}
