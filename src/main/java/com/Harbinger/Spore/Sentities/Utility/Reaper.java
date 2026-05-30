package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import com.Harbinger.Spore.Sentities.Projectile.VomitUsurperBall;
import java.util.ArrayList;
import java.util.EnumSet;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class Reaper extends UtilityEntity implements Enemy, ArmorPersentageBypass, RangedAttackMob, ColdWeakness {
   public static final List states = new ArrayList() {
      {
         this.add(Blocks.HAY_BLOCK.defaultBlockState());
         this.add(Blocks.SUGAR_CANE.defaultBlockState());
         this.add(Blocks.PUMPKIN.defaultBlockState());
         this.add(Blocks.CARVED_PUMPKIN.defaultBlockState());
         this.add(Blocks.MELON.defaultBlockState());
         this.add(Blocks.SWEET_BERRY_BUSH.defaultBlockState());
      }
   };
   private int attackAnimationTick;
   private int rangedAttackAnimationTick;
   private @Nullable BlockPos Targetpos;
   public static final EntityDataAccessor BIOMASS;
   public static final EntityDataAccessor STOMACH;
   public static final EntityDataAccessor COMPOSTER;

   public Reaper(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new HybridPathNavigation(this, this.level());
      this.setMaxUpStep(1.0F);
   }

   protected boolean canRide(Entity entity) {
      return !(entity instanceof Infected) && !(entity instanceof UtilityEntity) ? false : super.canRide(entity);
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         Vec3 vec3 = this.moveControl.getWantedY() > this.getY() ? new Vec3((double)0.0F, 0.01, (double)0.0F) : new Vec3((double)0.0F, -0.01, (double)0.0F);
         this.setDeltaMovement(this.getDeltaMovement().scale((double)0.75F).add(vec3));
         if (this.navigation.canFloat() && this.getRandom().nextFloat() < 0.4F) {
            this.getJumpControl().jump();
         }
      } else {
         super.travel(vec);
      }

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

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return super.calculateFallDamage(p_21237_, p_21238_) - 15;
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public int getRangedAttackAnimationTick() {
      return this.rangedAttackAnimationTick;
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, (double)1.25F, true, 1.2, 5.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(4, new SearchAroundGoal(this));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
         living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 1));
      }

      this.playSound((SoundEvent)Ssounds.REAPER_ATTACK.get());
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else if (value == 5) {
         this.rangedAttackAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public float amountOfDamage(float value) {
      return (float)((Double)SConfig.SERVER.reaper_damage.get() * (Double)SConfig.SERVER.global_damage.get() / (double)4.0F);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)) {
         amount /= 2.0F;
      }

      return super.hurt(source, amount);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.reaper_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.reaper_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.reaper_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.reaper_loot.get();
   }

   private void buffAI() {
      if (this.getHealth() < this.getMaxHealth() && !this.hasEffect(MobEffects.REGENERATION)) {
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, this.getHealth() < this.getMaxHealth() / 2.0F ? 1 : 0));
         this.setBiomass(this.getBiomass() - 1);
      }

   }

   public void setBiomass(int value) {
      this.entityData.set(BIOMASS, value);
   }

   public int getBiomass() {
      return (Integer)this.entityData.get(BIOMASS);
   }

   public void setStomach(int value) {
      this.entityData.set(STOMACH, value);
   }

   public int getStomach() {
      return (Integer)this.entityData.get(STOMACH);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(STOMACH, 0);
      this.entityData.define(BIOMASS, 0);
      this.entityData.define(COMPOSTER, false);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setBiomass(tag.getInt("biomass"));
      this.setStomach(tag.getInt("stomach"));
      this.setComposter(tag.getBoolean("composter"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("biomass", this.getBiomass());
      tag.putInt("stomach", this.getStomach());
      tag.putBoolean("composter", this.getComposter());
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public void setComposter(boolean value) {
      this.entityData.set(COMPOSTER, value);
   }

   public boolean getComposter() {
      return (Boolean)this.entityData.get(COMPOSTER);
   }

   private boolean searchComposter(BlockState block) {
      return !this.getComposter() && block.getBlock().equals(Blocks.COMPOSTER);
   }

   public void awardKillScore(Entity p_19953_, int p_19954_, DamageSource p_19955_) {
      this.setBiomass(this.getBiomass() + 1);
      super.awardKillScore(p_19953_, p_19954_, p_19955_);
   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      float value = blockstate.getDestroySpeed(this.level(), blockpos);
      return this.tickCount % 20 == 0 && value > 0.0F && value <= (float)this.getBreaking();
   }

   public int getBreaking() {
      return (Integer)SConfig.SERVER.hyper_bd.get();
   }

   public boolean hasLineOfSightBlocks(BlockPos pos) {
      BlockHitResult raytraceresult = this.level().clip(new ClipContext(this.getEyePosition(1.0F), new Vec3((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), Block.COLLIDER, Fluid.NONE, this));
      BlockPos position = raytraceresult.getBlockPos();
      return pos.equals(position) || this.level().isEmptyBlock(pos) || this.level().getBlockEntity(pos) == this.level().getBlockEntity(position);
   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F, (double)4.0F, (double)32.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState block = this.level().getBlockState(blockpos);
         if ((states.contains(block) || block.getBlock() instanceof CropBlock || block.getBlock() instanceof StemBlock || block.getBlock() instanceof SaplingBlock || this.searchComposter(block)) && this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
            this.setTargetPos(blockpos);
            break;
         }
      }

   }

   public @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 200 == 0) {
         this.searchBlocks();
         if ((float)this.getStomach() > 25.0F) {
            int val = this.getComposter() ? 2 : 5;
            this.setBiomass(this.getBiomass() + 1);
            this.setStomach(this.getStomach() - val);
            if (this.getComposter()) {
               this.playSound((SoundEvent)Ssounds.REAPER_COMPOST.get());
            }
         }

         if (this.getBiomass() > 10) {
            this.FeedNearbyInfected();
         }
      }

      if (this.tickCount % 100 == 0 && this.getStomach() > 0) {
         LivingEntity living = this.getTarget();
         if (living != null && this.hasLineOfSight(living)) {
            this.performRangedAttack(living, 0.0F);
         }
      }

      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.rangedAttackAnimationTick > 0) {
         --this.rangedAttackAnimationTick;
      }

      if (this.tickCount % 20 == 0 && this.getBiomass() > 0) {
         this.buffAI();
      }

      if (this.tickCount % 40 == 0 && this.horizontalCollision && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
         this.griefBlocks(this.getTarget());
      }

   }

   private void griefBlocks(LivingEntity livingEntity) {
      AABB aabb = livingEntity != null && livingEntity.getY() > this.getY() ? this.getBoundingBox().inflate(-0.2, (double)0.5F, -0.2).move((double)0.0F, (double)0.5F, (double)0.0F) : this.getBoundingBox().inflate((double)0.5F).move((double)0.0F, (double)0.5F, (double)0.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (this.blockBreakingParameter(blockstate, blockpos)) {
            this.interactBlock(blockpos, this.level());
         }
      }

   }

   public void FeedNearbyInfected() {
      AABB aabb = this.getBoundingBox().inflate(this.getComposter() ? (double)24.0F : (double)16.0F);

      for(Infected infected : this.level().getEntitiesOfClass(Infected.class, aabb)) {
         if (infected.getEvoPoints() < (Integer)SConfig.SERVER.min_kills.get() && infected instanceof EvolvingInfected && !(infected instanceof EvolvedInfected)) {
            int charge = (Integer)SConfig.SERVER.min_kills.get() - infected.getEvoPoints();
            infected.setEvoPoints(infected.getEvoPoints() + charge);
            infected.setKills(infected.getKills() + charge);
            infected.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
            this.setBiomass(this.getBiomass() - charge);
            break;
         }
      }

   }

   public boolean interactBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      return state.is(Utilities.biomass) ? level.setBlock(blockPos, ((net.minecraft.world.level.block.Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) : level.destroyBlock(blockPos, false, this);
   }

   public boolean interractWithBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      if ((state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock) && Math.random() < 0.3) {
         return level.setBlock(blockPos, ((net.minecraft.world.level.block.Block)Sblocks.ROTTEN_CROPS.get()).defaultBlockState(), 3);
      } else if ((state.getBlock() instanceof SaplingBlock || state.getBlock() instanceof SweetBerryBushBlock) && Math.random() < 0.3) {
         return level.setBlock(blockPos, ((net.minecraft.world.level.block.Block)Sblocks.ROTTEN_BUSH.get()).defaultBlockState(), 3);
      } else {
         int compostMod = this.getComposter() ? 8 : 4;
         this.setStomach(this.getStomach() + this.random.nextInt(compostMod));
         this.attackAnimationTick = 10;
         if (state.getBlock().equals(Blocks.COMPOSTER)) {
            this.playSound(SoundEvents.WOOD_BREAK);
            this.setComposter(true);
         } else if (Math.random() < 0.2) {
            this.playSound(SoundEvents.GENERIC_EAT);
         } else {
            this.playSound((SoundEvent)Ssounds.REAPER_HARVEST.get());
         }

         this.level().broadcastEntityEvent(this, (byte)4);
         return level.destroyBlock(blockPos, false, this);
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isInvisible() ? null : (SoundEvent)Ssounds.REAPER_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.EVOLVE_HURT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      VomitUsurperBall.shoot(this, livingEntity, (float)((Double)SConfig.SERVER.reaper_ranged_damage.get() * (Double)SConfig.SERVER.global_damage.get()));
      this.setStomach(this.getStomach() - 1);
      this.rangedAttackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)5);
      this.playSound((SoundEvent)Ssounds.REAPER_SPIT.get());
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.ABNORMALS;
   }

   static {
      BIOMASS = SynchedEntityData.defineId(Reaper.class, EntityDataSerializers.INT);
      STOMACH = SynchedEntityData.defineId(Reaper.class, EntityDataSerializers.INT);
      COMPOSTER = SynchedEntityData.defineId(Reaper.class, EntityDataSerializers.BOOLEAN);
   }

   public static class SearchAroundGoal extends Goal {
      private final Reaper specter;
      public int tryTicks;

      public SearchAroundGoal(Reaper specter) {
         this.specter = specter;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         return this.specter.getTargetPos() != null && this.specter.getTarget() == null;
      }

      protected void moveToBlock(BlockPos pos) {
         if (pos != null) {
            this.specter.navigation.moveTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)1.0F, (double)pos.getZ() + (double)0.5F, (double)1.0F);
         }

      }

      public void start() {
         this.moveToBlock(this.specter.getTargetPos());
         this.tryTicks = 0;
         super.start();
      }

      public void stop() {
         super.stop();
         this.specter.navigation.stop();
      }

      public boolean canContinueToUse() {
         return this.specter.getTarget() == null && this.specter.getTargetPos() != null;
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         BlockPos pos = this.specter.getTargetPos();
         if (pos != null && this.shouldRecalculatePath()) {
            this.moveToBlock(pos);
         }

         if (pos != null && pos.closerToCenterThan(this.specter.position(), (double)3.5F)) {
            this.specter.interractWithBlock(pos, this.specter.level());
            this.specter.setTargetPos((BlockPos)null);
            this.specter.searchBlocks();
            this.specter.navigation.stop();
         }

      }
   }
}
