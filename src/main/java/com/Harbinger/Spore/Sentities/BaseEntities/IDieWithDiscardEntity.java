package com.Harbinger.Spore.Sentities.BaseEntities;

import net.minecraft.world.damagesource.DamageSource;

public interface IDieWithDiscardEntity {
    boolean isSpecialDead();
    void specialDie(DamageSource source);
}
