package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Calamities.Hinderburg;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

public class TumoroidNuke extends UtilityEntity {
   public static final EntityDataAccessor TIMER;
   public static final EntityDataAccessor OVERCLOCKED;
   public static final EntityDataAccessor BUSTER;
   private Hinderburg hinderburg;

   public TumoroidNuke(Level level, Hinderburg hinderburg) {
      super((EntityType)Sentities.TUMOROID_NUKE.get(), level);
      this.hinderburg = hinderburg;
      this.moveTo(hinderburg.getX(), hinderburg.getY(), hinderburg.getZ());
      this.setYBodyRot(hinderburg.getYRot());
      this.setTimer(80);
   }

   public TumoroidNuke(EntityType type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TIMER, 0);
      this.entityData.define(OVERCLOCKED, false);
      this.entityData.define(BUSTER, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putBoolean("overclocked", (Boolean)this.entityData.get(OVERCLOCKED));
      tag.putBoolean("buster", (Boolean)this.entityData.get(BUSTER));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(OVERCLOCKED, tag.getBoolean("overclocked"));
      this.entityData.set(BUSTER, tag.getBoolean("buster"));
   }

   public int getTimer() {
      return (Integer)this.entityData.get(TIMER);
   }

   public void setTimer(int e) {
      this.entityData.set(TIMER, e);
   }

   private void tickTimer() {
      this.setTimer(this.getTimer() - 1);
   }

   public void setOverclocked(boolean value) {
      this.entityData.set(OVERCLOCKED, value);
   }

   public void setBuster(boolean value) {
      this.entityData.set(BUSTER, value);
   }

   public void tick() {
      super.tick();
      this.tickTimer();
      if (this.getTimer() <= 0) {
         if ((Boolean)this.entityData.get(BUSTER)) {
            this.explodeNuke(new BlockPos(0, 0, 0), (Boolean)this.entityData.get(OVERCLOCKED), (Integer)SConfig.SERVER.hinden_explosion.get() / 2);
            this.explodeNuke(new BlockPos(0, -5, 0), (Boolean)this.entityData.get(OVERCLOCKED), (Integer)SConfig.SERVER.hinden_explosion.get() / 2);
            this.explodeNuke(new BlockPos(0, -10, 0), (Boolean)this.entityData.get(OVERCLOCKED), (Integer)SConfig.SERVER.hinden_explosion.get() / 2);
         } else {
            this.explodeNuke(new BlockPos(0, 0, 0), (Boolean)this.entityData.get(OVERCLOCKED), (Integer)SConfig.SERVER.hinden_explosion.get());
         }
      }

   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WOMB_AMBIENT.get();
   }

   protected int calculateFallDamage(float value1, float value2) {
      if (!this.level().isClientSide && ForgeEventFactory.getMobGriefingEvent(this.level(), this) && this.fallDistance > 4.0F) {
         AABB aabb = this.getBoundingBox().inflate((double)1.0F);

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (blockstate.getDestroySpeed(this.level(), blockpos) < 3.0F && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F) {
               this.level().destroyBlock(blockpos, false, this);
            }
         }
      }

      return super.calculateFallDamage(value1, value2);
   }

   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      return false;
   }

   public void explodeNuke(BlockPos offset, boolean fire, int value) {
      if (!this.level().isClientSide) {
         Entity entity = (Entity)(this.hinderburg != null ? this.hinderburg : this);
         ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(this.level(), this) ? ExplosionInteraction.MOB : ExplosionInteraction.NONE;
         this.level().explode(entity, this.getX() + (double)offset.getX(), this.getY() + (double)offset.getY(), this.getZ() + (double)offset.getZ(), (float)value, explosion$blockinteraction);
         if (fire) {
            Level var7 = this.level();
            if (var7 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var7;
               Utilities.convertBlocks(serverLevel, this, this.getOnPos(), (double)14.0F, Blocks.FIRE.defaultBlockState());
            }
         }

         this.level().playSound(this, this.blockPosition(), (SoundEvent)Ssounds.TUMOROID_EXPLOSION.get(), SoundSource.MASTER, 1.0F, 1.0F);
         this.discard();
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)10.0F).add(Attributes.FOLLOW_RANGE, (double)4.0F);
   }

   static {
      TIMER = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.INT);
      OVERCLOCKED = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.BOOLEAN);
      BUSTER = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.BOOLEAN);
   }
}
