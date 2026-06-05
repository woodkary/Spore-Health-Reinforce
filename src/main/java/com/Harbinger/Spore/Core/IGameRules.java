package com.Harbinger.Spore.Core;

public interface IGameRules {
    void setCasingLightNot();
    void enableCasingLight();
    void disableCasingLight();
    void setCasingLightValue(boolean value);
    boolean casingLightAllowed();
}
