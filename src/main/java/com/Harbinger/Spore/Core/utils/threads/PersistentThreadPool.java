package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;

public final class PersistentThreadPool extends ThreadPoolExecutor {
    public static final ExecutorService INSTANCE;
    static{
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        Class<? extends ExecutorService>[] threadPoolClass=new Class[1];
        INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
                threadPoolClass,
                ExecutorService.class,
                PersistentThreadPool.class,
                new Class<?>[]{int.class,int.class,long.class,TimeUnit.class, BlockingQueue.class, ThreadFactory.class},
                threads,                // core
                threads,                // max
                60L, TimeUnit.SECONDS,  // idle timeout
                new LinkedBlockingQueue<>(),
                BytecodeUtil.createInstanceByName(
                        "com.Harbinger.Spore.Core.utils.threads.SporeThreadFactory",
                        new Class<?>[]{String.class},
                        "FakeMC") // 使用伪造线程
        );
        if(threadPoolClass[0]!=null){
            ClassReflectionUtil.removeCachedReflectionData(threadPoolClass[0]);
        }
        ClassReflectionUtil.removeCachedReflectionData(PersistentThreadPool.class);
    }
    public PersistentThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public PersistentThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public PersistentThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public PersistentThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }
    @Override
    public void shutdown() {
        // do nothing
    }
    @Override
    public List<Runnable> shutdownNow() {
        return List.of();
        // do nothing
    }
    @Override
    public boolean isShutdown() {
        return false;
    }
    @Override
    public boolean isTerminated() {
        return false;
    }
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(task);
    }
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(task, result);
    }
    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }
    @Override
    public void execute(@NotNull Runnable command) {
        super.execute(command);
    }
}
