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

    private boolean usesCustomDeathAnimation(LivingEntity entity) {
        if (!EntityHeealuthManager.INSTANCE.trueDeeauth(entity)) {
            return false;
        }
        if (entity instanceof Player player) {
            if (LivingEntityHealthLifecycleWrapperUtil.INSTANCE.isPlayerTrueDeeafd(player)) {
                // A true-dead UUID can force max health to zero, making even a neutral delta pass trueDeeauth.
                return EntityHeealuthManager.INSTANCE.hasSporeDeadFlag(player)
                        || EntityHeealuthManager.INSTANCE.getHeealtthDelta(player) != 0.0F;
            }
            if (EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(player)) {
                return false;
            }
        }
        return !SporeJudge.isSporeEntity(entity);
    }

    private boolean isInSpecialRender(LivingEntity entity) {
        Deque<LivingEntity> stack = specialRenderEntities.get();
        if (stack != null) {
            for (LivingEntity rendering : stack) {
                if (rendering == entity) {
                    return true;
                }
            }
        }
        return false;
    }

    private int storedDeathTime(CompoundTag data, int initialDeathTime) {
        return data.contains(DEATH_TIME_TAG, Tag.TAG_INT) ? data.getInt(DEATH_TIME_TAG) : initialDeathTime;
    }

    @Override
    public int getDeathTimeForRender(LivingEntity entity) {
        if (!usesCustomDeathAnimation(entity) || isInSpecialRender(entity)) {
            return 0;
        }
        return storedDeathTime(entity.getPersistentData(), entity.deathTime);
    }

    @Override
    public int deathTimeGetFieldHook(LivingEntity entity, int initialDeathTime) {
        if (entity == null || !usesCustomDeathAnimation(entity)) {
            return initialDeathTime;
        }
        if (isInSpecialRender(entity)) {
            return 0;
        }
        int customDeathTime = storedDeathTime(entity.getPersistentData(), initialDeathTime);
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

    @Override
    public void accept(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity entity = livingTickEvent.getEntity();
        CompoundTag data = entity.getPersistentData();
        if (!usesCustomDeathAnimation(entity)) {
            data.remove(DEATH_TIME_TAG);
            return;
        }
        if (!entity.level().shouldTickDeath(entity)) {
            return;
        }

        int nextDeathTime = storedDeathTime(data, entity.deathTime) + 1;
        data.putInt(DEATH_TIME_TAG, nextDeathTime);
        entity.deathTime = nextDeathTime;
    }
}
