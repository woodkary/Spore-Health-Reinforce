package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class NaNAABBClass extends AABB {
    public static final AABB INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            AABB.class,
            NaNAABBClass.class,
            new Class<?>[]{double.class,double.class,double.class,double.class,double.class,double.class},
            Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN
    );
    public NaNAABBClass(double p_82295_, double p_82296_, double p_82297_, double p_82298_, double p_82299_, double p_82300_) {
        super(p_82295_, p_82296_, p_82297_, p_82298_, p_82299_, p_82300_);
    }

    public NaNAABBClass(BlockPos p_82305_) {
        super(p_82305_);
    }

    public NaNAABBClass(BlockPos p_82307_, BlockPos p_82308_) {
        super(p_82307_, p_82308_);
    }

    public NaNAABBClass(Vec3 p_82302_, Vec3 p_82303_) {
        super(p_82302_, p_82303_);
    }

    @Override
    public AABB setMinX(double p_165881_) {
        return this;
    }

    @Override
    public AABB setMinY(double p_165888_) {
        return this;
    }

    @Override
    public AABB setMinZ(double p_165890_) {
        return this;
    }

    @Override
    public AABB setMaxX(double p_165892_) {
        return this;
    }

    @Override
    public AABB setMaxY(double p_165894_) {
        return this;
    }

    @Override
    public AABB setMaxZ(double p_165896_) {
        return this;
    }

    @Override
    public double min(Direction.Axis p_82341_) {
        return Double.NaN;
    }

    @Override
    public double max(Direction.Axis p_82375_) {
        return Double.NaN;
    }

    @Override
    public boolean equals(Object p_82398_) {
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.MIN_VALUE;
    }

    @Override
    public AABB contract(double p_82311_, double p_82312_, double p_82313_) {
        return this;
    }

    @Override
    public AABB expandTowards(Vec3 p_82370_) {
        return this;
    }

    @Override
    public AABB expandTowards(double p_82364_, double p_82365_, double p_82366_) {
        return this;
    }

    @Override
    public AABB inflate(double p_82378_, double p_82379_, double p_82380_) {
        return this;
    }

    @Override
    public AABB inflate(double p_82401_) {
        return this;
    }

    @Override
    public AABB intersect(AABB p_82324_) {
        return this;
    }

    @Override
    public AABB minmax(AABB p_82368_) {
        return this;
    }

    @Override
    public AABB move(double p_82387_, double p_82388_, double p_82389_) {
        return this;
    }

    @Override
    public AABB move(BlockPos p_82339_) {
        return this;
    }

    @Override
    public AABB move(Vec3 p_82384_) {
        return this;
    }

    @Override
    public boolean intersects(AABB p_82382_) {
        return false;
    }

    @Override
    public boolean intersects(double p_82315_, double p_82316_, double p_82317_, double p_82318_, double p_82319_, double p_82320_) {
        return false;
    }

    @Override
    public boolean intersects(Vec3 p_82336_, Vec3 p_82337_) {
        return false;
    }

    @Override
    public boolean contains(Vec3 p_82391_) {
        return false;
    }

    @Override
    public boolean contains(double p_82394_, double p_82395_, double p_82396_) {
        return false;
    }

    @Override
    public double getSize() {
        return Double.NaN;
    }

    @Override
    public double getXsize() {
        return Double.NaN;
    }

    @Override
    public double getYsize() {
        return Double.NaN;
    }

    @Override
    public double getZsize() {
        return Double.NaN;
    }

    @Override
    public AABB deflate(double p_165898_, double p_165899_, double p_165900_) {
        return this;
    }

    @Override
    public AABB deflate(double p_82407_) {
        return this;
    }

    @Override
    public Optional<Vec3> clip(Vec3 p_82372_, Vec3 p_82373_) {
        return Optional.of(NaNVec3.INSTANCE);
    }

    @Override
    public double distanceToSqr(Vec3 p_273572_) {
        return Double.NaN;
    }

    @Override
    public boolean hasNaN() {
        return false;
    }
}
