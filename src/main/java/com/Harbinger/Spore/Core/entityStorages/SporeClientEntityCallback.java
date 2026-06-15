package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.TransientEntitySectionManager;

import java.lang.invoke.MethodHandle;

public final class SporeClientEntityCallback<T extends EntityAccess> extends TransientEntitySectionManager<T>.Callback  {
    public static final Class<? extends EntityInLevelCallback> callbackClass= (Class<? extends EntityInLevelCallback>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeClientEntityCallback.class,
            TransientEntitySectionManager.class,
            EntityAccess.class,
            long.class,
            EntitySection.class
    );
    static MethodHandle constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            callbackClass,
            SporeClientEntityCallback.class,
            TransientEntitySectionManager.class,
            EntityAccess.class,
            long.class,
            EntitySection.class
    );
    public SporeClientEntityCallback(TransientEntitySectionManager<T> manager,T p_157673_, long p_157674_, EntitySection<T> p_157675_) {
        manager.super(p_157673_, p_157674_, p_157675_);
    }
    @Override
    public void onMove() {
        super.onMove();
    }

    @Override
    public void onRemove(Entity.RemovalReason removalReason) {
        super.onRemove(removalReason);
        if(entity instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special){
            special.specialDie(liv.lastDamageSource!=null?liv.lastDamageSource:liv.damageSources().cactus());
        }
    }
}
