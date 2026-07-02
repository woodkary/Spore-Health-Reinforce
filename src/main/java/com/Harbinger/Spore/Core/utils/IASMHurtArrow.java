package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;

public interface IASMHurtArrow {
    void wrap(Object arrow);

    Class<?> getOrginalClass(Class<?> wrapperValue);

    void onHitEntityHook(Projectile arrow, EntityHitResult result);
}
