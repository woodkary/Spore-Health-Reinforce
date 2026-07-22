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
import java.util.Optional;

/**
 * @author karywoodOyo
 */
public final class ClassLoaderUtil extends ClassLoader implements IClassLoader, IOptionalClassValueLoader {
    public static final IClassLoader INSTANCE = BytecodeUtil.createHiddenSingletonInstance(
            IClassLoader.class,
            ClassLoaderUtil.class,
            new Class<?>[]{ClassLoader.class},
            ClassLoaderUtil.class.getClassLoader()
    );
    private final ClassValue<Optional<Class<?>>> hiddenClassCache =
            new LoadingClassValue<>(new OptionalClassValueFunction(this));
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
        Class<?> original = clazz.getSuperclass();
        return original == null ? clazz : tryAvoidHiddenClass(original);
    }

    @Override
    public Class<?> deffineneClazz(ClassNode node, Class<?> initialClass) {
        ClassWriter cw = new MixinClassWriter(3);
        node.accept(cw);
        String replace = node.name.replace('/', '.');
        byte[] bytes = cw.toByteArray();
        try {
            return define(replace, bytes);
        }catch (Exception e) {
            LogUtil.errorf("failed to define all return class by my ClassLoader" + replace);
            LogUtil.printStackTrace(e);
        }
        if (initialClass != null) {
            try {
                return ClassUtil.deffineneClazz(initialClass.getClassLoader(), replace, bytes);
            } catch (Exception e) {
                LogUtil.errorf("failed to define all return class by other ClassLoader" + replace);
            }
        }
        try {
            return ClassUtil.deffineneClazz(Thread.currentThread().getContextClassLoader(), replace, bytes);
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
            return hiddenClassCache.get(callback).orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public Optional<Class<?>> loadClassValue(Class<?> callback) {
        try {
            ClassNode node = new ClassNode();
            AllReturnUtil.INSTANCE.ttranssansformNode(node, callback);
            return Optional.ofNullable(deffineneHiddenClazz(node, callback));
        } catch (Throwable throwable) {
            LogUtil.errorf("failed to build all return wrapper for %s: %s",
                    callback.getName(), throwable.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Class<?> deffineneHiddenClazz(ClassNode node, Class<?> initialClass) {
        ClassWriter cw = new MixinClassWriter(3);
        node.accept(cw);
        byte[] bytes = cw.toByteArray();

        try {
            Class<?> hidden = ClassUtil.deffineneHiddenClazz(this,initialClass, bytes, true);
            if (hidden != null) {
                return hidden;
            }
        } catch (Exception e) {
            LogUtil.errorf("failed to define hidden all return class by host " + initialClass.getName());
            LogUtil.printStackTrace(e);
        }

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

//    @Override
//    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        return Class.forName(name, resolve, parent);
//    }

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
