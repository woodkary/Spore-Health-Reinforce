package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public class Chemist extends EvolvedInfected {
   private int attackAnimationTick;
   private static final EntityDataAccessor BLOW_TIME;

   public Chemist(EntityType p_33002_, Level p_33003_) {
      super(p_33002_, p_33003_);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.chemist_loot.get();
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.knight_damage(this) : super.getCustomDamage(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.chemist_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.chemist_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.chemist_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
         if (this.level().isClientSide) {
            Vec3 forward = this.getLookAngle().normalize().scale((double)1.0F);
            double px = this.getX() + forward.x;
            double py = this.getEyeY() - (double)0.25F;
            double pz = this.getZ() + forward.z;

            for(int i = 0; i < 8; ++i) {
               this.level().addParticle(ParticleTypes.FLAME, px + this.random.nextDouble() - this.random.nextDouble(), py, pz + this.random.nextDouble() - this.random.nextDouble(), (double)0.0F, 0.05, (double)0.0F);
            }
         }
      } else {
         super.handleEntityEvent(value);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BLOW_TIME, 0);
   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      entity.setRemainingFireTicks(200);
      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();
      if (this.getBlowTime() > 0) {
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + (double)1.0F, this.getZ(), (double)0.0F, 0.1, (double)0.0F);
         this.tickExplosion();
      }

      if (this.getBlowTime() > 60) {
         this.explodeChemist();
      }

   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

   }

   public void explodeChemist() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel serverLevel) {
         ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(this.level(), this) && (Boolean)SConfig.SERVER.chemist_explosion_on.get() ? ExplosionInteraction.MOB : ExplosionInteraction.NONE;
         serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), (float)((Double)SConfig.SERVER.chemist_explosion.get() * (double)1.0F), explosion$blockinteraction);
         Utilities.convertBlocks(serverLevel, this, this.getOnPos(), (double)7.0F, Blocks.FIRE.defaultBlockState());
         this.discard();
      }

   }

   public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
      return effectInstance.getEffect().isBeneficial() ? super.addEffect(effectInstance, entity) : false;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() != null && Math.random() < 0.2) {
         this.tickExplosion();
      }

      return super.hurt(source, amount);
   }

   public void setBlowTime(int i) {
      this.entityData.set(BLOW_TIME, i);
   }

   public int getBlowTime() {
      return (Integer)this.entityData.get(BLOW_TIME);
   }

   private void tickExplosion() {
      if (!this.level().isClientSide) {
         this.setBlowTime(this.getBlowTime() + 1);
         if (this.getBlowTime() == 1) {
            this.playSound((SoundEvent)Ssounds.CHEMIST_FUSE.get());
         }

      }
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
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

   static {
      BLOW_TIME = SynchedEntityData.defineId(Chemist.class, EntityDataSerializers.INT);
   }
}
