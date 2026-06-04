package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HurtTargetGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Variants.NaiadVariants;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class Naiad extends EvolvedInfected implements WaterInfected, VariantKeeper {
   public static final EntityDataAccessor TERRITORY;
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor TRIDENT_CHARGE;
   private static final EntityDataAccessor CHARGE_TICKS;
   public int aggroTicks;
   private Vec3 chargeTarget;

   public Naiad(EntityType p_33002_, Level p_33003_) {
      super(p_33002_, p_33003_);
      this.moveControl = new NaiadSwimControl(this);
      this.navigation = new HybridPathNavigation(this, this.level());
      this.setMaxUpStep(1.0F);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(1, new NaiadChargeGoal(this));
      this.goalSelector.addGoal(3, new BreakBoatsGoal(this, 1.2));
      this.goalSelector.addGoal(4, new CustomMeleeAttackGoal(this, (double)1.0F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)5.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new FindWaterTerritoryGoal(this));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8) {
         protected @Nullable Vec3 getPosition() {
            return Utilities.generatePositionAway(this.mob.position(), (double)16.0F);
         }
      });
   }

   public boolean isNoGravity() {
      return this.isInWater();
   }

   protected void addTargettingGoals() {
      this.goalSelector.addGoal(2, (new HurtTargetGoal(this, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity), new Class[]{Infected.class})).setAlertOthers(Infected.class));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> livingEntity instanceof Player || ((List)SConfig.SERVER.whitelist.get()).contains(livingEntity.getEncodeId())) {
         protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(targetDistance);
         }
      });
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> (Boolean)SConfig.SERVER.at_mob.get() && this.TARGET_SELECTOR.test(livingEntity)) {
         protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(targetDistance);
         }
      });
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TERRITORY, BlockPos.ZERO);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
      this.entityData.define(TRIDENT_CHARGE, 0);
      this.entityData.define(CHARGE_TICKS, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("TerritoryX", ((BlockPos)this.entityData.get(TERRITORY)).getX());
      tag.putInt("TerritoryY", ((BlockPos)this.entityData.get(TERRITORY)).getY());
      tag.putInt("TerritoryZ", ((BlockPos)this.entityData.get(TERRITORY)).getZ());
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      int tX = tag.getInt("TerritoryX");
      int tY = tag.getInt("TerritoryY");
      int tZ = tag.getInt("TerritoryZ");
      this.entityData.set(TERRITORY, new BlockPos(tX, tY, tZ));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public BlockPos getTerritory() {
      return (BlockPos)this.entityData.get(TERRITORY);
   }

   public void setTerritory(BlockPos pos) {
      this.entityData.set(TERRITORY, pos);
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public void travel(Vec3 input) {
      if (this.isCharging()) {
         this.move(MoverType.SELF, this.getDeltaMovement());
      } else {
         if (this.isEffectiveAi() && this.isInFluidType()) {
            this.moveRelative(0.1F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.getVariant() == NaiadVariants.TRITON ? (double)0.75F : 0.85));
         } else {
            super.travel(input);
         }

      }
   }

   public int getTridentCharge() {
      return (Integer)this.entityData.get(TRIDENT_CHARGE);
   }

   public void setTridentCharge(int value) {
      this.entityData.set(TRIDENT_CHARGE, value);
   }

   public void tickCharge() {
      this.entityData.set(TRIDENT_CHARGE, (Integer)this.entityData.get(TRIDENT_CHARGE) + 1);
   }

   public boolean isCharging() {
      return (Integer)this.entityData.get(CHARGE_TICKS) > 0;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.naiad_loot.get();
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.knight_damage(this) : super.getCustomDamage(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.naiad_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.naiad_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.naiad_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (DATA_ID_TYPE_VARIANT.equals(dataAccessor)) {
         this.updateAttributes();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   private void updateAttributes() {
      double val = this.getVariant() == NaiadVariants.TRITON ? 1.2 : (double)1.0F;
      double health = (Double)SConfig.SERVER.naiad_hp.get() * val * (Double)SConfig.SERVER.global_health.get();
      double armor = (Double)SConfig.SERVER.naiad_armor.get() * val * (Double)SConfig.SERVER.global_armor.get();
      AttributeInstance healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
      AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
      if (healthAttr != null) {
         healthAttr.setBaseValue(health);
      }

      SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(this, (float)health);
      if (armorAttr != null) {
         armorAttr.setBaseValue(armor);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (entity.isInFluidType()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0), this);
            living.addEffect(new MobEffectInstance((MobEffect)Seffects.MARKER.get(), 200, 1));
         }

         entity.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, (double)-1.0F, (double)0.0F));
      }

      return super.doHurtTarget(entity);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.DROWNED_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.DROWNED_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      NaiadVariants variant = (NaiadVariants)Util.getRandom(NaiadVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   public NaiadVariants getVariant() {
      return NaiadVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= NaiadVariants.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return NaiadVariants.values().length;
   }

   private void setVariant(NaiadVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (living.isInWater() && living.getHealth() < living.getMaxHealth()) {
            return true;
         }
      }

      AttributeInstance instance = this.getAttribute(Attributes.FOLLOW_RANGE);
      if (instance != null) {
         double value = instance.getValue() / (double)2.0F;
         if ((double)entity.distanceTo(this) > value) {
            return false;
         }
      }

      return super.hasLineOfSight(entity);
   }

   public void tick() {
      super.tick();
      LivingEntity target = this.getTarget();
      if (target != null) {
         this.aggroTicks = 300;
      } else if (this.aggroTicks > 0) {
         --this.aggroTicks;
      }

      if (!this.isCharging() && this.getTridentCharge() < 200 && this.getVariant() == NaiadVariants.TRITON) {
         this.tickCharge();
      }

      if (this.isInWater()) {
         if (this.isCharging() && this.chargeTarget != null) {
            this.performChargeMovement();
         }

         Vec3 vec3 = target == null ? this.getDeltaMovement() : target.position();
         if (vec3.horizontalDistanceSqr() > (double)2.5E-7F) {
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            float pitch = (float)(Mth.atan2(dy, horizontal) * (180D / Math.PI));
            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yBodyRot = lerpRotation(this.yRotO, this.getYRot());
         }
      } else if (this.isCharging()) {
         this.stopCharge();
      }

   }

   protected static float lerpRotation(float currentRotation, float targetRotation) {
      while(targetRotation - currentRotation < -180.0F) {
         currentRotation -= 360.0F;
      }

      while(targetRotation - currentRotation >= 180.0F) {
         currentRotation += 360.0F;
      }

      return Mth.lerp(0.2F, currentRotation, targetRotation);
   }

   private void performChargeMovement() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel serverLevel) {
         if (this.chargeTarget != null && this.isCharging() && this.isInWater()) {
            Vec3 currentPos = this.position();
            Vec3 direction = this.chargeTarget.subtract(currentPos);
            if (direction.lengthSqr() < 1.0E-7) {
               this.stopCharge();
            } else {
               direction = direction.normalize();
               Vec3 currentMotion = this.getDeltaMovement();
               Vec3 boost = direction.scale((double)1.5F).add(currentMotion.scale((double)0.25F));
               this.setDeltaMovement(boost);
               if (this.tickCount % 2 == 0) {
                  serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
                  serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
               }

               for(Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate((double)1.5F))) {
                  if (entity instanceof LivingEntity) {
                     LivingEntity living = (LivingEntity)entity;
                     if (Utilities.TARGET_SELECTOR.Test(living)) {
                        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 4.0F;
                        entity.hurt(this.level().damageSources().trident(this, this), damage);
                        entity.setDeltaMovement(direction.scale((double)1.5F));
                     }
                  }
               }

               int t = (Integer)this.entityData.get(CHARGE_TICKS) - 1;
               this.entityData.set(CHARGE_TICKS, t);
               if (t <= 0) {
                  this.stopCharge();
               }

            }
         } else {
            this.stopCharge();
         }
      }
   }

   public void startCharge() {
      LivingEntity target = this.getTarget();
      if (target != null && this.isInWater()) {
         this.entityData.set(CHARGE_TICKS, 40);
         this.chargeTarget = target.position();
         this.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 1.0F, 1.0F);
         Vec3 direction = target.position().subtract(this.position()).normalize();
         this.setDeltaMovement(direction.scale(1.2));
         this.setTridentCharge(0);
      }

   }

   public void stopCharge() {
      this.entityData.set(CHARGE_TICKS, 0);
      this.chargeTarget = null;
      Vec3 reduced = this.getDeltaMovement().scale(0.3);
      this.setDeltaMovement(reduced);
   }

   static {
      TERRITORY = SynchedEntityData.defineId(Naiad.class, EntityDataSerializers.BLOCK_POS);
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Naiad.class, EntityDataSerializers.INT);
      TRIDENT_CHARGE = SynchedEntityData.defineId(Naiad.class, EntityDataSerializers.INT);
      CHARGE_TICKS = SynchedEntityData.defineId(Naiad.class, EntityDataSerializers.INT);
   }

   public static class FindWaterTerritoryGoal extends Goal {
      private final Naiad naiad;
      private BlockPos targetPos;
      public int tryTicks;

      public FindWaterTerritoryGoal(Naiad naiad) {
         this.naiad = naiad;
      }

      public boolean canUse() {
         if (this.naiad.aggroTicks <= 0 && this.naiad.getTarget() == null) {
            BlockPos territory = this.naiad.getTerritory();
            return territory.equals(BlockPos.ZERO) || territory.distToCenterSqr(this.naiad.position()) > (double)400.0F;
         } else {
            return false;
         }
      }

      public void start() {
         Level level = this.naiad.level();
         BlockPos currentTerritory = this.naiad.getTerritory();
         this.tryTicks = 0;
         if (currentTerritory.equals(BlockPos.ZERO)) {
            this.targetPos = this.findNearestWaterBiome(level, this.naiad.blockPosition());
            if (this.targetPos != null) {
               this.naiad.setTerritory(this.targetPos);
            }
         } else {
            this.targetPos = currentTerritory;
         }

         if (this.targetPos != null) {
            this.moveToBlock();
         }

      }

      public void moveToBlock() {
         this.naiad.getNavigation().moveTo((double)this.targetPos.getX(), (double)this.targetPos.getY(), (double)this.targetPos.getZ(), (double)1.0F);
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         if (this.naiad.isInWater()) {
            Vec3 motion = this.naiad.getDeltaMovement();
            BlockPos pos = this.naiad.getTerritory();
            Vec3 target = new Vec3((double)pos.getX() - this.naiad.getX(), (double)pos.getY() - this.naiad.getY(), (double)pos.getZ() - this.naiad.getZ());
            if (target.lengthSqr() > 1.0E-7) {
               target = target.normalize().scale(0.1).add(motion.scale(0.45));
            }

            this.naiad.setDeltaMovement(target);
            this.naiad.getLookControl().setLookAt(target.x, target.y, target.z, 30.0F, 30.0F);
         } else if (this.naiad.getTerritory() != BlockPos.ZERO && this.targetPos != null && this.shouldRecalculatePath()) {
            this.moveToBlock();
         }

      }

      public boolean canContinueToUse() {
         return this.targetPos != null && this.naiad.aggroTicks <= 0 && this.naiad.getTarget() == null && this.targetPos.distToCenterSqr(this.naiad.position()) > (double)9.0F;
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      private BlockPos findNearestWaterBiome(Level level, BlockPos origin) {
         int range = 128;
         int step = 4;

         for(int r = 4; r <= range; r += step) {
            for(int i = 0; i < r * 2; i += step) {
               BlockPos pos1 = origin.offset(r, 0, i - r);
               BlockPos pos2 = origin.offset(-r, 0, i - r);
               BlockPos pos3 = origin.offset(i - r, 0, r);
               BlockPos pos4 = origin.offset(i - r, 0, -r);
               BlockPos[] positions = new BlockPos[]{pos1, pos2, pos3, pos4};

               for(BlockPos pos : positions) {
                  if (level.isLoaded(pos)) {
                     Holder<Biome> biome = level.getBiome(pos);
                     if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
                        BlockPos surfacePos = this.findWaterSurface(level, pos);
                        return surfacePos != null ? surfacePos : pos;
                     }
                  }
               }
            }
         }

         return null;
      }

      private BlockPos findWaterSurface(Level level, BlockPos pos) {
         for(int y = level.getSeaLevel(); y > level.getMinBuildHeight(); --y) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!level.getFluidState(checkPos).isEmpty() && level.getBlockState(checkPos.above()).isAir()) {
               return checkPos;
            }
         }

         return null;
      }
   }

   public static class BreakBoatsGoal extends Goal {
      private final Naiad naiad;
      private Boat targetBoat;
      private int breakTime;
      private final double speedModifier;

      public BreakBoatsGoal(Naiad naiad, double speedModifier) {
         this.naiad = naiad;
         this.speedModifier = speedModifier;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         List<Boat> boats = this.naiad.level().getEntitiesOfClass(Boat.class, this.naiad.getBoundingBox().inflate((double)16.0F, (double)32.0F, (double)16.0F), (boatx) -> boatx != null && !boatx.isRemoved());
         if (boats.isEmpty()) {
            return false;
         } else {
            Boat closestBoat = null;
            double closestDistance = Double.MAX_VALUE;

            for(Boat boat : boats) {
               double distance = this.naiad.distanceToSqr(boat);
               if (distance < closestDistance) {
                  closestDistance = distance;
                  closestBoat = boat;
               }
            }

            if (closestBoat != null) {
               this.targetBoat = closestBoat;
               return true;
            } else {
               return false;
            }
         }
      }

      public void start() {
         this.breakTime = 0;
         if (this.targetBoat != null && this.naiad.tickCount % 20 == 0) {
            this.naiad.getNavigation().moveTo(this.targetBoat, this.speedModifier);
         }

      }

      public boolean canContinueToUse() {
         return this.targetBoat != null && !this.targetBoat.isRemoved() && this.naiad.distanceToSqr(this.targetBoat) <= (double)256.0F;
      }

      public void tick() {
         if (this.targetBoat != null && !this.targetBoat.isRemoved()) {
            if (this.naiad.isEyeInFluidType(this.naiad.getEyeInFluidType())) {
               Vec3 vec3 = this.naiad.getDeltaMovement();
               Vec3 vec31 = new Vec3(this.targetBoat.getX() - this.naiad.getX(), this.targetBoat.getY() - this.naiad.getY(), this.targetBoat.getZ() - this.naiad.getZ());
               if (vec31.lengthSqr() > 1.0E-7) {
                  vec31 = vec31.normalize().scale((double)0.25F).add(vec3.scale(0.01));
               }

               this.naiad.setDeltaMovement(vec31.x, vec31.y, vec31.z);
            }

            this.naiad.getLookControl().setLookAt(this.targetBoat, 30.0F, 30.0F);
            double distance = this.naiad.distanceToSqr(this.targetBoat);
            if (this.naiad.tickCount % 20 == 0) {
               this.naiad.getNavigation().moveTo(this.targetBoat, this.speedModifier);
            }

            if (distance < (double)9.0F) {
               this.naiad.getLookControl().setLookAt(this.targetBoat, 30.0F, 30.0F);
               ++this.breakTime;
               if (this.breakTime >= 20) {
                  this.breakBoat();
               }
            }

         }
      }

      public void stop() {
         this.targetBoat = null;
         this.breakTime = 0;
         this.naiad.getNavigation().stop();
      }

      private void breakBoat() {
         if (this.targetBoat != null && !this.targetBoat.isRemoved()) {
            Level level = this.naiad.level();
            if (level instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)level;
               serverLevel.sendParticles(ParticleTypes.SPLASH, this.targetBoat.getX(), this.targetBoat.getY() + (double)0.5F, this.targetBoat.getZ(), 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1);
               serverLevel.playSound((Player)null, this.targetBoat.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
            }

            if (!this.targetBoat.hasCustomName() && this.naiad.getRandom().nextFloat() < 0.8F) {
               this.targetBoat.spawnAtLocation(new ItemStack(Items.STICK));
            }

            this.targetBoat.discard();
            this.stop();
         }

      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }
   }

   private static class NaiadSwimControl extends MoveControl {
      public NaiadSwimControl(Mob mob) {
         super(mob);
      }

      public void tick() {
         if (!this.mob.isInWater()) {
            super.tick();
         } else {
            if (this.operation == Operation.MOVE_TO) {
               this.operation = Operation.WAIT;
               double dx = this.wantedX - this.mob.getX();
               double dy = this.wantedY - this.mob.getY();
               double dz = this.wantedZ - this.mob.getZ();
               float targetYaw = (float)(Mth.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
               this.mob.setYRot(this.rotlerp(this.mob.getYRot(), -targetYaw, 10.0F));
               double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
               this.mob.setZza((float)speed);
               if (Math.abs(dy) > 1.0E-4) {
                  this.mob.setYya(dy > (double)0.0F ? (float)speed : (float)(-speed));
               }
            }

         }
      }
   }

   private static class NaiadChargeGoal extends Goal {
      private final Naiad naiad;
      private int cooldown;

      private NaiadChargeGoal(Naiad naiad) {
         this.naiad = naiad;
         this.cooldown = 0;
      }

      public boolean canUse() {
         if (this.cooldown > 0) {
            --this.cooldown;
            return false;
         } else {
            LivingEntity target = this.naiad.getTarget();
            return this.naiad.getVariant() == NaiadVariants.TRITON && this.naiad.getTridentCharge() >= 200 && target != null && target.isAlive() && this.naiad.isInWater() && this.naiad.distanceToSqr(target) > (double)4.0F && this.naiad.distanceToSqr(target) < (double)256.0F && this.naiad.hasLineOfSight(target);
         }
      }

      public boolean canContinueToUse() {
         return false;
      }

      public void start() {
         this.naiad.startCharge();
         this.cooldown = 100;
      }

      public boolean requiresUpdateEveryTick() {
         return false;
      }
   }
}
