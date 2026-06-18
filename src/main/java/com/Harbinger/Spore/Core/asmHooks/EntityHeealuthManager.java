package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.network.HealthDeltaPacket;
import com.Harbinger.Spore.network.HealthDeltaPacketHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EntityHeealuthManager implements IEntityHealth {
    public static final IEntityHealth INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IEntityHealth.class,
            EntityHeealuthManager.class
    );
    public static final Object NULL_OBJECT=new Object();
    private static final int REMOVE_ENTITIES_COUNT=10;
    private static final int MAX_QUEUE_SIZE=1000;
    private static final String SPORE_DEAD_FLAG = "SporeDeeaadfd";
    private final Map<LivingEntity,IFloatEntry> heaalthDeltaMap= ProtectedConcurrentHashMap.newInstance();
    private final Map<Entity,Boolean> serverNoRecurs=new WeakHashMap<>();
    private final Queue<Entity> pendingEntities= new ConcurrentLinkedQueue<>();
    private final Map<Entity,Object> queuingEntities= new ConcurrentHashMap<>();
    @OnlyIn(Dist.CLIENT)
    private final Map<Entity,Boolean> clientNoRecurs=new WeakHashMap<>();
    private int tickCount=0;

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
    @Override
    public void setPlayerAlliive(Player player){
        player.getPersistentData().remove(SPORE_DEAD_FLAG);
        if(!player.level.isClientSide){
            setHeealtthDelta(player,0.0f);
        }else{
            setHeealtthDeltaLocal(player,0.0f);
        }

    }
    public void tick(){
        tickCount+=1;
        //每隔20秒全实体扫描
        if(tickCount%400==0){
            for (LivingEntity entity : heaalthDeltaMap.keySet()) {
                if (shouldQueueForCleanup(entity)) {
                    markDeleted(entity);
                }
            }
        }
        //每隔40秒释放一次内存
        if(tickCount%800==0){
            //首先保证队列大小不超过1000
            while(!pendingEntities.isEmpty()&&pendingEntities.size()>MAX_QUEUE_SIZE){
                Entity toRemove=pendingEntities.poll();
                queuingEntities.remove(toRemove);
                if(toRemove!=null){
                    if (toRemove instanceof LivingEntity livingEntity) {
                        heaalthDeltaMap.remove(livingEntity);
                    }
                    removeTrueDeeauthMark(toRemove);
                }
            }
            //然后再释放10个实体
            int count=0;
            while(!pendingEntities.isEmpty()&&count<REMOVE_ENTITIES_COUNT){
                Entity toRemove=pendingEntities.poll();
                queuingEntities.remove(toRemove);
                if(toRemove!=null){
                    if (toRemove instanceof LivingEntity livingEntity) {
                        heaalthDeltaMap.remove(livingEntity);
                    }
                    removeTrueDeeauthMark(toRemove);
                }
                count+=1;
            }
        }
    }
    @Override
    public EntityTickList getEntityTickList(Level level){
        if(level instanceof ServerLevel sl){
            return sl.entityTickList;
        }else if(level instanceof ClientLevel cl){
            return cl.tickingEntities;
        }
        return null;
    }
    @Override
    public EntityLookup<? extends EntityAccess> getEntityLookup(Level level){
        if(level instanceof ServerLevel sl){
            return sl.entityManager.visibleEntityStorage;
        }else if(level instanceof ClientLevel cl){
            return cl.entityStorage.entityStorage;
        }
        return null;
    }
    @Override
    public Long2ObjectMap<EntitySection<Entity>> getEntitySections(Level level) {
        if(level instanceof ServerLevel sl){
            return sl.entityManager.sectionStorage.sections;
        }else if(level instanceof ClientLevel cl){
            return cl.entityStorage.sectionStorage.sections;
        }
        return null;
    }
    private boolean shouldQueueForCleanup(Entity entity) {
        if (entity == null || queuingEntities.containsKey(entity) || !entity.isRemoved()) {
            return false;
        }
        try {
            if (entity.level == null) {
                return true;
            }
            var tickList = getEntityTickList(entity.level);
            if (tickList != null && tickList.contains(entity)) {
                return false;
            }
            EntityLookup<?> lookup = getEntityLookup(entity.level);
            if (lookup == null) {
                return true;
            }
            return !lookup.byUuid.containsKey(entity.uuid) && !lookup.byId.containsKey(entity.id);
        } catch (Throwable ignored) {
            return false;
        }
    }
    public void markDeleted(Entity entity){
        if (entity == null) {
            return;
        }
        if (queuingEntities.containsKey(entity)) {
            return;
        }
        pendingEntities.offer(entity);
        queuingEntities.put(entity, NULL_OBJECT);
    }
    public boolean containsDeltaKey(LivingEntity entity){
        return heaalthDeltaMap.containsKey(entity);
    }
    public float getHeealtthDelta(LivingEntity entity){
        return FloatEntry.INSTANCE.getFloatValue(heaalthDeltaMap.get(entity), 0.0f);
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
        heaalthDeltaMap.put(entity,FloatEntry.INSTANCE.newInstance(delta));
        HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id,delta));
    }
    public void setHeealtthDeltaLocal(LivingEntity entity,float delta){
        heaalthDeltaMap.put(entity,FloatEntry.INSTANCE.newInstance(delta));
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
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(entity);
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
        return entity.getPersistentData().contains(SPORE_DEAD_FLAG);
    }
    public boolean isDeeadfOrDyaging(LivingEntity entity,boolean initialValue){
        if(isSporeEntity(entity)){
            return SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(entity)<=0.0f;
        }
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
        if(entity instanceof Player player&&isSpectatorOrCreative(player)){
            return Math.max(20.0f,player.getMaxHealth());
        }
        if(isTrueDeeauthCalled(entity)){
            return initialHealth;
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
            float delta = getHeealtthDelta(entity);
            if (delta != delta) {
                delta = Float.NEGATIVE_INFINITY;
                heaalthDeltaMap.put(entity, FloatEntry.INSTANCE.newInstance(delta));
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
        if(entity instanceof Player player&&isSpectatorOrCreative(player)){
            return Math.max(20.0f,player.getMaxHealth());
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
            float delta = getHeealtthDelta(entity);
            if (delta != delta) {
                delta = Float.NEGATIVE_INFINITY;
                heaalthDeltaMap.put(entity, FloatEntry.INSTANCE.newInstance(delta));
                HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id, delta));
                entity.getPersistentData().putBoolean(SPORE_DEAD_FLAG, true);
            }
            return Math.min(initialHealth, maxHealth + delta);
        }finally {
            setTrueDeeauthCalled(entity,false);
        }
    }
    private float hurt(LivingEntity entity, float damage){
        IFloatEntry deltaEntry = heaalthDeltaMap.get(entity);
        float delta;
        float health=entity.getHealth();
        if (deltaEntry == null) {
            delta = Math.max(0.0F, entity.getMaxHealth() - health) + damage;
            delta = -delta;
        } else {
            delta = deltaEntry.getFloatValue();
            delta -= damage;
        }
        heaalthDeltaMap.put(entity, FloatEntry.INSTANCE.newInstance(delta));
        HealthDeltaPacketHandler.sendToClient(new HealthDeltaPacket(entity.id,delta));
        return health;
    }
    public void hurt(LivingEntity entity, float damage, DamageSource source){
        if(damage<=0.0f){
            heal(entity,-damage,source);
            return;
        }
        if(source!=null){
            entity.getCombatTracker().recordDamage(source,damage);
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
        if(entity.getHealth()<=0.0f){
            killEntity(entity,source);
        }
    }
    public void killEntity(LivingEntity entity,DamageSource source) {
        if (source != null) {
            entity.getCombatTracker().recordDamage(source, Float.POSITIVE_INFINITY);
        }
        setHeealtthDelta(entity, Float.NEGATIVE_INFINITY);
        HeasdalthUtil.INSTANCE.die(entity, source != null ? source : entity.damageSources().genericKill());
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.createDeathWrapppper(entity);
        entity.getPersistentData().putBoolean(SPORE_DEAD_FLAG, true);
    }
    public void heal(LivingEntity entity,float heal){
        IFloatEntry deltaEntry = heaalthDeltaMap.get(entity);
        float delta;
        if (deltaEntry == null) {
            delta=0.0f;
        }else{
            delta = deltaEntry.getFloatValue();
            delta =Math.min(delta+heal,0.0f);
        }
        heaalthDeltaMap.put(entity, FloatEntry.INSTANCE.newInstance(delta));
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
