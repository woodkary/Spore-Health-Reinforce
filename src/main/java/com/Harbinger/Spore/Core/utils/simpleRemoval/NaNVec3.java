package com.Harbinger.Spore.Core.utils.simpleRemoval;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.EnumSet;

final class NaNVec3 extends Vec3 {
    public static final Vec3 INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            Vec3.class,
            NaNVec3.class,
            new Class<?>[]{double.class,double.class,double.class},
            Double.NaN,Double.NaN,Double.NaN
    );
    public NaNVec3(double p_82484_, double p_82485_, double p_82486_) {
        super(p_82484_, p_82485_, p_82486_);
    }

    public NaNVec3(Vector3f p_253821_) {
        super(p_253821_);
    }

    @Override
    public Vec3 vectorTo(Vec3 p_82506_) {
        return this;
    }

    @Override
    public Vec3 normalize() {
        return this;
    }

    @Override
    public double dot(Vec3 p_82527_) {
        return Double.NaN;
    }

    @Override
    public Vec3 cross(Vec3 p_82538_) {
        return this;
    }

    @Override
    public Vec3 subtract(Vec3 p_82547_) {
        return this;
    }

    @Override
    public Vec3 subtract(double p_82493_, double p_82494_, double p_82495_) {
        return this;
    }

    @Override
    public Vec3 add(Vec3 p_82550_) {
        return this;
    }

    @Override
    public Vec3 add(double p_82521_, double p_82522_, double p_82523_) {
        return this;
    }

    @Override
    public boolean closerThan(Position p_82510_, double p_82511_) {
        return false;
    }

    @Override
    public double distanceTo(Vec3 p_82555_) {
        return Double.NaN;
    }

    @Override
    public double x() {
        return Double.NaN;
    }

    @Override
    public double y() {
        return Double.NaN;
    }

    @Override
    public double z() {
        return Double.NaN;
    }

    @Override
    public double distanceToSqr(Vec3 p_82558_) {
        return Double.NaN;
    }

    @Override
    public double distanceToSqr(double p_82532_, double p_82533_, double p_82534_) {
        return Double.NaN;
    }

    @Override
    public Vec3 scale(double p_82491_) {
        return this;
    }

    @Override
    public Vec3 reverse() {
        return this;
    }

    @Override
    public Vec3 multiply(Vec3 p_82560_) {
        return this;
    }

    @Override
    public Vec3 offsetRandom(RandomSource p_272810_, float p_273473_) {
        return this;
    }

    @Override
    public Vec3 multiply(double p_82543_, double p_82544_, double p_82545_) {
        return this;
    }

    @Override
    public double length() {
        return Double.NaN;
    }

    @Override
    public double lengthSqr() {
        return Double.NaN;
    }

    @Override
    public double horizontalDistance() {
        return Double.NaN;
    }

    @Override
    public double horizontalDistanceSqr() {
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
    public Vec3 lerp(Vec3 p_165922_, double p_165923_) {
        return this;
    }

    @Override
    public Vec3 xRot(float p_82497_) {
        return this;
    }

    @Override
    public Vec3 yRot(float p_82525_) {
        return this;
    }

    @Override
    public Vec3 zRot(float p_82536_) {
        return this;
    }

    @Override
    public Vec3 align(EnumSet<Direction.Axis> p_82518_) {
        return this;
    }

    @Override
    public double get(Direction.Axis p_82508_) {
        return Double.NaN;
    }

    @Override
    public Vec3 with(Direction.Axis p_193104_, double p_193105_) {
        return this;
    }

    @Override
    public Vec3 relative(Direction p_231076_, double p_231077_) {
        return this;
    }
}
