package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.Entity;

/**
 * @author karywoodOyo
 */
public class SporeJudge {
    public static boolean isSporeEntity(Entity entity) {
        if(entity == null) return false;
        return isSporeClass(entity.getClass());
    }
    public static boolean isSporeClass(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null && pkg.getName().contains("Spore");
    }
}
