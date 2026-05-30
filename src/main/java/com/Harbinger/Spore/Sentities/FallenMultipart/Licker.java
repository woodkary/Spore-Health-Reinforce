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
import net.minecraftforge.fluids.FluidType;

public class Licker extends FallenMultipartEntity {
   public static final EntityDataAccessor BURNED;

   public Licker(EntityType type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BURNED, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("burned", (Boolean)this.entityData.get(BURNED));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(BURNED, tag.getBoolean("burned"));
   }

   public boolean getBurned() {
      return (Boolean)this.entityData.get(BURNED);
   }

   public void setBurned(boolean i) {
      this.entityData.set(BURNED, i);
   }

   public boolean fireImmune() {
      return this.getBurned();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.gazen_tongue_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.gazen_hp.get() / (double)6.0F * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.gazen_armor.get() / (double)4.0F * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   static {
      BURNED = SynchedEntityData.defineId(Licker.class, EntityDataSerializers.BOOLEAN);
   }
}
