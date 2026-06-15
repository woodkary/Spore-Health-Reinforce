package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.HeasdalthUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.logging.Level;

public final class SporeEntityByIdMap<V extends EntityAccess> extends Int2ObjectLinkedOpenHashMap<V> implements ISporeEntityStorage {
    private static final Class<? extends Int2ObjectMap<? extends EntityAccess>> mapClass= (Class<? extends Int2ObjectMap<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityByIdMap.class
    );
    private static MethodHandle noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class
    );
    private static MethodHandle mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class,
            Map.class
    );
    private static MethodHandle i2oMapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            mapClass,
            SporeEntityByIdMap.class,
            Int2ObjectMap.class
    );

    public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(Int2ObjectMap<V> m){
        i2oMapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                i2oMapArg,
                mapClass,
                SporeEntityByIdMap.class,
                Int2ObjectMap.class
        );
        if(i2oMapArg!=null){
            try{
                return (Int2ObjectMap<V>) i2oMapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>(m);
    }
    public SporeEntityByIdMap() {
    }public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(){
        noArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                noArg,
                mapClass,
                SporeEntityByIdMap.class
        );
        if(noArg!=null){
            try{
                return (Int2ObjectMap<V>) noArg.invoke();
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>();
    }
    public static <V extends EntityAccess> Int2ObjectMap<V> newInstance(Map<? extends Integer, ? extends V> m){
        mapArg= MethodHandleUtil.INSTANCE.ensureConstructor(
                mapArg,
                mapClass,
                SporeEntityByIdMap.class,
                Map.class
        );
        if(mapArg!=null){
            try{
                return (Int2ObjectMap<V>) mapArg.invoke(m);
            } catch (Throwable t) {
                LogUtil.errorf("failed to new SporeEntityByIdMap instance", t.getMessage());
            }
        }
        return new SporeEntityByIdMap<>(m);
    }

    public SporeEntityByIdMap(Map<? extends Integer, ? extends V> m) {
        super(m);
    }

    public SporeEntityByIdMap(Int2ObjectMap<V> m) {
        super(m);
    }

    @Override
    public V remove(int k) {
        V res=super.remove(k);
        if(res instanceof LivingEntity liv&&liv.getHealth()>0.0f&&liv instanceof IDieWithDiscardEntity special){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,0.0f);
            DamageSource source = liv.lastDamageSource != null ? liv.lastDamageSource : liv.damageSources().cactus();
            special.specialDie(source);
            HeasdalthUtil.INSTANCE.genericDie(liv, source);
        }
        return res;
    }
}
