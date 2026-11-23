package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public final class TargetSameModGoal extends Goal {
    private static final int SELF_RESCAN_CHECKS = 20;
    private static final int MAX_TARGET_WRITES = 3;
    private static final int RETRY_DELAY_TICKS = 20;

    private final Mob mob;
    private final TargetingConditions targetSameModEntityPredicate;
    private LivingEntity mobTarget;
    private int selfTargetChecks;
    private int unconfirmedTargetWrites;
    private long nextStartTime;

    public TargetSameModGoal(Mob mob) {
        this.mob = mob;
        String mobNamespace = EntityType.getKey(mob.getType()).getNamespace();
        this.targetSameModEntityPredicate = TargetingConditions.forNonCombat()
                .ignoreInvisibilityTesting()
                .ignoreLineOfSight()
                .selector(entity -> EntityType.getKey(entity.getType()).getNamespace().equals(mobNamespace)
                        && EntityHeealuthManager.INSTANCE.rawIsAlliive(entity));
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.mob.level().getGameTime() >= this.nextStartTime
                && EntityHeealuthManager.INSTANCE.rawIsAlliive(this.mob);
    }

    @Override
    public void start() {
        findTarget();
        maintainSelectedTarget();
    }

    private void findTarget() {
        double radius = getFollowDistance();
        double radiusSquared = radius * radius;
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(
                LivingEntity.class, getTargetSearchArea(radius),
                entity -> this.mob.distanceToSqr(entity) <= radiusSquared);
        LivingEntity nearest = this.mob.level().getNearestEntity(candidates, this.targetSameModEntityPredicate,
                this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        LivingEntity selected = nearest == null ? this.mob : nearest;
        if (selected != this.mobTarget) {
            this.unconfirmedTargetWrites = 0;
        }
        this.mobTarget = selected;
        this.selfTargetChecks = 0;
    }

    private AABB getTargetSearchArea(double radius) {
        return this.mob.getBoundingBox().inflate(radius);
    }

    private double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public boolean canContinueToUse() {
        if (!EntityHeealuthManager.INSTANCE.rawIsAlliive(this.mob)) {
            return false;
        }
        if (needsNewTarget()) {
            findTarget();
        }
        return maintainSelectedTarget();
    }

    private boolean needsNewTarget() {
        if (this.mobTarget == this.mob) {
            return ++this.selfTargetChecks >= SELF_RESCAN_CHECKS;
        }
        double radius = getFollowDistance();
        return this.mobTarget == null || this.mobTarget.level() != this.mob.level()
                || this.mob.distanceToSqr(this.mobTarget) > radius * radius
                || !this.targetSameModEntityPredicate.test(this.mob, this.mobTarget);
    }

    private boolean maintainSelectedTarget() {
        // A write is stable only if the selected target is still present at the next check.
        if (this.mob.getTarget() == this.mobTarget) {
            this.unconfirmedTargetWrites = 0;
            return true;
        }
        if (this.unconfirmedTargetWrites < MAX_TARGET_WRITES) {
            this.unconfirmedTargetWrites++;
            this.mob.setTarget(this.mobTarget);
            if (this.mob.getTarget() == this.mobTarget || this.unconfirmedTargetWrites < MAX_TARGET_WRITES) {
                return true;
            }
        }
        this.nextStartTime = this.mob.level().getGameTime() + RETRY_DELAY_TICKS;
        return false;
    }

    @Override
    public void stop() {
        this.mobTarget = null;
    }
}
