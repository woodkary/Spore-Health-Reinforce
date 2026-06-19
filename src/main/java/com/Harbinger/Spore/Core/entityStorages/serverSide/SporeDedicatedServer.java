package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.entityStorages.GameTickerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.function.BooleanSupplier;

public final class SporeDedicatedServer extends DedicatedServer {
    public static final Class<? extends MinecraftServer> CLASS = (Class<? extends MinecraftServer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeDedicatedServer.class,
            Thread.class,
            LevelStorageSource.LevelStorageAccess.class,
            PackRepository.class,
            WorldStem.class,
            DedicatedServerSettings.class,
            DataFixer.class,
            Services.class,
            ChunkProgressListenerFactory.class
    );
    public SporeDedicatedServer(Thread p_214789_, LevelStorageSource.LevelStorageAccess p_214790_, PackRepository p_214791_, WorldStem p_214792_, DedicatedServerSettings p_214793_, DataFixer p_214794_, Services p_214795_, ChunkProgressListenerFactory p_214796_) {
        super(p_214789_, p_214790_, p_214791_, p_214792_, p_214793_, p_214794_, p_214795_, p_214796_);
    }
    public void tickServer(BooleanSupplier p_129871_){
        GameTickerUtil.INSTANCE.tickServer(this);
        super.tickServer(p_129871_);
    }
}
