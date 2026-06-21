package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

final class InfiniteChunkPos extends ChunkPos {
    public static final ChunkPos INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            ChunkPos.class,
            InfiniteChunkPos.class
    );
    public InfiniteChunkPos() {
        super(Integer.MAX_VALUE,Integer.MIN_VALUE);
    }

    @Override
    public long toLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getMiddleBlockX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMiddleBlockZ() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMinBlockX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMinBlockZ() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxBlockX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMaxBlockZ() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getRegionX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getRegionZ() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getRegionLocalX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getRegionLocalZ() {
        return Integer.MAX_VALUE;
    }

    @Override
    public BlockPos getBlockAt(int p_151385_, int p_151386_, int p_151387_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public int getBlockX(int p_151383_) {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getBlockZ(int p_151392_) {
        return Integer.MAX_VALUE;
    }

    @Override
    public BlockPos getMiddleBlockPosition(int p_151395_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public BlockPos getWorldPosition() {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public int getChessboardDistance(ChunkPos p_45595_) {
        return Integer.MAX_VALUE;
    }
}
