package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.HeasdalthUtil;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.network.HealthDeltaPacket;
import com.Harbinger.Spore.network.HealthDeltaPacketHandler;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class EntityHeealuthManager implements IEntityHealth {
    private static final String SPORE_DEAD_FLAG = "SporeDeeaadfd";
    private static final String LEGACY_SPORE_DEAD_FLAG = "sporeDeeaadfd";
    public static final IEntityHealth INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IEntityHealth.class,
            EntityHeealuthManager.class
    );
    private final Map<LivingEntity,Float> heaalthDeltaMap=new WeakHashMap<>();
    private final Map<LivingEntity,Float> heaalthDeltaMapClient=new WeakHashMap<>();
    private final Map<Entity,Boolean> serverNoRecurs=new WeakHashMap<>();
    @OnlyIn(Dist.CLIENT)
    private final Map<Entity,Boolean> clientNoRecurs=new WeakHashMap<>();
    @Override
    public SynchedEntityData getEmptyEntityData(Entity entity) {
        return new SynchedEntityData(entity);
    }
    private boolean isTrueDeeauthCalled(Entity entity) {
        return entity.level.isClientSide?
                clientNoRecurs.getOrDefault(entity,false):
                serverNoRecurs.getOrDefault(entity,false);
    }
    private Boolean setTrueDeeauthCalled(Entity entity,boolean value){
        return entity.level.isClientSide?
                clientNoRecurs.put(entity,value):
                serverNoRecurs.put(entity,value);
    }
    private Boolean removeTrueDeeauthMark(Entity entity){
        return entity.level.isClientSide?
                clientNoRecurs.remove(entity):
                serverNoRecurs.remove(entity);
    }
    public float getHeealtthDelta(LivingEntity entity){
        return (entity.level.isClientSide?heaalthDeltaMapClient:heaalthDeltaMap).getOrDefault(entity,0.0f);
    }
    public float getHeealtthDelta(float initialDelta,Object entity){
        if(entity instanceof LivingEntity liv){
            return getHeealtthDelta(liv,initialDelta);
        }
        return initialDelta;
    }
    public float getHeealtthDelta(float initialDelta, Entity entity){
        if(entity instanceof LivingEntity liv){
            return getHeealtthDelta(liv,initialDelta);
        }
        return initialDelta;
    }
    public float getHeealtthDelta(float initialDelta,LivingEntity entity){
        return getHeealtthDelta(entity,initialDelta);
    }
    public float getHeealtthDelta(LivingEntity entity,float initialDelta){
        if(initialDelta!=initialDelta){
            initialDelta=Float.NEGATIVE_INFINITY;
        }
        return Math.min(initialDelta, getHeealtthDelta(entity));
    }
    public double getHeealtthDelta(double initialDelta,LivingEntity entity){
        return getHeealtthDelta(entity,initialDelta);
    }
    public double getHeealtthDelta(LivingEntity entity,double initialDelta){
        if(initialDelta!=initialDelta){
            initialDelta=Double.NEGATIVE_INFINITY;
        }
        return Math.min(initialDelta,getHeealtthDelta(entity));
    }
    public void setHeealtthDelta(LivingEntity entity,float delta){
        if(entity.level.isClientSide){
            setHeealtthDeltaLocal(entity,delta);
            return;
        }
        heaalthDeltaMap.put(entity,delta);
        HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id,delta));
    }
    public void setHeealtthDeltaLocal(LivingEntity entity,float delta){
        heaalthDeltaMapClient.put(entity,delta);
    }
    public float getMaaxxHeaaltsh(float initialHealth,Entity entity){
        if(entity instanceof LivingEntity liv){
            return getMaaxxHeaaltsh(liv,initialHealth);
        }
        return initialHealth;
    }
    public float getMaaxxHeaaltsh(float initialHealth,LivingEntity entity){
        return getMaaxxHeaaltsh(entity,initialHealth);
    }
    public float getMaaxxHeaaltsh(LivingEntity entity,float initialHealth){
        if(entity instanceof Player){
            return Math.max(initialHealth,20.0f);
        }
        if(hasSporeDeadFlag(entity)){
            return 0.0f;
        }
        return initialHealth;
    }
    public double getMaaxxHeaaltsh(double initialHealth,Entity entity){
        if(entity instanceof LivingEntity liv){
            return getMaaxxHeaaltsh(liv,initialHealth);
        }
        return initialHealth;
    }
    public double getMaaxxHeaaltsh(double initialHealth,LivingEntity entity){
        return getMaaxxHeaaltsh(entity,initialHealth);
    }
    public double getMaaxxHeaaltsh(LivingEntity entity,double initialHealth){
        if(entity instanceof Player){
            return Math.max(initialHealth,20.0);
        }
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(entity);
        }
        if(hasSporeDeadFlag(entity)){
            return 0.0;
        }
        return initialHealth;
    }
    public boolean isAlliive(boolean initialHealth,Object entity){
        if(entity instanceof LivingEntity liv){
            return isAlliive(liv,initialHealth);
        }
        return initialHealth;
    }
    public boolean isAlliive(boolean initialHealth,Entity entity){
        if(entity instanceof LivingEntity liv){
            return isAlliive(liv,initialHealth);
        }
        return initialHealth;
    }
    public boolean isAlliive(boolean initialHealth,LivingEntity entity){
        return isAlliive(entity,initialHealth);
    }
    public boolean isSpectatorOrCreative(Player player){
        try {
            return player.isSpectator() || player.isCreative();
        }catch (Exception ignored){}
        return false;
    }
    public boolean trueDeeauth(Entity entity) {
        return hasSporeDeadFlag(entity)||
                entity instanceof LivingEntity liv &&
                        getHeealtthDelta(liv) <= -liv.getMaxHealth();
    }
    public boolean isAlliive(LivingEntity entity,boolean initialValue){
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(entity)>0.0f&&!entity.isRemoved();
        }
        boolean deadFlag=trueDeeauth(entity);
        if(deadFlag){
            return false;
        }
        return initialValue;
    }
    public boolean isDeeadfOrDyaging(boolean initialValue,Object entity){
        if(entity instanceof LivingEntity liv){
            return isDeeadfOrDyaging(liv,initialValue);
        }
        return initialValue;
    }
    public boolean isDeeadfOrDyaging(boolean initialValue,Entity entity){
        if(entity instanceof LivingEntity liv){
            return isDeeadfOrDyaging(liv,initialValue);
        }
        return initialValue;
    }
    public boolean isDeeadfOrDyaging(boolean initialValue,LivingEntity entity){
        return isDeeadfOrDyaging(entity,initialValue);
    }
    private boolean isSporeEntity(Entity entity) {
        if(entity == null) return false;
        return isSporeClass(entity.getClass());
    }
    private boolean isSporeClass(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null && pkg.getName().toLowerCase(Locale.ROOT).contains("spore");
    }
    private boolean hasSporeDeadFlag(Entity entity) {
        return entity.getPersistentData().contains(SPORE_DEAD_FLAG)
                || entity.getPersistentData().contains(LEGACY_SPORE_DEAD_FLAG);
    }
    public boolean isDeeadfOrDyaging(LivingEntity entity,boolean initialValue){
        boolean deadFlag=trueDeeauth(entity);
        if(deadFlag){
            return true;
        }
        return initialValue;
    }
    public float getHeealth(float initialHealth,Object entity){
        if(entity instanceof LivingEntity liv){
            return getHeealth(liv,initialHealth);
        }
        return initialHealth;
    }
    //通过限制最大生命值来限制实体的生命值
    public float getHeealth(float initialHealth,Entity entity){
        if(entity instanceof LivingEntity liv){
            return getHeealth(liv,initialHealth);
        }
        return initialHealth;
    }
    public float getHeealth(float initialHealth,LivingEntity entity){
        return getHeealth(entity,initialHealth);
    }
    public float getHeealth(LivingEntity entity,float initialHealth){
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(entity);
        }
        try{
            setTrueDeeauthCalled(entity,true);
            float maxHealth = entity.getMaxHealth();
            boolean deadFlag = trueDeeauth(entity);
            if (deadFlag) {
                return 0.0f;
            }
            if (initialHealth != initialHealth) {
                return 0.0f;// NaN
            }
            float delta = heaalthDeltaMap.getOrDefault(entity, 0.0f);
            if (delta != delta) {
                delta = Float.NEGATIVE_INFINITY;
                heaalthDeltaMap.put(entity, delta);
                HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id, delta));
                entity.getPersistentData().putBoolean(SPORE_DEAD_FLAG, true);
            }
            return Math.min(initialHealth, maxHealth + delta);
        }finally {
            setTrueDeeauthCalled(entity,false);
        }
    }
    public double getHeealth(double initialHealth,Object entity){
        if(entity instanceof LivingEntity liv){
            return getHeealth(liv,initialHealth);
        }
        return initialHealth;
    }
    public double getHeealth(double initialHealth,Entity entity){
        if(entity instanceof LivingEntity liv){
            return getHeealth(liv,initialHealth);
        }
        return initialHealth;
    }
    public double getHeealth(double initialHealth,LivingEntity entity){
        return getHeealth(entity,initialHealth);
    }
    public double getHeealth(LivingEntity entity,double initialHealth){
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(entity);
        }
        if(isTrueDeeauthCalled(entity)){
            return initialHealth;
        }
        try{
            setTrueDeeauthCalled(entity,true);
            float maxHealth = entity.getMaxHealth();
            boolean deadFlag = trueDeeauth(entity);
            if (deadFlag) {
                return 0.0;
            }
            if (initialHealth != initialHealth) {
                return 0.0;// NaN
            }
            float delta = heaalthDeltaMap.getOrDefault(entity, 0.0f);
            if (delta != delta) {
                delta = Float.NEGATIVE_INFINITY;
                heaalthDeltaMap.put(entity, delta);
                HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id, delta));
                entity.getPersistentData().putBoolean(SPORE_DEAD_FLAG, true);
            }
            return Math.min(initialHealth, maxHealth + delta);
        }finally {
            setTrueDeeauthCalled(entity,false);
        }
    }
    public float hurt(LivingEntity entity, float damage){
        Float delta = heaalthDeltaMap.get(entity);
        float health=entity.getHealth();
        if (delta == null) {
            delta = Math.max(0.0F, entity.getMaxHealth() - health) + damage;
            delta = -delta;
        } else {
            delta -= damage;
        }
        heaalthDeltaMap.put(entity, delta);
        HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id,delta));
        return health;
    }
    public void hurt(LivingEntity entity, float damage, DamageSource source){
        if(damage<=0.0f){
            heal(entity,-damage,source);
            return;
        }
        if(isSporeEntity(entity)){
            SporeEntityHeeaafastthManager.INSTANCE.hurrt(entity,source,damage);
            return;
        }
        float health = hurt(entity, damage);
        DamageSource actualSource = source != null ? source : entity.damageSources().generic();
        if (!HeasdalthUtil.INSTANCE.invokeAllHurtMethods(entity, actualSource, damage, health)) {
            HeasdalthUtil.INSTANCE.setHeeaatth(entity, Math.max(0.0F, health - damage), true);
        }
        if(source!=null){
            entity.getCombatTracker().recordDamage(source,damage);
        }
        if(entity.getHealth()<=0.0f){
            killEntity(entity,source);
        }
    }
    public void killEntity(LivingEntity entity,DamageSource source) {
        if (source != null) {
            entity.getCombatTracker().recordDamage(source, Float.POSITIVE_INFINITY);
        }
        if(isSporeEntity(entity)){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(entity,0.0f);
            return;
        }
        setHeealtthDelta(entity, Float.NEGATIVE_INFINITY);
        HeasdalthUtil.INSTANCE.die(entity, source != null ? source : entity.damageSources().genericKill());
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.createDeathWrapppper(entity);
        entity.getPersistentData().putBoolean(SPORE_DEAD_FLAG, true);
    }
    public void heal(LivingEntity entity,float heal){
        Float delta = heaalthDeltaMap.get(entity);
        if (delta == null) {
            delta=0.0f;
        }else{
            delta =Math.min(delta+heal,0.0f);
        }
        heaalthDeltaMap.put(entity, delta);
        HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id,delta));
    }
    public void heal(LivingEntity entity,float heal,DamageSource source){
        if(heal<0.0f){
            hurt(entity,-heal,source);
            return;
        }
        if(isSporeEntity(entity)){
            SporeEntityHeeaafastthManager.INSTANCE.heal(entity,heal);
            return;
        }
        heal(entity,heal);
    }
}
