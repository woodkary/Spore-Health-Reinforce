package com.Harbinger.Spore.Core.asmHooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface IGetTargetHook {
    LivingEntity getTargetCallSiteHook(Mob mob, LivingEntity original);
}
