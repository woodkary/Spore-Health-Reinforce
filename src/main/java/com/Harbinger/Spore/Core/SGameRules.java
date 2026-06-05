package com.Harbinger.Spore.Core;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;

public final class SGameRules implements IGameRules {
    public static final IGameRules INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IGameRules.class,
            SGameRules.class
    );
    private volatile boolean casingLightAllowed;
    @Override
    public synchronized void setCasingLightNot(){
        casingLightAllowed=!casingLightAllowed;
    }
    public void enableCasingLight(){
        casingLightAllowed=true;
    }
    public void disableCasingLight(){
        casingLightAllowed=false;
    }
    public void setCasingLightValue(boolean value){
        casingLightAllowed=value;
    }
    @Override
    public boolean casingLightAllowed() {
        return casingLightAllowed;
    }


}
