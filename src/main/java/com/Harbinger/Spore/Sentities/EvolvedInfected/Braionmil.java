package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.BraionmilSwellGoal;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Hyper.Brot;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

public class Braionmil extends EvolvedInfected implements EvolvingInfected {
   private static final EntityDataAccessor DATA_SWELL_DIR;
   private int swell;
   private final int maxSwell = 40;

   public Braionmil(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_braio_loot.get();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SWELL_DIR, -1);
   }

   public void addAdditionalSaveData(CompoundTag data) {
      super.addAdditionalSaveData(data);
      Objects.requireNonNull(this);
      data.putShort("Fuse", (short)40);
   }

   public void tick() {
      if (this.isAlive()) {
         int i = this.getSwellDir();
         if (i > 0 && this.swell == 0) {
            this.playSound((SoundEvent)Ssounds.BRAIOMIL_ATTACK.get(), 1.0F, 0.5F);
         }

         this.swell += i;
         if (this.swell < 0) {
            this.swell = 0;
         }

         int var10000 = this.swell;
         Objects.requireNonNull(this);
         if (var10000 >= 40) {
            Objects.requireNonNull(this);
            this.swell = 40;
            this.setSwellDir(-1);
            this.chemAttack(this);
         }
      }

      super.tick();
      this.tickHyperEvolution(this);
   }

   public int getSwellDir() {
      return (Integer)this.entityData.get(DATA_SWELL_DIR);
   }

   public void setSwellDir(int p_32284_) {
      this.entityData.set(DATA_SWELL_DIR, p_32284_);
   }

   private void chemAttack(LivingEntity pLivingEntity) {
      AABB boundingBox = pLivingEntity.getBoundingBox().inflate((double)8.0F);

      for(Entity entity : pLivingEntity.level().getEntities(pLivingEntity, boundingBox)) {
         if (entity instanceof LivingEntity livingEntity) {
            if (!Utilities.helmetList().contains(livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem()) && this.TARGET_SELECTOR.test(livingEntity)) {
               for(String str : (List<String>)SConfig.SERVER.braio_effects.get()) {
                  String[] string = str.split("\\|");
                  MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string[0]));
                  if (effect != null && !livingEntity.hasEffect(effect)) {
                     livingEntity.addEffect(new MobEffectInstance(effect, Integer.parseUnsignedInt(string[1]), Integer.parseUnsignedInt(string[2])));
                  }
               }
            }
         }
      }

   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.braiomil_damage(this) : super.getCustomDamage(entity);
   }

   public void aiStep() {
      super.aiStep();
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      Level world = this.level();
      if (this.swell >= 25) {
         for(int i = 0; i < 360; ++i) {
            if (i % 20 == 0) {
               world.addParticle(ParticleTypes.SMOKE, x, y + 1.2, z, Math.cos((double)i) * 0.15, Math.sin((double)i) * Math.cos((double)i) * 0.15, Math.sin((double)i) * 0.15);
            }
         }
      }

   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)(2.0F + entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(3, new BraionmilSwellGoal(this, 1.1));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.braio_melee_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.braio_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.braio_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, (double)28.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_GROWL.get();
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

   public void HyperEvolve(LivingEntity living) {
      Brot brot = new Brot((EntityType)Sentities.BROTKATZE.get(), this.level());

      for(MobEffectInstance mobeffectinstance : this.getActiveEffects()) {
         brot.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      brot.setKills(this.getKills());
      brot.setEvoPoints(this.getEvoPoints() - (Integer)SConfig.SERVER.min_kills_hyper.get());
      brot.setCustomName(this.getCustomName());
      brot.setPos(this.getX(), this.getY(), this.getZ());
      Level var7 = this.level();
      if (var7 instanceof ServerLevel serverLevel) {
         brot.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      }

      this.level().addFreshEntity(brot);
      this.discard();
      EvolvingInfected.super.HyperEvolve(living);
   }

   static {
      DATA_SWELL_DIR = SynchedEntityData.defineId(Braionmil.class, EntityDataSerializers.INT);
   }
}
