package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeSet;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeSetProxy;
import net.minecraftforge.event.TickEvent;

import java.util.concurrent.ConcurrentHashMap;

public final class TickableLivingRetransformManager implements ILivingRetransformManager {
    public static final ILivingRetransformManager INSTANCE;
    private final ISporeSet<IStopStatusAccessibleRunnable> livingClasses= SporeSetProxy.newInstance(ConcurrentHashMap.newKeySet());
    @Override
    public void add(LivingEntityRetransformationTask.Strategy strategy, Class<?>... livingClasses) {
        this.livingClasses.actualAdd(new LivingEntityRetransformationTask(strategy, livingClasses));
    }
    @Override
    public void accept(TickEvent tickEvent) {
        for (IStopStatusAccessibleRunnable task : livingClasses) {
            LivingEntityRetransformationTask.submitTask(task);
        }
    }
    static {
        Class<? extends ILivingRetransformManager>[] clazz=new Class[1];
        INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
                clazz,
                ILivingRetransformManager.class,
                TickableLivingRetransformManager.class,
                new Class<?>[0]
        );
        if(clazz[0]!=null){
            ClassReflectionUtil.removeCachedReflectionData(clazz[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(TickableLivingRetransformManager.class);
    }
}
