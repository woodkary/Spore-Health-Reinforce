package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.agents.transformers.SporeLivingEntityHealthTransformerBootstrap;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.unremovableCollections.ISporeSet;
import com.Harbinger.Spore.Core.utils.unremovableCollections.SporeSetProxy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LivingEntityRetransformationTask implements IStopStatusAccessibleRunnable {
    private static final ISporeSet<IStopStatusAccessibleRunnable> taskSet= SporeSetProxy.newInstance(ConcurrentHashMap.newKeySet());
    public static synchronized void submitLivingEntityClassesMixed(Class<?>... classes){
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(List.of(classes),Strategy.MIXED);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    public static void submitLivingEntityClassesJVMTIOnly(Class<?>... classes){
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(List.of(classes),Strategy.JVMTI);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    public static void submitLivingEntityClassesAll(Class<?>... classes){
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(List.of(classes),Strategy.ALL);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    
    
    private final List<Class<?>> livingEntityClasses;
    private final Strategy strategy;

    public LivingEntityRetransformationTask(List<Class<?>> livingEntityClasses,Strategy strategy) {
        this.livingEntityClasses = livingEntityClasses;
        this.strategy = strategy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LivingEntityRetransformationTask that = (LivingEntityRetransformationTask) o;
        return Objects.equals(livingEntityClasses, that.livingEntityClasses) && strategy == that.strategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(livingEntityClasses, strategy);
    }

    @Override
    public void run() {
        try {
            strategy.strategy.applyClasses(livingEntityClasses);
        } catch (Throwable throwable) {
            LogUtil.errorf("LivingEntity retransform task failed. %s",throwable.getMessage());
            LogUtil.printStackTrace(throwable);
        } finally {
            taskSet.actualRemove(this);
        }
    }

    private interface RetransformStrategy {
        void applyClasses(List<Class<?>> classes);
    }
    public enum Strategy {
        MIXED((classes)->SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClasses(classes.toArray(new Class[0]))),
        JVMTI((classes)->SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClassesJVMTIOnly(classes.toArray(new Class[0]))),
        ALL((classes)->{
            Class<?>[] classList=classes.toArray(new Class[0]);
            SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClasses(classList);
            SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClassesJVMTIOnly(classList);
        });
        private final RetransformStrategy strategy;

        Strategy(RetransformStrategy strategy) {
            this.strategy = strategy;
        }
    }
}
