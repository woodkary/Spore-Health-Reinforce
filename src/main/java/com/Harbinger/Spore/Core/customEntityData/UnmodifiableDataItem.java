package com.Harbinger.Spore.Core.customEntityData;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public final class UnmodifiableDataItem<T> extends SynchedEntityData.DataItem<T> {
    private final T value1;
    private final T initialValue1;
    private final boolean isSetToDefault;
    public UnmodifiableDataItem(EntityDataAccessor<T> p_135394_, T value) {
        super(p_135394_, value);
        this.value1=value;
        this.initialValue1=value;
        isSetToDefault = true;
    }
    public UnmodifiableDataItem(EntityDataAccessor<T> p_135394_, T value,T initialValue1) {
        super(p_135394_, value);
        this.value1=value;
        this.initialValue1=initialValue1;
        isSetToDefault = this.initialValue1.equals(this.value1);
    }

    @Override
    public EntityDataAccessor<T> getAccessor() {
        return this.accessor;
    }

    @Override
    public void setValue(T p_135398_) {

    }

    @Override
    public T getValue() {
        return this.value1;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void setDirty(boolean p_135402_) {

    }

    @Override
    public boolean isSetToDefault() {
        return isSetToDefault;
    }

    @Override
    public SynchedEntityData.DataValue<T> value() {
        return SynchedEntityData.DataValue.create(this.accessor, this.value1);
    }
}
