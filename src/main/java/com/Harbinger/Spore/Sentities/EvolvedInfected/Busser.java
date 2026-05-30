package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Carrier;
import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.BusserFlyAndDrop;
import com.Harbinger.Spore.Sentities.AI.BusserSwellGoal;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.PhayerGrabAndDropTargets;
import com.Harbinger.Spore.Sentities.AI.PullGoal;
import com.Harbinger.Spore.Sentities.AI.TransportInfected;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.Experimental.ExpAirPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedArialMovementControl;
import com.Harbinger.Spore.Sentities.Projectile.StingerProjectile;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBlockProjectile;
import com.Harbinger.Spore.Sentities.Variants.BusserVariants;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class Busser extends EvolvedInfected implements Carrier, FlyingInfected, RangedAttackMob, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor DATA_SWELL_DIR;
   private static final EntityDataAccessor DATA_CARRY_STATE;
   private int flytimeV;
   private int swell;

   public Busser(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedArialMovementControl(this, 20, true);
      this.navigation = new ExpAirPathNavigation(this, level);
   }

   public boolean causeFallDamage(float p_147105_, float p_147106_, DamageSource p_147107_) {
      return false;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_bus_loot.get();
   }

   protected void positionRider(Entity entity, MoveFunction p_19958_) {
      super.positionRider(entity, p_19958_);
      Vec3 vec3 = (new Vec3(0.4, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      entity.setPos(this.getX() + vec3.x, this.getY() - 1.2, this.getZ() + vec3.z);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(4, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         public boolean canUse() {
            return Busser.this.getTypeVariant() != 3 && super.canUse();
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)5.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(3, new PullGoal(this, (double)32.0F, (double)16.0F) {
         public boolean canUse() {
            return Busser.this.getTypeVariant() == 0 && super.canUse();
         }
      });
      this.goalSelector.addGoal(5, new BusserFlyAndDrop(this, 6) {
         public boolean canUse() {
            return Busser.this.getTypeVariant() == 0 && super.canUse();
         }
      });
      this.goalSelector.addGoal(6, new TransportInfected(this, Mob.class, 0.8, (e) -> ((List)SConfig.SERVER.can_be_carried.get()).contains(e.getEncodeId()) || ((List)SConfig.SERVER.ranged.get()).contains(e.getEncodeId())) {
         public boolean canUse() {
            return Busser.this.getTypeVariant() == 0 && super.canUse();
         }
      });
   }

   public void addVariantGoals() {
      if (this.getTypeVariant() == 3) {
         this.goalSelector.addGoal(3, new ScatterShotRangedGoal(this, 1.2, 40, 20.0F, 1, 3) {
            public boolean canUse() {
               return super.canUse() && Busser.this.getTypeVariant() == 3;
            }
         });
      }

      if (this.getTypeVariant() == 1) {
         this.goalSelector.addGoal(3, new PhayerGrabAndDropTargets(this));
      }

      if (this.getTypeVariant() == 2) {
         this.goalSelector.addGoal(3, new BusserSwellGoal(this));
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.bus_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.bus_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.bus_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.FLYING_SPEED, 0.4);
   }

   public void tick() {
      super.tick();
      if (this.isAlive() && this.getTypeVariant() == 2) {
         this.manageExplosiveBusser();
      }

      if (this.tickCount % 200 == 0 && (!this.level().canSeeSky(this.blockPosition()) || this.level().isNight())) {
         this.playSound((SoundEvent)Ssounds.PHAYRES_SCREECH.get());
      }

      if (this.tickCount % 40 == 0 && this.getVariant() == BusserVariants.TRANSPORTER) {
         if (this.getCarriedBlock() == null) {
            this.setCarriedBlock(this.selectBlock());
         } else {
            LivingEntity living = this.getTarget();
            if (living != null) {
               this.ThrowBlock(living);
            }
         }
      }

   }

   public void manageExplosiveBusser() {
      int i = this.getSwellDir();
      if (i > 0 && this.swell == 0) {
         this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
         this.gameEvent(GameEvent.PRIME_FUSE);
      }

      if (i != 0) {
         if (i > 0) {
            ++this.swell;
         } else {
            --this.swell;
         }

         if (this.swell < 0) {
            this.swell = 0;
         }
      }

      if (this.swell >= 20) {
         this.swell = 20;
         this.explodeBusser();
      }

   }

   private void explodeBusser() {
      if (!this.level().isClientSide) {
         ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(this.level(), this) ? ExplosionInteraction.MOB : ExplosionInteraction.NONE;
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)(Integer)SConfig.SERVER.kami_busser_explosion.get(), explosion$blockinteraction);
         this.discard();

         for(int i = 0; i < 3; ++i) {
            int x = this.random.nextInt(-2, 2);
            int z = this.random.nextInt(-2, 2);
            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX() + (double)x, this.getY(), this.getZ() + (double)z);
            areaeffectcloud.setRadius(2.5F);
            areaeffectcloud.setRadiusOnUse(-0.5F);
            areaeffectcloud.setWaitTime(10);
            areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 2));
            areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 600, 0));
            areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
            this.level().addFreshEntity(areaeffectcloud);
         }
      }

   }

   public BlockState selectBlock() {
      AABB aabb = this.getBoundingBox().inflate(0.2, (double)1.5F, 0.2);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.getDestroySpeed(this.level(), blockpos) < 5.0F && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && blockstate.isSolidRender(this.level(), blockpos)) {
            this.level().destroyBlock(blockpos, false);
            return blockstate;
         }
      }

      return null;
   }

   public void ThrowBlock(LivingEntity livingEntity) {
      if (!this.level().isClientSide && this.getCarriedBlock() != null) {
         ThrownBlockProjectile thrownBlockProjectile = new ThrownBlockProjectile(this.level(), this, 10.0F, this.getCarriedBlock(), this.TARGET_SELECTOR);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.0F;
         double dz = livingEntity.getZ() - this.getZ();
         thrownBlockProjectile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
         thrownBlockProjectile.shoot(dx, dy - thrownBlockProjectile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
         this.level().addFreshEntity(thrownBlockProjectile);
         this.setCarriedBlock((BlockState)null);
      }

   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && !this.onGround()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement().scale(this.isInWater() ? 0.1 : (double)0.75F));
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(vec);
      }

   }

   public boolean isVehicle() {
      return super.isVehicle() || this.getVariant() == BusserVariants.TRANSPORTER && this.getCarriedBlock() != null;
   }

   protected void customServerAiStep() {
      if (this.getTypeVariant() == 1 && this.isVehicle()) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.03, (double)0.0F));
         if (this.flytimeV < 200) {
            ++this.flytimeV;
         } else {
            this.flytimeV = 0;
            this.ejectPassengers();
         }
      }

      if (this.tickCount % 20 == 0 && this.getTarget() == null && !this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.2, (double)0.0F));
      }

      super.customServerAiStep();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
      this.entityData.define(DATA_SWELL_DIR, -1);
      this.entityData.define(DATA_CARRY_STATE, Optional.empty());
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         tag.put("carriedBlockState", NbtUtils.writeBlockState(blockstate));
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
      BlockState blockstate = null;
      if (tag.contains("carriedBlockState", 10)) {
         blockstate = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("carriedBlockState"));
         if (blockstate.isAir()) {
            blockstate = null;
         }
      }

      this.setCarriedBlock(blockstate);
   }

   public void setCarriedBlock(@Nullable BlockState state) {
      this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(state));
   }

   @Nullable
   public BlockState getCarriedBlock() {
      return (BlockState)((Optional)this.entityData.get(DATA_CARRY_STATE)).orElse((BlockState)null);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (DATA_ID_TYPE_VARIANT.equals(dataAccessor)) {
         this.addVariantGoals();
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.getVariant() == BusserVariants.ENHANCED ? super.getDimensions(pose).scale(1.2F) : super.getDimensions(pose);
   }

   public int getSwellDir() {
      return (Integer)this.entityData.get(DATA_SWELL_DIR);
   }

   public void setSwellDir(int p_32284_) {
      this.entityData.set(DATA_SWELL_DIR, p_32284_);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      BusserVariants variant = (BusserVariants)Util.getRandom(BusserVariants.values(), this.random);
      this.setVariant(variant);
      if (this.getTypeVariant() == 1) {
         AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
         AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
         if (health != null) {
            health.setBaseValue((Double)SConfig.SERVER.bus_hp.get() * (double)1.5F * (Double)SConfig.SERVER.global_health.get());
         }

         if (armor != null) {
            armor.setBaseValue((Double)SConfig.SERVER.bus_armor.get() * (double)1.5F * (Double)SConfig.SERVER.global_armor.get());
         }
      }

      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public BusserVariants getVariant() {
      return BusserVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= BusserVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return BusserVariants.values().length;
   }

   private void setVariant(BusserVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
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

   public void performRangedAttack(LivingEntity entity, float p_33318_) {
      StingerProjectile stinger = new StingerProjectile(this.level(), this, (float)((Double)SConfig.SERVER.bus_damage.get() * (double)1.0F));
      stinger.moveTo(this.getX(), this.getY(), this.getZ());
      double dx = entity.getX() - this.getX();
      double dy = entity.getY() + (double)entity.getEyeHeight() - (double)2.0F;
      double dz = entity.getZ() - this.getZ();
      stinger.shoot(dx, dy - stinger.getY() + Math.hypot(dx, dz) * (double)0.2F, dz, 2.0F, 12.0F);
      this.level().addFreshEntity(stinger);
      this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.3, (double)0.0F));
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.INT);
      DATA_SWELL_DIR = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.INT);
      DATA_CARRY_STATE = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
   }
}
