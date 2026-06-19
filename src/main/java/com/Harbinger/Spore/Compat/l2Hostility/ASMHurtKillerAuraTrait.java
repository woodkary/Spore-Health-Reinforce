package com.Harbinger.Spore.Compat.l2Hostility;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.logic.TraitEffectCache;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.content.traits.legendary.KillerAuraTrait;
import dev.xkmc.l2hostility.init.data.LHConfig;
import dev.xkmc.l2hostility.init.data.LHDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.function.Predicate;

public final class ASMHurtKillerAuraTrait extends KillerAuraTrait {
    @SuppressWarnings("unchecked")
    public static final Class<? extends MobTrait> killerAuraTraitClass= (Class<? extends MobTrait>) BytecodeUtil.resolveHiddenClassOrSelf(
            ASMHurtKillerAuraTrait.class,
            ChatFormatting.class
    );
    public ASMHurtKillerAuraTrait(ChatFormatting format) {
        super(format);
    }
    @Override
    public void tick(LivingEntity mob, int level) {
        super.tick(mob, level);
        int itv = LHConfig.COMMON.killerAuraInterval.get() / level;
        int damage = LHConfig.COMMON.killerAuraDamage.get() * level;
        int range = LHConfig.COMMON.killerAuraRange.get();
        if (!mob.level().isClientSide() && mob.tickCount % itv == 0) {
            MobTraitCap cap = MobTraitCap.HOLDER.get(mob);
            AABB box = mob.getBoundingBox().inflate(range);
            for(LivingEntity e : mob.level().getEntitiesOfClass(LivingEntity.class,
                    box,
                    LivingEntityJudge.newInstance(mob,range))) {
                TraitEffectCache cache = new TraitEffectCache(e);
                for (Map.Entry<MobTrait, Integer> entry : cap.traits.entrySet()) {
                    MobTrait trait = entry.getKey();
                    Integer value = entry.getValue();
                    trait.postHurtPlayer(value, mob, cache);
                }
                SporeAttackUtil.INSTANCE.attack(e,mob,(float)damage);
            }
        }
    }
    private static final class LivingEntityJudge implements Predicate<LivingEntity> {
        @SuppressWarnings("unchecked")
        private static final Class<? extends Predicate<LivingEntity>> clazz= (Class<? extends Predicate<LivingEntity>>) BytecodeUtil.resolveHiddenClassOrSelf(
                LivingEntityJudge.class,
                LivingEntity.class,
                int.class
        );
        private static MethodHandle constructor;
        static {
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    clazz,
                    LivingEntityJudge.class,
                    LivingEntity.class,
                    int.class
            );
        }
        private static Predicate<LivingEntity> newInstance(LivingEntity self, int range){
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    clazz,
                    LivingEntityJudge.class,
                    LivingEntity.class,
                    int.class
            );
            if(constructor!=null){
                try{
                    return (Predicate<LivingEntity>) constructor.invoke(self,range);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new instance of LivingEntityJudge,%s",e.getMessage());
                }
            }
            return new LivingEntityJudge(self,range);
        }
        private final LivingEntity self;
        private final int range;
        public LivingEntityJudge(LivingEntity self, int range) {
            this.self = self;
            this.range = range;
        }

        @Override
        public boolean test(LivingEntity e) {
            return !SporeJudge.isSporeEntity(e)&&(e instanceof Player pl&&!pl.getAbilities().instabuild||
                    e instanceof Mob target&& self.equals(target.getTarget())||
                    self instanceof Mob mob&&e.equals(mob.getTarget()))&&
                    !(e.distanceTo(self) > range);
        }
    }
}
