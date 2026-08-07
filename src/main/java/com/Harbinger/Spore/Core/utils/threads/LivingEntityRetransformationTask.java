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
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(Strategy.MIXED,classes);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    public static void submitLivingEntityClassesJVMTIOnly(Class<?>... classes){
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(Strategy.JVMTI,classes);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    public static void submitLivingEntityClassesAll(Class<?>... classes){
        IStopStatusAccessibleRunnable runnable = new LivingEntityRetransformationTask(Strategy.ALL,classes);
        if(taskSet.actualAdd(runnable)){
            PersistentThreadPool.INSTANCE.submit(runnable);
        }
    }
    
    
    private final Class<?>[] livingEntityClasses;
    private final Strategy strategy;

    public LivingEntityRetransformationTask(Strategy strategy,Class<?>... livingEntityClasses) {
        this.livingEntityClasses = livingEntityClasses;
        this.strategy = strategy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LivingEntityRetransformationTask that = (LivingEntityRetransformationTask) o;
        return Arrays.equals(livingEntityClasses, that.livingEntityClasses) && strategy == that.strategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(livingEntityClasses), strategy);
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
        void applyClasses(Class<?>... classes);
    }
    public enum Strategy {
        MIXED(SporeLivingEntityHealthTransformerBootstrap.INSTANCE::retransformMaybeHiddenClasses),
        JVMTI(SporeLivingEntityHealthTransformerBootstrap.INSTANCE::retransformMaybeHiddenClassesJVMTIOnly),
        ALL((classes)->{
            SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClasses(classes);
            SporeLivingEntityHealthTransformerBootstrap.INSTANCE.retransformMaybeHiddenClassesJVMTIOnly(classes);
        });
        private final RetransformStrategy strategy;

        Strategy(RetransformStrategy strategy) {
            this.strategy = strategy;
        }
    }
}
