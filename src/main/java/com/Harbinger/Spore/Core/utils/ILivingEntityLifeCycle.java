package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.player.Player;

public interface ILivingEntityLifeCycle {
    Class<?> getOrginalClass(Class<?> wrapperValue);
    void createWrapppper(Object entity);
    void createDeathWrapppper(Object entity);

    Class<?> buildWrapperClass(Class<?> callback);
    Class<?> buildDeathWrapperClass(Class<?> callback);
}
