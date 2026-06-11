package com.Harbinger.Spore.Core.asmHooks;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;

import java.lang.invoke.MethodHandle;

final class FloatEntry implements IFloatEntry {
    public static final IFloatEntryFactory INSTANCE=BytecodeUtil.createHiddenSingletonInstance(
            IFloatEntryFactory.class,
            FloatEntryFactory.class
    );
    private static final Class<? extends IFloatEntry> entryClass = (Class<? extends IFloatEntry>) BytecodeUtil.resolveHiddenClassOrSelf(
            FloatEntry.class,
            float.class
    );

    private final int upper;
    private final int lower;
    public FloatEntry(float value) {
        int intValue = Float.floatToIntBits(value);
        this.upper = (intValue >>> 16) - 127;
        this.lower = (intValue & 0xFFFF) - 63;
    }
    @Override
    public float getFloatValue() {
        int recombinedInt = ((upper + 127) << 16) | ((lower + 63) & 0xFFFF);
        return Float.intBitsToFloat(recombinedInt);
    }
    private static final class FloatEntryFactory implements IFloatEntryFactory {
        private MethodHandle constructor;
        public FloatEntryFactory() {
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    null,
                    entryClass,
                    FloatEntry.class,
                    float.class
            );
        }
        @Override
        public IFloatEntry newInstance(float value) {
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    entryClass,
                    FloatEntry.class,
                    float.class
            );
            if(constructor != null) {
                try{
                    return (IFloatEntry) constructor.invoke(value);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to get new instance of FloatEntry. %s", e.getMessage());
                }
            }
            return new FloatEntry(value);
        }
        @Override
        public float getFloatValue(IFloatEntry entry, float defaultValue) {
            return entry != null ? entry.getFloatValue() : defaultValue;
        }
        @Override
        public boolean isValidHealthValue(IFloatEntry health) {
            if (health == null) {
                return false;
            }
            float value = health.getFloatValue();
            return !Float.isNaN(value) && value > 0.0f;
        }
    }
}
