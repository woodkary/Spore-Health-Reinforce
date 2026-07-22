package com.Harbinger.Spore.Core.utils;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

import java.lang.reflect.Field;
import java.util.*;

public final class BossEventUtil implements IBossEvent {
    public static final IBossEvent INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IBossEvent.class,
            BossEventUtil.class
    );
    private final ClassValue<Optional<Field>> entityBossEventFields =
            new LoadingClassValue<>(new BossEventFieldFunction());
    @Override
    public BossEvent findBossEvent(Object entity) {
        Field field = getBossEventField(entity.getClass());
        return field!=null? (BossEvent) ClassUtil.getFieldValue(field,entity):null;
    }
    @Override
    public void disableBossEvent(Object entity) {
        BossEvent bossEvent=findBossEvent(entity);
        if(bossEvent==null){
            return;
        }
        bossEvent.setProgress(0.0f);
        bossEvent.setPlayBossMusic(false);
        bossEvent.setCreateWorldFog(false);
        bossEvent.setDarkenScreen(false);
        if(bossEvent instanceof ServerBossEvent sBossEvent){
            sBossEvent.removeAllPlayers();
            sBossEvent.setVisible(false);
        }
    }

    private Field getBossEventField(Class<?> type) {
        return entityBossEventFields.get(type).orElse(null);
    }
}
