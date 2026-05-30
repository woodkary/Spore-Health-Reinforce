package com.Harbinger.Spore.Sentities.Experiments;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Lacerator extends Experiment {
   public Lacerator(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.lacerator_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.lacerator_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.lacerator_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)30.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.lacerator_loot.get();
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new LaceratorMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)2.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.2F));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.LACERATOR_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 40 == 0) {
         this.playSound((SoundEvent)Ssounds.ENGINE.get());
         if (this.isAggressive()) {
            this.playSound((SoundEvent)Ssounds.SAW_SOUND.get());
         }
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (Math.random() < (double)0.1F) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
         }
      }

      return super.doHurtTarget(entity);
   }

   public static class LaceratorMeleeAttackGoal extends CustomMeleeAttackGoal {
      public LaceratorMeleeAttackGoal(PathfinderMob p_25552_, double p_25553_, boolean p_25554_) {
         super(p_25552_, p_25553_, p_25554_);
      }

      protected void resetAttackCooldown(int value) {
         super.resetAttackCooldown(5);
      }

      protected void checkAndPerformAttack(LivingEntity entity, double at) {
         super.checkAndPerformAttack(entity, at);
         entity.hurtTime = 5;
         entity.invulnerableTime = 5;
      }

      public void start() {
         super.start();
         this.mob.playSound((SoundEvent)Ssounds.SAW_SOUND.get());
      }
   }
}
