package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;

public interface ICustomLifeCycleEntity {
    LivingEntity entity();
    default void initCustom(){
        LivingEntity entity = entity();
        SporeEntityHeeaafastthManager.INSTANCE.initSporeEntity(entity);
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.createWrapppper(entity);
    }
    default void healSelf(float amount) {
        LivingEntity entity = entity();
        if (amount > 0.0F) {
            float f = SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(entity);
            if (f > 0.0F) {
                SporeEntityHeeaafastthManager.INSTANCE.heal(entity, amount);
            }

        }
    }
    default boolean isProtoOrCalamity(){
        LivingEntity entity = entity();
        return entity instanceof Proto||entity instanceof Calamity;
    }
    default void tickCustomLifeCycle(){
        SporeEntityHeeaafastthManager.INSTANCE.updateIFrameTick(entity());
    }
    default void actualHurt(DamageSource source, float damage) {
        LivingEntity liv=entity();
        boolean isProtoOrCalamity = isProtoOrCalamity();
        //普通生物可以直接受伤，Proto和Calamity需要经过无敌帧
        if (!liv.isInvulnerableTo(source)&&
                (!isProtoOrCalamity||!SporeEntityHeeaafastthManager.INSTANCE.isInvul(liv,source))) {
            boolean isFreezeDamage = source.is(DamageTypes.FREEZE);
            //Proto和Calamity会有限伤
            boolean shouldLimitDamage = isProtoOrCalamity && !isFreezeDamage;
            damage = ForgeHooks.onLivingHurt(liv, source, damage);
            float reduceRate = 1.0f;
            if(shouldLimitDamage){
                reduceRate = 0.4f;
                damage = Math.min(liv.getMaxHealth() * reduceRate, damage);
            }
            SporeEntityHeeaafastthManager.INSTANCE.setIffranemeTicgk(liv,0);
            if (damage <= 0.0F) {
                return;
            }
            liv.hurtArmor(source,damage);
            if(!isFreezeDamage){
                damage= SporeAttackUtil.INSTANCE.damageReduction(liv,damage, source);
            }
            float f1 = Math.max(damage - liv.getAbsorptionAmount(), 0.0F);
            liv.setAbsorptionAmount(liv.getAbsorptionAmount() - (damage - f1));
            float f = damage - f1;
            if (f > 0.0F && f < 3.4028235E37F) {
                Entity entity = source.getEntity();
                if (entity instanceof ServerPlayer serverplayer) {
                    serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
                }
            }
            float oldF1=f1;
            f1=ForgeHooks.onLivingDamage(liv, source, f1);
            //新的f1不能大于原来的f1
            if(shouldLimitDamage) {
                f1 = Math.min(f1, oldF1);
            }
            if (f1 != 0.0F) {
                if(shouldLimitDamage) {
                    f1 = Math.min(liv.getMaxHealth() * reduceRate, f1);
                }
                liv.getCombatTracker().recordDamage(source, f1);
                SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv, Mth.clamp(
                        SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(liv) - f1,
                        0.0f,
                        SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(liv)));
                liv.setAbsorptionAmount(liv.getAbsorptionAmount() - f1);
                liv.gameEvent(GameEvent.ENTITY_DAMAGE);
            }
        }
    }
    default void addSaveData(CompoundTag tag){
        LivingEntity liv = entity();
        tag.putFloat("sporeHefaaltytah", SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(liv));
    }
    default void readSaveData(CompoundTag tag){
        LivingEntity liv = entity();
        if(tag.tags.size()<5|| StackTraceUtil.isCallFromOther()) {
            return;
        }
        if(tag.contains("sporeHefaaltytah")){
            SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv,tag.getFloat("sporeHefaaltytah"));
        }
    }
}
