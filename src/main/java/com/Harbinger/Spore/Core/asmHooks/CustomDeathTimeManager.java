package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LivingEntityHealthLifecycleWrapperUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CustomDeathTimeManager implements IDeathTimeManager {
    private static final String DEATH_TIME_TAG = "spore$DTicks";
    private final ThreadLocal<Deque<LivingEntity>> specialRenderEntities = new ThreadLocal<>();

    public static final IDeathTimeManager INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IDeathTimeManager.class,
            CustomDeathTimeManager.class
    );

    @Override
    public int deathTimeGetFieldHook(LivingEntity entity, int initialDeathTime) {
        if (entity == null) {
            return initialDeathTime;
        }
        Deque<LivingEntity> stack = specialRenderEntities.get();
        if (stack != null) {
            for (LivingEntity rendering : stack) {
                if (rendering == entity) {
                    return 0;
                }
            }
        }
        boolean trueDeadPlayer = isTrueDeadPlayer(entity);
        if (!trueDeadPlayer && SporeJudge.isSporeEntity(entity)) {
            return initialDeathTime;
        }
        if (!trueDeadPlayer && entity instanceof Player player
                && EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(player)) {
            return 0;
        }
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(DEATH_TIME_TAG, Tag.TAG_INT)) {
            return initialDeathTime;
        }
        int customDeathTime = data.getInt(DEATH_TIME_TAG);
        if (initialDeathTime != customDeathTime) {
            entity.deathTime = customDeathTime;
        }
        return customDeathTime;
    }

    @Override
    public void enterSpecialRender(LivingEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Special render entity must not be null");
        }
        Deque<LivingEntity> stack = specialRenderEntities.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            specialRenderEntities.set(stack);
        }
        stack.push(entity);
    }

    @Override
    public void exitSpecialRender(LivingEntity entity) {
        Deque<LivingEntity> stack = specialRenderEntities.get();
        if (stack == null || stack.peek() != entity) {
            throw new IllegalStateException("Special render scopes must exit in nesting order");
        }
        stack.pop();
        if (stack.isEmpty()) {
            specialRenderEntities.remove();
        }
    }

    private boolean isTrueDeadPlayer(LivingEntity entity) {
        return entity instanceof Player player
                && LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player);
    }

    @Override
    public void accept(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity entity = livingTickEvent.getEntity();
        CompoundTag data = entity.getPersistentData();
        boolean trueDeadPlayer = isTrueDeadPlayer(entity);
        if (!trueDeadPlayer && !EntityHeealuthManager.INSTANCE.isDeeadfOrDyaging(entity, entity.isDeadOrDying())) {
            boolean wasManaged = data.contains(DEATH_TIME_TAG, Tag.TAG_INT);
            data.remove(DEATH_TIME_TAG);
            if (wasManaged) {
                entity.deathTime = 0;
            }
            return;
        }
        if (!trueDeadPlayer && (SporeJudge.isSporeEntity(entity)||
                entity instanceof Player player&&
                        EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(player))) {
            data.remove(DEATH_TIME_TAG);
            return;
        }
        if (!entity.level().shouldTickDeath(entity)) {
            return;
        }

        int deathTime = data.contains(DEATH_TIME_TAG, Tag.TAG_INT)
                ? data.getInt(DEATH_TIME_TAG)
                : entity.deathTime;
        int nextDeathTime = deathTime + 1;
        data.putInt(DEATH_TIME_TAG, nextDeathTime);
        entity.deathTime = nextDeathTime;
    }
}
