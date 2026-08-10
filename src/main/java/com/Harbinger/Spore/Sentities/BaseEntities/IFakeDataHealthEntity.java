package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.entityStorages.ICustomEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

public interface IFakeDataHealthEntity {
    LivingEntity _this();
    default float getVanillaDataHealth() {
        if (!(_this().entityData instanceof ICustomEntityData custom)) {
            return 0.0F;
        }

        SynchedEntityData.DataItem<?> item = custom.vanillaItemsById()
                .get(LivingEntity.DATA_HEALTH_ID.getId());

        return item != null && item.getValue() instanceof Number number
                ? number.floatValue()
                : 0.0F;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    default void setVanillaDataHealth(float value) {
        if (_this().entityData instanceof ICustomEntityData custom) {
            SynchedEntityData.DataItem item = custom.vanillaItemsById()
                    .get(LivingEntity.DATA_HEALTH_ID.getId());
            if (item != null) {
                item.setValue(value);
            }
        }
    }
    default void initDATA_HEALTH_IDToZero(){
        _this().entityData.set(LivingEntity.DATA_HEALTH_ID,0.0f);
        setVanillaDataHealth(0.0F);
    }
    default void hurtDellta(float damage) {
        if (!(damage > 0.0F)) {
            return;
        }

        float current = getDefault0HllealthDelta();
        if (current > 0.0F) {
            setDefault0HllealthDelta(
                    Math.max(current - damage, 0.0F)
            );
        }

        LivingEntity living = _this();
        current = living.entityData.get(LivingEntity.DATA_HEALTH_ID);
        if (current > 0.0F) {
            living.entityData.set(
                    LivingEntity.DATA_HEALTH_ID,
                    Math.max(current - damage, 0.0F)
            );
        }

        current = getVanillaDataHealth();
        if (current > 0.0F) {
            setVanillaDataHealth(
                    Math.max(current - damage, 0.0F)
            );
        }
    }
    default void clearHllealthDelta(){
        setDefault0HllealthDelta(0.0f);
        _this().entityData.set(LivingEntity.DATA_HEALTH_ID,0.0f);
        setVanillaDataHealth(0.0F);
    }
    default float getAllHllealthDelta() {
        return positive(getDefault0HllealthDelta())
                + positive(_this().entityData.get(LivingEntity.DATA_HEALTH_ID))
                + positive(getVanillaDataHealth());
    }

    private float positive(float value) {
        return !Float.isNaN(value) && value > 0.0F ? value : 0.0F;
    }
    void setDefault0HllealthDelta(float delta);//理论上永远传入0
    float getDefault0HllealthDelta();//不被修改时应永远返回0
    default void addFakeAdditionalData(CompoundTag tag) {
        tag.putFloat("fakeDataHealth",getDefault0HllealthDelta());
        tag.putFloat("dataHealth",_this().entityData.get(LivingEntity.DATA_HEALTH_ID));
        tag.putFloat("vanillaDataHealth",getVanillaDataHealth());
    }
    default void readFakeHealthData(CompoundTag tag) {
        if(tag.contains("fakeDataHealth")) {
            setDefault0HllealthDelta(tag.getFloat("fakeDataHealth"));
        }
        if(tag.contains("dataHealth")) {
            _this().entityData.set(LivingEntity.DATA_HEALTH_ID,tag.getFloat("dataHealth"));
        }
        if(tag.contains("vanillaDataHealth")) {
            setVanillaDataHealth(tag.getFloat("vanillaDataHealth"));
        }
    }
}
