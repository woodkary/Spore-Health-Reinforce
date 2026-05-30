package com.Harbinger.Spore.Sentities.Organoids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.entity.PartEntity;

public class TentaclePart extends PartEntity {
   public final Tentacle parent;
   public final String name;
   protected final EntityDimensions size;
   public final float length;

   public TentaclePart(Tentacle parent, String name, EntityDimensions size, float length) {
      super(parent);
      this.parent = parent;
      this.name = name;
      this.size = size;
      this.length = length;
      this.setPos(parent.getX(), parent.getY(), parent.getZ());
      this.refreshDimensions();
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   public boolean isPickable() {
      return true;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.size;
   }

   public boolean is(Entity entity) {
      return this == entity || this.parent == entity;
   }

   public Packet getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   public void deserializeNBT(Tag nbt) {
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() == this.parent) {
         return false;
      } else {
         return !this.isInvulnerableTo(source) && this.parent.hurt(this, source, amount);
      }
   }
}
