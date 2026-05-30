package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class Inebriator extends EvolvedInfected {
   private int attackAnimationTick;
   private LivingEntity patient;

   public Inebriator(EntityType type, Level level) {
      super(type, level);
   }

   public void setPatient(LivingEntity patient) {
      this.patient = patient;
   }

   public LivingEntity getPatient() {
      return this.patient;
   }

   public List getEffects() {
      List<MobEffectInstance> values = new ArrayList<>();

      for(String s : (List<String>)SConfig.SERVER.ineb_buffs.get()) {
         String[] val = s.split("\\|");
         MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(val[0]));
         if (effect != null) {
            values.add(new MobEffectInstance(effect, Integer.parseUnsignedInt(val[1]), Integer.parseUnsignedInt(val[2])));
         }
      }

      return values;
   }

   public void checkForPatients() {
      Level level = this.level();
      if (!level.isClientSide()) {
         AABB aabb = this.getBoundingBox().inflate((double)4.0F, (double)1.0F, (double)4.0F);
         List<Entity> entities = level.getEntities(this, aabb, (entityx) -> {
            boolean var10000;
            if (entityx instanceof LivingEntity livingEntity) {
               if (livingEntity instanceof Infected || livingEntity instanceof UtilityEntity) {
                  var10000 = true;
                  return var10000;
               }
            }

            var10000 = false;
            return var10000;
         });
         if (entities.isEmpty()) {
            return;
         }

         Entity entity = (Entity)entities.get(this.random.nextInt(entities.size()));
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!(livingEntity instanceof Inebriator) && this.hasLineOfSight(livingEntity) && !livingEntity.isInvulnerable()) {
               this.setPatient(livingEntity);
            }
         }
      }

   }

   public void InjectMedicine(List<MobEffectInstance> effects) {
      LivingEntity entity = this.getPatient();
      if (entity != null) {
         for(MobEffectInstance instance : effects) {
            entity.addEffect(instance);
         }

         this.level().broadcastEntityEvent(this, (byte)4);
         this.playSound((SoundEvent)Ssounds.INEBRIATER_INJECT.get());
      }

   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.tickCount % 40 == 0) {
         this.checkForPatients();
         LivingEntity livingEntity = this.getPatient();
         if (this.getPatient() != null && !this.getPatient().isAlive()) {
            this.setPatient((LivingEntity)null);
         }

         if (livingEntity != null) {
            if (!livingEntity.isAlive()) {
               this.setPatient((LivingEntity)null);
            } else if (livingEntity.distanceTo(this) < 4.0F) {
               this.InjectMedicine(this.getEffects());
            }
         }
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inebriater_loot.get();
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.ineb_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.ineb_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.ineb_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new FollowPatientGoal(this, (double)1.5F, 4.0F, 2.0F));
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }

         public boolean canUse() {
            AABB aabb = this.mob.getBoundingBox().inflate((double)4.0F, (double)1.0F, (double)4.0F);
            List<Entity> allies = this.mob.level().getEntities(this.mob, aabb, (entity) -> (entity instanceof Infected || entity instanceof UtilityEntity) && !(entity instanceof Inebriator));
            return allies.isEmpty() && super.canUse();
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.SCIENTIST_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public static class FollowPatientGoal extends Goal {
      private final Inebriator mob;
      private LivingEntity target;
      private final double speedModifier;
      private final float stopDistance;
      private final float startDistance;

      public FollowPatientGoal(Inebriator mob, double speedModifier, float startDistance, float stopDistance) {
         this.mob = mob;
         this.speedModifier = speedModifier;
         this.startDistance = startDistance;
         this.stopDistance = stopDistance;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         this.target = this.mob.getPatient();
         return this.target != null && this.target.isAlive() && this.mob.distanceTo(this.target) > this.startDistance;
      }

      public boolean canContinueToUse() {
         return this.target != null && this.target.isAlive() && this.mob.distanceTo(this.target) > this.stopDistance;
      }

      public void tick() {
         if (this.target != null) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
         }

      }

      public void stop() {
         this.target = null;
         this.mob.getNavigation().stop();
      }
   }
}
