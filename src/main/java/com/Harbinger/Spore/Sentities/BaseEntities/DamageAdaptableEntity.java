package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.network.AdaptableHurtColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;

public interface DamageAdaptableEntity {
    float adaptDamage(DamageSource source, float damage);

    AdaptableHurtColor getClientHurtColor();

    boolean hasClientHurtFeedback();

    void readAdaptData(CompoundTag tag);
    void addAdaptData(CompoundTag tag);
}
