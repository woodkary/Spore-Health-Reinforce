package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.entityStorages.SporeEntityGetter;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SporeServerLevel extends ServerLevel {
    public static final Class<? extends ServerLevel> levelClass= (Class<? extends ServerLevel>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeServerLevel.class,
            MinecraftServer.class,
            Executor.class,
            LevelStorageSource.LevelStorageAccess.class,
            ServerLevelData.class,
            ResourceKey.class,
            LevelStem.class,
            ChunkProgressListener.class,
            boolean.class,
            long.class,
            List.class,
            boolean.class,
            RandomSequences.class
    );
    public SporeServerLevel(MinecraftServer p_214999_, Executor p_215000_, LevelStorageSource.LevelStorageAccess p_215001_, ServerLevelData p_215002_, ResourceKey<Level> p_215003_, LevelStem p_215004_, ChunkProgressListener p_215005_, boolean p_215006_, long p_215007_, List<CustomSpawner> p_215008_, boolean p_215009_, @Nullable RandomSequences p_288977_) {
        super(p_214999_, p_215000_, p_215001_, p_215002_, p_215003_, p_215004_, p_215005_, p_215006_, p_215007_, p_215008_, p_215009_, p_288977_);
    }
    @Override
    public Iterable<Entity> getAllEntities() {
        return this.getEntities().getAll();
    }
    public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> p_143281_, Predicate<? super T> p_143282_) {
        List<T> list = Lists.newArrayList();
        this.getEntities(p_143281_, p_143282_, list);
        return list;
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> p_262152_, Predicate<? super T> p_261808_, List<? super T> p_261583_) {
        this.getEntities(p_262152_, p_261808_, p_261583_, Integer.MAX_VALUE);
    }
    public @NotNull LevelEntityGetter<Entity> getEntities() {
        if(this.entityManager.entityGetter.getClass()!= SporeEntityGetter.entityGetterClass){
            this.entityManager.entityGetter= SporeEntityGetter.newInstance(this.entityManager.entityGetter,this.entityManager.visibleEntityStorage,this.entityManager.sectionStorage);
        }
        return this.entityManager.getEntityGetter();
    }
    @Override
    public void tick(BooleanSupplier p_8794_) {
        ProfilerFiller profilerfiller = this.getProfiler();
        this.handlingTick = true;
        profilerfiller.push("world border");
        this.getWorldBorder().tick();
        profilerfiller.popPush("weather");
        this.advanceWeatherCycle();
        int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        if (this.sleepStatus.areEnoughSleeping(i) && this.sleepStatus.areEnoughDeepSleeping(i, this.players)) {
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                long j = this.getDayTime() + 24000L;
                this.setDayTime(ForgeEventFactory.onSleepFinished(this, j - j % 24000L, this.getDayTime()));
            }

            this.wakeUpAllPlayers();
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && this.isRaining()) {
                this.resetWeatherCycle();
            }
        }

        this.updateSkyBrightness();
        this.tickTime();
        profilerfiller.popPush("tickPending");
        if (!this.isDebug()) {
            long k = this.getGameTime();
            profilerfiller.push("blockTicks");
            this.blockTicks.tick(k, 65536, new TickBlockOrFluidBiConsumer<>(this));
            profilerfiller.popPush("fluidTicks");
            this.fluidTicks.tick(k, 65536, new TickBlockOrFluidBiConsumer<>(this));
            profilerfiller.pop();
        }

        profilerfiller.popPush("raid");
        this.raids.tick();
        profilerfiller.popPush("chunkSource");
        this.getChunkSource().tick(p_8794_, true);
        profilerfiller.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        profilerfiller.pop();
        boolean flag = !this.players.isEmpty() || ForgeChunkManager.hasForcedChunks(this);
        if (flag) {
            this.resetEmptyTime();
        }

        if (flag || this.emptyTime++ < 300) {
            profilerfiller.push("entities");
            if (this.dragonFight != null) {
                profilerfiller.push("dragonFight");
                this.dragonFight.tick();
                profilerfiller.pop();
            }

            this.entityTickList.forEach(new EntityTickListConsumer(this,profilerfiller));
            profilerfiller.pop();
            this.tickBlockEntities();
        }

        profilerfiller.push("entityManagement");
        this.entityManager.tick();
        profilerfiller.pop();
    }
    @Override
    public void tickNonPassenger(Entity p_8648_) {
        if (SimpleRemoveUtil.INSTANCE.isRemoved(p_8648_)) {
            return;
        }
        super.tickNonPassenger(p_8648_);
    }
    @Override
    public boolean addFreshEntity(Entity p_8837_) {
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_8837_) && this.addEntity(p_8837_);
    }
    @Override
    public boolean addEntity(Entity p_8873_) {
        if (SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(p_8873_)) {
            return false;
        }
        return super.addEntity(p_8873_);
    }
    @Override
    public @Nullable Entity getEntity(int id) {
        if(SimpleRemoveUtil.INSTANCE.isRemoved(id)){
            return null;
        }
        return this.getEntities().get(id);
    }

    private record TickBlockOrFluidBiConsumer<T>(ServerLevel level) implements BiConsumer<BlockPos, T> {
        @Override
            public void accept(BlockPos blockPos, T t) {
                if (t instanceof Block block) {
                    this.level.tickBlock(blockPos, block);
                } else if (t instanceof Fluid fluid) {
                    this.level.tickFluid(blockPos, fluid);
                }
            }
        }

    private record EntityTickListConsumer(ServerLevel level,
                                          ProfilerFiller profilerfiller) implements Consumer<Entity> {
        @Override
            public void accept(Entity e0) {
                if (!e0.isRemoved()) {
                    if (this.level.shouldDiscardEntity(e0)) {
                        e0.discard();
                    } else {
                        profilerfiller.push("checkDespawn");
                        e0.checkDespawn();
                        profilerfiller.pop();
                        if (this.level.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(e0.chunkPosition().toLong())) {
                            Entity entity = e0.getVehicle();
                            if (entity != null) {
                                if (!entity.isRemoved() && entity.hasPassenger(e0)) {
                                    return;
                                }

                                e0.stopRiding();
                            }

                            profilerfiller.push("tick");
                            if (!e0.isRemoved() && !(e0 instanceof PartEntity)) {
                                this.level.guardEntityTick(this.level::tickNonPassenger, e0);
                            }

                            profilerfiller.pop();
                        }
                    }
                }
            }
        }

}
