package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Fluids.BileLiquid;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HurtTargetGoal;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental.ExpAirPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedArialMovementControl;
import com.Harbinger.Spore.Sentities.Variants.GargoyleVariants;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Gargoyl extends EvolvedInfected implements FlyingInfected, ArmedInfected, HasUsableSlot, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor ATTACK_TICKS;

   public Gargoyl(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedArialMovementControl(this, 20, false);
      this.navigation = new ExpAirPathNavigation(this, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.gargoyle_health.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.gargoyle_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.gargoyle_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.FLYING_SPEED, 0.4);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.gargoyle_loot.get();
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      InfectedPlayer.createName(this, (List)SConfig.DATAGEN.name.get());
      InfectedPlayer.createItems(this, EquipmentSlot.HEAD, (List)SConfig.DATAGEN.player_h.get());
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficulty, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      this.populateDefaultEquipmentSlots(this.random, difficulty);
      this.setVariant((GargoyleVariants)Util.getRandom(GargoyleVariants.values(), this.random));
      return super.finalizeSpawn(serverLevelAccessor, difficulty, p_21436_, p_21437_, p_21438_);
   }

   public boolean isBomb() {
      return this.getVariant() == GargoyleVariants.BOMBER && this.getHealth() <= this.getMaxHealth() / 4.0F;
   }

   public boolean causeFallDamage(float damage_val, float protection_val, DamageSource source) {
      if (!(this.fallDistance < 3.0F) && this.isAlive() && this.getVariant() != GargoyleVariants.ICHOR) {
         boolean bomb = this.getVariant() == GargoyleVariants.BOMBER && this.getHealth() > this.getMaxHealth() / 4.0F;
         if (this.getVariant() == GargoyleVariants.VALKYRIE) {
            this.setAttackTicks(80);
            this.playSound((SoundEvent)Ssounds.LANDING.get(), 2.2F, 0.8F);
            return false;
         } else {
            float ratio = 0.1F;
            float attackMulti = 1.0F + this.fallDistance * ratio;
            double smashRange = (double)2.0F + (double)this.fallDistance * (double)0.25F;
            double blockBreaking = (double)1.0F + (double)this.fallDistance * 0.15;
            smashRange = smashRange > (double)16.0F ? (double)16.0F : smashRange;
            attackMulti = attackMulti > 3.0F ? 3.0F : attackMulti;
            smashRange = bomb ? smashRange * (double)1.5F : smashRange;
            attackMulti = bomb ? attackMulti * 1.5F : attackMulti;
            this.DamageEntities(this.level(), smashRange, attackMulti);
            this.SmashStomp(this.level(), this.blockPosition(), smashRange, blockBreaking > (double)32.0F ? (double)32.0F : blockBreaking);
            this.playSound((SoundEvent)Ssounds.LANDING.get(), 2.0F, 0.8F);
            if (bomb) {
               this.level().explode(this, (double)this.getBlockX(), (double)this.getBlockY(), (double)this.getBlockZ(), 2.0F, ExplosionInteraction.NONE);
               this.hurt(this.level().damageSources().generic(), 5.0F);
            }

            return false;
         }
      } else {
         return false;
      }
   }

   protected void SmashStomp(Level level, BlockPos pos, double range, double breaking) {
      if (level instanceof ServerLevel serverLevel) {
         for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
            for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
               for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
                  double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
                  if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < range + (double)0.5F) {
                     BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                     BlockState state = level.getBlockState(blockpos);
                     boolean airBelow = level.getBlockState(blockpos.below()).isAir();
                     double breakSpeed = (double)state.getDestroySpeed(level, pos);
                     if (airBelow && state.getDestroySpeed(level, pos) >= 0.0F && breakSpeed <= breaking && Math.random() < 0.3 && !state.isAir()) {
                        FallingBlockEntity.fall(serverLevel, blockpos, state);
                        serverLevel.removeBlock(blockpos, false);
                     }
                  }
               }
            }
         }
      }

   }

   protected void DamageEntities(Level level, double range, float multiplier) {
      AttributeInstance instance = this.getAttribute(Attributes.ATTACK_DAMAGE);
      if (instance != null && !level.isClientSide) {
         instance.setBaseValue((Double)SConfig.SERVER.gargoyle_damage.get() * (Double)SConfig.SERVER.global_damage.get() * (double)multiplier);
         AABB aabb = this.getBoundingBox().inflate(range);
         boolean bloom = this.getVariant() == GargoyleVariants.BLOOMING;

         for(Entity entity : this.level().getEntities(this, aabb)) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (Utilities.TARGET_SELECTOR.Test(living)) {
                  this.doHurtTarget(living);
                  if (bloom) {
                     living.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 1));
                  }
               }
            }
         }

      }
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && !this.onGround()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement().scale(this.isInWater() ? 0.2 : (this.getVariant() == GargoyleVariants.BLOOMING ? (double)0.5F : (double)1.0F)));
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(vec);
      }

      this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F));
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (dataAccessor.equals(DATA_ID_TYPE_VARIANT)) {
         double prot = (double)1.0F;
         AttributeInstance protection = this.getAttribute(Attributes.ARMOR);
         if (this.getVariant() == GargoyleVariants.VALKYRIE) {
            prot = (double)2.0F;
         }

         if (protection != null) {
            protection.setBaseValue((Double)SConfig.SERVER.gargoyle_armor.get() * prot);
         }

         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(this.getVariant() == GargoyleVariants.VALKYRIE ? 1.2F : 1.0F);
   }

   protected void addTargettingGoals() {
      this.goalSelector.addGoal(2, (new HurtTargetGoal(this, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity), new Class[]{Infected.class})).setAlertOthers(Infected.class));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> livingEntity instanceof Player || ((List)SConfig.SERVER.whitelist.get()).contains(livingEntity.getEncodeId())) {
         protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
         }
      });
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> (Boolean)SConfig.SERVER.at_mob.get() && this.TARGET_SELECTOR.test(livingEntity)) {
         protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
         }
      });
   }

   public void setAttackTicks(int value) {
      this.entityData.set(ATTACK_TICKS, value);
   }

   public int getAttackTicks() {
      return (Integer)this.entityData.get(ATTACK_TICKS);
   }

   public boolean canAttack() {
      return this.getAttackTicks() > 0;
   }

   public void tick() {
      super.tick();
      if (this.canAttack()) {
         this.setAttackTicks(this.getAttackTicks() - 1);
      }

      if (this.getVariant() == GargoyleVariants.ICHOR) {
         for(int i = 0; i < 5; ++i) {
            float randomX = (float)(this.position().x + (double)(this.random.nextFloat() - this.random.nextFloat()) * 1.2);
            float randomY = (float)(this.position().y + (double)(this.random.nextFloat() - this.random.nextFloat()) * 1.2);
            float randomZ = (float)(this.position().z + (double)(this.random.nextFloat() - this.random.nextFloat()) * 1.2);
            this.level().addParticle(ParticleTypes.FALLING_HONEY, (double)randomX, (double)randomY, (double)randomZ, (double)0.0F, (double)-1.0F, (double)0.0F);
         }
      }

      if (this.getVariant() == GargoyleVariants.BLOOMING) {
         for(int i = 0; i < 10; ++i) {
            float randomX = (float)(this.position().x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 6.0F));
            float randomY = (float)(this.position().y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 6.0F));
            float randomZ = (float)(this.position().z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 6.0F));
            this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, (double)randomX, (double)randomY, (double)randomZ, (double)0.0F, (double)0.0F, (double)0.0F);
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
      this.entityData.define(ATTACK_TICKS, 0);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }

         public boolean canUse() {
            return super.canUse() && (Gargoyl.this.canAttack() || Gargoyl.this.isBomb());
         }
      });
      this.goalSelector.addGoal(1, new GargoyleDiveGoal(this));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.ADVENTURER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_DEATH.get();
   }

   public boolean hasUsableSlot(EquipmentSlot slot) {
      return slot == EquipmentSlot.HEAD;
   }

   public GargoyleVariants getVariant() {
      return GargoyleVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i >= 0 && i < GargoyleVariants.values().length ? i : 0);
   }

   public int amountOfMutations() {
      return GargoyleVariants.values().length;
   }

   private void setVariant(GargoyleVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public boolean hasLineOfSight(Entity entity) {
      return entity.isInWater() ? false : super.hasLineOfSight(entity);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Gargoyl.class, EntityDataSerializers.INT);
      ATTACK_TICKS = SynchedEntityData.defineId(Gargoyl.class, EntityDataSerializers.INT);
   }

   public static class GargoyleDiveGoal extends Goal {
      private final Gargoyl gargoyle;
      private int state = 0;

      public GargoyleDiveGoal(Gargoyl mob) {
         this.gargoyle = mob;
      }

      public boolean canUse() {
         if (!this.gargoyle.canAttack() && !this.gargoyle.isBomb()) {
            LivingEntity target = this.gargoyle.getTarget();
            return target != null && target.isAlive() && this.gargoyle.distanceTo(target) < 32.0F && !target.isInWater();
         } else {
            return false;
         }
      }

      public void start() {
         this.state = 0;
      }

      public void tick() {
         LivingEntity target = this.gargoyle.getTarget();
         if (target != null) {
            switch (this.state) {
               case 0:
                  Vec3 pos = new Vec3(target.getX(), target.getY() + (double)(this.gargoyle.getVariant() == GargoyleVariants.ICHOR ? 5 : 10), target.getZ());
                  this.gargoyle.getMoveControl().setWantedPosition(pos.x, pos.y, pos.z, 1.2);
                  if (pos.y > this.gargoyle.getY()) {
                     this.gargoyle.setDeltaMovement(this.gargoyle.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
                  }

                  if (this.gargoyle.distanceToSqr(pos) < (double)4.0F) {
                     if (this.gargoyle.getVariant() == GargoyleVariants.ICHOR) {
                        this.createHitBox();
                     } else {
                        this.state = 1;
                     }
                  }
                  break;
               case 1:
                  this.gargoyle.setDeltaMovement(this.gargoyle.getDeltaMovement().add((double)0.0F, -1.6, (double)0.0F));
                  this.gargoyle.hurtMarked = true;
                  if (this.gargoyle.onGround()) {
                     this.state = 2;
                  }
                  break;
               case 2:
                  this.stop();
            }

         }
      }

      public void createHitBox() {
         AABB aabb = this.gargoyle.getBoundingBox().inflate((double)1.0F, (double)6.0F, (double)1.0F).move((double)0.0F, (double)-4.0F, (double)0.0F);
         List<Entity> entities = this.gargoyle.level().getEntities(this.gargoyle, aabb);

         for(Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
               if (Utilities.TARGET_SELECTOR.Test(living)) {
                  living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 400, 1), this.gargoyle);

                  for(MobEffectInstance instance : BileLiquid.bileEffects()) {
                     living.addEffect(instance);
                  }
               }
            }
         }

         if (entities.isEmpty()) {
            this.state = 1;
         }

      }

      public boolean canContinueToUse() {
         LivingEntity target = this.gargoyle.getTarget();
         if (!this.gargoyle.isBomb() && target != null) {
            return this.state != 2 && this.gargoyle.isAlive();
         } else {
            return false;
         }
      }

      public void stop() {
         this.state = 0;
      }
   }
}
