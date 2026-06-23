package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.entityStorages.GameTickerUtil;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeClientLevel;
import com.Harbinger.Spore.Core.entityStorages.clientSide.SporeTransientEntitySectionManager;
import com.Harbinger.Spore.Core.entityStorages.serverSide.SporePersistentEntitySectionManager;
import com.Harbinger.Spore.Core.entityStorages.serverSide.SporeServerLevel;
import com.Harbinger.Spore.Core.utils.KlassPointerUtil;
import com.Harbinger.Spore.network.SyncLegalPositionPacket;
import com.Harbinger.Spore.network.SyncLegalPositionPacketHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface IDieWithDiscardEntity {
    LivingEntity self();
    boolean isSpecialDefasd();
    boolean hasLegalPosition();
    Vec3 lastLegalPosition();
    void setLegalPosition(Vec3 position);
    default void syncAtFinalizeSpawn(){
        LivingEntity self = self();
        setLegalPosition(self.position);
        SyncLegalPositionPacketHandler.sendToClient(new SyncLegalPositionPacket(self.id,self.position));
    }
    default void setLegalPosition(double x, double y, double z){
        setLegalPosition(new Vec3(x,y,z));
    }
    default void tickLegalPosition(){
        LivingEntity self = self();
        if((self.tickCount<30||self.tickCount%30==0)&&hasLegalPosition()){
            setLegalPosition(self.position);
        }
        if(self.level instanceof ServerLevel serverLevel&&serverLevel.entityManager.getClass()!=SporePersistentEntitySectionManager.managerClass){
            KlassPointerUtil.INSTANCE.replaceClass(serverLevel.entityManager,SporePersistentEntitySectionManager.managerClass,"",0,0.0f);
        }
        if(self.level instanceof ClientLevel clientlevel&&clientlevel.getClass()!=SporeClientLevel.clientLevelClass){
            KlassPointerUtil.INSTANCE.replaceClass(clientlevel, SporeClientLevel.clientLevelClass,"",0,0.0f);
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
