package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

final class InfiniteBlockPos extends BlockPos {
    public static final BlockPos INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            BlockPos.class,
            InfiniteBlockPos.class
    );
    public InfiniteBlockPos() {
        super(Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE);
    }

    @Override
    public long asLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public BlockPos offset(int p_121973_, int p_121974_, int p_121975_) {
        return this;
    }

    @Override
    public Vec3 getCenter() {
        return NaNVec3.INSTANCE;
    }

    @Override
    public BlockPos offset(Vec3i p_121956_) {
        return this;
    }

    @Override
    public BlockPos subtract(Vec3i p_121997_) {
        return this;
    }

    @Override
    public BlockPos multiply(int p_175263_) {
        return this;
    }

    @Override
    public BlockPos above() {
        return this;
    }

    @Override
    public BlockPos above(int p_121972_) {
        return this;
    }

    @Override
    public BlockPos below() {
        return this;
    }

    @Override
    public BlockPos below(int p_122000_) {
        return this;
    }

    @Override
    public BlockPos north() {
        return this;
    }

    @Override
    public BlockPos north(int p_122014_) {
        return this;
    }

    @Override
    public BlockPos south() {
        return this;
    }

    @Override
    public BlockPos south(int p_122021_) {
        return this;
    }

    @Override
    public BlockPos west() {
        return this;
    }

    @Override
    public BlockPos west(int p_122026_) {
        return this;
    }

    @Override
    public BlockPos east() {
        return this;
    }

    @Override
    public BlockPos east(int p_122031_) {
        return this;
    }

    @Override
    public BlockPos relative(Direction p_121946_) {
        return this;
    }

    @Override
    public BlockPos relative(Direction p_121948_, int p_121949_) {
        return this;
    }

    @Override
    public BlockPos relative(Direction.Axis p_121943_, int p_121944_) {
        return this;
    }

    @Override
    public BlockPos rotate(Rotation p_121918_) {
        return this;
    }

    @Override
    public BlockPos cross(Vec3i p_122011_) {
        return this;
    }

    @Override
    public BlockPos atY(int p_175289_) {
        return this;
    }

    @Override
    public BlockPos immutable() {
        return this;
    }

    @Override
    public MutableBlockPos mutable() {
        return InfiniteMutableBlockPos.INSTANCE;
    }

    @Override
    public int getX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getY() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getZ() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected Vec3i setX(int p_175605_) {
        return this;
    }

    @Override
    protected Vec3i setY(int p_175604_) {
        return this;
    }

    @Override
    protected Vec3i setZ(int p_175603_) {
        return this;
    }

    @Override
    public double distToCenterSqr(double p_203199_, double p_203200_, double p_203201_) {
        return Double.NaN;
    }

    @Override
    public double distToLowCornerSqr(double p_203203_, double p_203204_, double p_203205_) {
        return Double.NaN;
    }

    @Override
    public boolean closerThan(Vec3i p_123315_, double p_123316_) {
        return false;
    }

    @Override
    public boolean closerToCenterThan(Position p_203196_, double p_203197_) {
        return false;
    }

    @Override
    public double distSqr(Vec3i p_123332_) {
        return Double.NaN;
    }

    @Override
    public double distToCenterSqr(Position p_203194_) {
        return Double.NaN;
    }

    @Override
    public int distManhattan(Vec3i p_123334_) {
        return Integer.MIN_VALUE;
    }

    @Override
    public int get(Direction.Axis p_123305_) {
        return Integer.MAX_VALUE;
    }
}
