package com.Harbinger.Spore.Core.asmHooks;

interface IFloatEntryFactory {
    IFloatEntry newInstance(float value);
    float getFloatValue(IFloatEntry entry, float defaultValue);
    boolean isValidHealthValue(IFloatEntry health);
}
