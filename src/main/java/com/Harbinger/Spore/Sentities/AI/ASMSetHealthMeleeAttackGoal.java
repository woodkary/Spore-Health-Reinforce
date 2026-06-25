package com.Harbinger.Spore.Sentities.AI;

import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.Hyper.Inquisitor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface ASMSetHealthMeleeAttackGoal {
    Mob mob();
    double attackReachSqr(LivingEntity target);
    default float getBonus(){
        return 0.0f;
    }
    int ticksUntilNextAttack();
    default void tickASMAttack(){
        Mob mob = mob();
        LivingEntity target= mob.getTarget();
        if (target == null || SporeJudge.isSporeEntity(target)||!target.isAlive()) {
            return;
        }
        if(!SporeJudge.isSporeEntity(mob)){
            return;
        }
        double distanceSquared = mob.getPerceivedTargetDistanceSquareForMeleeAttack(target);
        double d0 = attackReachSqr(target);
        if(distanceSquared > d0||ticksUntilNextAttack() > 0){
            return;
        }
        float attackDamage = (float) mob.attributes.getValue(Attributes.ATTACK_DAMAGE)*0.3f;
        attackDamage+=getBonus();
        SporeAttackUtil.INSTANCE.attack(target, mob, attackDamage);
    }
}
