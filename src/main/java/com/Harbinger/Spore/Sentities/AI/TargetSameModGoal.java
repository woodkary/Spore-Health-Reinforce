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
    private final Mob mob;
    private LivingEntity mobTarget;
    private int resetSelfTargetTicks;
    private final TargetingConditions targetSameModEntityPredicate;

    public TargetSameModGoal(Mob mob) {
        this.mob = mob;
        String mobNamespace = EntityType.getKey(mob.getType()).getNamespace();
        this.targetSameModEntityPredicate = TargetingConditions.forNonCombat()
                .ignoreInvisibilityTesting()
                .ignoreLineOfSight()
                .selector(entity -> EntityType.getKey(entity.getType()).getNamespace().equals(mobNamespace));
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.resetSelfTargetTicks = 0;
    }
    @Override
    public boolean canUse() {
        findTarget();
        return true;
    }

    private void findTarget() {
        double radius = getFollowDistance();
        double radiusSquared = radius * radius;
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(
                LivingEntity.class, getTargetSearchArea(radius),
                entity -> this.mob.distanceToSqr(entity) <= radiusSquared);
        this.mobTarget = this.mob.level().getNearestEntity(candidates, this.targetSameModEntityPredicate,
                this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if (this.mobTarget == null) {
            this.mobTarget = this.mob;
        }
    }

    private AABB getTargetSearchArea(double radius) {
        return this.mob.getBoundingBox().inflate(radius);
    }
    private double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
    @Override
    public void start() {
        this.mob.setTarget(this.mobTarget);
    }
    @Override
    public boolean canContinueToUse() {
        LivingEntity tar = this.mob.getTarget();
        if(tar==this.mob){
            return ++this.resetSelfTargetTicks<20;
        }
        if(tar==null) {
            return false;
        }
        this.resetSelfTargetTicks=0;
        double followDistance = getFollowDistance();
        return tar==mobTarget&&
                EntityHeealuthManager.INSTANCE.rawIsAlliive(tar)&&
                tar.level()==this.mob.level()&&
                this.mob.distanceToSqr(tar) <= followDistance * followDistance;
    }

    @Override
    public void stop() {
        this.resetSelfTargetTicks=0;
    }
}
