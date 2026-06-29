package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeClientLevel;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeMinecraftClient;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeTransientEntitySectionManager;
import com.Harbinger.Spore.Core.entityStorages.serverSide.*;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class GameTickerUtil implements IGameTicker {
    public static IGameTicker INSTANCE= BytecodeUtil.createHiddenSingletonInstance(IGameTicker.class,GameTickerUtil.class);
    public void replaceServer(MinecraftServer server) {
        Class<? extends MinecraftServer> serverClass = null;
        if(server instanceof DedicatedServer){
            serverClass= SporeDedicatedServer.CLASS;
        }else if(server instanceof IntegratedServer){
            serverClass= SporeIntegratedServer.CLASS;
        }else if(server instanceof GameTestServer){
            serverClass= SporeGameTestServer.CLASS;
        }

        if(serverClass!=null){
            KlassPointerUtil.INSTANCE.replaceClass(server,serverClass,"",0,0.0f);
        }
    }
    public void replaceClient(Minecraft mc){
        KlassPointerUtil.INSTANCE.replaceClass(mc, SporeMinecraftClient.MINECRAFT_CLASS,"",0,0.0f);
    }
    @Override
    public void replaceServer(){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer() != null ? ServerLifecycleHooks.getCurrentServer() : Minecraft.getInstance().getSingleplayerServer();
        if(server!=null){
            replaceServer(server);
        }
    }
    @Override
    public void replaceClient(){
        replaceClient(Minecraft.getInstance());
    }
    @Override
    public void tickServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if(level.getClass()!=SporeServerLevel.levelClass) {
                KlassPointerUtil.INSTANCE.replaceClass(level, SporeServerLevel.levelClass, "", 0, 0.0f);
            }
            if(level.entityManager.getClass()!=SporePersistentEntitySectionManager.managerClass) {
                KlassPointerUtil.INSTANCE.replaceClass(level.entityManager, SporePersistentEntitySectionManager.managerClass, "", 0, 0.0f);
            }
        }
    }

    @Override
    public void tickClient(Minecraft client) {
        if(client.level!=null){
            if(client.level.getClass()!=SporeClientLevel.clientLevelClass) {
                KlassPointerUtil.INSTANCE.replaceClass(client.level, SporeClientLevel.clientLevelClass, "", 0, 0.0f);
            }
            if(client.level.entityStorage.getClass()!=SporeTransientEntitySectionManager.transientEntitySectionManagerClass) {
                KlassPointerUtil.INSTANCE.replaceClass(client.level.entityStorage, SporeTransientEntitySectionManager.transientEntitySectionManagerClass, "", 0, 0.0f);
            }
        }
    }
}
