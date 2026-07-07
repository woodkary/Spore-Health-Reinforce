package com.Harbinger.Spore.Core.utils.effects;

import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.unremovableCollections.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;
import java.util.*;

public final class SporeEffectsUtil implements IEffectManager {
    public static final IEffectManager INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IEffectManager.class,
            SporeEffectsUtil.class
    );
    @Override
    public boolean checkEffect(MobEffectInstance effect){
        return effect != null && effect.getEffect()==Seffects.HEALING_INHIBITION.get();
    }
    @Override
    public boolean checkAndAddEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source){
        if(!checkEffect(effect)){
            return false;
        }
        forceAddEffect(target, effect, source);
        return true;
    }
    @Override
    public void forceAddEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source) {
        if(target.activeEffects.getClass()!=SporeMapProxy.mapClass){
            target.activeEffects=SporeMapProxy.newInstance(target.activeEffects);
        }
        MobEffectInstance mobeffectinstance;
        if(target.activeEffects instanceof ISporeMap<MobEffect, MobEffectInstance> sporeMap){
            mobeffectinstance = sporeMap.actualPut(effect.getEffect(), effect);
        }else{
            mobeffectinstance=target.activeEffects.put(effect.getEffect(), effect);
        }
        if (mobeffectinstance == null) {
            target.onEffectAdded(effect, source);
        } else {
            target.onEffectUpdated(effect, true, source);
        }
        if(effect.getEffect()==Seffects.HEALING_INHIBITION.get()&&!EntityHeealuthManager.INSTANCE.containsDeltaKey(target)){
            EntityHeealuthManager.INSTANCE.hurt(target,0.0f);
        }
    }
    @Override
    public Collection<MobEffectInstance> getActiveEffectsHook(LivingEntity target, Collection<MobEffectInstance> initial) {
        MobEffect healInhibit = Seffects.HEALING_INHIBITION.get();
        if(target.activeEffects.containsKey(healInhibit)){
            Set<MobEffectInstance> set = new HashSet<>(initial);
            set.add(target.activeEffects.get(healInhibit));
            return set;
        }
        return initial;
    }
    @Override
    public Map<MobEffect, MobEffectInstance> getActiveEffectsMapHook(LivingEntity target, Map<MobEffect, MobEffectInstance> initial) {
        MobEffect healInhibit = Seffects.HEALING_INHIBITION.get();
        if(target.activeEffects.containsKey(healInhibit)){
            Map<MobEffect, MobEffectInstance> map=new HashMap<>(initial);
            map.put(healInhibit,target.activeEffects.get(healInhibit));
            return map;
        }
        return initial;
    }
    @Override
    public boolean hasEffectHook(LivingEntity target, MobEffect effect, boolean initial){
        MobEffect healInhibit = Seffects.HEALING_INHIBITION.get();
        if(target.activeEffects.containsKey(healInhibit)&&effect==healInhibit){
            return true;
        }
        return initial;
    }
    @Override
    public MobEffectInstance getEffectHook(LivingEntity target, MobEffect effect, MobEffectInstance initial){
        MobEffect healInhibit = Seffects.HEALING_INHIBITION.get();
        if(target.activeEffects.containsKey(healInhibit)&&effect==healInhibit){
            return target.activeEffects.get(healInhibit);
        }
        return initial;
    }

    @Override
    public boolean removeEffect(LivingEntity target, MobEffect effect) {
        MobEffectInstance mobeffectinstance;
        if(target.activeEffects instanceof ISporeMap<MobEffect, MobEffectInstance> sporeEffectMap){
            mobeffectinstance=sporeEffectMap.actualRemove(effect);
        }else{
            mobeffectinstance=target.activeEffects.remove(effect);
        }
        if (mobeffectinstance != null) {
            target.onEffectRemoved(mobeffectinstance);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void accept(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity entity = livingTickEvent.getEntity();
        if(!(entity.activeEffects instanceof ISporeMap<MobEffect, MobEffectInstance> sporeEffectMap)){
            return;
        }
        Iterator<MobEffect> temp = sporeEffectMap.keySet().iterator();
        if(!(temp instanceof ISporeIterator<MobEffect> iterator)){
            return;
        }
        boolean foundHealInhibit=false;
        try {
            while(iterator.hasNext()) {
                MobEffect mobeffect = iterator.next();
                MobEffectInstance mobeffectinstance = sporeEffectMap.get(mobeffect);
                if (!mobeffectinstance.hasRemainingDuration()&&!entity.level().isClientSide) {
                    //如果补充迭代成功删除了禁疗效果，则将activeEffects归还给普通HashMap管理
                    foundHealInhibit|=mobeffectinstance.getEffect()== Seffects.HEALING_INHIBITION.get();
                    iterator.actualRemove();
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        if(foundHealInhibit){
            //回退到普通HashMap
            entity.activeEffects= new HashMap<>(entity.activeEffects);
        }
    }
}
