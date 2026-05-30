package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.Sentities.Carrier;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.LeapGoal;
import com.Harbinger.Spore.Sentities.AI.TransportInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Hyper.Grober;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Leaper extends EvolvedInfected implements Carrier, EvolvingInfected {
   public Leaper(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new WallClimberNavigation(this, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_leap_loot.get();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new LeapGoal(this, 0.8F) {
         public boolean canContinueToUse() {
            return Leaper.this.onGround();
         }
      });
      this.goalSelector.addGoal(3, new TransportInfected(this, Mob.class, 0.8, (entity) -> ((List)SConfig.SERVER.ranged.get()).contains(entity.getEncodeId()) || ((List)SConfig.SERVER.support.get()).contains(entity.getEncodeId()) && !(entity instanceof Carrier)));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 10;
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.leaper_damage(this) : super.getCustomDamage(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.leap_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.leap_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.leap_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.ATTACK_KNOCKBACK, (double)3.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource source) {
      return this.isVehicle() ? false : super.causeFallDamage(f1, f2, source);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_DEATH.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   protected void positionRider(Entity entity, MoveFunction p_19958_) {
      super.positionRider(entity, p_19958_);
      Vec3 vec3 = (new Vec3(-0.2, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      entity.setPos(this.getX() + vec3.x, this.getY() + 1.6, this.getZ() + vec3.z);
   }

   public void tick() {
      super.tick();
      this.tickHyperEvolution(this);
   }

   public void HyperEvolve(LivingEntity living) {
      Grober grober = new Grober((EntityType)Sentities.GROBER.get(), this.level());

      for(MobEffectInstance mobeffectinstance : this.getActiveEffects()) {
         grober.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      grober.setKills(this.getKills());
      grober.setEvoPoints(this.getEvoPoints() - (Integer)SConfig.SERVER.min_kills_hyper.get());
      grober.setCustomName(this.getCustomName());
      grober.setPos(this.getX(), this.getY(), this.getZ());
      Level var7 = this.level();
      if (var7 instanceof ServerLevel serverLevel) {
         grober.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      }

      this.level().addFreshEntity(grober);
      this.discard();
      EvolvingInfected.super.HyperEvolve(living);
   }
}
