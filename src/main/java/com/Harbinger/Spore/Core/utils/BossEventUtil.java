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
    private final Map<Class<?>, Field> entityBossEventFields = new HashMap<>();
    private final Set<Class<?>> notBossEntities = new HashSet<>();
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
        if (notBossEntities.contains(type)) {
            return null;
        }

        Field cached = entityBossEventFields.get(type);
        if (cached != null) {
            return cached;
        }
        List<Class<?>> visitedClasses = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            Field field = entityBossEventFields.get(current);
            visitedClasses.add(current);
            if (field == null) {
                field = findDeclaredBossEventField(current);
                if (field != null) {
                    for (Class<?> visitedClass : visitedClasses) {
                        entityBossEventFields.put(visitedClass, field);
                    }
                }
            }

            if (field != null) {
                entityBossEventFields.put(type, field);
                return field;
            }
        }

        notBossEntities.add(type);
        return null;
    }

    private Field findDeclaredBossEventField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (BossEvent.class.isAssignableFrom(field.getType())) {
                return field;
            }
        }
        return null;
    }
}
