package com.Harbinger.Spore.Sentities.AI;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;

final class AmphibianCalamityNodeEvaluator extends NodeEvaluator {
    private final NodeEvaluator landMode;
    private final NodeEvaluator waterMode;
    private final Mob owner;

    public AmphibianCalamityNodeEvaluator(NodeEvaluator landMode, NodeEvaluator waterMode, Mob owner) {
        this.landMode = landMode;
        this.waterMode = waterMode;
        this.owner = owner;
    }

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        super.prepare(region, mob);
        this.syncOptionsToModes();
        this.landMode.prepare(region, mob);
        this.waterMode.prepare(region, mob);
    }

    @Override
    public void done() {
        this.landMode.done();
        this.waterMode.done();
        super.done();
    }

    @Override
    public Node getStart() {
        return this.activeMode().getStart();
    }

    @Override
    public Target getGoal(double v, double v1, double v2) {
        return this.activeMode().getGoal(v, v1, v2);
    }

    @Override
    public int getNeighbors(Node[] nodes, Node node) {
        return this.activeMode().getNeighbors(nodes, node);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i1, int i2, Mob mob) {
        Mob currentMob = mob != null ? mob : this.getCurrentMob();
        return this.activeMode(currentMob).getBlockPathType(blockGetter, i, i1, i2, currentMob);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i1, int i2) {
        Mob currentMob = this.getCurrentMob();
        return this.activeMode(currentMob).getBlockPathType(blockGetter, i, i1, i2, currentMob);
    }

    @Override
    public void setCanPassDoors(boolean value) {
        super.setCanPassDoors(value);
        this.landMode.setCanPassDoors(value);
        this.waterMode.setCanPassDoors(value);
    }

    @Override
    public void setCanOpenDoors(boolean value) {
        super.setCanOpenDoors(value);
        this.landMode.setCanOpenDoors(value);
        this.waterMode.setCanOpenDoors(value);
    }

    @Override
    public void setCanFloat(boolean value) {
        super.setCanFloat(value);
        this.landMode.setCanFloat(value);
        this.waterMode.setCanFloat(value);
    }

    @Override
    public void setCanWalkOverFences(boolean value) {
        super.setCanWalkOverFences(value);
        this.landMode.setCanWalkOverFences(value);
        this.waterMode.setCanWalkOverFences(value);
    }

    private void syncOptionsToModes() {
        this.landMode.setCanPassDoors(this.canPassDoors());
        this.waterMode.setCanPassDoors(this.canPassDoors());
        this.landMode.setCanOpenDoors(this.canOpenDoors());
        this.waterMode.setCanOpenDoors(this.canOpenDoors());
        this.landMode.setCanFloat(this.canFloat());
        this.waterMode.setCanFloat(this.canFloat());
        this.landMode.setCanWalkOverFences(this.canWalkOverFences());
        this.waterMode.setCanWalkOverFences(this.canWalkOverFences());
    }

    private NodeEvaluator activeMode() {
        return this.activeMode(this.getCurrentMob());
    }

    private NodeEvaluator activeMode(@Nullable Mob mob) {
        return mob != null && mob.isInFluidType() ? this.waterMode : this.landMode;
    }

    private Mob getCurrentMob() {
        return this.mob != null ? this.mob : this.owner;
    }
}
