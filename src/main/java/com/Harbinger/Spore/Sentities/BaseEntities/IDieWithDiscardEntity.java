package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.utils.StackTraceUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface IDieWithDiscardEntity {
    LivingEntity self();
    boolean isSpecialDead();
    boolean hasLegalPosition();
    Vec3 lastLegalPosition();
    void setLegalPosition(Vec3 position);
    default void setLegalPosition(double x, double y, double z){
        setLegalPosition(new Vec3(x,y,z));
    }
    default void tickLegalPosition(){
        LivingEntity self = self();
        if(self.tickCount%30==0&&hasLegalPosition()){
            setLegalPosition(self.position);
        }
    }
    default void addAdditionalLegalPositionData(CompoundTag tag){
        Vec3 pos = lastLegalPosition();
        tag.putDouble("spore:lastLegalX", pos.x);
        tag.putDouble("spore:lastLegalY", pos.y);
        tag.putDouble("spore:lastLegalZ", pos.z);
    }
    default void readAdditionalLegalPositionData(CompoundTag tag){
        if(tag.contains("spore:lastLegalX")&&tag.contains("spore:lastLegalY")&&tag.contains("spore:lastLegalZ")){
            setLegalPosition(tag.getDouble("spore:lastLegalX"),tag.getDouble("spore:lastLegalY"),tag.getDouble("spore:lastLegalZ"));
        }
    }

    void specialDie(DamageSource source);
}
