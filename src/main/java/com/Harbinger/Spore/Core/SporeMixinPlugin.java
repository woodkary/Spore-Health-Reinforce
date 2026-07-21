package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Core.agents.transformers.InstrumentationImplTransformUtil;
import com.Harbinger.Spore.Core.asmHooks.HiddenDefineHook;
import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Spore;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class SporeMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
    private static void lifeCycleTransformerCallSite(){

    }
    private static void init(){
        Throwable.class.toString();
        try{
            Class.forName("java.lang.Throwable$WrappedPrintStream");
        }catch (ClassNotFoundException ignored){}
        ClassLoader classLoader = SporeMixinPlugin.class.getClassLoader();
        if(classLoader!=null) {
            try {
                BytecodeUtil.deffineneClazz(classLoader,"com.Harbinger.Spore.Core.asmHooks.HiddenDefineHook");
            } catch (Throwable e) {
                LogUtil.errorf("failed to load agents transformer class");
            }
        }
        HiddenDefineHook.inspectHiddenDefine();

        if(classLoader != null){
            try {
                BytecodeUtil.deffineneClazz(classLoader, "com.Harbinger.Spore.Core.agents.transformers.InstrumentationImplTransformUtil");
            } catch (Throwable e) {
                LogUtil.errorf("failed to load agents transformer class");
            }
        }
        InstrumentationImplTransformUtil.INSTANCE.inspectInstrumentationImpl();
    }
    static{
        init();
    }
}
