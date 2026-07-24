package com.Harbinger.Spore.Core.utils.transformation.transBootStrap;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.ClassUtil;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.transformation.pluginMap.SporePluginHashMap;
import com.Harbinger.Spore.Core.utils.transformation.plugins.SporePluginPackageHost;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public final class SporeTransformationBootStrap implements ITransformationBootStrap, Consumer<Object> {
    private static final String BOOTSTRAP_OWNER =
            "com/Harbinger/Spore/Core/utils/transformation/transBootStrap/SporeTransformationBootStrap";
    private static final String BOOTSTRAP_INTERFACE =
            "com/Harbinger/Spore/Core/utils/transformation/transBootStrap/ITransformationBootStrap";
    private static final String LAUNCH_PLUGIN_HANDLER_INTERNAL = "cpw/mods/modlauncher/LaunchPluginHandler";
    private static final String OPTIONAL_DESC = "Ljava/util/Optional;";
    private static final String ENUM_MAP_DESC = "Ljava/util/EnumMap;";
    public static final ITransformationBootStrap INSTANCE=new SporeTransformationBootStrap();
    private final Map<String, ILaunchPluginService> protectedPluginsMap;
    private final Map<String,Class<?>> protectedPluginsClasses;
    public SporeTransformationBootStrap() {
        List<Class<?>> pluginClasses = resolveHiddenPlugins(
                "com.Harbinger.Spore.Core.utils.transformation.plugins.SporeLifeCycleCallSitePlugin"
        );
        Map<String, ILaunchPluginService> t1=new HashMap<>();
        Map<String,Class<?>> t2=new HashMap<>();
        //假设所有Constructor都是无参构造
        for (Class<?> pluginClass : pluginClasses) {
            Constructor constructor=null;
            try {
                constructor = pluginClass.getDeclaredConstructor();
            }catch (NoSuchMethodException e) {
                LogUtil.errorf("failed to find constructor for class %s", pluginClass.getName());
            }
            if(constructor!=null&&ClassUtil._new(pluginClass, constructor) instanceof ILaunchPluginService plugin){
                t1.put(plugin.name(), plugin);
                t2.put(plugin.name(), pluginClass);
                continue;
            }
            MethodHandles.Lookup lookup = ClassUtil.getLookup();
            if(lookup==null){
                continue;
            }
            try {
                MethodType ctorType = MethodType.methodType(void.class, new Class<?>[0]);
                MethodHandle handleCtor = lookup.findConstructor(pluginClass, ctorType);
                Object instance = handleCtor.invoke();
                if (instance instanceof ILaunchPluginService plugin) {
                    t1.put(plugin.name(), plugin);
                    t2.put(plugin.name(), pluginClass);
                }
            } catch (Throwable t) {
                LogUtil.error("failed to instantiate class by lookup, fallback to reflection.");
            }
        }
        protectedPluginsMap=Map.copyOf(t1);
        protectedPluginsClasses=Map.copyOf(t2);
    }
    private List<Class<?>> resolveHiddenPlugins(String n1) {
        List<Class<?>> pluginClasses = new ArrayList<>();
        Class<?> clazz=resolveHiddenPlugin(n1);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        return pluginClasses;
    }
    private List<Class<?>> resolveHiddenPlugins(String n1,String n2) {
        List<Class<?>> pluginClasses = new ArrayList<>();
        Class<?> clazz=resolveHiddenPlugin(n1);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        clazz=resolveHiddenPlugin(n2);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        return pluginClasses;
    }
    private List<Class<?>> resolveHiddenPlugins(String n1,String n2,String n3) {
        List<Class<?>> pluginClasses = new ArrayList<>();
        Class<?> clazz=resolveHiddenPlugin(n1);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        clazz=resolveHiddenPlugin(n2);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        clazz=resolveHiddenPlugin(n3);
        if (clazz != null) {
            pluginClasses.add(clazz);
        }
        return pluginClasses;
    }
    private List<Class<?>> resolveHiddenPlugins(String... classNames) {
        List<Class<?>> pluginClasses = new ArrayList<>();
        for (String className : classNames) {
            Class<?> clazz=resolveHiddenPlugin(className);
            if (clazz != null) {
                pluginClasses.add(clazz);
            }
        }
        return pluginClasses;
    }
    private Class<?> resolveHiddenPlugin(String className) {
        try {
            byte[] bytes = BytecodeUtil.loadClassBytes(className);
            return ClassUtil.deffineneHiddenClazz(SporePluginPackageHost.class, bytes, true);
        } catch (Throwable throwable) {
            LogUtil.errorf("failed to define hidden launch plugin %s, %s", className, throwable.getMessage());
            return null;
        }
    }

    @Override
    public void initPluginsMap(LaunchPluginHandler handler) {
        Map<String, ILaunchPluginService> plugins =
                (Map<String, ILaunchPluginService>) ClassUtil.getFieldValue(LaunchPluginHandler.class,handler, "plugins");
        if(plugins==null){
            return;
        }
        SporePluginHashMap newMap=new SporePluginHashMap(
                protectedPluginsMap,
                this,
                protectedPluginsMap);
        newMap.tryPutMinecraftPlugins(plugins);

        for (Map.Entry<String, ILaunchPluginService> entry : newMap.entrySet()) {
            String key = entry.getKey();
            ILaunchPluginService value = entry.getValue();
            Class<?> originalClass=protectedPluginsClasses.get(key);
            if(value.getClass()!=originalClass){
                KlassPointerUtil.INSTANCE.replaceClass(value,originalClass,null,0,0.0f);
            }
        }

        newMap.putAll(plugins);
        newMap.putAll(protectedPluginsMap);

        ClassUtil.setFieldValue(LaunchPluginHandler.class,"plugins",handler, newMap);
    }
    @Override
    public void handleComputeReturn(EnumMap<ILaunchPluginService.Phase, List<ILaunchPluginService>> res){
        for (Map.Entry<ILaunchPluginService.Phase, List<ILaunchPluginService>> phaseListEntry : res.entrySet()) {
            List<ILaunchPluginService> list = phaseListEntry.getValue();
            if(phaseListEntry.getKey()==ILaunchPluginService.Phase.BEFORE) {
                Collection<ILaunchPluginService> values = protectedPluginsMap.values();
                Set<ILaunchPluginService> missingPlugins=new HashSet<>(values);
                for (ILaunchPluginService plugin : list) {
                    if (values.contains(plugin)) {
                        missingPlugins.remove(plugin);
                    }
                }
                list.addAll(missingPlugins);
            }

        }
    }
    @Override
    public Optional<ILaunchPluginService> handleGetReturn(LaunchPluginHandler handler, String name, Optional<ILaunchPluginService> initialValue){
        Set<String> keys=protectedPluginsMap.keySet();
        if(keys.contains(name)&&initialValue.isEmpty()){
            return Optional.of(protectedPluginsMap.get(name));
        }
        return initialValue;
    }
    private boolean exclude(Object target){
        return false;
    }
    @Override
    public boolean test(Object o) {
        return exclude(o);
    }
    private String getJarPath(Class<?> clazz) {
        String file = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (!file.isEmpty()) {
            if (file.startsWith("union:")) {
                file = file.substring(6);
            }

            if (file.startsWith("/")) {
                file = file.substring(1);
            }

            file = file.substring(0, file.lastIndexOf(".jar") + 4);
            file = file.replaceAll("/", "\\\\");
        }

        return URLDecoder.decode(file, StandardCharsets.UTF_8);
    }
    //将自己从转换插件列表中移除
    @Override
    public void coexistenceCoreAndMod() {
        List<NamedPath> found = (List)ClassUtil.getFieldValue(ModDirTransformerDiscoverer.class, "found");
        if(found==null){
            return;
        }
        //found.removeIf((namedPath) -> getJarPath(SporeTransformationBootStrap.class).equals(namedPath.paths()[0].toString()));
        Iterator<NamedPath> iterator = found.iterator();
        while(iterator.hasNext()){
            NamedPath path = iterator.next();
            if(getJarPath(SporeTransformationBootStrap.class).equals(path.paths()[0].toString())){
                iterator.remove();
            }
        }

        ((EnumMap)ClassUtil.getFieldValue(ClassUtil.getFieldValue(Launcher.class,Launcher.INSTANCE, "moduleLayerHandler"), "completedLayers")).values().forEach(this);
    }
    private void iterateLayerInfo(Object layerInfo){
        ModuleLayer layer = (ModuleLayer)ClassUtil.getFieldValue(layerInfo, "layer");
        if(layer==null){
            return;
        }
        for (Module module : layer.modules()) {
            if (module.getName().equals(SporeTransformationBootStrap.class.getModule().getName())) {
                Set<ResolvedModule> modules = new HashSet<>((Collection<ResolvedModule>)ClassUtil.getFieldValue(Configuration.class,layer.configuration(), "modules"));
                Map<String, ResolvedModule> nameToModule = new HashMap<>((Map<String, ResolvedModule>)ClassUtil.getFieldValue(Configuration.class,layer.configuration(), "nameToModule"));
                modules.remove(nameToModule.remove(SporeTransformationBootStrap.class.getModule().getName()));
                ClassUtil.setFieldValue(Configuration.class,"modules",layer.configuration(), modules);
                ClassUtil.setFieldValue(Configuration.class,"nameToModule",layer.configuration(), nameToModule);
            }
        }
    }
    @Override
    public void accept(Object o) {
        iterateLayerInfo(o);
    }
    @Override
    public void wrapLaunchPluginHandler(LaunchPluginHandler handler){
        Class<?> handlerClass=handler.getClass();
        if(!handlerClass.getName().contains("SporeHandlerWrapper")){
            ClassLoader classLoader = SporeTransformationBootStrap.class.getClassLoader();
            ClassNode cn=new ClassNode();
            defineHandlerSubClass(cn, handlerClass);
            ClassWriter cw = new ClassWriter(3);
            cn.accept(cw);
            byte[] bytes = cw.toByteArray();
            handlerClass=ClassUtil.deffineneClazz(classLoader, cn.name.replace('/', '.'), bytes);
            if(handlerClass!=null) {
                KlassPointerUtil.INSTANCE.replaceClass(handler, handlerClass,"",0,0.0f);
                LogUtil.log("[Spore CoreMod] SporeHandlerWrapper class generated and loaded.");
            }else {
                LogUtil.error("[Spore CoreMod] ERROR: Failed to generate SporeHandlerWrapper class!");
            }
        }
    }

    private void defineHandlerSubClass(ClassNode node, Class<?> handlerClass) {
        String superName = Type.getInternalName(handlerClass);
        String wrapperName =
                "com/Harbinger/Spore/Core/utils/transformation/transBootStrap/SporeHandlerWrapper";
        node.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                wrapperName,
                null,
                superName,
                null
        );
        node.visitSource(".dynamic", null);
        emitGetWrapper(node, superName);
        emitComputeTransformerSetWrapper(node, superName);
        node.visitEnd();
    }

    private void emitGetWrapper(ClassNode node, String superName) {
        String descriptor = "(Ljava/lang/String;)" + OPTIONAL_DESC;
        MethodVisitor method = node.visitMethod(Opcodes.ACC_PUBLIC, "get", descriptor, null, null);
        method.visitCode();
        emitInitPluginsMap(method);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "get", descriptor, false);
        method.visitVarInsn(Opcodes.ASTORE, 2);
        method.visitFieldInsn(
                Opcodes.GETSTATIC,
                BOOTSTRAP_OWNER,
                "INSTANCE",
                "L" + BOOTSTRAP_INTERFACE + ";"
        );
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                BOOTSTRAP_INTERFACE,
                "handleGetReturn",
                "(L" + LAUNCH_PLUGIN_HANDLER_INTERNAL + ";Ljava/lang/String;"
                        + OPTIONAL_DESC + ")" + OPTIONAL_DESC,
                true
        );
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private void emitComputeTransformerSetWrapper(ClassNode node, String superName) {
        String descriptor = "(Lorg/objectweb/asm/Type;ZLjava/lang/String;"
                + "Lcpw/mods/modlauncher/TransformerAuditTrail;)" + ENUM_MAP_DESC;
        MethodVisitor method = node.visitMethod(
                Opcodes.ACC_PUBLIC,
                "computeLaunchPluginTransformerSet",
                descriptor,
                null,
                null
        );
        method.visitCode();
        emitInitPluginsMap(method);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitVarInsn(Opcodes.ILOAD, 2);
        method.visitVarInsn(Opcodes.ALOAD, 3);
        method.visitVarInsn(Opcodes.ALOAD, 4);
        method.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                superName,
                "computeLaunchPluginTransformerSet",
                descriptor,
                false
        );
        method.visitVarInsn(Opcodes.ASTORE, 5);
        method.visitFieldInsn(
                Opcodes.GETSTATIC,
                BOOTSTRAP_OWNER,
                "INSTANCE",
                "L" + BOOTSTRAP_INTERFACE + ";"
        );
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                BOOTSTRAP_INTERFACE,
                "handleComputeReturn",
                "(" + ENUM_MAP_DESC + ")V",
                true
        );
        method.visitVarInsn(Opcodes.ALOAD, 5);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private void emitInitPluginsMap(MethodVisitor method) {
        method.visitFieldInsn(
                Opcodes.GETSTATIC,
                BOOTSTRAP_OWNER,
                "INSTANCE",
                "L" + BOOTSTRAP_INTERFACE + ";"
        );
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                BOOTSTRAP_INTERFACE,
                "initPluginsMap",
                "(L" + LAUNCH_PLUGIN_HANDLER_INTERNAL + ";)V",
                true
        );
    }
}
