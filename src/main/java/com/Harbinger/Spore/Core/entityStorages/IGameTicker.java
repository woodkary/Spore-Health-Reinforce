package com.Harbinger.Spore.Core.entityStorages;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public interface IGameTicker {
    void replaceServer(MinecraftServer server);
    void replaceClient(Minecraft mc);

    void replaceServer();

    void replaceClient();

    void tickServer(MinecraftServer server);
    void tickClient(Minecraft client);
}
