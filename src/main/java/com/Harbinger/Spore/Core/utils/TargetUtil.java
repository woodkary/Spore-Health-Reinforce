package com.Harbinger.Spore.Core.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TargetUtil implements ITarget {
    public static final ITarget INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            ITarget.class,
            TargetUtil.class
    );
    public LivingEntity getLivingEntityByUUID(Level level, UUID uuid) {
        return getLivingEntityByUUID(level.getServer(), uuid);
    }

    public LivingEntity getLivingEntityByUUID(MinecraftServer server, UUID uuid) {
        if (uuid != null && server != null) {

            for (ServerLevel world : server.getAllLevels()) {
                Entity entity = world.getEntity(uuid);
                if (entity instanceof LivingEntity livingEntity) {
                    return livingEntity;
                }
            }
        }

        return null;
    }
}
