package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public final class CustomDeathTimeManager implements IDeathTimeManager {
    private static final String DEATH_TIME_TAG = "spore$DeathTime";

    public static final IDeathTimeManager INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IDeathTimeManager.class,
            CustomDeathTimeManager.class
    );

    @Override
    public int deathTimeGetFieldHook(LivingEntity entity, int initialDeathTime) {
        if (entity == null || SporeJudge.isSporeEntity(entity)) {
            return initialDeathTime;
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
    public void accept(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity entity = livingTickEvent.getEntity();
        CompoundTag data = entity.getPersistentData();
        if (!entity.isDeadOrDying()) {
            boolean wasManaged = data.contains(DEATH_TIME_TAG, Tag.TAG_INT);
            data.remove(DEATH_TIME_TAG);
            if (wasManaged) {
                entity.deathTime = 0;
            }
            return;
        }
        if (SporeJudge.isSporeEntity(entity)) {
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
