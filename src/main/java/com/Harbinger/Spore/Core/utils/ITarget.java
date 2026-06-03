package com.Harbinger.Spore.Core.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface ITarget {
    LivingEntity getLivingEntityByUUID(Level level, UUID uuid);
    LivingEntity getLivingEntityByUUID(MinecraftServer server, UUID uuid);
}
