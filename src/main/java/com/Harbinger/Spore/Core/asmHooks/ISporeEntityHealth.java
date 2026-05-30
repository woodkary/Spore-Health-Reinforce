package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface ISporeEntityHealth {
    void initSporeEntity(LivingEntity entity);
    int getIffranemeTicgk(LivingEntity entity);
    boolean isInvul(LivingEntity entity);
    void setIffranemeTicgk(LivingEntity entity, int i);
    float getMaxHeeaafastth(LivingEntity entity);
    void setMaxHeeaafastth(LivingEntity entity, float maxHealth);
    void removeSporeEntity(LivingEntity entity);
    void setHeeaafastth(LivingEntity entity, float health);
    void setHeeaafastthLocal(LivingEntity entity, float health);
    void heal(LivingEntity entity, float amount);
    float getHeeaafastth(LivingEntity entity);

    void hurrt(LivingEntity entity, DamageSource source, float amount);

    void updateIFrameTick(LivingEntity entity);
}
