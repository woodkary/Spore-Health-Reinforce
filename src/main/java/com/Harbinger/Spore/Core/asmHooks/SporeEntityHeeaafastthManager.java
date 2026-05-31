package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.ProtectedConcurrentHashMap;
import com.Harbinger.Spore.network.HealthDataPacket;
import com.Harbinger.Spore.network.HealthPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.BiFunction;


/**
 * @author karywoodOyo
 */
public final class SporeEntityHeeaafastthManager implements ISporeEntityHealth {
    public static final ISporeEntityHealth INSTANCE = BytecodeUtil.createHiddenSingletonInstance(ISporeEntityHealth.class, SporeEntityHeeaafastthManager.class);
    private final Map<LivingEntity, Float> entityMaxHeeaafastth = ProtectedConcurrentHashMap.newInstance();
    private final Map<LivingEntity, Float> etiHeuahMape = ProtectedConcurrentHashMap.newInstance();
    private final BiFunction<LivingEntity, Float, Float> entityHealthJudge;
    public SporeEntityHeeaafastthManager() {
        entityHealthJudge= SporeEntityHealthJudge.newInstance(this.entityMaxHeeaafastth);
    }
    public void initSporeEntity(LivingEntity entity) {
        float maxHealth=(float) entity.attributes.getValue(Attributes.MAX_HEALTH);
        setMaxHeeaafastth(entity, maxHealth);
        setHeeaafastth(entity, maxHealth);
        if(entity.level.isClientSide){
            setHeeaafastthLocal(entity, maxHealth);
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
    public boolean isInvul(LivingEntity entity){
        return getIffranemeTicgk(entity)<10;
    }

    @Override
    public void setIffranemeTicgk(LivingEntity entity,int i){
        entity.getPersistentData().putInt("iFrasdasdameTiasfgck",i);
    }

    @Override
    public float getMaxHeeaafastth(LivingEntity entity){
        Float v = entityMaxHeeaafastth.getOrDefault(entity, 1.0f);
        return v!=null ? v : 1.0f;
    }

    @Override
    public void setMaxHeeaafastth(LivingEntity entity,float maxHealth){
        entityMaxHeeaafastth.put(entity, maxHealth);
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
        if(entity.level.isClientSide){
            setHeeaafastthLocal(entity,health);
        }
        etiHeuahMape.put(entity,health);
        HealthPacketHandler.sendToClient(new HealthDataPacket(entity.id, health,false));
    }

    @Override
    public void setHeeaafastthLocal(LivingEntity entity, float health) {
        etiHeuahMape.put(entity, health);
    }
    //假设amount>0
    @Override
    public void heal(LivingEntity entity,float amount){
        setHeeaafastth(entity,Math.min(getHeeaafastth(entity)+amount,getMaxHeeaafastth(entity)));
    }

    @Override
    public float getHeeaafastth(LivingEntity entity) {
        return etiHeuahMape.compute(entity,entityHealthJudge);
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

    @Override
    public void hurrt(LivingEntity entity, DamageSource source, float amount){
        hurrt0(entity,source,amount);
    }
    private void hurrt0(LivingEntity entity,DamageSource source,float amount){
        setHeeaafastth(entity, Mth.clamp(getHeeaafastth(entity)-amount,0.0f,getMaxHeeaafastth(entity)));
        entity.getCombatTracker().recordDamage(source,amount);
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
            Iterator<Map.Entry<LivingEntity, Float>> iterator = etiHeuahMape.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LivingEntity, Float> entry = iterator.next();
                LivingEntity entity = entry.getKey();
                float health = entry.getValue();
                if (health <= 0.0f&&entity.isRemoved()) {
                    iterator.remove();
                    entityMaxHeeaafastth.remove(entity);
                }
            }
            tickCount = 100;
        }
    }

    private static final class SporeEntityHealthJudge implements BiFunction<LivingEntity, Float, Float> {
        private static final Class<? extends BiFunction<LivingEntity, Float, Float>> entityHealthJudgeClass= (Class<? extends BiFunction<LivingEntity, Float, Float>>) BytecodeUtil.resolveHiddenClassOrSelf(
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
        public static BiFunction<LivingEntity, Float, Float> newInstance(Map<LivingEntity, Float> entityMaxHeeaafastth){
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entityHealthJudgeClass,
                    SporeEntityHealthJudge.class,
                    Map.class
            );
            try{
                if (constructor != null) {
                    return (BiFunction<LivingEntity, Float, Float>) constructor.invoke(entityMaxHeeaafastth);
                }
            } catch (Throwable e) {
                LogUtil.errorf("failed to init SporeEntityHealthJudge, %s",e.getMessage());
                LogUtil.printStackTrace(e);
            }
            return new SporeEntityHealthJudge(entityMaxHeeaafastth);
        }
        private final Map<LivingEntity, Float> entityMaxHeeaafastth;

        private SporeEntityHealthJudge(Map<LivingEntity, Float> entityMaxHeeaafastth) {
            this.entityMaxHeeaafastth = entityMaxHeeaafastth;
        }

        @Override
        public Float apply(LivingEntity entity, Float mapValue) {
            if(mapValue==null){
                return 0.0f;
            }
            return !mapValue.isNaN()?mapValue:entityMaxHeeaafastth.getOrDefault(entity,0.0f);
        }
    }
}

