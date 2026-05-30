package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.PullGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Hyper.Hevoker;
import com.Harbinger.Spore.Sentities.Utility.InfEvoClaw;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedEvoker extends EvolvedInfected implements EvolvingInfected {
   private static final EntityDataAccessor HAS_ARM;

   public InfectedEvoker(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_evo_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_evo_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_evo_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_evoker_loot.get();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(HAS_ARM, true);
   }

   public void tick() {
      super.tick();
      if (!(Boolean)this.getEntityData().get(HAS_ARM) && ((AttributeInstance)Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))).getBaseValue() == (Double)SConfig.SERVER.inf_evo_damage.get() * (Double)SConfig.SERVER.global_damage.get()) {
         AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);

         assert attackDamage != null;

         attackDamage.setBaseValue((Double)SConfig.SERVER.inf_evo_damage.get() / (double)2.0F * (Double)SConfig.SERVER.global_damage.get());
      }

      this.tickHyperEvolution(this);
   }

   public boolean hasArm() {
      return (Boolean)this.entityData.get(HAS_ARM);
   }

   public void setArm(boolean b) {
      this.entityData.set(HAS_ARM, b);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("arm", (Boolean)this.entityData.get(HAS_ARM));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(HAS_ARM, tag.getBoolean("arm"));
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      RandomSource randomsource = p_33282_.getRandom();
      this.populateDefaultEquipmentSlots(randomsource, p_33283_);
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_EVOKER_GROWL.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_EVOKER_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_EVOKER_DEATH.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.4, true) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (Boolean)InfectedEvoker.this.entityData.get(InfectedEvoker.HAS_ARM) ? (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth()) : (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(1, new PullGoal(this, (double)32.0F, (double)8.0F) {
         public boolean canUse() {
            return InfectedEvoker.this.switchy();
         }

         public void start() {
            super.start();
            this.mob.playSound((SoundEvent)Ssounds.EVOKER_SUCK.get());
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
   }

   private boolean switchy() {
      if (this.getTarget() == null) {
         return false;
      } else {
         double ze = this.distanceToSqr(this.getTarget());
         return ze > (double)200.0F && ze < (double)600.0F && (Boolean)this.entityData.get(HAS_ARM) && this.tickCount % 5 == 0;
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (Math.random() < 0.3) {
         this.SummonArm(this.level(), this.getX(), this.getY(), this.getZ(), this);
      }

      return super.hurt(source, amount);
   }

   private void SummonArm(LevelAccessor levelAccessor, double x, double y, double z, Entity entity) {
      if (levelAccessor instanceof ServerLevel _level) {
         if ((Boolean)this.entityData.get(HAS_ARM)) {
            Mob entityToSpawn = new InfEvoClaw((EntityType)Sentities.CLAW.get(), _level);
            entityToSpawn.moveTo(x, y, z, levelAccessor.getRandom().nextFloat() * 360.0F, 0.0F);
            entityToSpawn.finalizeSpawn(_level, levelAccessor.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
            levelAccessor.addFreshEntity(entityToSpawn);
            this.entityData.set(HAS_ARM, false);
         }
      }

   }

   public void HyperEvolve(LivingEntity living) {
      Hevoker brot = new Hevoker((EntityType)Sentities.HEVOKER.get(), this.level());

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

   public String origin() {
      return "minecraft:evoker";
   }

   static {
      HAS_ARM = SynchedEntityData.defineId(InfectedEvoker.class, EntityDataSerializers.BOOLEAN);
   }
}
