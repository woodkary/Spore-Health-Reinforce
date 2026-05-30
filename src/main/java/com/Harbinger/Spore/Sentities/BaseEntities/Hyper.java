package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.BufferAI;
import com.Harbinger.Spore.Sentities.AI.LocHiv.LocalTargettingGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.SearchAreaGoal;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class Hyper extends Infected {
   public static final EntityDataAccessor NEST;

   public Hyper(EntityType type, Level level) {
      super(type, level);
      this.navigation = new WallClimberNavigation(this, level);
   }

   public boolean canStarve() {
      return false;
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 5;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.HYPER;
   }

   protected void addRegularGoals() {
      this.goalSelector.addGoal(3, new LocalTargettingGoal(this));
      this.goalSelector.addGoal(4, new GoBackToTheNest(this));
      this.goalSelector.addGoal(4, new SearchAreaGoal(this, 1.2));
      this.goalSelector.addGoal(5, new BufferAI(this));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
   }

   public boolean removeWhenFarAway(double value) {
      Level var4 = this.level();
      if (!(var4 instanceof ServerLevel serverLevel)) {
         return false;
      } else {
         SporeSavedData data = SporeSavedData.getDataLocation(serverLevel);
         return data != null && data.getAmountOfHiveminds() >= (Integer)SConfig.SERVER.proto_spawn_world_mod.get() && value > (double)256.0F;
      }
   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      float value = blockstate.getDestroySpeed(this.level(), blockpos);
      return this.tickCount % 20 == 0 && (value > 0.0F && value <= (float)this.getBreaking() || blockstate.is(Utilities.biomass));
   }

   protected boolean canRide(Entity entity) {
      return !(entity instanceof Infected) && !(entity instanceof UtilityEntity) ? false : super.canRide(entity);
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.HYPER_DAMAGE.get();
   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (this.distanceToSqr(livingEntity) < (double)100.0F) {
            return true;
         }
      }

      return super.hasLineOfSight(entity);
   }

   public boolean hurt(DamageSource source, float amount) {
      return this.level().getDifficulty() == Difficulty.HARD && (double)amount > this.getDamageCap() && (Boolean)SConfig.SERVER.damagecap.get() ? super.hurt(source, (float)this.getDamageCap()) : super.hurt(source, amount);
   }

   public double getDamageCap() {
      return (double)(this.getMaxHealth() / 3.0F);
   }

   public int getBreaking() {
      return (Integer)SConfig.SERVER.hyper_bd.get();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("nestX", ((BlockPos)this.entityData.get(NEST)).getX());
      tag.putInt("nestY", ((BlockPos)this.entityData.get(NEST)).getY());
      tag.putInt("nestZ", ((BlockPos)this.entityData.get(NEST)).getZ());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      int x = tag.getInt("nestX");
      int y = tag.getInt("nestY");
      int z = tag.getInt("nestZ");
      this.entityData.set(NEST, new BlockPos(x, y, z));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(NEST, BlockPos.ZERO);
   }

   public BlockPos getNestLocation() {
      return (BlockPos)this.entityData.get(NEST);
   }

   public void setNestLocation(BlockPos pos) {
      this.entityData.set(NEST, pos);
   }

   public boolean additionalBreakingTriggers() {
      return this.getLastDamageSource() == this.damageSources().inWall();
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      this.setNestLocation(this.getOnPos());
      return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   static {
      NEST = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.BLOCK_POS);
   }

   static class GoBackToTheNest extends Goal {
      protected Hyper hyper;
      public int tryTicks;

      public GoBackToTheNest(Hyper hyper) {
         this.hyper = hyper;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canContinueToUse() {
         return this.hyper.getTarget() == null;
      }

      public boolean canUse() {
         return this.hyper.getEvoPoints() > 1 && this.hyper.getNestLocation() != BlockPos.ZERO;
      }

      protected void moveMobToBlock(BlockPos pos) {
         double x = (double)this.hyper.random.nextInt(-2, 2) + (double)0.5F;
         double z = (double)this.hyper.random.nextInt(-2, 2) + (double)0.5F;
         this.hyper.getNavigation().moveTo((double)pos.getX() + x, (double)(pos.getY() + 1), (double)pos.getZ() + z, (double)1.0F);
      }

      protected void tryToLayCorpsesAround() {
         AABB aabb = this.hyper.getBoundingBox().inflate((double)10.0F);

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            Level level = this.hyper.level();
            boolean isGround = level.getBlockState(blockpos).isCollisionShapeFullBlock(level, blockpos);
            boolean isAir = level.getBlockState(blockpos.above()).isAir();
            if (Math.random() < 0.01 && isGround && isAir && !level.isClientSide) {
               level.setBlock(blockpos.above(), ((Block)Sblocks.REMAINS.get()).defaultBlockState(), 3);
               this.hyper.setEvoPoints(this.hyper.getEvoPoints() - 1);
               break;
            }
         }

      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 80 == 0;
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         if (this.hyper.getNestLocation() != BlockPos.ZERO && this.shouldRecalculatePath()) {
            this.moveMobToBlock(this.hyper.getNestLocation());
         }

      }

      public void start() {
         this.moveMobToBlock(this.hyper.getNestLocation());
         BlockPos pos = this.hyper.getNestLocation();
         if (this.hyper.distanceToSqr((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()) < (double)80.0F) {
            this.tryToLayCorpsesAround();
         }

         super.start();
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }
   }
}
