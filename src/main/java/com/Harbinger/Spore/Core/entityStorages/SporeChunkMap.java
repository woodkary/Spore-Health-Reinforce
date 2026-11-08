package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class SporeChunkMap extends ChunkMap {
    public static final Class<? extends ChunkMap> chunkMapClass = (Class<? extends ChunkMap>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeChunkMap.class,
            ServerLevel.class,
            LevelStorageSource.LevelStorageAccess.class,
            DataFixer.class,
            StructureTemplateManager.class,
            Executor.class,
            BlockableEventLoop.class,
            LightChunkGetter.class,
            ChunkGenerator.class,
            ChunkProgressListener.class,
            ChunkStatusUpdateListener.class,
            Supplier.class,
            int.class,
            boolean.class
    );
    public SporeChunkMap(ServerLevel p_214836_, LevelStorageSource.LevelStorageAccess p_214837_, DataFixer p_214838_, StructureTemplateManager p_214839_, Executor p_214840_, BlockableEventLoop<Runnable> p_214841_, LightChunkGetter p_214842_, ChunkGenerator p_214843_, ChunkProgressListener p_214844_, ChunkStatusUpdateListener p_214845_, Supplier<DimensionDataStorage> p_214846_, int p_214847_, boolean p_214848_) {
        super(p_214836_, p_214837_, p_214838_, p_214839_, p_214840_, p_214841_, p_214842_, p_214843_, p_214844_, p_214845_, p_214846_, p_214847_, p_214848_);
    }
    @Override
    protected void addEntity(Entity entity) {
        if(SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity)){
            return;
        }
        try {
            super.addEntity(entity);
        }catch (Throwable ignored) {}
    }
    @Override
    protected void tick() {
        if(!(this.entityMap instanceof ISporeEntityStorage)){
            this.entityMap= SporeTrackedEntityMap.newInstance(this.entityMap);
        }
        super.tick();
    }
}
