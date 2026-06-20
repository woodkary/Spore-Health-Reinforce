package com.Harbinger.Spore.Sentities.BaseEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public interface IFakeDataHealthEntity {
    LivingEntity _this();
    void setDefault0HllealthDelta(float delta);//理论上永远传入0
    float getDefault0HllealthDelta();//不被修改时应永远返回0
    default void addFakeAdditionalData(CompoundTag tag) {
        tag.putFloat("fakeDataHealth",getDefault0HllealthDelta());
    }
    default void readFakeHealthData(CompoundTag tag) {
        if(tag.contains("fakeDataHealth")) {
            setDefault0HllealthDelta(tag.getFloat("fakeDataHealth"));
        }
    }
}
