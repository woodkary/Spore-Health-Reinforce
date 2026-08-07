package com.Harbinger.Spore.Core.utils.threads;

import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class SporeThread extends Thread {
    public SporeThread() {
    }
    public SporeThread(Runnable target, String name, int f1, float f2, double f3) {
        super(target, name);
    }

    public SporeThread(Runnable target) {
        super(target);
    }

    public SporeThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public SporeThread(@NotNull String name) {
        super(name);
    }

    public SporeThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    public SporeThread(Runnable target, String name) {
        super(target, name);
    }

    public SporeThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
    }

    public SporeThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    public SporeThread(ThreadGroup group, Runnable target, String name, long stackSize, boolean inheritThreadLocals) {
        super(group, target, name, stackSize, inheritThreadLocals);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if(StackTraceUtil.ENTITY_TICKS==null) {
            StackTraceElement[] stackTrace = super.getStackTrace();
            StackTraceUtil.ENTITY_TICKS = new StackTraceElement[stackTrace.length];
            Arrays.fill(StackTraceUtil.ENTITY_TICKS, new StackTraceElement("net.minecraft.world.entity.Entity", "tick", "Entity.java", 0));
        }
        return StackTraceUtil.ENTITY_TICKS.clone();
    }

    @Override
    public void interrupt() {

    }

    @Override
    public boolean isInterrupted() {
        return false;
    }
}
