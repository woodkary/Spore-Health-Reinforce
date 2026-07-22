package com.Harbinger.Spore.Core.utils.transformation.transBootStrap;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface ITransformationBootStrap extends Predicate<Object> {
    void initPluginsMap(LaunchPluginHandler handler);

    void handleComputeReturn(EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> res);

    Optional<ILaunchPluginService> handleGetReturn(LaunchPluginHandler handler, String name, Optional<ILaunchPluginService> initialValue);

    void coexistenceCoreAndMod();

    void wrapLaunchPluginHandler(LaunchPluginHandler handler);
}
