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

public class HowitzerArm extends FallenMultipartEntity {
   public static final EntityDataAccessor RIGHT;
   public static final EntityDataAccessor NUCLEAR;

   public HowitzerArm(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.howit_hp.get() / (double)4.0F * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.howit_armor.get() / (double)4.0F * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.howit_foot_loot.get();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(RIGHT, true);
      this.entityData.define(NUCLEAR, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("right", (Boolean)this.entityData.get(RIGHT));
      tag.putBoolean("nuclear", (Boolean)this.entityData.get(NUCLEAR));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(RIGHT, tag.getBoolean("right"));
      this.entityData.set(NUCLEAR, tag.getBoolean("nuclear"));
   }

   public boolean getRight() {
      return (Boolean)this.entityData.get(RIGHT);
   }

   public void setRight(boolean i) {
      this.entityData.set(RIGHT, i);
   }

   public boolean getNuclear() {
      return (Boolean)this.entityData.get(NUCLEAR);
   }

   public void setNuclear(boolean i) {
      this.entityData.set(NUCLEAR, i);
   }

   static {
      RIGHT = SynchedEntityData.defineId(HowitzerArm.class, EntityDataSerializers.BOOLEAN);
      NUCLEAR = SynchedEntityData.defineId(HowitzerArm.class, EntityDataSerializers.BOOLEAN);
   }
}
