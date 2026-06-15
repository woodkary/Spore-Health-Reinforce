package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface IHeasdalthUtil {
    void setHeeaatth(LivingEntity entity, float health);
    void setHeeaatth(LivingEntity entity, float health, boolean hard);
    void setHeeaatth(LivingEntity entity, float health, boolean hard, boolean invokeAll);
    void hardSetHeeathtuthWithoutSync(LivingEntity entity, float health, boolean invokeAll);
    boolean invokeAllHurtMethods(LivingEntity entity, DamageSource damageSource, float amount, float currentHealth);
    void die(LivingEntity target, DamageSource source);
    void genericDie(LivingEntity target, DamageSource source);
    float setHealthAdjuster(LivingEntity entity, float health);
}
