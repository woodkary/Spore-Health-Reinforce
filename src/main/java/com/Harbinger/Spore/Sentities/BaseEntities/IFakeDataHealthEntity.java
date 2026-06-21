package com.Harbinger.Spore.Sentities.BaseEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public interface IFakeDataHealthEntity {
    LivingEntity _this();
    default void initDATA_HEALTH_IDToZero(){
        _this().entityData.set(LivingEntity.DATA_HEALTH_ID,0.0f);
    }
    default void hurtDellta(float damage){
        LivingEntity living = _this();
        float current=getDefault0HllealthDelta();
        if(current>0.0f) {
            setDefault0HllealthDelta(Math.max(current-damage,0.0f));
        }
        current=living.entityData.get(LivingEntity.DATA_HEALTH_ID);
        if(current>0.0f) {
            living.entityData.set(LivingEntity.DATA_HEALTH_ID,Math.max(current-damage,0.0f));
        }
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
