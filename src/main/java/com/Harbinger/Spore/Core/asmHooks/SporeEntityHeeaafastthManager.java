package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.entityStorages.*;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.ProtectedConcurrentHashMap;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.ICalamityMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import com.Harbinger.Spore.Sentities.BaseEntities.IFakeDataHealthEntity;
import com.Harbinger.Spore.network.HealthDataPacket;
import com.Harbinger.Spore.network.HealthPacketHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.BiFunction;


/**
 * @author karywoodOyo
 */
public final class SporeEntityHeeaafastthManager implements ISporeEntityHealth {
    public static final ISporeEntityHealth INSTANCE = BytecodeUtil.createHiddenSingletonInstance(ISporeEntityHealth.class, SporeEntityHeeaafastthManager.class);
    private final Map<LivingEntity, IFloatEntry> entityMaxHeeaafastth = ProtectedConcurrentHashMap.newInstance();
    private final Map<LivingEntity, IFloatEntry> etiHeuahMape = ProtectedConcurrentHashMap.newInstance();
    private final BiFunction<LivingEntity, IFloatEntry, IFloatEntry> entityHealthJudge;
    public SporeEntityHeeaafastthManager() {
        entityHealthJudge= SporeEntityHealthJudge.newInstance(this.entityMaxHeeaafastth);
    }

    public void initSporeEntity(LivingEntity entity) {
        if (!isSporeEntity(entity)) {
            return;
        }
        float maxHealth = getAttributeMaxHealth(entity);
        entityMaxHeeaafastth.put(entity, FloatEntry.INSTANCE.newInstance(maxHealth));
        etiHeuahMape.put(entity, FloatEntry.INSTANCE.newInstance(maxHealth));
        if(entity instanceof IDieWithDiscardEntity) {
            replaceEntityMap(entity);
        }
    }
    @Override
    public void replaceEntityMap(Entity entity) {
        if(entity.level instanceof ServerLevel sl){
            if (!(sl.entityManager.visibleEntityStorage instanceof ISporeEntityStorage)) {
                sl.entityManager.visibleEntityStorage= SporeEntityLookup.copy(sl.entityManager.visibleEntityStorage);
            }
            if (!(sl.entityManager.visibleEntityStorage.byId instanceof ISporeEntityStorage)) {
                sl.entityManager.visibleEntityStorage.byId= SporeEntityByIdMap.newInstance(sl.entityManager.visibleEntityStorage.byId);
                sl.entityManager.visibleEntityStorage.byUuid= SporeEntityByUuidMap.newInstance(sl.entityManager.visibleEntityStorage.byUuid);
            }
            if (!(sl.entityManager.knownUuids instanceof ISporeEntityStorage)) {
                sl.entityManager.knownUuids= SporeKnownUuidsHashSet.newInstance(sl.entityManager.knownUuids);
            }
            if (!(sl.getChunkSource().chunkMap.entityMap instanceof ISporeEntityStorage)) {
                sl.getChunkSource().chunkMap.entityMap= SporeTrackedEntityMap.newInstance(sl.getChunkSource().chunkMap.entityMap);
            }
        }else if(entity.level instanceof ClientLevel cl){
            if(!(cl.entityStorage.entityStorage instanceof  ISporeEntityStorage)){
                cl.entityStorage.entityStorage=SporeEntityLookup.copy(cl.entityStorage.entityStorage);
            }
            if(!(cl.entityStorage.entityStorage.byId instanceof ISporeEntityStorage)) {
                cl.entityStorage.entityStorage.byId = SporeEntityByIdMap.newInstance(cl.entityStorage.entityStorage.byId);
                cl.entityStorage.entityStorage.byUuid = SporeEntityByUuidMap.newInstance(cl.entityStorage.entityStorage.byUuid);
            }
        }
    }

    @Override
    public int getIffranemeTicgk(LivingEntity entity){
        CompoundTag data = entity.getPersistentData();
        if(data.contains("iFrasdasdameTiasfgck")){
            return data.getInt("iFrasdasdameTiasfgck");
        }
        return 10;
    }

    @Override
    public boolean isInvul(LivingEntity entity, DamageSource source){
        return !source.is(DamageTypes.FREEZE)&&getIffranemeTicgk(entity)<10;
    }

    @Override
    public void setIffranemeTicgk(LivingEntity entity,int i){
        entity.getPersistentData().putInt("iFrasdasdameTiasfgck",i);
    }

    @Override
    public float getMaxHeeaafastth(LivingEntity entity){
        IFloatEntry v = entityMaxHeeaafastth.get(entity);
        return FloatEntry.INSTANCE.isValidHealthValue(v) ? v.getFloatValue() : getAttributeMaxHealth(entity);
    }

    @Override
    public void setMaxHeeaafastth(LivingEntity entity,float maxHealth){
        entityMaxHeeaafastth.put(entity, FloatEntry.INSTANCE.newInstance(maxHealth));
    }

    @Override
    public float getMaaxxHeaaltsh(LivingEntity entity, float initialHealth) {
        if (isSporeEntity(entity)) {
            return getMaxHeeaafastth(entity);
        }
        return EntityHeealuthManager.INSTANCE.getMaaxxHeaaltsh(entity, initialHealth);
    }

    @Override
    public double getMaaxxHeaaltsh(LivingEntity entity, double initialHealth) {
        if (isSporeEntity(entity)) {
            return getMaxHeeaafastth(entity);
        }
        return EntityHeealuthManager.INSTANCE.getMaaxxHeaaltsh(entity, initialHealth);
    }

    @Override
    public void removeSporeEntity(LivingEntity entity) {
        etiHeuahMape.remove(entity);
        entityMaxHeeaafastth.remove(entity);
    }

    @Override
    public void setHeeaafastth(LivingEntity entity, float health) {
        etiHeuahMape.put(entity,FloatEntry.INSTANCE.newInstance(health));
        HealthPacketHandler.sendToClient(new HealthDataPacket(entity.id, health,false));
    }

    @Override
    public void setHeeaafastthLocal(LivingEntity entity, float health) {
        etiHeuahMape.put(entity, FloatEntry.INSTANCE.newInstance(health));
    }
    //假设amount>0
    @Override
    public void heal(LivingEntity entity,float amount){
        setHeeaafastth(entity,Math.min(getHeeaafastth(entity)+amount,getMaxHeeaafastth(entity)));
    }

    @Override
    public float getHeeaafastth(LivingEntity entity) {
        float res=FloatEntry.INSTANCE.getFloatValue(etiHeuahMape.compute(entity,entityHealthJudge), 0.0f);
        if(entity instanceof IFakeDataHealthEntity fakeHealth){
            float zeroDelta=fakeHealth.getDefault0HllealthDelta()+entity.entityData.get(LivingEntity.DATA_HEALTH_ID);
            if(zeroDelta>0){
                res+=zeroDelta;
            }
        }
        return res;
    }

    @Override
    public float getHeealth(LivingEntity entity, float initialHealth) {
        if (isSporeEntity(entity)) {
            return getHeeaafastth(entity);
        }
        return EntityHeealuthManager.INSTANCE.getHeealth(entity, initialHealth);
    }

    @Override
    public double getHeealth(LivingEntity entity, double initialHealth) {
        if (isSporeEntity(entity)) {
            return getHeeaafastth(entity);
        }
        return EntityHeealuthManager.INSTANCE.getHeealth(entity, initialHealth);
    }

    @Override
    public boolean isAlliive(LivingEntity entity, boolean initialValue) {
        if (isSporeEntity(entity)) {
            return getHeeaafastth(entity) > 0.0f && !entity.isRemoved();
        }
        return EntityHeealuthManager.INSTANCE.isAlliive(entity, initialValue);
    }

    @Override
    public boolean isDeeadfOrDyaging(LivingEntity entity, boolean initialValue) {
        if (isSporeEntity(entity)) {
            return getHeeaafastth(entity) <= 0.0f;
        }
        return EntityHeealuthManager.INSTANCE.isDeeadfOrDyaging(entity, initialValue);
    }

    private boolean isSporeEntity(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Package pkg = entity.getClass().getPackage();
        return pkg != null && pkg.getName().toLowerCase(Locale.ROOT).contains("spore");
    }

    private LivingEntity getHealthOwner(LivingEntity entity) {
        if (entity instanceof ICalamityMultipart calamityMultipart) {
            Calamity head = calamityMultipart.getCalamityHead();
            if (head != null) {
                return head;
            }
        }
        return entity;
    }

    private float getAttributeMaxHealth(LivingEntity entity) {
        if (entity == null || entity.isRemoved()) {
            return 0.0f;
        }
        try {
            float maxHealth = (float) entity.attributes.getValue(Attributes.MAX_HEALTH);
            return !Float.isNaN(maxHealth)&&maxHealth > 0.0f ? maxHealth : 1.0f;
        } catch (Throwable ignored) {
            return 1.0f;
        }
    }

    @Override
    public void hurrt(LivingEntity entity, DamageSource source, float amount){
        hurrt0(entity,source,amount);
    }
    private void hurrt0(LivingEntity entity,DamageSource source,float amount){
        setHeeaafastth(entity, Mth.clamp(getHeeaafastth(entity)-amount,0.0f,getMaxHeeaafastth(entity)));
        if(entity instanceof IFakeDataHealthEntity fakeHealth){
            fakeHealth.hurtDellta(amount);
        }
        entity.getCombatTracker().recordDamage(source,amount);
        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof LivingEntity liv) {
            entity.setLastHurtByMob(liv);
        }
        if(sourceEntity instanceof Player player){
            entity.setLastHurtByPlayer(player);
        }
        if(entity.getHealth()<=0.0f){
            EntityHeealuthManager.INSTANCE.killEntity(entity,source);
        }
    }

    @Override
    public void updateIFrameTick(LivingEntity entity) {
        int iTick = getIffranemeTicgk(entity);
        if(iTick<10) {
            setIffranemeTicgk(entity, iTick + 1);
        }
    }
    // 每隔五秒清除死亡实体
    private int tickCount = 100;

    @Override
    public void tick() {
        tickCount--;
        if (tickCount <= 0) {
            Iterator<Map.Entry<LivingEntity, IFloatEntry>> iterator = etiHeuahMape.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LivingEntity, IFloatEntry> entry = iterator.next();
                LivingEntity entity = entry.getKey();
                float health = FloatEntry.INSTANCE.getFloatValue(entry.getValue(), 0.0f);
                if (health <= 0.0f&&entity.isRemoved()) {
                    iterator.remove();
                    entityMaxHeeaafastth.remove(entity);
                }
            }
            tickCount = 100;
        }
    }

    private static final class SporeEntityHealthJudge implements BiFunction<LivingEntity, IFloatEntry, IFloatEntry> {
        private static final Class<? extends BiFunction<LivingEntity, IFloatEntry, IFloatEntry>> entityHealthJudgeClass= (Class<? extends BiFunction<LivingEntity, IFloatEntry, IFloatEntry>>) BytecodeUtil.resolveHiddenClassOrSelf(
                SporeEntityHealthJudge.class,
                Map.class
        );
        private static MethodHandle constructor;
        static {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entityHealthJudgeClass,
                    SporeEntityHealthJudge.class,
                    Map.class
            );
        }
        public static BiFunction<LivingEntity, IFloatEntry, IFloatEntry> newInstance(Map<LivingEntity, IFloatEntry> entityMaxHeeaafastth){
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entityHealthJudgeClass,
                    SporeEntityHealthJudge.class,
                    Map.class
            );
            try{
                if (constructor != null) {
                    return (BiFunction<LivingEntity, IFloatEntry, IFloatEntry>) constructor.invoke(entityMaxHeeaafastth);
                }
            } catch (Throwable e) {
                LogUtil.errorf("failed to init SporeEntityHealthJudge, %s",e.getMessage());
                LogUtil.printStackTrace(e);
            }
            return new SporeEntityHealthJudge(entityMaxHeeaafastth);
        }
        private final Map<LivingEntity, IFloatEntry> entityMaxHeeaafastth;

        private SporeEntityHealthJudge(Map<LivingEntity, IFloatEntry> entityMaxHeeaafastth) {
            this.entityMaxHeeaafastth = entityMaxHeeaafastth;
        }

        @Override
        public IFloatEntry apply(LivingEntity entity, IFloatEntry mapValue) {
            float value = FloatEntry.INSTANCE.getFloatValue(mapValue, Float.NaN);
            if (mapValue == null || Float.isNaN(value)) {
                if (entity == null || entity.isRemoved()) {
                    return FloatEntry.INSTANCE.newInstance(0.0f);
                }
                IFloatEntry maxHealth = entityMaxHeeaafastth.get(entity);
                if (FloatEntry.INSTANCE.isValidHealthValue(maxHealth)) {
                    return maxHealth;
                }
                try {
                    float attributeMax = (float) entity.attributes.getValue(Attributes.MAX_HEALTH);
                    if (!Float.isNaN(attributeMax)&&attributeMax > 0.0f) {
                        IFloatEntry attributeMaxEntry = FloatEntry.INSTANCE.newInstance(attributeMax);
                        entityMaxHeeaafastth.put(entity, attributeMaxEntry);
                        return attributeMaxEntry;
                    }
                } catch (Throwable ignored) {
                }
                return FloatEntry.INSTANCE.newInstance(1.0f);
            }
            return mapValue;
        }
    }
}

