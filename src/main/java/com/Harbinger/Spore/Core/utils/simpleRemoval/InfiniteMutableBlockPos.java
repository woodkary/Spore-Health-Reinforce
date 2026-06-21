package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

final class InfiniteMutableBlockPos extends BlockPos.MutableBlockPos {
    public static final BlockPos.MutableBlockPos INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            BlockPos.MutableBlockPos.class,
            InfiniteMutableBlockPos.class
    );
    @Override
    public BlockPos offset(int p_122163_, int p_122164_, int p_122165_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public BlockPos multiply(int p_175305_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public BlockPos relative(Direction p_122152_, int p_122153_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public BlockPos relative(Direction.Axis p_122145_, int p_122146_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public BlockPos rotate(Rotation p_122138_) {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public MutableBlockPos set(int p_122179_, int p_122180_, int p_122181_) {
        return this;
    }

    @Override
    public MutableBlockPos set(double p_122170_, double p_122171_, double p_122172_) {
        return this;
    }

    @Override
    public MutableBlockPos set(Vec3i p_122191_) {
        return this;
    }

    @Override
    public MutableBlockPos set(AxisCycle p_122140_, int p_122141_, int p_122142_, int p_122143_) {
        return this;
    }

    @Override
    public MutableBlockPos set(long p_122189_) {
        return this;
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i p_122160_, Direction p_122161_) {
        return this;
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i p_122155_, int p_122156_, int p_122157_, int p_122158_) {
        return this;
    }

    @Override
    public MutableBlockPos setWithOffset(Vec3i p_175307_, Vec3i p_175308_) {
        return this;
    }

    @Override
    public MutableBlockPos move(Direction p_122174_) {
        return this;
    }

    @Override
    public MutableBlockPos move(Direction p_122176_, int p_122177_) {
        return this;
    }

    @Override
    public MutableBlockPos move(int p_122185_, int p_122186_, int p_122187_) {
        return this;
    }

    @Override
    public MutableBlockPos move(Vec3i p_122194_) {
        return this;
    }

    @Override
    public MutableBlockPos clamp(Direction.Axis p_122148_, int p_122149_, int p_122150_) {
        return this;
    }

    @Override
    public MutableBlockPos setX(int p_175341_) {
        return this;
    }

    @Override
    public MutableBlockPos setY(int p_175343_) {
        return this;
    }

    @Override
    public MutableBlockPos setZ(int p_175345_) {
        return this;
    }

    @Override
    public BlockPos immutable() {
        return InfiniteBlockPos.INSTANCE;
    }

    @Override
    public long asLong() {
        return InfiniteBlockPos.INSTANCE.asLong();
    }

    @Override
    public MutableBlockPos mutable() {
        return InfiniteBlockPos.INSTANCE.mutable();
    }

    @Override
    public BlockPos atY(int p_175289_) {
        return InfiniteBlockPos.INSTANCE.atY(p_175289_);
    }

    @Override
    public BlockPos cross(Vec3i p_122011_) {
        return InfiniteBlockPos.INSTANCE.cross(p_122011_);
    }

    @Override
    public Vec3 getCenter() {
        return InfiniteBlockPos.INSTANCE.getCenter();
    }

    @Override
    public BlockPos offset(Vec3i p_121956_) {
        return InfiniteBlockPos.INSTANCE.offset(p_121956_);
    }

    @Override
    public BlockPos subtract(Vec3i p_121997_) {
        return InfiniteBlockPos.INSTANCE.subtract(p_121997_);
    }

    @Override
    public BlockPos above() {
        return InfiniteBlockPos.INSTANCE.above();
    }

    @Override
    public BlockPos above(int p_121972_) {
        return InfiniteBlockPos.INSTANCE.above(p_121972_);
    }

    @Override
    public BlockPos below() {
        return InfiniteBlockPos.INSTANCE.below();
    }

    @Override
    public BlockPos below(int p_122000_) {
        return InfiniteBlockPos.INSTANCE.below(p_122000_);
    }

    @Override
    public BlockPos north() {
        return InfiniteBlockPos.INSTANCE.north();
    }

    @Override
    public BlockPos south() {
        return InfiniteBlockPos.INSTANCE.south();
    }

    @Override
    public BlockPos north(int p_122014_) {
        return InfiniteBlockPos.INSTANCE.north(p_122014_);
    }

    @Override
    public BlockPos south(int p_122021_) {
        return InfiniteBlockPos.INSTANCE.south(p_122021_);
    }

    @Override
    public BlockPos west() {
        return InfiniteBlockPos.INSTANCE.west();
    }

    @Override
    public BlockPos west(int p_122026_) {
        return InfiniteBlockPos.INSTANCE.west(p_122026_);
    }

    @Override
    public BlockPos east() {
        return InfiniteBlockPos.INSTANCE.east();
    }

    @Override
    public BlockPos east(int p_122031_) {
        return InfiniteBlockPos.INSTANCE.east(p_122031_);
    }

    @Override
    public BlockPos relative(Direction p_121946_) {
        return InfiniteBlockPos.INSTANCE.relative(p_121946_);
    }
}
