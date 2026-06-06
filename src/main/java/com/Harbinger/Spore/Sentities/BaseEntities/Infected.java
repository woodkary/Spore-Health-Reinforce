package com.Harbinger.Spore.Sentities.BaseEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sblocks.WallRemainsBlock;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.HurtTargetGoal;
import com.Harbinger.Spore.Sentities.AI.InfectedConsumeFromRemains;
import com.Harbinger.Spore.Sentities.AI.InfectedPanicGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.BufferAI;
import com.Harbinger.Spore.Sentities.AI.LocHiv.FollowOthersGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.LocalTargettingGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.SearchAreaGoal;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.Vomit;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class Infected extends Monster implements ColdWeakness,ICustomLifeCycleEntity {
   public static final EntityDataAccessor HUNGER;
   public static final EntityDataAccessor KILLS;
   public static final EntityDataAccessor EVOLUTION_POINTS;
   public static final EntityDataAccessor EVOLUTION;
   public static final EntityDataAccessor LINKED;
   public static final EntityDataAccessor PERSISTENT;
   public static final EntityDataAccessor ORIGIN;
   @Nullable
   private BlockPos searchPos;
   @Nullable
   private LivingEntity partner;
   public Predicate<LivingEntity> TARGET_SELECTOR = (entity) -> Utilities.TARGET_SELECTOR.Test(entity);

   public Infected(EntityType type, Level level) {
      super(type, level);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
      this.xpReward = 5;
      initCustom();
   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      actualHurt(source, amount);
   }

   public List<String> getDropList() {
      return null;
   }

   @Nullable
   public BlockPos getSearchPos() {
      return this.searchPos;
   }

   public void setSearchPos(@Nullable BlockPos searchPos) {
      this.searchPos = searchPos;
   }

   public void travel(Vec3 p_32858_) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
         if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -0.005, (double)0.0F));
         }
      } else {
         super.travel(p_32858_);
      }

   }

   public void setFollowPartner(@Nullable LivingEntity followPartner) {
      this.partner = followPartner;
   }

   public LivingEntity getFollowPartner() {
      return this.partner;
   }

   public int getMaxAirSupply() {
      return 1200;
   }

   protected int increaseAirSupply(int p_28389_) {
      return this.getMaxAirSupply();
   }

   public Packet getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean doHurtTarget(Entity entity) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (entity instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         entity.setSecondsOnFire(i * 4);
      }

      boolean flag = entity.hurt(this.getCustomDamage(this), f);
      if (flag) {
         if (f1 > 0.0F && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0), this);
            livingEntity.knockback((double)(f1 * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, (double)1.0F, 0.6));
         }

         this.doEnchantDamageEffects(this, entity);
         this.setLastHurtMob(entity);
      }

      if (entity instanceof Player player) {
         this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
      }

      return flag;
   }

   public void maybeDisableShield(Player p_21425_, ItemStack p_21426_, ItemStack p_21427_) {
      if (!p_21426_.isEmpty() && !p_21427_.isEmpty() && p_21426_.getItem() instanceof AxeItem && p_21427_.is(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            p_21425_.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent(p_21425_, (byte)30);
         }
      }

   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      if (Math.random() < (double)0.5F) {
         return SdamageTypes.infected_damage1(entity);
      } else if (Math.random() < (double)0.5F) {
         return SdamageTypes.infected_damage2(entity);
      } else {
         return Math.random() < (double)0.5F ? SdamageTypes.infected_damage3(entity) : this.damageSources().mobAttack(this);
      }
   }

   protected void addTargettingGoals() {
      this.goalSelector.addGoal(2, (new HurtTargetGoal(this, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity), new Class[]{Infected.class})).setAlertOthers(Infected.class));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> livingEntity instanceof Player || ((List)SConfig.SERVER.whitelist.get()).contains(livingEntity.getEncodeId())));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class, true, (livingEntity) -> (Boolean)SConfig.SERVER.at_mob.get() && this.TARGET_SELECTOR.test(livingEntity)));
   }

   protected void addRegularGoals() {
      this.goalSelector.addGoal(3, new LocalTargettingGoal(this));
      this.goalSelector.addGoal(4, new BufferAI(this));
      this.goalSelector.addGoal(3, new OpenDoorGoal(this, true) {
         public boolean canUse() {
            return super.canUse() && Infected.this.getLinked() && (Boolean)SConfig.SERVER.higher_thinking.get();
         }

         public void start() {
            this.mob.swing(InteractionHand.MAIN_HAND);
            super.start();
         }
      });
      this.goalSelector.addGoal(4, new SearchAreaGoal(this, 1.2));
      this.goalSelector.addGoal(5, new InfectedPanicGoal(this, (double)1.5F));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      this.goalSelector.addGoal(7, new InfectedConsumeFromRemains(this));
      this.goalSelector.addGoal(10, new FollowOthersGoal(this, Infected.class, (entity) -> true));
      this.goalSelector.addGoal(10, new FollowOthersGoal(this, Calamity.class, (entity) -> this instanceof EvolvingInfected));
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.addRegularGoals();
   }
   public void tick() {
      super.tick();
      tickCustomLifeCycle();
   }

   public boolean canStarve() {
      return (Boolean)SConfig.SERVER.should_starve.get() && (Integer)this.entityData.get(EVOLUTION_POINTS) <= 0;
   }

   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.tickCount % 20 == 0) {
         this.applyColdWeaknessEffects();
         this.handleStarvationProgress();
         if ((this.horizontalCollision || this.additionalBreakingTriggers()) && this.canGrief()) {
            this.breakNearbyBlocks();
         }

         if (this.horizontalCollision && this.isInWater()) {
            this.jumpInFluid((FluidType)ForgeMod.WATER_TYPE.get());
         }
      }

   }

   public boolean additionalBreakingTriggers() {
      return false;
   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      return (blockstate.getBlock() instanceof AbstractGlassBlock || blockstate.getBlock() instanceof LeavesBlock) && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && blockstate.getDestroySpeed(this.level(), blockpos) < 2.0F;
   }

   private void handleStarvationProgress() {
      if (this.canStarve()) {
         int currentHunger = this.getHunger();
         int hungerThreshold = (Integer)SConfig.SERVER.hunger.get();
         boolean freezingPenalty = this.isInPowderSnow || this.isFreazing();
         int hungerIncrement = freezingPenalty ? 2 : 1;
         if (currentHunger < hungerThreshold) {
            this.setHunger(currentHunger + hungerIncrement);
         } else if (!this.hasEffect((MobEffect)Seffects.STARVATION.get())) {
            this.addEffect(new MobEffectInstance((MobEffect)Seffects.STARVATION.get(), 100, 0));
         }

      }
   }

   private void applyColdWeaknessEffects() {
      if ((Boolean)SConfig.SERVER.weaktocold.get()) {
         if (this.isInPowderSnow || this.isFreazing()) {
            this.addEffect(new MobEffectInstance((MobEffect)Seffects.FROSTBITE.get(), 100, 0, false, false), this);
         }
      }
   }

   private boolean canGrief() {
      return ForgeEventFactory.getMobGriefingEvent(this.level(), this);
   }

   private void breakNearbyBlocks() {
      boolean brokeAny = false;
      AABB aabb = this.getBoundingBox().inflate(0.2).move((double)0.0F, (double)0.5F, (double)0.0F);

      for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState state = this.level().getBlockState(pos);
         if (this.blockBreakingParameter(state, pos)) {
            brokeAny |= this.interactBlock(pos, this.level());
         }
      }

      if (!brokeAny && this.onGround()) {
         this.jumpFromGround();
      }

   }

   public boolean interactBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      return state.is(Utilities.biomass) ? level.setBlock(blockPos, ((Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) : level.destroyBlock(blockPos, false, this);
   }

   public boolean isStarving() {
      return (Integer)this.entityData.get(HUNGER) >= (Integer)SConfig.SERVER.hunger.get() || this.hasEffect((MobEffect)Seffects.STARVATION.get());
   }

   public boolean removeWhenFarAway(double p_21542_) {
      return this.getEvoPoints() >= (Integer)SConfig.SERVER.min_kills.get() && this instanceof EvolvingInfected ? false : super.removeWhenFarAway(p_21542_);
   }

   public boolean isFreazing() {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY());
      int k = Mth.floor(this.getZ());
      BlockPos blockpos = new BlockPos(i, j, k);
      Biome biome = (Biome)this.level().getBiome(blockpos).value();
      return (Boolean)SConfig.SERVER.weaktocold.get() && (double)biome.getBaseTemperature() <= 0.2;
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      this.entityData.set(KILLS, (Integer)this.entityData.get(KILLS) + 1);
      this.entityData.set(EVOLUTION_POINTS, (Integer)this.entityData.get(EVOLUTION_POINTS) + 1);
      this.setHunger(0);
      super.awardKillScore(entity, i, damageSource);
   }

   public void setHunger(Integer count) {
      this.entityData.set(HUNGER, count);
   }

   public int getHunger() {
      return (Integer)this.entityData.get(HUNGER);
   }

   public void setKills(Integer count) {
      this.entityData.set(KILLS, count);
   }

   public int getKills() {
      return (Integer)this.entityData.get(KILLS);
   }

   public void setEvoPoints(Integer count) {
      this.entityData.set(EVOLUTION_POINTS, count);
   }

   public int getEvoPoints() {
      return (Integer)this.entityData.get(EVOLUTION_POINTS);
   }

   public void setLinked(Boolean count) {
      this.entityData.set(LINKED, count);
   }

   public boolean getLinked() {
      return (Boolean)this.entityData.get(LINKED);
   }

   public int getEvolutionCoolDown() {
      return (Integer)this.entityData.get(EVOLUTION);
   }

   public void setEvolution(int u) {
      this.entityData.set(EVOLUTION, u);
   }

   public void setPersistent(Boolean count) {
      this.entityData.set(PERSISTENT, count);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("hunger", (Integer)this.entityData.get(HUNGER));
      tag.putInt("kills", (Integer)this.entityData.get(KILLS));
      tag.putInt("evo_points", (Integer)this.entityData.get(EVOLUTION_POINTS));
      tag.putInt("evolution", (Integer)this.entityData.get(EVOLUTION));
      tag.putBoolean("linked", (Boolean)this.entityData.get(LINKED));
      tag.putBoolean("persistent", (Boolean)this.entityData.get(PERSISTENT));
      tag.putString("origin", (String)this.entityData.get(ORIGIN));
      addSaveData(tag);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(HUNGER, tag.getInt("hunger"));
      this.entityData.set(KILLS, tag.getInt("kills"));
      this.entityData.set(EVOLUTION, tag.getInt("evolution"));
      this.entityData.set(EVOLUTION_POINTS, tag.getInt("evo_points"));
      this.entityData.set(LINKED, tag.getBoolean("linked"));
      this.entityData.set(PERSISTENT, tag.getBoolean("persistent"));
      this.entityData.set(ORIGIN, tag.getString("origin"));
      readSaveData(tag);
   }
   public void heal(float amount) {
      healSelf(amount);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(HUNGER, 0);
      this.entityData.define(KILLS, 0);
      this.entityData.define(EVOLUTION_POINTS, 0);
      this.entityData.define(LINKED, false);
      this.entityData.define(PERSISTENT, false);
      this.entityData.define(EVOLUTION, 0);
      this.entityData.define(ORIGIN, this.origin());
   }

   public String origin() {
      return "";
   }

   public void setOrigin(String string) {
      this.entityData.set(ORIGIN, string);
   }

   public String getOrigin() {
      return (String)this.entityData.get(ORIGIN);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.hasEffect((MobEffect)Seffects.STARVATION.get()) && source == this.damageSources().generic()) {
         Level var4 = this.level();
         if (var4 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var4;
            double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
            double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)0.25F * (double)5.0F;
            double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
            serverLevel.sendParticles((SimpleParticleType)Sparticles.SPORE_PARTICLE.get(), x0, y0, z0, 4, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }
      }

      if (!(source.getDirectEntity() instanceof AcidBall) && !(source.getDirectEntity() instanceof Vomit)) {
         if (source.getDirectEntity() != null) {
            this.setSearchPos(new BlockPos((int)source.getDirectEntity().getX(), (int)source.getDirectEntity().getY(), (int)source.getDirectEntity().getZ()));
         }

         return super.hurt(source, amount);
      } else {
         return false;
      }
   }

   public static boolean checkMonsterInfectedRules(EntityType p_219014_, ServerLevelAccessor levelAccessor, MobSpawnType type, BlockPos pos, RandomSource source) {
      return levelAccessor.getDifficulty() != Difficulty.PEACEFUL ? furtherSpawnParameters(p_219014_, levelAccessor, type, pos, source) : false;
   }

   private static boolean furtherSpawnParameters(EntityType p_219014_, ServerLevelAccessor levelAccessor, MobSpawnType type, BlockPos pos, RandomSource source) {
      MinecraftServer server = levelAccessor.getServer();
      if (server != null && server.getPlayerList().getPlayers().isEmpty()) {
         return false;
      } else if (p_219014_ instanceof WaterInfected) {
         return levelAccessor.getFluidState(pos.below()).is(FluidTags.WATER);
      } else {
         return isDarkEnoughToSpawn(levelAccessor, pos, source) && checkMobSpawnRules(p_219014_, levelAccessor, type, pos, source);
      }
   }

   public boolean addEffect(MobEffectInstance effectInstance, @org.jetbrains.annotations.Nullable Entity entity) {
      if ((Integer)this.entityData.get(HUNGER) >= (Integer)SConfig.SERVER.hunger.get() && (effectInstance.getEffect() == MobEffects.HEAL || effectInstance.getEffect() == MobEffects.REGENERATION)) {
         this.setHunger(0);
      }

      return super.addEffect(effectInstance, entity);
   }

   public void die(DamageSource source) {
      this.placeRemains(source);
      this.placeFrozenRemains();
      if ((Boolean)this.entityData.get(PERSISTENT)) {
         for(int i = 0; i < this.random.nextInt(1, 4); ++i) {
            super.die(source);
         }
      } else {
         super.die(source);
      }

   }

   private void placeRemains(DamageSource source) {
      if (this.hasEffect((MobEffect)Seffects.STARVATION.get())) {
         if (source.is(DamageTypes.GENERIC)) {
            if (!this.level().isClientSide()) {
               AABB aabb = this.getBoundingBox().inflate((double)1.0F);
               RandomSource random = this.level().getRandom();

               for(BlockPos blockPos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
                  BlockState groundState = this.level().getBlockState(blockPos);
                  BlockPos abovePos = blockPos.above();
                  BlockState aboveState = this.level().getBlockState(abovePos);
                  if (groundState.isSolidRender(this.level(), blockPos) && aboveState.isAir()) {
                     if (random.nextFloat() < 0.9F) {
                        BlockState growth = random.nextBoolean() ? ((Block)Sblocks.GROWTHS_BIG.get()).defaultBlockState() : ((Block)Sblocks.GROWTHS_SMALL.get()).defaultBlockState();
                        this.level().setBlock(abovePos, growth, 3);
                     }

                     if (random.nextFloat() < 0.3F) {
                        BlockState remains;
                        if (random.nextBoolean()) {
                           Direction randomHorizontal = Plane.HORIZONTAL.getRandomDirection(random);
                           remains = (BlockState)((Block)Sblocks.WALL_REMAINS.get()).defaultBlockState().setValue(WallRemainsBlock.FACING, randomHorizontal);
                        } else {
                           remains = ((Block)Sblocks.REMAINS.get()).defaultBlockState();
                        }

                        this.level().setBlock(abovePos, remains, 3);
                        break;
                     }
                  }
               }

            }
         }
      }
   }

   private void placeFrozenRemains() {
      if ((this.isFreazing() || this.getTicksFrozen() > 0) && Math.random() < 0.3) {
         AABB aabb = this.getBoundingBox().inflate((double)1.0F);

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockState = this.level().getBlockState(blockpos);
            BlockState above = this.level().getBlockState(blockpos.above());
            if (!this.level().isClientSide() && blockState.isSolidRender(this.level(), blockpos) && above.isAir() && Math.random() < 0.3) {
               this.level().setBlock(blockpos.above(), ((Block)Sblocks.FROZEN_REMAINS.get()).defaultBlockState(), 3);
               break;
            }
         }
      }

   }

   public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @org.jetbrains.annotations.Nullable SpawnGroupData p_21437_, @org.jetbrains.annotations.Nullable CompoundTag p_21438_) {
      this.setDefaultLinkage(serverLevelAccessor);
      this.spawnWithPoints();
      if (!(this instanceof Experiment) && (Boolean)SConfig.SERVER.daytime_spawn.get() && p_21436_ == MobSpawnType.NATURAL) {
         this.teleportToSurface(this.level(), this);
      }

      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   public void setDefaultLinkage(ServerLevelAccessor level) {
      if (level instanceof ServerLevel serverLevel) {
         SporeSavedData data = SporeSavedData.getDataLocation(serverLevel);
         if (data != null && data.getAmountOfHiveminds() >= (Integer)SConfig.SERVER.proto_spawn_world_mod.get()) {
            this.setLinked(true);
            if (Math.random() < 0.3 && this instanceof EvolvingInfected) {
               EvolvingInfected evolvingInfected = (EvolvingInfected)this;
               if (evolvingInfected instanceof EvolvedInfected) {
                  this.setEvoPoints(this.getEvoPoints() + (Integer)SConfig.SERVER.min_kills_hyper.get());
               } else {
                  this.setEvoPoints(this.getEvoPoints() + (Integer)SConfig.SERVER.min_kills.get());
               }

               this.setEvolution((Integer)SConfig.SERVER.evolution_age_human.get());
            }

            this.enchantEquipment(this);
         }
      }

   }

   public void spawnWithPoints() {
      if (!(Boolean)SConfig.SERVER.at_mob.get() && Math.random() < 0.3 && this instanceof EvolvingInfected) {
         this.setEvoPoints((Integer)SConfig.SERVER.min_kills.get());
      }

   }

   public void teleportToSurface(Level level, Mob entity) {
      if (!level.canSeeSky(entity.blockPosition())) {
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(entity.getX()), level.getMaxBuildHeight(), Mth.floor(entity.getZ()));

         while(pos.getY() > level.getMinBuildHeight()) {
            pos.move(Direction.DOWN);
            BlockState state = level.getBlockState(pos);
            BlockState stateAbove = level.getBlockState(pos.above());
            if (state.isSolidRender(level, pos) && stateAbove.isAir()) {
               entity.teleportTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.01, (double)pos.getZ() + (double)0.5F);
               return;
            }
         }

      }
   }

   public void enchantEquipment(LivingEntity living) {
      if (living instanceof ArmedInfected armedInfected) {
         armedInfected.enchantItems(living);
      }

   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.hasEffect((MobEffect)Seffects.MARKER.get())) {
            return true;
         }
      }

      return super.hasLineOfSight(entity);
   }

   public void dropCustomDeathLoot(DamageSource source, int val, boolean bool) {
      super.dropCustomDeathLoot(source, val, bool);
      if (this.getDropList() != null) {
         if (!this.getDropList().isEmpty()) {
            for(String str : this.getDropList()) {
               String[] string = str.split("\\|");
               ItemStack itemStack = new ItemStack((ItemLike)Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string[0]))));
               int m = 1;
               if (Integer.parseUnsignedInt(string[2]) == Integer.parseUnsignedInt(string[3])) {
                  int o = Integer.parseUnsignedInt(string[3]);
                  m = val > 0 ? this.random.nextInt(o, o + val) : o;
               } else if (Integer.parseUnsignedInt(string[2]) >= 1 && Integer.parseUnsignedInt(string[2]) >= 1) {
                  int v1 = Integer.parseUnsignedInt(string[2]);
                  int v2 = Integer.parseUnsignedInt(string[3]);
                  float e = (float)m * 0.15F * (float)val;
                  int i = e > (float)val ? (int)e : val;
                  m = this.random.nextInt(v1, v2 + i);
               }

               int value = Integer.parseUnsignedInt(string[1]) + val * 10;
               if (Math.random() < (double)((float)value / 100.0F)) {
                  itemStack.setCount(m);
                  ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack);
                  item.setPickUpDelay(10);
                  this.level().addFreshEntity(item);
               }
            }
         }

      }
   }

   public String getMutation() {
      return null;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.INFECTED;
   }

   static {
      HUNGER = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
      KILLS = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
      EVOLUTION_POINTS = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
      EVOLUTION = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
      LINKED = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.BOOLEAN);
      PERSISTENT = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.BOOLEAN);
      ORIGIN = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.STRING);
   }

   @Override
   public LivingEntity entity() {
      return this;
   }
   @Override
   public boolean isProtoOrCalamity(){
      return false;
   }
}
