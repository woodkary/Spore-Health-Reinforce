package com.Harbinger.Spore.Sentities.AI;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public final class TargetSameModGoal extends Goal {
    private final Mob mob;
    private LivingEntity mobTarget;
    private int resetSelfTargetTicks;
    private final TargetingConditions targetSameModEntityPredicate;

    public TargetSameModGoal(Mob mob) {
        this.mob = mob;
        String mobNamespace=EntityType.getKey(mob.getType()).getNamespace();
        this.targetSameModEntityPredicate = TargetingConditions.forNonCombat().selector(entity->
                EntityType.getKey(entity.getType()).getNamespace().equals(mobNamespace));
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.resetSelfTargetTicks = 0;
    }
    @Override
    public boolean canUse() {
        findTarget();
        return true;
    }
    private void findTarget() {
        this.mobTarget = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.getFollowDistance()), (p_148152_) -> true), this.targetSameModEntityPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if(this.mobTarget==null){
            this.mobTarget=this.mob;
        }
    }
    private AABB getTargetSearchArea(double p_26069_) {
        return this.mob.getBoundingBox().inflate(p_26069_, 4.0, p_26069_);
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
            return ++this.resetSelfTargetTicks>=20;
        }else{
            this.resetSelfTargetTicks=0;
            return tar==mobTarget;
        }
    }

    @Override
    public void stop() {
        this.resetSelfTargetTicks=0;
    }
}
