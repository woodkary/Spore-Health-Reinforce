package com.Harbinger.Spore.Compat.l2Hostility;

import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public interface IMobTraits {
    int getTraitLevel(LivingEntity liv, String traitName);
    Map<?,?> getTraits(Object c);
    <T> boolean isMobTraitCapClass(T obj);
}
