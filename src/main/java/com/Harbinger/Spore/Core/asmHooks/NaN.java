package com.Harbinger.Spore.Core.asmHooks;

final class NaN implements IFloatEntry {
    @Override
    public float getFloatValue() {
        return Float.NaN;
    }
}
