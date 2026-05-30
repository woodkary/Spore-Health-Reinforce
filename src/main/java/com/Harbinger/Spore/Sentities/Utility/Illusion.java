package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Variants.IllusionVariants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Illusion extends UtilityEntity implements ArmorPersentageBypass, Enemy {
   private static final EntityDataAccessor SEE_ABLE;
   private static final EntityDataAccessor BODY;
   private static final EntityDataAccessor TYPE;
   private static final EntityDataAccessor TARGET_ID;
   private static final EntityDataAccessor ADVANCED;

   public Illusion(EntityType type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SEE_ABLE, true);
      this.entityData.define(ADVANCED, false);
      this.entityData.define(TYPE, 0);
      this.entityData.define(TARGET_ID, 0);
      this.entityData.define(BODY, "spore:knight");
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("type", (Integer)this.entityData.get(TYPE));
      tag.putInt("target_id", (Integer)this.entityData.get(TARGET_ID));
      tag.putBoolean("see_able", (Boolean)this.entityData.get(SEE_ABLE));
      tag.putBoolean("advanced", (Boolean)this.entityData.get(ADVANCED));
      tag.putString("body", (String)this.entityData.get(BODY));
      super.addAdditionalSaveData(tag);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(SEE_ABLE, tag.getBoolean("see_able"));
      this.entityData.set(ADVANCED, tag.getBoolean("advanced"));
      this.entityData.set(TYPE, tag.getInt("type"));
      this.entityData.set(TARGET_ID, tag.getInt("target_id"));
      this.entityData.set(BODY, tag.getString("body"));
      super.readAdditionalSaveData(tag);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)10.0F).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_DAMAGE, (double)1.0F).add(Attributes.MOVEMENT_SPEED, 0.3);
   }

   public boolean canBeSeenByAnyone() {
      return (Boolean)this.entityData.get(SEE_ABLE);
   }

   public int getTargetId() {
      return (Integer)this.entityData.get(TARGET_ID);
   }

   public void setTargetId(int value) {
      this.entityData.set(TARGET_ID, value);
   }

   public void setAdvanced(boolean value) {
      this.entityData.set(ADVANCED, value);
   }

   protected void registerGoals() {
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> this.getSeeAble() && this.TARGET_SELECTOR.test(livingEntity)));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> !this.getSeeAble() && livingEntity.getId() == this.getTargetId()));
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.3, true));
   }

   public void setTypeVariant(IllusionVariants variant) {
      this.entityData.set(TYPE, variant.getVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(TYPE);
   }

   public void setBody(String i) {
      this.entityData.set(BODY, i);
   }

   public String getBody() {
      return (String)this.entityData.get(BODY);
   }

   public void setSeeAble(boolean value) {
      this.entityData.set(SEE_ABLE, value);
   }

   public boolean getSeeAble() {
      return (Boolean)this.entityData.get(SEE_ABLE);
   }

   public boolean hurt(DamageSource source, float p_21017_) {
      if (source.getEntity() != null) {
         this.discard();
      }

      return false;
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return this.getSeeAble() ? super.getCustomDamage(entity) : SdamageTypes.mental_damage(entity);
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 800 == 0 && !this.getSeeAble()) {
         this.discard();
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (this.getSeeAble()) {
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 400, 2));
         } else {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
         }

         if ((Boolean)this.entityData.get(ADVANCED)) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
         }
      }

      this.discard();
      return super.doHurtTarget(entity);
   }

   protected @Nullable SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.MADNESS.get();
   }

   public int getAmbientSoundInterval() {
      return 200;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @javax.annotation.Nullable SpawnGroupData p_146749_, @javax.annotation.Nullable CompoundTag p_146750_) {
      IllusionVariants variant = (IllusionVariants)Util.getRandom(IllusionVariants.values(), this.random);
      this.setTypeVariant(variant);
      this.setBody(variant.getEntityValue());
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public IllusionVariants getVariant() {
      return IllusionVariants.byId(this.getTypeVariant() & 255);
   }

   public float amountOfDamage(float value) {
      return this.getSeeAble() ? 0.0F : (float)((Double)SConfig.SERVER.halucinations_damage.get() * (double)1.0F);
   }

   public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
      return false;
   }

   static {
      SEE_ABLE = SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.BOOLEAN);
      BODY = SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.STRING);
      TYPE = SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.INT);
      TARGET_ID = SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.INT);
      ADVANCED = SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.BOOLEAN);
   }
}
