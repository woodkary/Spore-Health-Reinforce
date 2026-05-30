package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidType;

public class Jagdhund extends EvolvedInfected {
   private static final EntityDataAccessor UNDERGROUND;
   public static final EntityDataAccessor BORROW;
   public static final EntityDataAccessor EMERGE;

   public Jagdhund(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.jagd_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.jagd_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.jagd_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)46.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.jagd_loot.get();
   }

   public void setUnderground(boolean value) {
      this.entityData.set(UNDERGROUND, value);
   }

   public boolean isUnderground() {
      return (Boolean)this.entityData.get(UNDERGROUND);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("underground", (Boolean)this.entityData.get(UNDERGROUND));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(UNDERGROUND, tag.getBoolean("underground"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(UNDERGROUND, false);
      this.entityData.define(BORROW, 0);
      this.entityData.define(EMERGE, 0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F) {
         public boolean canUse() {
            return super.canUse() && !Jagdhund.this.isUnderground();
         }
      });
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected void checkAndPerformAttack(LivingEntity entity, double at) {
            if (!Jagdhund.this.isUnderground() || Jagdhund.this.isEmerging()) {
               super.checkAndPerformAttack(entity, at);
            }

         }

         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public void tick() {
      super.tick();
      Entity target = this.getTarget();
      if (this.tickCount % 20 == 0 && (!this.isSoftEnough(this.getOnPos()) || this.isInFluidType()) && this.isUnderground() && !this.isEmerging()) {
         this.tickEmerging();
      }

      if (this.tickCount % 10 == 0 && target != null) {
         if (target.distanceToSqr(this) > (double)100.0F && !this.isUnderground() && !this.isBurrowing() && this.isSoftEnough(this.getOnPos())) {
            this.tickBurrowing();
         }

         if (target.distanceToSqr(this) < (double)50.0F && this.isUnderground() && !this.isEmerging()) {
            this.tickEmerging();
         }
      }

      if (this.isEmerging() || this.isBurrowing()) {
         this.SummonParticles(this.getOnPos());
      }

      if (this.navigation.isInProgress() && this.isUnderground()) {
         this.SummonParticles(this.getOnPos());
      }

      if (this.isEmerging()) {
         this.tickEmerging();
      } else if (this.isBurrowing()) {
         this.tickBurrowing();
      }

   }

   private void SummonParticles(BlockPos pos) {
      for(int l = 0; l < this.random.nextInt(3, 6); ++l) {
         Level var4 = this.level();
         if (var4 instanceof ServerLevel serverLevel) {
            int xi = this.random.nextInt(-1, 1);
            int zi = this.random.nextInt(-1, 1);
            if (this.level().getBlockState(pos).getBlock().asItem() != ItemStack.EMPTY.getItem()) {
               serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.level().getBlockState(pos).getBlock())), this.getX() + (double)xi, this.getY() - 0.1, this.getZ() + (double)zi, 3, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, ((double)this.random.nextFloat() - (double)1.0F) * 0.08, (double)0.15F);
            }
         }
      }

   }

   public boolean isInvisible() {
      return this.isUnderground() && !this.isEmerging();
   }

   public boolean canDrownInFluidType(FluidType type) {
      return super.canDrownInFluidType(type) && !this.isUnderground();
   }

   private boolean isSoftEnough(BlockPos pos) {
      return this.level().getBlockState(pos).getDestroySpeed(this.level(), pos) < 4.0F;
   }

   public boolean isEmerging() {
      return (Integer)this.entityData.get(EMERGE) > 0;
   }

   public void tickEmerging() {
      int emerging = (Integer)this.entityData.get(EMERGE);
      if (emerging > this.getEmerge_tick()) {
         this.setUnderground(false);
         emerging = -1;
      }

      this.entityData.set(EMERGE, emerging + 1);
   }

   public boolean isBurrowing() {
      return (Integer)this.entityData.get(BORROW) > 0;
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         this.setUnderground(true);
         burrowing = -1;
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
      }

      return super.doHurtTarget(entity);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!source.is(DamageTypes.IN_FIRE) && !source.is(DamageTypes.ON_FIRE) && !source.is(DamageTypes.EXPLOSION)) {
         return !this.isUnderground() && !this.isEmerging() && !this.isBurrowing() ? super.hurt(source, amount) : false;
      } else {
         return super.hurt(source, amount);
      }
   }

   public double getMyRidingOffset() {
      return (double)-1.0F;
   }

   public void onSyncedDataUpdated(EntityDataAccessor accessor) {
      super.onSyncedDataUpdated(accessor);
      if (UNDERGROUND.equals(accessor)) {
         this.refreshDimensions();
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isUnderground() ? new EntityDimensions(1.2F, 0.1F, false) : super.getDimensions(pose);
   }

   public int getEmerge() {
      return (Integer)this.entityData.get(EMERGE);
   }

   public int getBorrow() {
      return (Integer)this.entityData.get(BORROW);
   }

   public int getBorrow_tick() {
      return 60;
   }

   public int getEmerge_tick() {
      return 60;
   }

   protected SoundEvent getAmbientSound() {
      return this.isUnderground() ? null : (SoundEvent)Ssounds.HUSK_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (this.isUnderground()) {
         this.playSound(state.getSoundType(this.level(), pos, this).getBreakSound(), 0.15F, 0.5F);
      } else {
         this.playSound(this.getStepSound(), 0.15F, 1.0F);
      }

   }

   static {
      UNDERGROUND = SynchedEntityData.defineId(Jagdhund.class, EntityDataSerializers.BOOLEAN);
      BORROW = SynchedEntityData.defineId(Jagdhund.class, EntityDataSerializers.INT);
      EMERGE = SynchedEntityData.defineId(Jagdhund.class, EntityDataSerializers.INT);
   }
}
