package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Hyper.Wendigo;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class Stalker extends EvolvedInfected implements EvolvingInfected {
   public static final EntityDataAccessor CAMO;
   private int camo;

   public Stalker(EntityType type, Level level) {
      super(type, level);
      this.navigation = new WallClimberNavigation(this, level);
      this.moveControl = new InfectedWallMovementControl(this);
   }

   public void customServerAiStep() {
      this.setSprinting(this.isAggressive() && this.getTarget() != null && (this.getTarget().isSprinting() || this.getTarget().getHealth() < this.getTarget().getMaxHealth() / 2.0F));
      this.spawnSprintParticle();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_stalker_loot.get();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.3F));
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.stalker_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.stalker_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.stalker_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_PILLAGER_AMBIENT.get();
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

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("camo", (Integer)this.entityData.get(CAMO));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(CAMO, tag.getInt("camo"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CAMO, 0);
   }

   public int getCamo() {
      return (Integer)this.entityData.get(CAMO);
   }

   public void setCamo(int i) {
      this.entityData.set(CAMO, i);
   }

   public int getBiomeTint() {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY());
      int k = Mth.floor(this.getZ());
      BlockPos blockpos = new BlockPos(i, j, k);
      Biome biome = (Biome)this.level().getBiome(blockpos).value();
      return biome.getFoliageColor();
   }

   public void tick() {
      super.tick();
      this.tickHyperEvolution(this);
      if (this.camo <= 1200) {
         ++this.camo;
      } else {
         this.camo = 0;
         this.setCamo(this.getBiomeTint());
      }

   }

   public void HyperEvolve(LivingEntity living) {
      Wendigo wendigo = new Wendigo((EntityType)Sentities.WENDIGO.get(), this.level());

      for(MobEffectInstance mobeffectinstance : this.getActiveEffects()) {
         wendigo.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      wendigo.setKills(this.getKills());
      wendigo.setEvoPoints(this.getEvoPoints() - (Integer)SConfig.SERVER.min_kills_hyper.get());
      wendigo.setCustomName(this.getCustomName());
      wendigo.setPos(this.getX(), this.getY(), this.getZ());
      Level var7 = this.level();
      if (var7 instanceof ServerLevel serverLevel) {
         wendigo.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      }

      this.level().addFreshEntity(wendigo);
      this.discard();
      EvolvingInfected.super.HyperEvolve(living);
   }

   static {
      CAMO = SynchedEntityData.defineId(Stalker.class, EntityDataSerializers.INT);
   }
}
