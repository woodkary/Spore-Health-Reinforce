package com.Harbinger.Spore.Sentities.Hyper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

public class HevokerPart extends PartEntity {
   public final Hevoker parentMob;
   public final String name;
   private final EntityDimensions size;

   public HevokerPart(Hevoker parent, String name, float s, float s2) {
      super(parent);
      this.parentMob = parent;
      this.name = name;
      this.size = EntityDimensions.scalable(s, s2);
      this.refreshDimensions();
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag compoundTag) {
   }

   protected void addAdditionalSaveData(CompoundTag compoundTag) {
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      return this.parentMob.interact(this, player, hand);
   }

   public boolean isPickable() {
      return true;
   }

   public boolean is(Entity entity) {
      return this == entity || this.parentMob == entity;
   }

   public Packet getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   public EntityDimensions getDimensions(Pose p_31023_) {
      return this.size;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public void deserializeNBT(Tag nbt) {
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() == this.parentMob) {
         return false;
      } else {
         return !this.isInvulnerableTo(source) && this.parentMob.hurt(this, source, amount);
      }
   }
}
