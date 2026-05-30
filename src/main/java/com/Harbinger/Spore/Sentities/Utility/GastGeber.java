package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.BufferAI;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GastGeber extends EvolvedInfected implements FoliageSpread {
   public static final EntityDataAccessor SPREAD_POINT;
   public static final EntityDataAccessor TIME_ROOTED;
   public static final EntityDataAccessor AGGRESSION;
   public static final EntityDataAccessor SPREAD_INTERVAL;
   private static final int maxCounter;
   public final int maxRootTime;

   public GastGeber(EntityType type, Level level) {
      super(type, level);
      this.maxRootTime = (Integer)SConfig.SERVER.gastgeber_root_time.get();
   }

   public boolean removeWhenFarAway(double p_21542_) {
      return false;
   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      float value = blockstate.getDestroySpeed(this.level(), blockpos);
      return this.tickCount % 20 == 0 && value > 0.0F && value <= (float)this.getBreaking();
   }

   public int getBreaking() {
      return (Integer)SConfig.SERVER.experiment_bd.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.gastgeber_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.gastgeber_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.gastgeber_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)20.0F).add(Attributes.ATTACK_KNOCKBACK, (double)3.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.gastgaber_loot.get();
   }

   public boolean canStarve() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.GAST_AMBIENT.get();
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

   protected void addRegularGoals() {
      this.goalSelector.addGoal(3, new MeleeAttackGoal(this, (double)1.5F, true) {
         public boolean canUse() {
            return super.canUse() && GastGeber.this.getAggression() > 0;
         }
      });
      this.goalSelector.addGoal(3, new FindPlaceToInfect(this));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, (double)1.0F));
      this.goalSelector.addGoal(4, new BufferAI(this));
   }

   public boolean isRooted() {
      return this.getTimeRooted() > 0;
   }

   public BlockPos getRootPoint() {
      return (BlockPos)this.entityData.get(SPREAD_POINT);
   }

   public void setRootPoint(BlockPos pos) {
      this.entityData.set(SPREAD_POINT, pos);
   }

   public int getTimeRooted() {
      return (Integer)this.entityData.get(TIME_ROOTED);
   }

   public void setTimeRooted(int value) {
      this.entityData.set(TIME_ROOTED, value);
   }

   public int getAggression() {
      return (Integer)this.entityData.get(AGGRESSION);
   }

   public void setAggression(int value) {
      this.entityData.set(AGGRESSION, value);
   }

   public int getSpreadInterval() {
      return (Integer)this.entityData.get(SPREAD_INTERVAL);
   }

   public void setSpreadInterval(int value) {
      this.entityData.set(SPREAD_INTERVAL, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SPREAD_POINT, BlockPos.ZERO);
      this.entityData.define(TIME_ROOTED, 0);
      this.entityData.define(AGGRESSION, 0);
      this.entityData.define(SPREAD_INTERVAL, 0);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      int x = tag.getInt("spreadX");
      int y = tag.getInt("spreadY");
      int z = tag.getInt("spreadZ");
      this.setRootPoint(new BlockPos(x, y, z));
      this.setTimeRooted(tag.getInt("root"));
      this.setAggression(tag.getInt("anger"));
      this.setSpreadInterval(tag.getInt("spread"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("spreadX", this.getRootPoint().getX());
      tag.putInt("spreadY", this.getRootPoint().getY());
      tag.putInt("spreadZ", this.getRootPoint().getZ());
      tag.putInt("root", this.getTimeRooted());
      tag.putInt("anger", this.getAggression());
      tag.putInt("spread", this.getSpreadInterval());
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() instanceof LivingEntity) {
         this.setAggression(30);
         this.setTimeRooted(0);
      }

      return super.hurt(source, amount);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 120, 2));
      }

      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();
      if (this.isRooted() && this.onGround()) {
         this.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
      }

      if (this.tickCount % 20 == 0 && this.isAlive()) {
         if (this.getAggression() > 0) {
            this.setAggression(this.getAggression() - 1);
         }

         if (this.isRooted()) {
            this.setTimeRooted(this.getTimeRooted() - 1);
            if (this.getSpreadInterval() < maxCounter) {
               this.setSpreadInterval(this.getSpreadInterval() + 1);
               if (this.getSpreadInterval() == maxCounter - 1) {
                  this.playSound((SoundEvent)Ssounds.PUFF.get());
               }
            } else {
               if ((Boolean)SConfig.SERVER.mound_foliage.get()) {
                  this.SpreadInfection(this.level(), (double)(Integer)SConfig.SERVER.gastgeber_range.get(), this.getOnPos());
               } else {
                  this.SpreadEffect();
               }

               this.setSpreadInterval(0);
            }

            if (this.getHealth() < this.getMaxHealth()) {
               this.setHealth(this.getHealth() + 1.0F);
            }
         }
      }

      if (this.tickCount % 200 == 0 && this.isRooted() && this.getTarget() != null) {
         this.playSound((SoundEvent)Ssounds.GAST_AMBIENT.get(), 2.0F, 2.0F);
         this.SpreadEffect();
      }

      if (this.tickCount % 1200 == 0 && !this.isRooted() && this.getAggression() <= 0) {
         this.findNewPos();
      }

   }

   public void SpreadEffect() {
      AABB aabb = this.getBoundingBox().inflate((double)16.0F);

      for(Entity entity : this.level().getEntities(this, aabb, (e) -> {
         boolean var10000;
         if (e instanceof LivingEntity living) {
            if (this.TARGET_SELECTOR.test(living)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      })) {
         if (entity instanceof LivingEntity living) {
            if (!Utilities.helmetList().contains(living.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
               living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
               living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 1));
            }
         }
      }

      Level level = this.level();
      if (level instanceof ServerLevel serverLevel) {
         double x0 = this.getX() - ((double)this.random.nextFloat() - 0.2) * 0.2;
         double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.5F) * (double)0.5F * (double)10.0F;
         double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.2) * 0.2;
         serverLevel.sendParticles((SimpleParticleType)Sparticles.BLOOD_PARTICLE.get(), x0, y0, z0, 12, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
      }

   }

   public void aiStep() {
      super.aiStep();
      Level var2 = this.level();
      if (var2 instanceof ServerLevel serverLevel) {
         if (this.getSpreadInterval() > maxCounter - 3) {
            double x0 = this.getX() - ((double)this.random.nextFloat() - 0.2) * 0.2;
            double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.5F) * (double)0.5F * (double)10.0F;
            double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.2) * 0.2;
            serverLevel.sendParticles((SimpleParticleType)Sparticles.SPORE_PARTICLE.get(), x0, y0, z0, 9, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }
      }

   }

   public void findNewPos() {
      BlockPos pos = this.getOnPos();
      int x = this.random.nextInt(-32, 32);
      int z = this.random.nextInt(-32, 32);
      int y = this.random.nextInt(-6, 6);
      BlockPos repos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
      this.setRootPoint(repos);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (TIME_ROOTED.equals(dataAccessor) && this.getTimeRooted() == 0) {
         this.findNewPos();
      }

      if (EVOLUTION_POINTS.equals(dataAccessor) && this.getEvoPoints() > 0) {
         this.setKills(this.getKills() + 1);
         this.setEvoPoints(this.getEvoPoints() - 1);
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   static {
      SPREAD_POINT = SynchedEntityData.defineId(GastGeber.class, EntityDataSerializers.BLOCK_POS);
      TIME_ROOTED = SynchedEntityData.defineId(GastGeber.class, EntityDataSerializers.INT);
      AGGRESSION = SynchedEntityData.defineId(GastGeber.class, EntityDataSerializers.INT);
      SPREAD_INTERVAL = SynchedEntityData.defineId(GastGeber.class, EntityDataSerializers.INT);
      maxCounter = (Integer)SConfig.SERVER.gastgeber_spread_cooldown.get();
   }

   private static class FindPlaceToInfect extends Goal {
      private final GastGeber geber;
      public int tryTicks;

      public FindPlaceToInfect(GastGeber geber1) {
         this.geber = geber1;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         if (!this.geber.isRooted() && this.geber.getAggression() <= 0) {
            if (this.geber.level().getBlockState(this.geber.getRootPoint().above()) != Blocks.AIR.defaultBlockState()) {
               this.geber.findNewPos();
               return false;
            } else {
               return this.geber.getRootPoint() != BlockPos.ZERO;
            }
         } else {
            return false;
         }
      }

      protected void moveMobToBlock() {
         this.geber.getNavigation().moveTo((double)this.geber.getRootPoint().getX() + (double)0.5F, (double)this.geber.getRootPoint().getY(), (double)this.geber.getRootPoint().getZ() + (double)0.5F, (double)1.0F);
      }

      public void start() {
         this.moveMobToBlock();
         this.tryTicks = 0;
         super.start();
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         if (this.geber.getRootPoint() != BlockPos.ZERO && this.shouldRecalculatePath()) {
            this.geber.getNavigation().moveTo((double)this.geber.getRootPoint().getX(), (double)this.geber.getRootPoint().getY(), (double)this.geber.getRootPoint().getZ(), (double)1.0F);
         }

         if (this.geber.getRootPoint() != BlockPos.ZERO && this.geber.getRootPoint().closerToCenterThan(this.geber.position(), (double)9.0F)) {
            this.geber.setTimeRooted(this.geber.maxRootTime);
         }

      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }
   }
}
