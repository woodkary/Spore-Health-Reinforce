package com.Harbinger.Spore.Core.entityStorages.serverSide;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public final class ServerLevelEntityJudge<T extends Entity> implements Predicate<T> {
    public static final Predicate INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            Predicate.class,
            ServerLevelEntityJudge.class
    );
    @Override
    public boolean test(T entity) {
        return !SimpleRemoveUtil.INSTANCE.checkIsRemovedAndUpdate(entity);
    }
}
