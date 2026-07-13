package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.world.level.entity.LevelCallback;

import java.lang.invoke.MethodHandle;

public final class SporeEntityCallback<T> implements SporeLevelCallback<T> {
    private static final Class<? extends LevelCallback<?>> callbackClass= (Class<? extends LevelCallback<?>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntityCallback.class,
            LevelCallback.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            callbackClass,
            SporeEntityCallback.class,
            LevelCallback.class
    );
    public static<T> LevelCallback<T> newInstance(LevelCallback<T> owner){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                callbackClass,
                SporeEntityCallback.class,
                LevelCallback.class
        );
        if(constructor!=null){
            try{
                return (LevelCallback<T>) constructor.invoke(owner);
            } catch (Throwable e) {
                LogUtil.errorf("failed to new Entity Callback owner. %s",e.getMessage());
            }
        }
        return new SporeEntityCallback<>(owner);
    }
    private final LevelCallback<T> owner;

    public SporeEntityCallback(LevelCallback<T> owner) {
        this.owner = owner;
    }

    @Override
    public void onCreated(T t) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(t)){
            return;
        }
        owner.onCreated(t);
    }

    @Override
    public void onDestroyed(T t) {
        owner.onDestroyed(t);
    }

    @Override
    public void onTickingStart(T t) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(t)){
            return;
        }
        owner.onTickingStart(t);
    }

    @Override
    public void onTickingEnd(T t) {
        owner.onTickingEnd(t);
    }

    @Override
    public void onTrackingStart(T t) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(t)){
            return;
        }
        owner.onTrackingStart(t);
    }

    @Override
    public void onTrackingEnd(T t) {
        owner.onTrackingEnd(t);
    }

    @Override
    public void onSectionChange(T t) {
        owner.onSectionChange(t);
    }
}
