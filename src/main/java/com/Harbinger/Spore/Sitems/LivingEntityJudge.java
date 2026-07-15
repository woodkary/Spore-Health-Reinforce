package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.util.function.Predicate;

final class LivingEntityJudge implements Predicate<LivingEntity> {
    private static final Class<? extends Predicate<LivingEntity>> preClass= (Class<? extends Predicate<LivingEntity>>) BytecodeUtil.resolveHiddenClassOrSelf(
            LivingEntityJudge.class,
            Player.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            preClass,
            LivingEntityJudge.class,
            Player.class
    );
    static Predicate<LivingEntity> newInstance(Player player){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                preClass,
                LivingEntityJudge.class,
                Player.class
        );
        if(constructor!=null){
            try{
                return (Predicate<LivingEntity>) constructor.invoke(player);
            } catch (Throwable e) {
                LogUtil.errorf("failed to create Consumer. %s",e.getMessage());
            }
        }
        return new LivingEntityJudge(player);
    }
    private final Player player;

    private LivingEntityJudge(Player player) {
        this.player = player;
    }

    @Override
    public boolean test(LivingEntity entity) {
        return !entity.equals(player)&&entity.isAlive();
    }
}
