package com.Harbinger.Spore.Core.utils;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodType;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author karywoodOyo
 */
public class StackTraceUtil {
    public static final StackWalker WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /** 默认最大检查层数 */
    private static final int DEFAULT_MAX_DEPTH = 12;
    /** shouldStopThread 判定缓存 TTL（纳秒） */
    private static final long SHOULD_STOP_CACHE_TTL_NANOS = 300_000_000L; // 300ms
    private static final int SHOULD_STOP_CACHE_MAX_SIZE = 4096;
    private static final ConcurrentHashMap<Long, StopDecision> SHOULD_STOP_CACHE = new ConcurrentHashMap<>();
    public static final Predicate<String> IS_BAD_MOD_NAME=StackTraceUtil::isBadModName;
    /* =========================
     * 默认版本（12 层）
     * ========================= */
    public static boolean isCallFromTargetClass(
            Predicate<String> classNameJudge
    ) {
        return isCallFromTargetClass(classNameJudge, DEFAULT_MAX_DEPTH,4);
    }
    public static boolean isCallFromTargetClass(
            Predicate<String> classNameJudge,int startDeath
    ){
        return isCallFromTargetClass(classNameJudge,DEFAULT_MAX_DEPTH,startDeath);
    }
    public static boolean isCallFromClientLevel(){
        return WALKER.walk(stream ->
                stream
                        .skip(2)               // 跳过 StackWalker + 本方法
                        .limit(12)
                        .anyMatch(frame -> {
                            Class<?> declaringClass = frame.getDeclaringClass();
                            try {
                                return ClientLevel.class.isAssignableFrom(declaringClass);
                            } catch (Throwable t) {
                                // 防御：judge 不应影响主逻辑
                                return false;
                            }
                        })
        );
    }

    /* =========================
     * 可配置最大栈深版本
     * ========================= */
    public static boolean isCallFromTargetClass(
            Predicate<String> classNameJudge,
            int maxSize,
            int startDepth
    ) {
        if (maxSize <= 0) {
            return false;
        }

        return WALKER.walk(stream ->
                stream
                        .skip(startDepth)               // 跳过 StackWalker + 本方法
                        .limit(maxSize)
                        .anyMatch(frame -> {
                            String className =
                                    frame.getDeclaringClass().getName();
                            try {
                                return classNameJudge.test(className);
                            } catch (Throwable t) {
                                // 防御：judge 不应影响主逻辑
                                return false;
                            }
                        })
        );
    }

    public static boolean isCallFromOther() {
        return StackTraceUtil.isCallFromTargetClass(
                StackTraceUtil.IS_BAD_MOD_NAME
        );
    }
    public static boolean isBadModName(String name){
        name=name.replace("/",".");
        return !isOwnModName(name)&&
                !isMinecraftVanillaStrict(name)&&
                !isWhiteListedMod(name);
    }

    private static boolean isOwnModName(String name) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        return lowerName.startsWith("com.harbinger.spore.");
    }

    public static boolean isMinecraftVanilla(Object obj) {
        assert obj != null;

        String name = obj instanceof Class ? ((Class)obj).getName() : obj.getClass().getName();
        return isMinecraftVanilla(name);
    }
    public static boolean isMinecraftVanillaStrict(String name) {
        if(name.startsWith("net.minecraft.")) {
            String subString = name.substring("net.minecraft.".length());
            //判断是不是net.minecraft下的类（不含子包）
            if (!subString.contains(".")) {
                return true;
            }
            //逐个确认子包
            return subString.startsWith("advancements") ||
                    subString.startsWith("client") ||
                    subString.startsWith("commands") ||
                    subString.startsWith("core") ||
                    subString.startsWith("data") ||
                    subString.startsWith("gametest") ||
                    subString.startsWith("locale") ||
                    subString.startsWith("nbt") ||
                    subString.startsWith("network") ||
                    subString.startsWith("obfuscate") ||
                    subString.startsWith("realms") ||
                    subString.startsWith("recipebook") ||
                    subString.startsWith("resources") ||
                    subString.startsWith("server") ||
                    subString.startsWith("sounds") ||
                    subString.startsWith("stats") ||
                    subString.startsWith("tags") ||
                    subString.startsWith("util") ||
                    subString.startsWith("world");
        }
        if(name.startsWith("cpw.mods.")){
            String subString = name.substring("cpw.mods.".length());
            //判断是不是cpw.mods.下的类（不含子包）
            if (!subString.contains(".")) {
                return true;
            }
            return subString.startsWith("cl")||
                    subString.startsWith("jarhandling")||
                    subString.startsWith("niofs")||
                    subString.startsWith("util")||
                    subString.startsWith("modlauncher")||subString.startsWith("bootstraplauncher");
        }
        return isMinecraftVanilla(name);
    }

    public static boolean isMinecraftVanilla(String name) {
        if (name.startsWith("com.electronwill.")) {
            return true;
        } else if (name.startsWith("com.google.")) {
            return true;
        } else if (name.startsWith("com.mojang.")) {
            return true;
        } else if (name.startsWith("com.sun.")) {
            return true;
        } else if (name.startsWith("cpw.mods.")) {
            return true;
        } else if (name.startsWith("io.netty.")) {
            return true;
        } else if (name.startsWith("it.unimi.")) {
            return true;
        } else if (name.startsWith("java.")) {
            return true;
        } else if (name.startsWith("javax.")) {
            return true;
        } else if (name.startsWith("jdk.")) {
            return true;
        } else if (name.startsWith("joptsimple.")) {
            return true;
        } else if (name.startsWith("net.minecraft.")) {
            return true;
        } else if (name.startsWith("net.minecraftforge.")) {
            return true;
        } else if (name.startsWith("net.minecrell.")) {
            return true;
        } else if (name.startsWith("org.antlr.")) {
            return true;
        } else if (name.startsWith("org.apache.")) {
            return true;
        } else if (name.startsWith("org.lwjgl.")) {
            return true;
        } else if (name.startsWith("org.objectweb.")) {
            return true;
        } else if (name.startsWith("org.openjdk.")) {
            return true;
        } else if (name.startsWith("org.slf4j.")) {
            return true;
        } else if (name.startsWith("org.spongepowered.")) {
            return true;
        } else {
            return name.startsWith("sun.");
        }
    }

    public static boolean isWhiteListedMod(String name) {
        if (name.startsWith("com.llamalad7.mixinextras.")) {
            return true;
        } else if (name.startsWith("me.jellysquid.mods.sodium.")) {
            return true;
        } else if (name.startsWith("net.irisshaders.iris.")) {
            return true;
        } else if (name.startsWith("net.irisshaders.batchedentityrendering.")) {
            return true;
        } else if (name.startsWith("dev.tr7zw.entityculling.")) {
            return true;
        } else if (name.startsWith("net.raphimc.immediatelyfast.")) {
            return true;
        } else if (name.startsWith("malte0811.ferritecore.")) {
            return true;
        } else if (name.startsWith("software.bernie.geckolib.")) {
            return true;
        } else if (name.startsWith("com.supermartijn642.fusion.")) {
            return true;
        } else if (name.startsWith("com.supermartijn642.core.")) {
            return true;
        } else if (name.startsWith("dev.architectury.")) {
            return true;
        } else if (name.startsWith("top.theillusivec4.caelus.")) {
            return true;
        } else if (name.startsWith("com.github.alexthe666.citadel.")) {
            return true;
        } else if (name.startsWith("top.theillusivec4.curios.")) {
            return true;
        } else if (name.startsWith("virtuoel.pehkui.")) {
            return true;
        } else if (name.startsWith("dev.kosmx.playerAnim.")) {
            return true;
        } else if (name.startsWith("optifine.")) {
            return true;
        } else {
            return name.startsWith("net.optifine.")||
                    name.startsWith("mezz.jei.")||
                    name.startsWith("fi.dy.masa.tweakeroo")||
                    name.startsWith("oshi.")||
                    name.startsWith("io.github.flemmli97.mobbattle.");
        }
    }
    public static boolean isServerThread() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }
    public static void stopAllThreads(){
        try {

            Set<Thread> allThreads = Thread.getAllStackTraces().keySet();
            for (Thread thread : allThreads) {
                if (thread == null) {
                    continue;
                }
                if (!shouldStopThread(thread)) {
                    continue;
                }
                stop(thread);
            }
        }catch (Throwable t){
            LogUtil.errorf("Stopping all threads failed. %s",t);
            LogUtil.printStackTrace(t);
        }
    }
    private static void stop(Thread thread){
        LogUtil.logf("stopping thread %s of type %s",thread.getName(),thread.getClass().getName());
        try {
            ClassUtil.getLookup().findSpecial(
                    Thread.class,
                    "interrupt",
                    MethodType.methodType(void.class),
                    thread.getClass()
            ).invoke(thread);
        }catch (Throwable t){
            LogUtil.errorf("failed to stop thread by MethodHandle fallback to basic stop. %s", t);
            stop0(thread);
        }
        Optional.ofNullable(creeateveWrapperHidden(thread.getClass()))
                .ifPresent(wrapper -> KlassPointerUtil.INSTANCE.replaceClass(thread,wrapper,"",0,0.0f));
    }
    private static void stop0(Thread thread){
        thread.interrupt();
    }
    private static final ClassValue<Optional<Class<?>>> wrappers =
            new LoadingClassValue<>(StackTraceUtil::buildThreadWrapper);
    private static Class<?> creeateveWrapperHidden(Class<?> callback){
        if(callback.getName().contains("SporeAllReturnWrapper")) {
            return callback;
        }
        return wrappers.get(callback).orElse(callback);
    }
    private static Optional<Class<?>> buildThreadWrapper(Class<?> callback) {
        try {
            ClassNode node = new ClassNode();
            AllReturnUtil.INSTANCE.ttranssansformNode(node, callback);
            return Optional.ofNullable(ClassLoaderUtil.INSTANCE.deffineneHiddenClazz(node, callback));
        } catch (Throwable throwable) {
            LogUtil.errorf("failed to cast class %s: %s", callback.getName(), throwable.getMessage());
            return Optional.empty();
        }
    }
    public static boolean isClientThread() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
    }
    private static boolean shouldStopThread(Thread thread) {
        if (thread == null) return false;

        long now = System.nanoTime();
        long threadId = thread.getId();
        int identityHash = System.identityHashCode(thread);

        StopDecision cached = SHOULD_STOP_CACHE.get(threadId);
        if (cached != null
                && cached.identityHash == identityHash
                && cached.expiresAtNanos > now) {
            return cached.shouldStop;
        }

        boolean shouldStop = false;
        if (isBadModName(thread.getClass().getName())) {
            shouldStop = true;
        }

        if (!shouldStop) {
            StackTraceElement[] st = thread.getStackTrace();
            int max = Math.min(DEFAULT_MAX_DEPTH, st.length);
            for (int i = 0; i < max; i++) {
                if(st[i]==null) continue;
                String cn = st[i].getClassName();
                if (isBadModName(cn)) {
                    shouldStop = true;
                    break;
                }
            }
        }

        SHOULD_STOP_CACHE.put(threadId, new StopDecision(shouldStop, identityHash, now + SHOULD_STOP_CACHE_TTL_NANOS));
        pruneShouldStopCache(now);
        return shouldStop;
    }

    private static void pruneShouldStopCache(long now) {
        if (SHOULD_STOP_CACHE.size() <= SHOULD_STOP_CACHE_MAX_SIZE) {
            return;
        }
        SHOULD_STOP_CACHE.entrySet().removeIf(e -> e.getValue().expiresAtNanos <= now);
        if (SHOULD_STOP_CACHE.size() > SHOULD_STOP_CACHE_MAX_SIZE * 2) {
            int removed = 0;
            for (Long key : SHOULD_STOP_CACHE.keySet()) {
                SHOULD_STOP_CACHE.remove(key);
                if (++removed >= 256) {
                    break;
                }
            }
        }
    }

    private static final class StopDecision {
        final boolean shouldStop;
        final int identityHash;
        final long expiresAtNanos;

        StopDecision(boolean shouldStop, int identityHash, long expiresAtNanos) {
            this.shouldStop = shouldStop;
            this.identityHash = identityHash;
            this.expiresAtNanos = expiresAtNanos;
        }
    }
}
