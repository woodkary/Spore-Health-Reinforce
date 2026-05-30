package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Knight;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;

public class Verwa extends Organoid {
   public static final EntityDataAccessor STORED_MOB;
   private static final EntityDataAccessor TIMER;
   public AnimationState burst = new AnimationState();
   private int burstTimeout = 0;

   public Verwa(EntityType type, Level level) {
      super(type, level);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putString("stored_mob", (String)this.entityData.get(STORED_MOB));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(STORED_MOB, tag.getString("stored_mob"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TIMER, 0);
      this.entityData.define(STORED_MOB, "spore:knight");
   }

   public Entity getStoredEntity() {
      ResourceLocation location = new ResourceLocation((String)this.entityData.get(STORED_MOB));
      EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(location);
      return (Entity)(entityType != null ? entityType.create(this.level()) : new Knight((EntityType)Sentities.KNIGHT.get(), this.level()));
   }

   public void TickTimer() {
      this.entityData.set(TIMER, (Integer)this.entityData.get(TIMER) + 1);
      if ((Integer)this.entityData.get(TIMER) > 40 && this.level().isClientSide) {
         this.ClientAnimation();
      }

      if ((Integer)this.entityData.get(TIMER) > 80) {
         this.entityData.set(TIMER, -1);
         this.SummonStoredEntity();
         this.tickBurrowing();
      }

   }

   public List<String> getDropList() {
      List<? extends String> baseLoot = (List)SConfig.DATAGEN.verwa_loot.get();
      List<? extends String> storedLoot = new ArrayList();
      Entity var4 = this.getStoredEntity();
      if (var4 instanceof Infected infected) {
         if (!infected.getDropList().isEmpty()) {
            storedLoot = infected.getDropList();
         }
      }

      var4 = this.getStoredEntity();
      if (var4 instanceof UtilityEntity infected) {
         if (!infected.getDropList().isEmpty()) {
            storedLoot = infected.getDropList();
         }
      }

      List<String> combinedLoot = new ArrayList();
      combinedLoot.addAll(baseLoot);
      combinedLoot.addAll(storedLoot);
      return combinedLoot;
   }

   public void SummonStoredEntity() {
      Entity entity = this.getStoredEntity();
      this.awardHivemind();
      if (entity instanceof LivingEntity living) {
         for(String string : (List<String>)SConfig.SERVER.verwa_effect.get()) {
            MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
            if (effect != null) {
               living.addEffect(new MobEffectInstance(effect, 600, 1));
            }
         }

         if (living instanceof Infected infected) {
            infected.setLinked(true);
            infected.setPersistent(true);
         }

         Level var9 = this.level();
         if (var9 instanceof ServerLevel serverLevel) {
            if (living instanceof Mob mob) {
               mob.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
            }
         }
      }

      entity.moveTo(this.getX(), this.getY() + 0.2, this.getZ());
      this.level().addFreshEntity(entity);
   }

   public void ClientAnimation() {
      if (this.burstTimeout <= 0) {
         this.burstTimeout = 40;
         this.burst.start(this.tickCount);
      } else {
         --this.burstTimeout;
      }

   }

   public void tick() {
      super.tick();
      if (!this.isEmerging() && (Integer)this.entityData.get(TIMER) >= 0) {
         this.TickTimer();
      }

   }

   public boolean hurt(DamageSource source, float p_21017_) {
      if (this.isEmerging()) {
         return false;
      } else {
         this.tickBurrowing();
         return super.hurt(source, p_21017_);
      }
   }

   public int getEmerge_tick() {
      return 40;
   }

   public int getBorrow_tick() {
      return 60;
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         burrowing = -1;
         this.discard();
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.verwa_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.verwa_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)8.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WOMB_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public void setStoredMob(String storedMob) {
      this.entityData.set(STORED_MOB, storedMob);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      int i = ((List)SConfig.SERVER.verwa_summons.get()).size();
      this.entityData.set(STORED_MOB, (String)((List)SConfig.SERVER.verwa_summons.get()).get(this.random.nextInt(i)));
      return super.finalizeSpawn(serverLevelAccessor, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   static {
      STORED_MOB = SynchedEntityData.defineId(Verwa.class, EntityDataSerializers.STRING);
      TIMER = SynchedEntityData.defineId(Verwa.class, EntityDataSerializers.INT);
   }
}
