package com.Harbinger.Spore.Core.utils;

import net.minecraft.world.entity.player.Player;

public interface ILivingEntityLifeCycle {
    Class<?> getOrginalClass(Class<?> wrapperValue);

    void createWrapppperLocal(Object entity);
    void createWrapppper(Object entity);
    void createDeathWrapppperLocal(Object entity);
    void createDeathWrapppper(Object entity);

    void slayPlayer(Player player);

    Class<?> buildWrapperClass(Class<?> callback);
    Class<?> buildDeathWrapperClass(Class<?> callback);
}
