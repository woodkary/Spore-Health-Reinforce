package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

import java.lang.invoke.MethodHandle;

public final class SporeServerEntityCallback<T extends EntityAccess> extends PersistentEntitySectionManager<T>.Callback {
    public static final Class<? extends EntityInLevelCallback> callbackClass= (Class<? extends EntityInLevelCallback>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeServerEntityCallback.class,
            PersistentEntitySectionManager.class,
            EntityAccess.class,
            long.class,
            EntitySection.class
    );
    static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            callbackClass,
            SporeServerEntityCallback.class,
            PersistentEntitySectionManager.class,
            EntityAccess.class,
            long.class,
            EntitySection.class
    );
    public SporeServerEntityCallback(PersistentEntitySectionManager<T> manager,T p_157614_, long p_157615_, EntitySection p_157616_) {
        manager.super(p_157614_, p_157615_, p_157616_);
    }
    @Override
    public void onMove() {
        super.onMove();
    }

    @Override
    public void onRemove(Entity.RemovalReason removalReason) {
        super.onRemove(removalReason);
        if(entity instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            special.specialDie(liv.lastDamageSource!=null?liv.lastDamageSource:liv.damageSources().cactus());
        }
    }
}
