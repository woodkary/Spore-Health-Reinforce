package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class SporeThreadFactory implements ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger(1);
    private final String poolName;
    private final Constructor<?> sporeThreadConstructor;
    public SporeThreadFactory(String poolName) {
        this.poolName = poolName;
        Class<?> sporeThreadClass = BytecodeUtil.resolveHiddenClassByName(
                "com.Harbinger.Spore.Core.utils.threads.SporeThread",
                Runnable.class, String.class, int.class, float.class, double.class);
        Constructor<?> ctor=null;
        try {
            ctor = sporeThreadClass.getDeclaredConstructor(Runnable.class, String.class, int.class, float.class, double.class);
        }catch (NoSuchMethodException e) {
            LogUtil.error("failed to find SporeThread constructor");
        }
        sporeThreadConstructor = ctor;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        String s = poolName + "-worker-" + counter.getAndIncrement();
        // 使用 SporeThread 来伪造栈轨迹
        Thread t=null;
        if(sporeThreadConstructor!=null) {
            try {
                Object res=sporeThreadConstructor.newInstance(r,s,0,0.0f,0.0);
                if(res instanceof Thread thread) {
                    t=thread;
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                LogUtil.errorf("failed to instantiate SporeThread");
            }
        }
        if(t==null){
            t=new Thread(r,s);
        }
        t.setDaemon(true);
        return t;
    }
}
