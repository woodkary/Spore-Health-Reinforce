package com.Harbinger.Spore.Core.utils;

import com.Harbinger.Spore.Core.asmHooks.HiddenDefineHook;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author karywoodOyo
 */
public final class ClassLoaderUtil extends ClassLoader implements IClassLoader {
    public static final IClassLoader INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IClassLoader.class,
            ClassLoaderUtil.class,
            new Class<?>[]{ClassLoader.class},
            ClassLoaderUtil.class.getClassLoader()
    );
    private final ConcurrentMap<Class<?>, Class<?>> classCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> wrapperToOriginal = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> hiddenClassCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> hiddenWrapperToOriginal = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Boolean> hiddenClassBuildFailed = new ConcurrentHashMap<>();
    private final ProtectionDomain domain;

    public ClassLoaderUtil(ClassLoader parent) {
        super(parent);
        this.domain = buildDomain();
    }
    @Override
    public Class<?> tryAvoidHiddenClass(Class<?> clazz){
        if(!clazz.isHidden()){
            return clazz;
        }
        Class<?> original = HiddenDefineHook.tryGetOriginalClass(clazz);
        if (original != clazz) {
            return original;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if(!Modifier.isStatic(field.getModifiers())){
                return clazz;
            }
        }
        return clazz.getSuperclass();
    }
    @Override
    public Class<?> getOriginalClass(Class<?> clazz){
        if(!clazz.getName().contains("SporeAllReturnWrapper")){
            return clazz;
        }
        return tryAvoidHiddenClass((clazz.isHidden()?hiddenWrapperToOriginal:wrapperToOriginal).getOrDefault(clazz,clazz));
    }

    @Override
    public Class<?> deffineneClazz(ClassNode node, Class<?> initialClass) {
        if (initialClass != null) {
            Class<?> cached = classCache.get(initialClass);
            if (cached != null) {
                return cached;
            }
        }
        ClassWriter cw = new MixinClassWriter(3);
        node.accept(cw);
        String replace = node.name.replace('/', '.');
        byte[] bytes = cw.toByteArray();
        try {
            Class<?> defined = define(replace, bytes);
            if (defined != null && initialClass != null) {
                classCache.putIfAbsent(initialClass, defined);
                wrapperToOriginal.putIfAbsent(defined, initialClass);
            }
            return defined;
        }catch (Exception e) {
            LogUtil.errorf("failed to define all return class by my ClassLoader" + replace);
            LogUtil.printStackTrace(e);
        }
        if (initialClass != null) {
            try {
                Class<?> defined = ClassUtil.deffineneClazz(initialClass.getClassLoader(), replace, bytes);
                if (defined != null) {
                    classCache.putIfAbsent(initialClass, defined);
                    wrapperToOriginal.putIfAbsent(defined, initialClass);
                }
                return defined;
            } catch (Exception e) {
                LogUtil.errorf("failed to define all return class by other ClassLoader" + replace);
            }
        }
        try {
            Class<?> defined = ClassUtil.deffineneClazz(Thread.currentThread().getContextClassLoader(), replace, bytes);
            if (defined != null && initialClass != null) {
                classCache.putIfAbsent(initialClass, defined);
                wrapperToOriginal.putIfAbsent(defined, initialClass);
            }
            return defined;
        } catch (Exception e) {
            LogUtil.errorf("failed to define all return class by context ClassLoader" + replace);
            LogUtil.printStackTrace(e);
        }
        return null;
    }

    @Override
    public Class<?> creeateveWrapperHidden(Class<?> callback) {
        if (callback == null) {
            return null;
        }
        if (Entity.class.isAssignableFrom(callback) && !Player.class.isAssignableFrom(callback)) {
            if (Modifier.isFinal(callback.getModifiers())) {
                return null;
            }
            if (callback.getName().contains("SporeAllReturnWrapper")) {
                return callback;
            }
            Class<?> cached = hiddenClassCache.get(callback);
            if (cached != null) {
                return cached;
            }
            if (hiddenClassBuildFailed.containsKey(callback)) {
                return null;
            }
            try {
                synchronized (hiddenClassCache) {
                    Class<?> cache2 = hiddenClassCache.get(callback);
                    if (cache2 != null) {
                        return cache2;
                    }
                    if (hiddenClassBuildFailed.containsKey(callback)) {
                        return null;
                    }
                    ClassNode node = new ClassNode();
                    AllReturnUtil.INSTANCE.ttranssansformNode(node, callback);
                    Class<?> wrapper = deffineneHiddenClazz(node, callback);
                    if (wrapper != null) {
                        hiddenClassCache.putIfAbsent(callback, wrapper);
                        hiddenWrapperToOriginal.putIfAbsent(wrapper, callback);
                    } else {
                        hiddenClassBuildFailed.putIfAbsent(callback, Boolean.TRUE);
                    }
                    return wrapper;
                }
            } catch (Exception var2) {
                hiddenClassBuildFailed.putIfAbsent(callback, Boolean.TRUE);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Class<?> deffineneHiddenClazz(ClassNode node, Class<?> initialClass) {
        ClassWriter cw = new MixinClassWriter(3);
        node.accept(cw);
        byte[] bytes = cw.toByteArray();

        try {
            Class<?> hidden = ClassUtil.deffineneHiddenClazz(initialClass, bytes, true);
            if (hidden != null) {
                return hidden;
            }
        } catch (Exception e) {
            LogUtil.errorf("failed to define hidden all return class by host " + initialClass.getName());
            LogUtil.printStackTrace(e);
        }

        return deffineneClazz(node, initialClass);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return Class.forName(name, resolve, Thread.currentThread().getContextClassLoader());
    }

    private Class<?> define(String name, byte[] data) {
        return ClassUtil.deffineneClazz(this,name, data,this.domain);
    }

    private ProtectionDomain buildDomain() {
        ProtectionDomain domain1;
        URL original = null;
        try {
            if (this.getClass().getProtectionDomain() != null &&
                    this.getClass().getProtectionDomain().getCodeSource() != null) {
                original = this.getClass().getProtectionDomain().getCodeSource().getLocation();
            }
        } catch (Throwable ignored) {
        }
        if (original == null) {
            return null;
        }

        String path = original.toString();
        int index = path.indexOf("/mods");
        if (index != -1) {
            path = path.substring(0, index);
        }
        try{
            URL newUrl = new URL(path);
            CodeSource cs = new CodeSource(newUrl, (Certificate[])null);
            domain1 = new ProtectionDomain(cs, null);
        }catch (MalformedURLException e){
            LogUtil.errorf("malformed URL " + path);
            domain1 =null;
        }

        return domain1;
    }
}
