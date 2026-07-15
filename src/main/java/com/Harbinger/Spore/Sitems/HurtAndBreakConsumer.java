package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

final class HurtAndBreakConsumer implements Consumer<Player> {
    private static final Class<? extends Consumer<Player>> conClass= (Class<? extends Consumer<Player>>) BytecodeUtil.resolveHiddenClassOrSelf(
            HurtAndBreakConsumer.class,
            LivingEntity.class
    );
    private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
            null,
            conClass,
            HurtAndBreakConsumer.class,
            LivingEntity.class
    );
    static Consumer<Player> newInstance(LivingEntity entity){
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                conClass,
                HurtAndBreakConsumer.class,
                LivingEntity.class
        );
        if(constructor!=null){
            try{
                return (Consumer<Player>) constructor.invoke(entity);
            } catch (Throwable e) {
                LogUtil.errorf("failed to create HurtAndBreakConsumer. %s",e.getMessage());
            }
        }
        return new HurtAndBreakConsumer(entity);
    }
    private final LivingEntity entity;

    private HurtAndBreakConsumer(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void accept(Player player) {
        player.broadcastBreakEvent(entity.getUsedItemHand());
    }
}
