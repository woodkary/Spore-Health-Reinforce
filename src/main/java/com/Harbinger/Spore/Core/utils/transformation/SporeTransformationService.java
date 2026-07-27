package com.Harbinger.Spore.Core.utils.transformation;

import com.Harbinger.Spore.Core.utils.*;
import com.Harbinger.Spore.Core.utils.transformation.plugins.LifeCycleCallSiteHookResolver;
import com.Harbinger.Spore.Core.utils.transformation.plugins.LifeCycleCallSiteHookSpec;
import com.Harbinger.Spore.Core.utils.transformation.transBootStrap.ITransformationBootStrap;
import com.Harbinger.Spore.Core.utils.transformation.transBootStrap.SporeTransformationBootStrap;
import cpw.mods.modlauncher.*;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class SporeTransformationService implements ISporeTransformationService {
    public SporeTransformationService() {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return;
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return;
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.wrapLaunchPluginHandler(handler);
    }

    @Override
    public @NotNull String name() {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return "SporeTransformationService";
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return "SporeTransformationService";
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.wrapLaunchPluginHandler(handler);
        return "SporeTransformationService";
    }

    @Override
    public void initialize(IEnvironment environment) {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return;
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return;
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.wrapLaunchPluginHandler(handler);
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return;
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return;
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.wrapLaunchPluginHandler(handler);
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return List.of();
        }
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return List.of();
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.wrapLaunchPluginHandler(handler);
        return List.of();
    }
    private static void init(){
        Launcher launcher = Launcher.INSTANCE;
        if (launcher == null) {
            return;
        }
        KlassPointerUtil.INSTANCE.replaceClass(launcher,Launcher.class,"",0,0.0f);
        Object value = ClassUtil.getFieldValue(Launcher.class, launcher, "launchPlugins");
        if (!(value instanceof LaunchPluginHandler handler)) {
            return;
        }
        ITransformationBootStrap bootstrap = SporeTransformationBootStrap.INSTANCE;
        bootstrap.initPluginsMap(handler);
        bootstrap.coexistenceCoreAndMod();
        bootstrap.wrapLaunchPluginHandler(handler);

        Object cl = ClassUtil.getFieldValue(Launcher.class, launcher, "classLoader");
        if(!(cl instanceof TransformingClassLoader classLoader)){
            return;
        }
        Class<?> classLoaderClass=BytecodeUtil.resolveHiddenClassByName("com.Harbinger.Spore.Core.utils.transformation.transBootStrap.SporeTransformingClassLoader",
                TransformStore.class,LaunchPluginHandler.class, ModuleLayerHandler.class);
        KlassPointerUtil.INSTANCE.replaceClass(classLoader,classLoaderClass,"",0,0.0f);
    }
    static{
        LogUtil.log("Initializing SporeTransformationService");
        LifeCycleCallSiteHookResolver.class.getName();
        LifeCycleCallSiteHookSpec.class.getName();
        init();
    }

}
