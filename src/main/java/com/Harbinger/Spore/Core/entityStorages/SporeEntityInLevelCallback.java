package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.HeasdalthUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityInLevelCallback;

import java.lang.invoke.MethodHandle;

public final class SporeEntityInLevelCallback implements EntityInLevelCallback {
    private static final Class<? extends EntityInLevelCallback> callbackClass=(Class<? extends EntityInLevelCallback>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityInLevelCallback.class,
            LivingEntity.class,
            EntityInLevelCallback.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            callbackClass,
            SporeEntityInLevelCallback.class,
            LivingEntity.class,
            EntityInLevelCallback.class
    );
    static EntityInLevelCallback newInstance(LivingEntity entity, EntityInLevelCallback owner){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                callbackClass,
                SporeEntityInLevelCallback.class,
                LivingEntity.class,
                EntityInLevelCallback.class
        );
        if(constructor!=null){
            try{
                return (EntityInLevelCallback) constructor.invoke(entity,owner);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new SporeEntityInLevelCallback. %s",e.getMessage());
            }
        }
        return new SporeEntityInLevelCallback(entity,owner);
    }
    private final LivingEntity entity;
    private final EntityInLevelCallback owner;

    public SporeEntityInLevelCallback(LivingEntity entity, EntityInLevelCallback owner) {
        this.entity = entity;
        this.owner = owner;
    }

    @Override
    public void onMove() {
        owner.onMove();
    }

    @Override
    public void onRemove(Entity.RemovalReason removalReason) {
        owner.onRemove(removalReason);
        if(entity.getHealth()>0.0f&&entity instanceof IDieWithDiscardEntity special&&!special.isSpecialDefasd()){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(entity,0.0f);
            DamageSource source = entity.lastDamageSource != null ? entity.lastDamageSource : entity.damageSources().cactus();
            special.specialDie(source);
            HeasdalthUtil.INSTANCE.genericDie(entity, source);
        }
    }
}
