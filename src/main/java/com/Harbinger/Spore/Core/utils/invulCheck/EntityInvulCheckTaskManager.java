package com.Harbinger.Spore.Core.utils.invulCheck;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

//仅服务端逻辑
public final class EntityInvulCheckTaskManager implements IEntityCheckManager {
    public static final IEntityCheckManager INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IEntityCheckManager.class,
            EntityInvulCheckTaskManager.class
    );
    private final List<IEntityInvulCheckTask> tasks = new LinkedList<>();
    @Override
    public void preServerTick() {
        Iterator<IEntityInvulCheckTask> iterator = tasks.iterator();
        while(iterator.hasNext()){
            IEntityInvulCheckTask task = iterator.next();
            if(task.preEntityTick()){
                iterator.remove();
            }
        }
    }
    @Override
    public void add(LivingEntity entity){
        tasks.add(EntityInvulCheckTask.newInstance(entity));
    }

    @Override
    public void postServerTick() {
        for (IEntityInvulCheckTask task : tasks) {
            task.postEntityTick();
        }
    }
}
