package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.entityStorages.GameTickerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.function.BooleanSupplier;

public final class SporeIntegratedServer extends IntegratedServer {
    public static final Class<? extends MinecraftServer> CLASS = (Class<? extends MinecraftServer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeIntegratedServer.class,
            Thread.class,
            Minecraft.class,
            LevelStorageSource.LevelStorageAccess.class,
            PackRepository.class,
            WorldStem.class,
            Services.class,
            ChunkProgressListenerFactory.class
    );

    public SporeIntegratedServer(Thread p_235248_,
                                 Minecraft p_235249_,
                                 LevelStorageSource.LevelStorageAccess p_235250_,
                                 PackRepository p_235251_,
                                 WorldStem p_235252_,
                                 Services p_235253_,
                                 ChunkProgressListenerFactory p_235254_) {
        super(p_235248_, p_235249_, p_235250_, p_235251_, p_235252_, p_235253_, p_235254_);
    }

    public void tickServer(BooleanSupplier p_129871_){
        GameTickerUtil.INSTANCE.tickServer(this);
        super.tickServer(p_129871_);
    }
}
