package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.agents.IInstrumentations;
import com.Harbinger.Spore.Core.agents.InstrumentationUtil;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.network.HealthDataPacket;
import com.Harbinger.Spore.network.HealthPacketHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class HeasdalthUtil implements IHeasdalthUtil {
    private static final String DAMAGE_SOURCE_CLASS_NAME = "net.minecraft.world.damagesource.DamageSource";
    public static final IHeasdalthUtil INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IHeasdalthUtil.class,
            HeasdalthUtil.class
    );

    private final Map<Class<?>, List<Field>> allHealthFields = new WeakHashMap<>();
    private final Map<Class<?>, List<Method>> allSetHealthMethods = new WeakHashMap<>();
    private final Map<Class<?>, List<Field>> allSubFields = new WeakHashMap<>();
    private final Map<Class<?>, List<Method>> tickDeathMethods = new WeakHashMap<>();
    private final Map<Class<?>, List<Field>> tickDeathFields = new WeakHashMap<>();
    private final Map<Class<?>, Map<EntityDataAccessor<?>, String>> accessorNameCache = new WeakHashMap<>();
    private final Map<Class<?>, List<Method>> allHurtMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> deathMethodCache = new WeakHashMap<>();
    private final Map<Class<?>, List<Field>> deathFieldCache = new WeakHashMap<>();
    private final Map<Class<?>, List<Field>> staticHealthMapFields = new ConcurrentHashMap<>();
    private final Map<Field, MethodHandle> getMethodCache = new ConcurrentHashMap<>();
    private final Map<Field, MethodHandle> putMethodCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> initializedStaticClasses = new ConcurrentHashMap<>();
    private final Object nullObject = new Object();

    public HeasdalthUtil() {
    }

    @Override
    public void setHeeaatth(LivingEntity entity, float health) {
        if (!SporeJudge.isSporeEntity(entity)) {
            entity.setHealth(health);
        }
        SynchedEntityData data = entity.entityData;
        SynchedEntityData.DataItem dataItem = data.itemsById.get(LivingEntity.DATA_HEALTH_ID.getId());
        if (dataItem != null && !Objects.equals(health, dataItem.getValue())) {
            dataItem.value = health;
            entity.onSyncedDataUpdated(LivingEntity.DATA_HEALTH_ID);
            dataItem.dirty = true;
            data.isDirty = true;
        }
    }

    @Override
    public void setHeeaatth(LivingEntity entity, float health, boolean hard) {
        setHeeaatth(entity, health, hard, false);
    }

    @Override
    public void setHeeaatth(LivingEntity entity, float health, boolean hard, boolean invokeAll) {
        float damageTaken = entity.getHealth() - health;
        entity.getCombatTracker().recordDamage(
                entity.lastDamageSource != null ? entity.lastDamageSource : entity.damageSources().generic(),
                damageTaken
        );
        setHeeaatth(entity, health);
        if (!invokeAll && (!hard || entity.getHealth() <= health || !entity.isAlive())) {
            return;
        }
        hardSetHeeathtuthWithoutSync(entity, health, invokeAll);
        if (!entity.level.isClientSide) {
            HealthPacketHandler.sendToClient(new HealthDataPacket(entity.id, health, invokeAll));
        }
    }

    @Override
    public void hardSetHeeathtuthWithoutSync(LivingEntity entity, float health, boolean invokeAll) {
        try {
            setAllHeeaatth(entity, health, 2);
        } catch (Throwable ignored) {
        }
        if (!invokeAll && entity.getHealth() <= health) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        entity.addAdditionalSaveData(tag);
        setPossibleHealthTag(tag, health);
        entity.readAdditionalSaveData(tag);
        if (!invokeAll && entity.getHealth() <= health) {
            return;
        }
        oneRound(entity, health, entity.entityData);
        if (!invokeAll && entity.getHealth() <= health) {
            return;
        }
        setAllStaticHealthMap(entity, health);
        if (invokeAll || entity.getHealth() <= health) {
            return;
        }
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.createWrapppper(entity);
    }

    private void setAllHeeaatth(Object entity, float health, int depth) {
        if (entity == null || depth < 0) {
            return;
        }
        setAllHeeaatthFields(entity, health);
        setAllSetHeeaatthMethods(entity, health);
        if (depth == 0) {
            return;
        }
        List<Field> subFields = getAllSubFields(entity.getClass());
        for (Field field : subFields) {
            Object child = ClassUtil.getFieldValue(field, entity);
            if (child != null) {
                setAllHeeaatth(child, health, depth - 1);
            }
        }
    }

    private void setAllHeeaatthFields(Object entity, float health) {
        List<Field> healthFields = getAllHealthFields(entity.getClass());
        for (Field field : healthFields) {
            Class<?> type = field.getType();
            if (type == double.class || type == Double.class) {
                ClassUtil.setFieldValue(field, entity, (double) health);
            } else {
                ClassUtil.setFieldValue(field, entity, health);
            }
        }
    }

    private void setAllSetHeeaatthMethods(Object entity, float health) {
        List<Method> methods = getAllSetHealthMethods(entity.getClass());
        for (Method method : methods) {
            try {
                Class<?> type = method.getParameterTypes()[0];
                if (type == double.class || type == Double.class) {
                    method.invoke(entity, (double) health);
                } else {
                    method.invoke(entity, health);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private List<Field> getAllHealthFields(Class<?> clazz) {
        synchronized (allHealthFields) {
            List<Field> cached = allHealthFields.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Field> fields = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Field[] declaredFields = current.getDeclaredFields();
                for (Field field : declaredFields) {
                    String name = field.getName().toLowerCase(Locale.ROOT);
                    Class<?> type = field.getType();
                    if ((type == float.class || type == double.class || type == Float.class || type == Double.class)
                            && (name.contains("hp") || name.contains("heal"))
                            && !name.contains("target")) {
                        field.setAccessible(true);
                        fields.add(field);
                    }
                }
            }
            allHealthFields.put(clazz, fields);
            return fields;
        }
    }

    private List<Method> getAllSetHealthMethods(Class<?> clazz) {
        synchronized (allSetHealthMethods) {
            List<Method> cached = allSetHealthMethods.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Method> methods = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Method[] declaredMethods = current.getDeclaredMethods();
                for (Method method : declaredMethods) {
                    String name = method.getName().toLowerCase(Locale.ROOT);
                    if ((name.contains("hp") || ((name.contains("set") || name.contains("update")) && name.contains("heal")))
                            && !name.contains("target")
                            && method.getParameterCount() == 1
                            && isFloatOrDouble(method.getParameterTypes()[0])) {
                        method.setAccessible(true);
                        methods.add(method);
                    }
                }
            }
            allSetHealthMethods.put(clazz, methods);
            return methods;
        }
    }

    private List<Field> getAllSubFields(Class<?> clazz) {
        synchronized (allSubFields) {
            List<Field> cached = allSubFields.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Field> fields = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Field[] declaredFields = current.getDeclaredFields();
                for (Field field : declaredFields) {
                    Class<?> type = field.getType();
                    if (type.isPrimitive()
                            || type.isEnum()
                            || type == String.class
                            || Number.class.isAssignableFrom(type)
                            || type == Boolean.class
                            || type == Character.class) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            allSubFields.put(clazz, fields);
            return fields;
        }
    }

    private boolean isFloatOrDouble(Class<?> type) {
        return type == float.class || type == double.class || type == Float.class || type == Double.class;
    }

    private List<Field> getTickDeathFields(Class<?> clazz) {
        synchronized (tickDeathFields) {
            List<Field> cached = tickDeathFields.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Field> result = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Field[] fields = current.getDeclaredFields();
                for (Field field : fields) {
                    String name = field.getName().toLowerCase(Locale.ROOT);
                    if (isTickDeathFieldName(name) && field.getType() == int.class) {
                        field.setAccessible(true);
                        result.add(field);
                    }
                }
            }
            tickDeathFields.put(clazz, result);
            return result;
        }
    }

    private Map<EntityDataAccessor<?>, String> getAccessorNameMap(Class<?> clazz) {
        synchronized (accessorNameCache) {
            Map<EntityDataAccessor<?>, String> cached = accessorNameCache.get(clazz);
            if (cached != null) {
                return cached;
            }
            Map<EntityDataAccessor<?>, String> map = buildAccessorNameMap(clazz);
            accessorNameCache.put(clazz, map);
            return map;
        }
    }

    private Map<EntityDataAccessor<?>, String> buildAccessorNameMap(Class<?> clazz) {
        Map<EntityDataAccessor<?>, String> map = new HashMap<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if (EntityDataAccessor.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object value = ClassUtil.getFieldValue(field, (Object) null);
                    if (value instanceof EntityDataAccessor<?> accessor) {
                        map.put(accessor, field.getName());
                    }
                }
            }
        }
        return map;
    }

    private boolean isTickDeathFieldName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return (lower.contains("tick") || lower.contains("time"))
                && (lower.contains("death") || lower.contains("die") || lower.contains("dead") || lower.contains("kill"));
    }

    private List<SynchedEntityData.DataItem<?>> getTickDeathDataItems(Entity entity) {
        List<SynchedEntityData.DataItem<?>> result = new ArrayList<>();
        Map<EntityDataAccessor<?>, String> nameMap = getAccessorNameMap(entity.getClass());
        ObjectSet<Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>>> entries = entity.entityData.itemsById.int2ObjectEntrySet();
        for (Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>> entry : entries) {
            SynchedEntityData.DataItem<?> dataItem = entry.getValue();
            EntityDataAccessor<?> accessor = dataItem.getAccessor();
            if (accessor.getSerializer() == EntityDataSerializers.INT) {
                String name = nameMap.get(accessor);
                if (name != null && isTickDeathFieldName(name)) {
                    result.add(dataItem);
                }
            }
        }
        return result;
    }

    private List<Method> getTickDeathMethods(Class<?> clazz) {
        synchronized (tickDeathMethods) {
            List<Method> cached = tickDeathMethods.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Method> result = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Method[] methods = current.getDeclaredMethods();
                for (Method method : methods) {
                    String name = method.getName().toLowerCase(Locale.ROOT);
                    if (method.getParameterCount() == 0
                            && name.contains("tick")
                            && (name.contains("death") || name.contains("die") || name.contains("dead") || name.contains("kill"))) {
                        method.setAccessible(true);
                        result.add(method);
                    }
                }
            }
            tickDeathMethods.put(clazz, result);
            return result;
        }
    }

    private void oneRound(LivingEntity entity, float health, SynchedEntityData data) {
        ObjectSet<Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>>> entries = data.itemsById.int2ObjectEntrySet();
        for (Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>> entry : entries) {
            SynchedEntityData.DataItem dataItem = entry.getValue();
            EntityDataAccessor<?> accessor = dataItem.getAccessor();
            EntityDataSerializer<?> serializer = accessor.getSerializer();
            if (accessor != Player.DATA_PLAYER_ABSORPTION_ID
                    &&!isNegativeValue(dataItem.value)
                    &&(serializer == EntityDataSerializers.FLOAT
                    || dataItem.value instanceof Float
                    || dataItem.value instanceof Double)) {
                dataItem.value = health;
                entity.onSyncedDataUpdated(accessor);
                dataItem.dirty = true;
                data.isDirty = true;
            }
        }
    }
    private boolean isNegativeValue(Object value) {
        return value instanceof Number number&&number.doubleValue()<0.0;
    }

    private void setPossibleHealthTag(CompoundTag compoundTag, float health) {
        Set<String> keys = new HashSet<>(compoundTag.getAllKeys());
        for (String key : keys) {
            Tag tag = compoundTag.get(key);
            if (tag instanceof CompoundTag child) {
                setPossibleHealthTag(child, health);
            } else {
                String lowerKey = key.toLowerCase(Locale.ROOT);
                if (lowerKey.contains("health") || lowerKey.contains("hp")) {
                    if (tag instanceof FloatTag) {
                        compoundTag.putFloat(key, health);
                    } else if (tag instanceof DoubleTag) {
                        compoundTag.putDouble(key, health);
                    } else if (tag instanceof IntTag) {
                        compoundTag.putInt(key, (int) health);
                    } else if (tag instanceof LongTag) {
                        compoundTag.putLong(key, (long) health);
                    } else if (tag instanceof ShortTag) {
                        compoundTag.putShort(key, (short) health);
                    }
                }
            }
        }
    }

    private void setPoseDying(Entity entity) {
        SynchedEntityData data = entity.entityData;
        SynchedEntityData.DataItem dataItem = data.itemsById.get(Entity.DATA_POSE.getId());
        if (dataItem != null && !Objects.equals(Pose.DYING, dataItem.getValue())) {
            dataItem.value = Pose.DYING;
            entity.onSyncedDataUpdated(Entity.DATA_POSE);
            dataItem.dirty = true;
            data.isDirty = true;
        }
    }

    private float getHealth(LivingEntity entity) {
        SynchedEntityData.DataItem dataItem = entity.entityData.getItem(LivingEntity.DATA_HEALTH_ID);
        return (float) dataItem.value;
    }

    @Override
    public boolean invokeAllHurtMethods(LivingEntity entity, DamageSource damageSource, float amount, float currentHealth) {
        if (entity == null) {
            return false;
        }
        float expectedHealth = currentHealth;
        if (entity.hurt(damageSource, amount) && entity.getHealth() <= expectedHealth - amount) {
            return true;
        }
        expectedHealth = entity.getHealth();
        List<Method> methods = getAllHurtMethods(entity.getClass());
        expectedHealth -= amount * methods.size();
        for (Method method : methods) {
            try {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && params[0] == float.class) {
                    method.invoke(entity, amount);
                } else if (params.length == 2) {
                    if (isDamageSourceClass(params[0])) {
                        method.invoke(entity, damageSource, amount);
                    } else {
                        method.invoke(entity, amount, damageSource);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return entity.getHealth() <= expectedHealth;
    }

    private List<Method> getAllHurtMethods(Class<?> clazz) {
        List<Method> cached = allHurtMethods.get(clazz);
        if (cached != null) {
            return cached;
        }
        List<Method> methods = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            Method[] declaredMethods = current.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if ((name.contains("hurt") || name.contains("damage")) && !name.contains("target") && !name.contains("all")) {
                    Class<?>[] params = method.getParameterTypes();
                    boolean match = false;
                    if (params.length == 1 && params[0] == float.class) {
                        match = true;
                    } else if (params.length == 2) {
                        boolean hasFloat = params[0] == float.class || params[1] == float.class;
                        boolean hasDamageSource = isDamageSourceClass(params[0]) || isDamageSourceClass(params[1]);
                        match = hasFloat && hasDamageSource;
                    }
                    if (match) {
                        method.setAccessible(true);
                        methods.add(method);
                    }
                }
            }
        }
        allHurtMethods.put(clazz, methods);
        return methods;
    }

    private boolean isDamageSourceClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        try {
            Class<?> damageSourceClass = Class.forName(DAMAGE_SOURCE_CLASS_NAME);
            return damageSourceClass.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void setPossibleTag(CompoundTag compoundTag) {
        Set<String> keys = new HashSet<>(compoundTag.getAllKeys());
        for (String key : keys) {
            Tag tag = compoundTag.get(key);
            if (tag instanceof CompoundTag child) {
                setPossibleTag(child);
            } else {
                String lowerKey = key.toLowerCase(Locale.ROOT);
                boolean deathLike = !"SporeDeeaadfd".equals(key)
                        && (lowerKey.contains("dead")
                        || lowerKey.contains("death")
                        || lowerKey.contains("died")
                        || lowerKey.contains("die")
                        || lowerKey.contains("killed")
                        || lowerKey.contains("kill"));
                if (deathLike) {
                    if (tag instanceof ByteTag) {
                        compoundTag.putByte(key, (byte) 1);
                    } else if (tag instanceof IntTag) {
                        compoundTag.putInt(key, 1);
                    } else if (tag instanceof LongTag) {
                        compoundTag.putLong(key, 1L);
                    } else if (tag instanceof ShortTag) {
                        compoundTag.putShort(key, (short) 1);
                    }
                }
                if (tag instanceof IntArrayTag) {
                    compoundTag.putUUID(key, UUID.randomUUID());
                } else if (tag instanceof LongArrayTag) {
                    compoundTag.putLongArray(key, new long[]{4});
                } else if (tag instanceof ByteArrayTag) {
                    compoundTag.putByteArray(key, new byte[]{32});
                }
            }
        }
    }

    @Override
    public void die(LivingEntity target, DamageSource source) {
        DamageSource actualSource = source != null ? source : target.damageSources().genericKill();
        if (SporeJudge.isSporeEntity(target)) {
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(target, 0.0f);
        } else {
            setHeeaatth(target, 0.0f, true, true);
        }
        ObjectSet<Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>>> entries = target.entityData.itemsById.int2ObjectEntrySet();
        for (Int2ObjectMap.Entry<SynchedEntityData.DataItem<?>> entry : entries) {
            SynchedEntityData.DataItem dataItem = entry.getValue();
            if (dataItem.getAccessor().getSerializer() == EntityDataSerializers.BOOLEAN) {
                dataItem.value = false;
                target.onSyncedDataUpdated(dataItem.getAccessor());
                dataItem.dirty = true;
                target.entityData.isDirty = true;
            }
        }
        runDeathMethods(target, actualSource);
        setDeathFields(target, true);
        CompoundTag tag = new CompoundTag();
        target.addAdditionalSaveData(tag);
        setPossibleTag(tag);
        target.readAdditionalSaveData(tag);
        if (!target.isRemoved()) {
            Entity entity = actualSource.getEntity();
            LivingEntity killCredit = target.getKillCredit();
            if (target.deathScore >= 0 && killCredit != null) {
                killCredit.awardKillScore(target, target.deathScore, actualSource);
            }
            if (target.isSleeping()) {
                target.stopSleeping();
            }
            if (!target.level().isClientSide && target.hasCustomName()) {
                LogUtil.logf("Named entity %s died: %s", target, target.getCombatTracker().getDeathMessage().getString());
            }
            target.dead = true;
            target.getCombatTracker().recheckStatus();
            Level level = target.level();
            if (level instanceof ServerLevel serverLevel) {
                if (entity == null || entity.killedEntity(serverLevel, target)) {
                    target.gameEvent(GameEvent.ENTITY_DIE);
                    if (!(target instanceof Player)) {
                        target.dropAllDeathLoot(actualSource);
                    }
                }
                target.level().broadcastEntityEvent(target, (byte) 3);
            }
            setPoseDying(target);
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(target, actualSource));
        }
    }

    private boolean isDeathName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("dead")
                || n.contains("die")
                || n.contains("death")
                || n.contains("away")
                || n.contains("died")
                || n.contains("kill")
                || n.contains("weak"))
                && !(n.contains("time") || n.contains("tick"));
    }

    private void runDeathMethods(LivingEntity entity, DamageSource source) {
        List<Method> methods = getDeathMethods(entity.getClass());
        for (Method method : methods) {
            try {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 0) {
                    method.invoke(entity);
                } else if (paramTypes[0] == boolean.class) {
                    method.invoke(entity, true);
                } else if (paramTypes[0] == int.class) {
                    method.invoke(entity, 1);
                } else if (paramTypes[0] == long.class) {
                    method.invoke(entity, 1L);
                } else if (paramTypes[0] == float.class) {
                    method.invoke(entity, 1f);
                } else if (paramTypes[0] == double.class) {
                    method.invoke(entity, 1.0);
                } else if (paramTypes[0] == DamageSource.class) {
                    method.invoke(entity, source);
                }
            } catch (Throwable ignored) {
            }
        }
        if (!(entity instanceof Player)) {
            entity.die(source);
        }
    }

    private List<Method> getDeathMethods(Class<?> clazz) {
        synchronized (deathMethodCache) {
            List<Method> cached = deathMethodCache.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Method> list = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Method[] methods = current.getDeclaredMethods();
                for (Method method : methods) {
                    if (!isDeathName(method.getName())) {
                        continue;
                    }
                    int paramCount = method.getParameterCount();
                    if (paramCount == 0 || canInvokeDeathMethodWithOneArg(method)) {
                        method.setAccessible(true);
                        list.add(method);
                    }
                }
            }
            deathMethodCache.put(clazz, list);
            return list;
        }
    }

    private boolean canInvokeDeathMethodWithOneArg(Method method) {
        if (method.getParameterCount() != 1) {
            return false;
        }
        Class<?> type = method.getParameterTypes()[0];
        return type == boolean.class
                || Number.class.isAssignableFrom(type)
                || (type.isPrimitive() && type != void.class)
                || type == DamageSource.class;
    }

    private void setDeathFields(LivingEntity entity, boolean isDeath) {
        List<Field> fields = getDeathFields(entity.getClass());
        for (Field field : fields) {
            try {
                Class<?> type = field.getType();
                if (type == boolean.class) {
                    field.setBoolean(entity, isDeath);
                } else if (type == int.class) {
                    field.setInt(entity, isDeath ? 1 : 0);
                } else if (type == long.class) {
                    field.setLong(entity, isDeath ? 1L : 0L);
                } else if (type == float.class) {
                    field.setFloat(entity, isDeath ? 1f : 0f);
                } else if (type == double.class) {
                    field.setDouble(entity, isDeath ? 1.0 : 0.0);
                } else if (type == BlockPos.class && isDeath) {
                    BlockPos pos = entity.blockPosition();
                    field.set(entity, new BlockPos(pos.getX() - 1, pos.getY() + 1, pos.getZ() + 1));
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private List<Field> getDeathFields(Class<?> clazz) {
        synchronized (deathFieldCache) {
            List<Field> cached = deathFieldCache.get(clazz);
            if (cached != null) {
                return cached;
            }
            List<Field> list = new ArrayList<>();
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Field[] fields = current.getDeclaredFields();
                for (Field field : fields) {
                    if (!isDeathName(field.getName())) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (type == boolean.class
                            || type == int.class
                            || type == long.class
                            || type == float.class
                            || type == double.class
                            || type == BlockPos.class) {
                        field.setAccessible(true);
                        list.add(field);
                    }
                }
            }
            deathFieldCache.put(clazz, list);
            return list;
        }
    }

    @Override
    public float setHealthAdjuster(LivingEntity entity, float health) {
        float currentHealth = entity.getHealth();
        boolean notHealing = entity.getPersistentData().getBoolean("notHeaealing");
        if (health > currentHealth) {
            return notHealing ? currentHealth : health;
        }
        int multiplier = entity.getPersistentData().getInt("damagresasfeMultiplier");
        float damage = currentHealth - health;
        damage *= Math.max(1, multiplier);
        return currentHealth - damage;
    }

    private void setAllStaticHealthMap(Object entityObj, float health) {
        if (!(entityObj instanceof Entity entity)) {
            LogUtil.errorf("Entity object expected, got: %s", entityObj);
            return;
        }
        ensureStaticHealthMapsInitialized(entityObj.getClass());
        UUID entityUuid = entity.uuid;
        int entityId = entity.id;
        for (Map.Entry<Class<?>, List<Field>> entry : staticHealthMapFields.entrySet()) {
            List<Field> fields = entry.getValue();
            for (Field field : fields) {
                try {
                    Object mapObj = ClassUtil.getFieldValue(field, (Object) null);
                    if (!(mapObj instanceof Map map)) {
                        continue;
                    }
                    Set<?> keys = new HashSet<>(map.keySet());
                    for (Object key : keys) {
                        if (!matchesStaticHealthKey(key, entity, entityUuid, entityId, entityObj)) {
                            continue;
                        }
                        Object value = invokeSuperGetCached(field, map, key);
                        if (value == null) {
                            continue;
                        }
                        if (value instanceof Number) {
                            invokeSuperPutCached(field, map, key, health);
                        } else {
                            setCustomHealthFields(value, health);
                        }
                    }
                } catch (Throwable t) {
                    LogUtil.errorf("Failed to set static health map field %s.%s: %s",
                            entry.getKey().getName(),
                            field.getName(),
                            t.getMessage());
                }
            }
        }
    }

    private boolean matchesStaticHealthKey(Object key, Entity entity, UUID entityUuid, int entityId, Object entityObj) {
        if (key instanceof Entity keyEntity) {
            return keyEntity.equals(entity) || keyEntity.uuid.equals(entityUuid);
        }
        if (key instanceof UUID keyUuid) {
            return keyUuid.equals(entityUuid);
        }
        if (key instanceof Integer keyId) {
            return keyId == entityId;
        }
        return Objects.equals(key, entityObj);
    }

    private void ensureStaticHealthMapsInitialized(Class<?> entityClass) {
        if (initializedStaticClasses.containsKey(entityClass)) {
            return;
        }
        synchronized (initializedStaticClasses) {
            if (initializedStaticClasses.containsKey(entityClass)) {
                return;
            }
            IInstrumentations instrumentation = InstrumentationUtil.getInstance();
            if (instrumentation != null) {
                initializeStaticHealthMaps(instrumentation.getAllLoadedClasses());
            }
            initializedStaticClasses.put(entityClass, nullObject);
        }
    }

    private void initializeStaticHealthMaps(Class<?>[] loadedClasses) {
        if (loadedClasses == null || loadedClasses.length == 0) {
            return;
        }
        for (Class<?> clazz : loadedClasses) {
            if (clazz == null) {
                continue;
            }
            String className = clazz.getName().toLowerCase(Locale.ROOT);
            if (!className.contains("heal") && !className.contains("hp")) {
                continue;
            }
            List<Field> candidateMaps = new ArrayList<>();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName().toLowerCase(Locale.ROOT);
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (!name.contains("heal") && !name.contains("hp")) {
                    continue;
                }
                Class<?> fieldType = field.getType();
                boolean mapLike = Map.class.isAssignableFrom(fieldType)
                        || fieldType.getName().toLowerCase(Locale.ROOT).contains("map");
                if (mapLike) {
                    field.setAccessible(true);
                    candidateMaps.add(field);
                }
            }
            if (!candidateMaps.isEmpty()) {
                staticHealthMapFields.put(clazz, candidateMaps);
            }
        }
    }

    private Object invokeSuperGetCached(Field field, Map map, Object key) throws Throwable {
        MethodHandle handle = getMethodCache.get(field);
        if (handle == null) {
            handle = buildSpecialMapHandle(field, "get", MethodType.methodType(Object.class, Object.class));
            if (handle != null) {
                getMethodCache.put(field, handle);
            }
        }
        if (handle != null) {
            return handle.bindTo(map).invokeWithArguments(key);
        }
        return map.get(key);
    }

    private void invokeSuperPutCached(Field field, Map map, Object key, Object value) throws Throwable {
        MethodHandle handle = putMethodCache.get(field);
        if (handle == null) {
            handle = buildSpecialMapHandle(field, "put", MethodType.methodType(Object.class, Object.class, Object.class));
            if (handle != null) {
                putMethodCache.put(field, handle);
            }
        }
        if (handle != null) {
            handle.bindTo(map).invokeWithArguments(key, value);
        } else {
            map.put(key, value);
        }
    }

    private MethodHandle buildSpecialMapHandle(Field field, String name, MethodType type) {
        try {
            Object mapObj = ClassUtil.getFieldValue(field, (Object) null);
            if (mapObj == null) {
                return null;
            }
            Class<?> utilMapClass = findJavaUtilMapClass(mapObj.getClass());
            if (utilMapClass == null) {
                return null;
            }
            MethodHandles.Lookup lookup = ClassUtil.getLookup();
            return lookup.findSpecial(utilMapClass, name, type, mapObj.getClass());
        } catch (Throwable t) {
            LogUtil.errorf("Failed to cache map %s handle for field %s: %s", name, field.getName(), t.getMessage());
            return null;
        }
    }

    private Class<?> findJavaUtilMapClass(Class<?> mapClass) {
        Class<?> current = mapClass;
        while (current != null && Map.class.isAssignableFrom(current)) {
            Package pkg = current.getPackage();
            if (pkg != null && pkg.getName().startsWith("java.util")) {
                return current;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private void setCustomHealthFields(Object target, float health) {
        Class<?> type = target.getClass();
        Field dirtyField = null;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName().toLowerCase(Locale.ROOT);
            field.setAccessible(true);
            try {
                if ((name.contains("heal") || name.contains("hp")) && isNumberField(field.getType())) {
                    setNumberField(field, target, health);
                }
                if (dirtyField == null
                        && name.contains("dirty")
                        && (field.getType() == boolean.class || field.getType() == Boolean.class)) {
                    dirtyField = field;
                }
            } catch (Throwable t) {
                LogUtil.errorf("Failed to set field %s.%s: %s", type.getName(), field.getName(), t.getMessage());
            }
        }
        if (dirtyField != null) {
            try {
                ClassUtil.setFieldValue(dirtyField, target, true);
            } catch (Throwable t) {
                LogUtil.errorf("Failed to set dirty flag for %s: %s", type.getName(), t.getMessage());
            }
        }
    }

    private boolean isNumberField(Class<?> type) {
        return type == float.class
                || type == double.class
                || type == int.class
                || type == long.class
                || Number.class.isAssignableFrom(type);
    }

    private void setNumberField(Field field, Object target, float health) {
        Class<?> type = field.getType();
        if (type == Float.class || type == float.class) {
            ClassUtil.setFieldValue(field, target, health);
        } else if (type == Double.class || type == double.class) {
            ClassUtil.setFieldValue(field, target, (double) health);
        } else if (type == Integer.class || type == int.class) {
            ClassUtil.setFieldValue(field, target, (int) health);
        } else if (type == Long.class || type == long.class) {
            ClassUtil.setFieldValue(field, target, (long) health);
        }
    }
}
