package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SporeEntityByUuidMap<T extends EntityAccess> extends HashMap<UUID, T> implements ISporeEntityStorage {
    private static final Class<? extends HashMap<UUID,? extends EntityAccess>> mapClass= (Class<? extends HashMap<UUID,? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityByUuidMap.class
    );
    private static MethodHandle noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByUuidMap.class
    );
    private static MethodHandle mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByUuidMap.class,
            Map.class
    );
    public static <V extends EntityAccess> Map<UUID,V> newInstance(){
        noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                mapClass,
                SporeEntityByUuidMap.class
        );
        if(noArg!=null){
            try{
                return (Map<UUID,V>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByUuidMap instance", t.getMessage());
            }
        }
        return new SporeEntityByUuidMap<>();
    }
    public static <V extends EntityAccess> Map<UUID,V> newInstance(Map<? extends UUID, ? extends V> m){
        mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                mapArg,
                mapClass,
                SporeEntityByUuidMap.class,
                Map.class
        );
        if(mapArg!=null){
            try{
                return (Map<UUID,V>) mapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByUuidMap instance", t.getMessage());
            }
        }
        return new SporeEntityByUuidMap<>(m);
    }
    public SporeEntityByUuidMap() {
    }

    public SporeEntityByUuidMap(Map<? extends UUID, ? extends T> m) {
        super(m);
    }

    @Override
    public T remove(Object key) {
        T res=super.remove(key);
        if(res instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            special.specialDie(liv.lastDamageSource!=null?liv.lastDamageSource:liv.damageSources().cactus());
        }
        return res;
    }
}
