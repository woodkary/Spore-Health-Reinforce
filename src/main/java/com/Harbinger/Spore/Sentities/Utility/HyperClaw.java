package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.AI.LeapGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HyperClaw extends UtilityEntity implements ColdWeakness {
   public HyperClaw(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new HybridPathNavigation(this, this.level());
      this.setMaxUpStep(1.0F);
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         Vec3 vec3 = this.moveControl.getWantedY() > this.getY() ? new Vec3((double)0.0F, 0.01, (double)0.0F) : new Vec3((double)0.0F, -0.01, (double)0.0F);
         this.setDeltaMovement(this.getDeltaMovement().scale((double)0.75F).add(vec3));
         if (this.navigation.canFloat() && this.getRandom().nextFloat() < 0.4F) {
            this.getJumpControl().jump();
         }
      } else {
         super.travel(vec);
      }

   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new LeapGoal(this, 0.6F));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, (double)1.0F));
      super.registerGoals();
   }

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return super.calculateFallDamage(p_21237_, p_21238_) - 15;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_claw_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.hevoker_hp.get() / (double)4.0F * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.hevoker_damage.get() / (double)4.0F * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.hevoker_armor.get() / (double)4.0F * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)24.0F).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected void positionRider(Entity entity, MoveFunction callback) {
      super.positionRider(entity, callback);
      Vec3 vec3 = (new Vec3(-0.1, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      entity.setPos(this.getX() + vec3.x, this.getY() + 0.6, this.getZ() + vec3.z);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0), this);
      }

      return super.doHurtTarget(entity);
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.EVOLVED;
   }
}
