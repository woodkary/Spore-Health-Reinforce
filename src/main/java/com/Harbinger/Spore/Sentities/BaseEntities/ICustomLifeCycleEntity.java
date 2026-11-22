package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Compat.srps.SRPJudge;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;

public interface ICustomLifeCycleEntity {
    LivingEntity entity();
    default void initCustom(){
        LivingEntity entity = entity();
        SporeEntityHeeaafastthManager.INSTANCE.initSporeEntity(entity);
        LivingEntityHealthLifecycleWrapperUtil.INSTANCE.createWrapppper(entity);
    }
    default void onRemoved(){
        SporeEntityHeeaafastthManager.INSTANCE.removeSporeEntity(entity());
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
    default boolean livingEntityHurt(DamageSource p_21016_, float p_21017_) {
        LivingEntity liv=entity();
        if (!ForgeHooks.onLivingAttack(liv, p_21016_, p_21017_)) {
            return false;
        } else if (liv.isInvulnerableTo(p_21016_)) {
            return false;
        } else if (liv.level().isClientSide) {
            return false;
        } else if (liv.isDeadOrDying()) {
            return false;
        } else if (p_21016_.is(DamageTypeTags.IS_FIRE) && liv.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (liv.isSleeping() && !liv.level().isClientSide) {
                liv.stopSleeping();
            }

            liv.noActionTime = 0;
            float f = p_21017_;
            boolean flag = false;
            if (p_21017_ > 0.0F && liv.isDamageSourceBlocked(p_21016_)) {
                ShieldBlockEvent ev = ForgeHooks.onShieldBlock(liv, p_21016_, p_21017_);
                if (!ev.isCanceled()) {
                    if (ev.shieldTakesDamage()) {
                        liv.hurtCurrentlyUsedShield(p_21017_);
                    }

                    p_21017_ -= ev.getBlockedDamage();
                    if (!p_21016_.is(DamageTypeTags.IS_PROJECTILE)) {
                        Entity entity = p_21016_.getDirectEntity();
                        if (entity instanceof LivingEntity livingentity) {
                            liv.blockUsingShield(livingentity);
                        }
                    }

                    flag = p_21017_ <= 0.0F;
                }

                if (p_21016_.is(DamageTypeTags.IS_FREEZING) && liv.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                    p_21017_ *= 5.0F;
                }
            }

            liv.walkAnimation.setSpeed(1.5F);
            boolean flag1 = true;
            if ((float)liv.invulnerableTime > 10.0F && !p_21016_.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                if (p_21017_ <= liv.lastHurt) {
                    return false;
                }

                if(livingEntityActuallyHurt(p_21016_, p_21017_ - liv.lastHurt)<=0.0f){
                    return false;
                }
                liv.lastHurt = p_21017_;
                flag1 = false;
            } else {
                liv.lastHurt = p_21017_;
                liv.invulnerableTime = 20;
                if(livingEntityActuallyHurt(p_21016_, p_21017_)<=0.0f){
                    return false;
                }
                liv.hurtDuration = 10;
                liv.hurtTime = liv.hurtDuration;
            }

            if (p_21016_.is(DamageTypeTags.DAMAGES_HELMET) && !liv.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                liv.hurtHelmet(p_21016_, p_21017_);
                p_21017_ *= 0.75F;
            }

            Entity entity1 = p_21016_.getEntity();
            if (entity1 != null) {
                if (entity1 instanceof LivingEntity livingentity1) {
                    if (!p_21016_.is(DamageTypeTags.NO_ANGER)) {
                        liv.setLastHurtByMob(livingentity1);
                    }
                }

                if (entity1 instanceof Player player1) {
                    liv.lastHurtByPlayerTime = 100;
                    liv.lastHurtByPlayer = player1;
                } else if (entity1 instanceof TamableAnimal tamableEntity) {
                    if (tamableEntity.isTame()) {
                        liv.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity2 = tamableEntity.getOwner();
                        if (livingentity2 instanceof Player) {
                            liv.lastHurtByPlayer = (Player)livingentity2;
                        } else {
                            liv.lastHurtByPlayer = null;
                        }
                    }
                }
            }

            if (flag1) {
                if (flag) {
                    liv.level().broadcastEntityEvent(liv, (byte)29);
                } else {
                    liv.level().broadcastDamageEvent(liv, p_21016_);
                }

                if (!p_21016_.is(DamageTypeTags.NO_IMPACT) && (!flag || p_21017_ > 0.0F)) {
                    liv.markHurt();
                }

                if (entity1 != null && !p_21016_.is(DamageTypeTags.IS_EXPLOSION)) {
                    double d0 = entity1.getX() - liv.getX();

                    double d1;
                    for(d1 = entity1.getZ() - liv.getZ(); d0 * d0 + d1 * d1 < 1.0E-4; d1 = (Math.random() - Math.random()) * 0.01) {
                        d0 = (Math.random() - Math.random()) * 0.01;
                    }

                    liv.knockback(0.4F, d0, d1);
                    if (!flag) {
                        liv.indicateDamage(d0, d1);
                    }
                }
            }

            if (liv.isDeadOrDying()) {
                if (!liv.checkTotemDeathProtection(p_21016_)) {
                    SoundEvent soundevent = liv.getDeathSound();
                    if (flag1) {
                        liv.playSound(soundevent, liv.getSoundVolume(), liv.getVoicePitch());
                    }

                    liv.die(p_21016_);
                }
            } else if (flag1) {
                liv.playHurtSound(p_21016_);
            }

            boolean flag2 = !flag || p_21017_ > 0.0F;
            if (flag2) {
                liv.lastDamageSource = p_21016_;
                liv.lastDamageStamp = liv.level().getGameTime();
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity1, liv, p_21016_, f, p_21017_, flag);
            }

            return flag2;
        }
    }
    default void tickCustomLifeCycle(){
        SporeEntityHeeaafastthManager.INSTANCE.updateIFrameTick(entity());
    }
    float livingEntityActuallyHurt(DamageSource source, float amount);
    default float actualHurt(DamageSource source, float damage) {
        LivingEntity liv=entity();
        boolean isProtoOrCalamity = isProtoOrCalamity();
        //普通生物可以直接受伤，Proto和Calamity需要经过无敌帧
        if (!liv.isInvulnerableTo(source)&&
                (!isProtoOrCalamity||!SporeEntityHeeaafastthManager.INSTANCE.isInvul(liv,source))) {
            boolean isFreezeDamage = source.is(DamageTypes.FREEZE);
            boolean shouldLimitSRPDamage=!isFreezeDamage&&SRPJudge.isSRPEntity(source.getEntity());
            int srpLimitRate= (int) (liv.getMaxHealth()/30);
            //Proto和Calamity会有限伤
            boolean shouldLimitDamage = isProtoOrCalamity && !isFreezeDamage;
            damage = ForgeHooks.onLivingHurt(liv, source, damage);
            float reduceRate = 1.0f;
            if(shouldLimitSRPDamage){
                //针对SRP来源的伤害：不超过最大生命值/30，或10点
                damage=Math.min(Math.min(10,srpLimitRate), damage);
            }
            if(shouldLimitDamage){
                reduceRate = 0.4f;
                damage = Math.min(liv instanceof Proto?20.0f:liv.getMaxHealth() * reduceRate, damage);
            }
            SporeEntityHeeaafastthManager.INSTANCE.setIffranemeTicgk(liv,0);
            if (damage <= 0.0F) {
                return 0.0f;
            }
            liv.hurtArmor(source,damage);
            if(!isFreezeDamage){
                damage= SporeAttackUtil.INSTANCE.damageReduction(liv,damage, source);
            }
            if(liv instanceof DamageAdaptableEntity adaptable){
                damage=adaptable.adaptDamage(source,damage);
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
                if(shouldLimitSRPDamage){
                    //针对SRP来源的伤害：不超过最大生命值/30，或10点
                    f1=Math.min(Math.min(10,srpLimitRate), f1);
                }
                if(shouldLimitDamage) {
                    f1 = Math.min(liv instanceof Proto?20.0f:liv.getMaxHealth() * reduceRate, f1);
                }
                liv.getCombatTracker().recordDamage(source, f1);
                SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(liv, Mth.clamp(
                        SporeEntityHeeaafastthManager.INSTANCE.getHeeaafastth(liv) - f1,
                        0.0f,
                        SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(liv)));
                liv.setAbsorptionAmount(liv.getAbsorptionAmount() - f1);
                liv.gameEvent(GameEvent.ENTITY_DAMAGE);
                return f1;
            }
        }
        return 0.0f;
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
