package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Inquisitor extends Hyper {
   public static final EntityDataAccessor DAMAGE_BONUS;

   public Inquisitor(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inquisitor_loot.get();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("bonus_damage", this.getBonusDamage());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setBonusDamage(tag.getInt("bonus_damage"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DAMAGE_BONUS, 0);
   }

   public void setBonusDamage(int value) {
      this.entityData.set(DAMAGE_BONUS, value);
      AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
      if (damage != null && damage.getValue() < (Double)SConfig.SERVER.inquisitor_damage.get() * (double)2.0F * (Double)SConfig.SERVER.global_damage.get()) {
         double new_damage = (Double)SConfig.SERVER.inquisitor_damage.get() * (Double)SConfig.SERVER.global_damage.get() + (double)this.getBonusDamage() * (double)0.5F;
         damage.setBaseValue(new_damage);
      }

   }

   public int getBonusDamage() {
      return (Integer)this.entityData.get(DAMAGE_BONUS);
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      super.awardKillScore(entity, i, damageSource);
      this.setBonusDamage(this.getBonusDamage() + 1);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inquisitor_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inquisitor_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inquisitor_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean hurt(DamageSource source, float amount) {
      AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
      float baseArmor = (float)((Double)SConfig.SERVER.inquisitor_armor.get() * (Double)SConfig.SERVER.global_armor.get());
      float maxPossibleArmor = baseArmor * 3.0F;
      if (armor != null && this.getHealth() < this.getMaxHealth()) {
         double new_armor = (double)((this.getMaxHealth() - this.getHealth()) / 2.0F + baseArmor);
         armor.setBaseValue(new_armor > (double)maxPossibleArmor ? (double)maxPossibleArmor : new_armor);
      }

      return super.hurt(source, amount);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INQUISITOR_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   static {
      DAMAGE_BONUS = SynchedEntityData.defineId(Inquisitor.class, EntityDataSerializers.INT);
   }
}
