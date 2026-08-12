package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeMap;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeSet;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeMapProxy;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeSetProxy;
import net.minecraftforge.event.TickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TickableLivingRetransformManager implements ILivingRetransformManager, Function<LivingEntityRetransformationTask.Strategy,Set<Class<?>>> {
    public static final ILivingRetransformManager INSTANCE;
    private final ISporeMap<LivingEntityRetransformationTask.Strategy, Set<Class<?>>> livingClasses= SporeMapProxy.newInstance(new ConcurrentHashMap<>());
    @Override
    public void add(LivingEntityRetransformationTask.Strategy strategy, Class<?>... livingClasses) {
        if(strategy==LivingEntityRetransformationTask.Strategy.LOOP_MIXED||
            strategy==LivingEntityRetransformationTask.Strategy.LOOP_JVMTI||
            strategy==LivingEntityRetransformationTask.Strategy.LOOP_ALL){
            LogUtil.error("tickable retransform shouldn't accept task with dead loop");
            return;
        }
        Set<Class<?>> s = this.livingClasses.actualComputeIfAbsent(strategy, this);
        List<Class<?>> list = Arrays.asList(livingClasses);
        if(s instanceof ISporeSet<Class<?>> classSet) {
            classSet.actualAddAll(list);
        }else{
            s.addAll(list);
        }
    }
    @Override
    public void accept(TickEvent tickEvent) {
        for (Map.Entry<LivingEntityRetransformationTask.Strategy, Set<Class<?>>> entry : livingClasses.entrySet()) {
            LivingEntityRetransformationTask.submitTask(
                    new LivingEntityRetransformationTask(entry.getKey(),entry.getValue().toArray(new Class<?>[0]))
            );
        }
    }
    @Override
    public Set<Class<?>> apply(LivingEntityRetransformationTask.Strategy strategy) {
        return SporeSetProxy.newInstance(ConcurrentHashMap.newKeySet());
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
