package com.Harbinger.Spore.Sentities.FallenMultipart;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.BaseEntities.FallenMultipartEntity;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SiegerTail extends FallenMultipartEntity {
   public static final EntityDataAccessor WAR;

   public SiegerTail(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.sieger_tail_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sieger_hp.get() / (double)4.0F * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sieger_armor.get() / (double)4.0F * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(WAR, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("war", (Boolean)this.entityData.get(WAR));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(WAR, tag.getBoolean("war"));
   }

   public boolean getWar() {
      return (Boolean)this.entityData.get(WAR);
   }

   public void setWar(boolean i) {
      this.entityData.set(WAR, i);
   }

   static {
      WAR = SynchedEntityData.defineId(SiegerTail.class, EntityDataSerializers.BOOLEAN);
   }
}
