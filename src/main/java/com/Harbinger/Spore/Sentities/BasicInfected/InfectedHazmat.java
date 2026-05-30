package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Chemist;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Conductor;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Inebriator;
import com.Harbinger.Spore.Sentities.Variants.HazmatVariant;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedHazmat extends Infected implements VariantKeeper, EvolvingInfected {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor BLOW_TIME;

   public InfectedHazmat(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.SCIENTIST_AMBIENT.get();
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
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

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_hazmat_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_hazmat_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_hazmat_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
      this.entityData.define(BLOW_TIME, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      HazmatVariant variant = (HazmatVariant)Util.getRandom(HazmatVariant.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public void setBlowTime(int i) {
      this.entityData.set(BLOW_TIME, i);
   }

   public int getBlowTime() {
      return (Integer)this.entityData.get(BLOW_TIME);
   }

   private void tickExplosion() {
      this.setBlowTime(this.getBlowTime() + 1);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.getVariant() == HazmatVariant.TANK && Math.random() < (double)0.5F) {
         this.tickExplosion();
      }

      return super.hurt(source, amount);
   }

   public void tick() {
      super.tick();
      if (this.getVariant() == HazmatVariant.TANK) {
         if (this.getBlowTime() == 1) {
            this.playSound((SoundEvent)Ssounds.SCIENTIST_FUSE.get());
         }

         if (this.getBlowTime() > 0) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + (double)1.0F, this.getZ(), (double)0.0F, 0.1, (double)0.0F);
            this.tickExplosion();
         }

         if (this.getBlowTime() >= 60) {
            this.explodeTank();
         }
      }

      this.tickEvolution(this, (List)null, ScamperVariants.DEFAULT);
   }

   public void Evolve(Infected livingEntity, List value, ScamperVariants variants) {
      Level var5 = this.level();
      if (var5 instanceof ServerLevel world) {
         Infected infected = this.getMobPerVariant(world);

         for(MobEffectInstance mobeffectinstance : livingEntity.getActiveEffects()) {
            infected.addEffect(new MobEffectInstance(mobeffectinstance));
         }

         infected.setKills(livingEntity.getKills());
         infected.setEvoPoints(livingEntity.getEvoPoints());
         infected.setSearchPos(livingEntity.getSearchPos());
         infected.setLinked(livingEntity.getLinked());
         infected.setPos(livingEntity.getX(), livingEntity.getY() + (double)0.5F, livingEntity.getZ());
         infected.setCustomName(livingEntity.getCustomName());
         infected.finalizeSpawn(world, livingEntity.level().getCurrentDifficultyAt(new BlockPos((int)livingEntity.getX(), (int)livingEntity.getY(), (int)livingEntity.getZ())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
         world.addFreshEntity(infected);
         livingEntity.discard();
         double x0 = livingEntity.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
         double y0 = livingEntity.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
         double z0 = livingEntity.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
         world.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
      }

   }

   private Infected getMobPerVariant(ServerLevel world) {
      if (this.getVariant() == HazmatVariant.COAT) {
         return new Inebriator((EntityType)Sentities.INEBRIATER.get(), world);
      } else {
         return (Infected)(this.getVariant() == HazmatVariant.TANK ? new Chemist((EntityType)Sentities.CHEMIST.get(), world) : new Conductor((EntityType)Sentities.CONDUCTOR.get(), world));
      }
   }

   private void explodeTank() {
      if (!this.level().isClientSide) {
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)(Integer)SConfig.SERVER.gastank_explosion.get(), ExplosionInteraction.NONE);
         this.discard();
      }

   }

   public HazmatVariant getVariant() {
      return HazmatVariant.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= HazmatVariant.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return HazmatVariant.values().length;
   }

   private void setVariant(HazmatVariant variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public boolean addEffect(MobEffectInstance effectInstance, @org.jetbrains.annotations.Nullable Entity entity) {
      return !effectInstance.getEffect().isBeneficial() && this.getTypeVariant() != 2 ? false : super.addEffect(effectInstance, entity);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(InfectedHazmat.class, EntityDataSerializers.INT);
      BLOW_TIME = SynchedEntityData.defineId(InfectedHazmat.class, EntityDataSerializers.INT);
   }
}
