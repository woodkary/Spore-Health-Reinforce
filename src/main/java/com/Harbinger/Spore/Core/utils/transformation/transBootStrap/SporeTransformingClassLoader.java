package com.Harbinger.Spore.Core.utils.transformation.transBootStrap;

import com.Harbinger.Spore.Core.utils.ClassUtil;
import cpw.mods.modlauncher.*;

public final class SporeTransformingClassLoader extends TransformingClassLoader {
    public SporeTransformingClassLoader(TransformStore transformStore, LaunchPluginHandler pluginHandler, ModuleLayerHandler moduleLayerHandler) {
        super(transformStore, pluginHandler, moduleLayerHandler);
    }

    @Override
    protected byte[] maybeTransformClassBytes(byte[] bytes, String name, String context) {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return this.classBytes(bytes, name, context);
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return this.classBytes(bytes, name, context);
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.wrapLaunchPluginHandler(handler);
        return this.classBytes(bytes, name, context);
    }
    private byte[] classBytes(byte[] bytes, final String name,final String context) {
        if(name.startsWith("com.Harbinger.Spore.Core.")||
            name.startsWith("com/Harbinger/Spore/Core/")||
            name.startsWith("com.Harbinger.Spore.mixin.")||
            name.startsWith("com/Harbinger/Spore/Core/mixin/")) {
            return bytes;
        }
        return super.maybeTransformClassBytes(bytes, name, context);
    }

    @Override
    public Class<?> getLoadedClass(String name) {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return findLoadedClass(name);
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return findLoadedClass(name);
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.wrapLaunchPluginHandler(handler);
        return findLoadedClass(name);
    }
}
