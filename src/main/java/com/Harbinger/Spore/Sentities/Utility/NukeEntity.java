package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Ssounds;
import java.util.function.Predicate;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import com.Harbinger.Spore.Core.utils.SporeJudge;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class NukeEntity extends Entity {
   private static final EntityDataAccessor INIT_RANGE;
   private static final EntityDataAccessor RANGE;
   private static final EntityDataAccessor INIT_DURATION;
   private static final EntityDataAccessor DURATION;
   private static final EntityDataAccessor DAMAGE;
   public Predicate livingEntityPredicate = (entity) -> true;

   public NukeEntity(EntityType type, Level level) {
      super(type, level);
      this.setInitRange(1.0F);
      this.setRange(6.0F);
      this.setInitDuration(0);
      this.setDuration(600);
      this.setDamage(10.0F);
   }

   public void setInitRange(float value) {
      this.entityData.set(INIT_RANGE, value);
   }

   public void setRange(float value) {
      this.entityData.set(RANGE, value);
   }

   public void setInitDuration(int value) {
      this.entityData.set(INIT_DURATION, value);
   }

   public void setDuration(int value) {
      this.entityData.set(DURATION, value);
   }

   public void setDamage(float value) {
      this.entityData.set(DAMAGE, value);
   }

   public float getInitRange() {
      return (Float)this.entityData.get(INIT_RANGE);
   }

   public float getRange() {
      return (Float)this.entityData.get(RANGE);
   }

   public int getInitDuration() {
      return (Integer)this.entityData.get(INIT_DURATION);
   }

   public int getDuration() {
      return (Integer)this.entityData.get(DURATION);
   }

   public float getDamage() {
      return (Float)this.entityData.get(DAMAGE);
   }

   protected void defineSynchedData() {
      this.entityData.define(INIT_RANGE, 1.0F);
      this.entityData.define(RANGE, 6.0F);
      this.entityData.define(INIT_DURATION, 0);
      this.entityData.define(DURATION, 600);
      this.entityData.define(DAMAGE, 10.0F);
   }

   protected void readAdditionalSaveData(CompoundTag compoundTag) {
      this.setInitRange(compoundTag.getFloat("init_range"));
      this.setRange(compoundTag.getFloat("range"));
      this.setInitDuration(compoundTag.getInt("init_duration"));
      this.setDuration(compoundTag.getInt("duration"));
      this.setDamage(compoundTag.getFloat("damage"));
   }

   protected void addAdditionalSaveData(CompoundTag compoundTag) {
      compoundTag.putFloat("init_range", this.getInitRange());
      compoundTag.putFloat("range", this.getRange());
      compoundTag.putInt("init_duration", this.getInitDuration());
      compoundTag.putInt("duration", this.getDuration());
      compoundTag.putFloat("damage", this.getDamage());
   }

   public Packet getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   public void tick() {
      if (!this.level().isClientSide) {
         this.calculateRange();
         if (this.getInitDuration() >= this.getDuration()) {
            this.discard();
         }

         if (this.tickCount % 10 == 0) {
            this.hurtEntities();
            this.damageAround(this.level(), (double)(this.getInitRange() + 4.0F), this.getOnPos());
         }

         if (this.getInitDuration() == 1) {
            this.playNukeSound();
         }
      }

      super.tick();
   }

   private void calculateRange() {
      if (this.getDuration() > this.getInitDuration()) {
         this.setInitDuration(this.getInitDuration() + 1);
      }

      int remainingDuration = this.getDuration() - this.getInitDuration();
      if (remainingDuration > 0) {
         this.setInitRange(this.getInitRange() + this.getRange() / (float)remainingDuration);
      } else {
         this.setInitRange(this.getInitRange());
      }

   }

   public void hurtEntities() {
      AABB aabb = this.getBoundingBox().inflate((double)(this.getInitRange() + 5.0F));

      for(Entity entity : this.level().getEntities(this, aabb, (entityx) -> entityx instanceof LivingEntity)) {
         if (entity instanceof LivingEntity living) {
            if (this.livingEntityPredicate.test(living)) {
               living.setSecondsOnFire(10);
               this.addEffect(living);
               DamageSource source = this.damageSources().inFire();
               float damage = this.getDamage();
               if(!SporeJudge.isSporeEntity(living)&&living instanceof Player p&&!EntityHeealuthManager.INSTANCE.isSpectatorOrCreative(p)) {
                  SporeAttackUtil.INSTANCE.dealDamage(living, source, damage);
               }
               living.hurt(source, damage);
               living.hurtTime = 10;
               living.invulnerableTime = 10;
            }
         }
      }

   }

   public void addEffect(LivingEntity living) {
      if (ModList.get().isLoaded("alexscaves")) {
         MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("alexscaves:irradiated"));
         if (effect != null) {
            living.addEffect(new MobEffectInstance(effect, 1200, 1));
         }
      } else {
         living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1));
      }

   }

   public void playNukeSound() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F);

      for(Entity entity : this.level().getEntities(this, aabb, (entityx) -> entityx instanceof ServerPlayer)) {
         if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound((SoundEvent)Ssounds.NUKE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         }
      }

   }

   private void damageAround(Level level, double range, BlockPos pos) {
      for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
         for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
            for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
               double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
               if (distance < range + (double)0.5F) {
                  BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                  BlockState blockstate = level.getBlockState(blockpos);
                  if (blockstate.is(Blocks.WATER)) {
                     level.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
                  } else if (Math.random() < 0.1 && blockstate.isSolidRender(level, blockpos) && level.getBlockState(blockpos.above()).isAir()) {
                     BlockState state = Math.random() < (double)0.5F ? Blocks.FIRE.defaultBlockState() : ((Block)Sblocks.ACID.get()).defaultBlockState();
                     level.setBlock(blockpos.above(), state, 3);
                  }
               }
            }
         }
      }

   }

   static {
      INIT_RANGE = SynchedEntityData.defineId(NukeEntity.class, EntityDataSerializers.FLOAT);
      RANGE = SynchedEntityData.defineId(NukeEntity.class, EntityDataSerializers.FLOAT);
      INIT_DURATION = SynchedEntityData.defineId(NukeEntity.class, EntityDataSerializers.INT);
      DURATION = SynchedEntityData.defineId(NukeEntity.class, EntityDataSerializers.INT);
      DAMAGE = SynchedEntityData.defineId(NukeEntity.class, EntityDataSerializers.FLOAT);
   }
}
