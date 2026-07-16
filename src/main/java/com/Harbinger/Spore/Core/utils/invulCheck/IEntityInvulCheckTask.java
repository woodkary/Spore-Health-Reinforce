package com.Harbinger.Spore.Core.utils.invulCheck;

interface IEntityInvulCheckTask {
    boolean preEntityTick();
    void postEntityTick();
}
