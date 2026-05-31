package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;

public interface ICustomLifeCycleEntity {
    default LivingEntity entity(){
        return (LivingEntity) this;
    }
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
    default void tickCustomLifeCycle(){
        SporeEntityHeeaafastthManager.INSTANCE.updateIFrameTick(entity());
    }
    default void actualHurt(DamageSource source, float damage) {
        LivingEntity liv=entity();
        if (!liv.isInvulnerableTo(source)&&!SporeEntityHeeaafastthManager.INSTANCE.isInvul(liv)) {
            damage = ForgeHooks.onLivingHurt(liv, source, damage);
            float reduceRate = 0.4f;
            damage = Math.min(liv.getMaxHealth() * reduceRate, damage);
            SporeEntityHeeaafastthManager.INSTANCE.setIffranemeTicgk(liv,0);
            if (damage <= 0.0F) {
                return;
            }
            liv.hurtArmor(source,damage);
            damage= damageReduction(liv,damage, source);
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
            ForgeHooks.onLivingDamage(liv, source, f1);
            //新的f1不能大于原来的f1
            f1=Math.min(f1,oldF1);
            if (f1 != 0.0F) {
                f1 = Math.min(liv.getMaxHealth() * reduceRate, f1);
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
    default float damageReduction(LivingEntity entity, float rawDamage, DamageSource source){
        // 原版护甲减伤
        float armor = (float) entity.attributes.getValue(Attributes.ARMOR);
        float toughness = (float) entity.attributes.getValue(Attributes.ARMOR_TOUGHNESS);

        // Vanilla reduction formula
        float armorReduction = 1.0F - Math.min(20.0F, Math.max(armor / 5.0F,
                armor - rawDamage / (2.0F + toughness / 4.0F))) / 25.0F;
        float reducedDamage = rawDamage * armorReduction;

        //抗性减伤
        if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            int reduction = (entity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - reduction;
            float f = reducedDamage * (float)j;
            float d = reducedDamage;
            reducedDamage = Math.max(f / 25.0F, 0.0F);
            float damageLeft = d - reducedDamage;
            if (damageLeft > 0.0F && damageLeft < 3.4028235E37F) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED), Math.round(damageLeft * 10.0F));
                }
            }
        }

        // 附魔减伤（Protection）
        int protLevel = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
        if (protLevel > 0) {
            reducedDamage *= (1.0F - protLevel * 0.04F);
        }



        return reducedDamage;
    }
}
