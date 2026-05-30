package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental.ExpAirPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedArialMovementControl;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Scavenger extends EvolvedInfected implements FlyingInfected {
   private int screams;
   private int ticksAggressive;

   public Scavenger(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedArialMovementControl(this, 20, false);
      this.navigation = new ExpAirPathNavigation(this, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.scavenger_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.scavenger_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.scavenger_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.FLYING_SPEED, 0.4);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.scavenger_loot.get();
   }

   public boolean canAttack() {
      return this.ticksAggressive > 0 || this.checkForHelp();
   }

   public boolean causeFallDamage(float p_147105_, float p_147106_, DamageSource p_147107_) {
      return false;
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && !this.onGround()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement().scale(this.isInWater() ? 0.2 : (double)1.0F));
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(vec);
      }

      this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F));
   }

   private boolean checkForHelp() {
      LivingEntity living = this.getTarget();
      if (living == null) {
         return false;
      } else if (living.getHealth() <= living.getMaxHealth() / 2.0F) {
         return true;
      } else {
         List<Entity> infected = this.level().getEntities(this, living.getBoundingBox().inflate((double)3.0F), (entity) -> entity instanceof Infected || entity instanceof UtilityEntity);
         return !infected.isEmpty();
      }
   }

   public void tick() {
      super.tick();
      LivingEntity living = this.getTarget();
      if (living != null && !this.canAttack()) {
         this.moveToTarget(living, (double)150.0F);
      }

      if (this.ticksAggressive > 0) {
         --this.ticksAggressive;
      }

   }

   public void scream(LivingEntity living) {
      if (this.screams > 3) {
         this.ticksAggressive = 200;
         this.screams = 0;
      } else {
         this.playSound((SoundEvent)Ssounds.SCAVENGER_SCREECH.get());
         ++this.screams;
         this.screamForHelp(living);
      }
   }

   public void screamForHelp(LivingEntity living) {
      AABB aabb = this.getBoundingBox().inflate((double)48.0F);

      for(Entity entity : this.level().getEntities(this, aabb, (entityx) -> entityx instanceof Infected)) {
         if (entity instanceof Infected infected1) {
            if (infected1.getTarget() == null) {
               infected1.setTarget(living);
            }
         }
      }

   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() != null) {
         this.ticksAggressive = 200;
      }

      return super.hurt(source, amount);
   }

   private void moveToTarget(LivingEntity living, double range) {
      double distance = this.distanceToSqr(living);
      if (this.tickCount % 80 == 0) {
         this.scream(living);
      }

      if (distance >= range) {
         if (this.tickCount % 20 == 0) {
            this.getNavigation().moveTo(living, (double)1.0F);
         }
      } else if (distance < range * (double)0.75F) {
         if (this.tickCount % 20 == 0) {
            Vec3 vec3 = Utilities.generatePositionAway(living.position(), (double)10.0F);
            this.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, (double)1.5F);
         }
      } else if (this.getNavigation().isDone()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply((double)0.0F, (double)1.0F, (double)0.0F).add(living.position().subtract(this.position()).normalize().yRot(90.0F)).scale(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)2.0F));
      } else {
         this.getNavigation().stop();
      }

      if (this.getY() < living.getY() + (double)4.0F) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
      }

      return super.doHurtTarget(entity);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         public boolean canUse() {
            return Scavenger.this.canAttack() && super.canUse();
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)2.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, (double)1.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_GROWL.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_DEATH.get();
   }
}
