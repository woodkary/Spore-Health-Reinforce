package com.Harbinger.Spore.Core.utils.transformation;

import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Core.utils.Log4j2PrintStream;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.transformation.plugins.LifeCycleCallSiteHookResolver;
import com.Harbinger.Spore.Core.utils.transformation.plugins.LifeCycleCallSiteHookSpec;
import com.Harbinger.Spore.Core.utils.transformation.transBootStrap.ITransformationBootStrap;
import com.Harbinger.Spore.Core.utils.transformation.transBootStrap.SporeTransformationBootStrap;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class SporeTransformationService implements ISporeTransformationService {
    @Override
    public @NotNull String name() {
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
    }
    static{
        LogUtil.log("Initializing SporeTransformationService");
        LifeCycleCallSiteHookResolver.class.getName();
        LifeCycleCallSiteHookSpec.class.getName();
        init();
    }

}
