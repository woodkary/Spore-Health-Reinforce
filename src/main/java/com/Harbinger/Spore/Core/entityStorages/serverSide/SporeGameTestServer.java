package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.entityStorages.GameTickerUtil;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.function.BooleanSupplier;

public final class SporeGameTestServer extends GameTestServer {
    public static final Class<? extends MinecraftServer> CLASS = (Class<? extends MinecraftServer>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeGameTestServer.class,
            Thread.class,
            LevelStorageSource.LevelStorageAccess.class,
            PackRepository.class,
            WorldStem.class,
            Collection.class,
            BlockPos.class
    );

    public SporeGameTestServer(Thread p_206597_,
                               LevelStorageSource.LevelStorageAccess p_206598_,
                               PackRepository p_206599_,
                               WorldStem p_206600_,
                               Collection<GameTestBatch> p_206601_,
                               BlockPos p_206602_) {
        super(p_206597_, p_206598_, p_206599_, p_206600_, p_206601_, p_206602_);
    }

    public void tickServer(BooleanSupplier p_129871_){
        GameTickerUtil.INSTANCE.tickServer(this);
        super.tickServer(p_129871_);
    }
}
