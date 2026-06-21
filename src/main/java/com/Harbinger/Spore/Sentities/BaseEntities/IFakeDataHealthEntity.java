package com.Harbinger.Spore.Sentities.BaseEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public interface IFakeDataHealthEntity {
    LivingEntity _this();
    default void initDATA_HEALTH_IDToZero(){
        _this().entityData.set(LivingEntity.DATA_HEALTH_ID,0.0f);
    }
    default void hurtDellta(float damage){
        if(damage>0.0f) {
            clearDefault0HllealthDelta();
        }
    }
    default float clearDefault0HllealthDelta(){
        float delta = getDefault0HllealthDelta();
        setDefault0HllealthDelta(0.0f);
        _this().entityData.set(LivingEntity.DATA_HEALTH_ID,0.0f);
        return delta;
    }
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
