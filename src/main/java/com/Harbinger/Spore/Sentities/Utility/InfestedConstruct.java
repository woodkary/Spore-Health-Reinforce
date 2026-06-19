package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.BaseEntities.*;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBlockProjectile;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class InfestedConstruct extends UtilityEntity implements RangedAttackMob, Enemy, ColdWeakness, ICustomLifeCycleEntity, IEventTickable {
   public static final EntityDataAccessor ACTIVE;
   public static final EntityDataAccessor DISPENSER;
   public static final EntityDataAccessor MACHINE_HEALTH;
   public static final EntityDataAccessor METAL_RESERVE;
   private static final Double maXmachineHp;
   public final Map metalAndValues;
   private @Nullable BlockPos Targetpos;
   private int attackAnimationTick;

   public InfestedConstruct(EntityType type, Level level) {
      super(type, level);
      this.navigation = new WallClimberNavigation(this, this.level());
      this.setMaxUpStep(1.0F);
      this.metalAndValues = this.getValues();
      initCustom();
   }
   @Override
   public void onRemovedFromWorld() {
      onRemoved();
   }
   @Override
   public void heal(float amount) {
      healSelf(amount);
   }
   @Override
   public boolean isProtoOrCalamity(){
      return false;
   }

   public List<String> getDropList() {
      return this.isActive() ? (List)SConfig.DATAGEN.construct_loot.get() : null;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_cons_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, (double)0.25F).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_cons_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_cons_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F);
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

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      entity.hurtMarked = true;
      entity.setDeltaMovement(entity.getDeltaMovement().add((double)0.0F, 0.8, (double)0.0F));
      this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      if (entity instanceof LivingEntity livingEntity) {
         livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0));
         livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
      }

      return super.doHurtTarget(entity);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
         this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else {
         super.handleEntityEvent(value);
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   private boolean canRangeAttack() {
      LivingEntity livingEntity = this.getTarget();
      return this.isActive() && livingEntity != null && livingEntity.getY() - (double)2.0F > this.getY();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ACTIVE, true);
      this.entityData.define(DISPENSER, false);
      this.entityData.define(METAL_RESERVE, 0);
      this.entityData.define(MACHINE_HEALTH, (float)(maXmachineHp * (double)1.0F));
   }

   public void setActive(boolean value) {
      this.entityData.set(ACTIVE, value);
   }

   public boolean isActive() {
      return (Boolean)this.entityData.get(ACTIVE);
   }

   public void setDispenser(boolean value) {
      this.entityData.set(DISPENSER, value);
   }

   public boolean isDispenser() {
      return (Boolean)this.entityData.get(DISPENSER);
   }

   public void setMachineHeeauklth(float value) {
      this.entityData.set(MACHINE_HEALTH, value);
   }

   public float getMachineHeeauklth() {
      return (Float)this.entityData.get(MACHINE_HEALTH);
   }

   public void setMetalReserve(int value) {
      this.entityData.set(METAL_RESERVE, value);
   }

   public int getMetalReserve() {
      return (Integer)this.entityData.get(METAL_RESERVE);
   }

   public @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setActive(tag.getBoolean("active"));
      this.setMachineHeeauklth(tag.getFloat("machine_hp"));
      this.entityData.set(DISPENSER, tag.getBoolean("dispenser"));
      this.entityData.set(METAL_RESERVE, tag.getInt("metal"));
      readSaveData(tag);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("active", this.isActive());
      tag.putFloat("machine_hp", this.getMachineHeeauklth());
      tag.putBoolean("dispenser", (Boolean)this.entityData.get(DISPENSER));
      tag.putInt("metal", (Integer)this.entityData.get(METAL_RESERVE));
      addSaveData(tag);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, false, (double)1.5F, 2.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new SearchAroundGoal(this));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, (double)1.0F));
   }

   public boolean isNoAi() {
      return super.isNoAi() || !this.isActive();
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   private List resistentSources() {
      List<DamageSource> resistentSources = new ArrayList();
      resistentSources.add(this.level().damageSources().onFire());
      resistentSources.add(this.level().damageSources().inFire());
      resistentSources.add(this.level().damageSources().lava());
      resistentSources.add(this.level().damageSources().hotFloor());
      return resistentSources;
   }

   public boolean hurt(DamageSource source, float value) {
      if (this.invulnerableTime == 0) {
         value = this.resistentSources().contains(source) ? value / 2.0F : value;
         if (this.getMachineHeeauklth() > 0.0F) {
            float damage = this.getDamageAfterArmorAbsorb(source, value);
            this.setMachineHeeauklth(damage > this.getMachineHeeauklth() ? 0.0F : this.getMachineHeeauklth() - damage);
            this.invulnerableTime = 10;
            this.hurtTime = 10;
            this.playHurtSound(source);
            return true;
         } else {
            return super.hurt(source, value);
         }
      } else {
         return false;
      }
   }

   public boolean hasLineOfSightBlocks(BlockPos pos) {
      BlockHitResult raytraceresult = this.level().clip(new ClipContext(this.getEyePosition(1.0F), new Vec3((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), Block.COLLIDER, Fluid.NONE, this));
      BlockPos position = raytraceresult.getBlockPos();
      return pos.equals(position) || this.level().isEmptyBlock(pos) || this.level().getBlockEntity(pos) == this.level().getBlockEntity(position);
   }

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return 0;
   }

   public void awardKillScore(Entity p_19953_, int p_19954_, DamageSource p_19955_) {
      super.awardKillScore(p_19953_, p_19954_, p_19955_);
      this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
   }

   public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
      return !effectInstance.getEffect().isBeneficial() && effectInstance.getEffect() != Seffects.CORROSION.get() ? false : super.addEffect(effectInstance, entity);
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      BlockState state = this.getBlock();
      if (!this.level().isClientSide && state != null) {
         ThrownBlockProjectile thrownBlockProjectile = new ThrownBlockProjectile(this.level(), this, 10.0F, state, this.TARGET_SELECTOR);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.0F;
         double dz = livingEntity.getZ() - this.getZ();
         thrownBlockProjectile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
         thrownBlockProjectile.shoot(dx, dy - thrownBlockProjectile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 1.0F, 6.0F);
         this.level().addFreshEntity(thrownBlockProjectile);
      }

   }

   public BlockState getBlock() {
      AABB aabb = this.getBoundingBox().inflate(0.2);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.getDestroySpeed(this.level(), blockpos) < 5.0F && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && !blockstate.isAir()) {
            this.level().destroyBlock(blockpos, false);
            return blockstate;
         }
      }

      return null;
   }

   public Map getValues() {
      Map<Item, Integer> values = new HashMap();

      for(String string : (List<String>)SConfig.SERVER.cons_blocks.get()) {
         String[] strings = string.split("\\|");
         int value = Integer.parseInt(strings[1]);
         Item stack = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(strings[0]));
         if (stack != null && value > 0) {
            values.put(stack, value);
         }
      }

      return values;
   }

   public void tick() {
      super.tick();
      tickCustomLifeCycle();
      tickEventBus();
      if (this.tickCount % 40 == 0) {
         if (ForgeEventFactory.getMobGriefingEvent(this.level(), this) && this.horizontalCollision) {
            this.griefBlocks();
         }

         if ((double)this.getMachineHeeauklth() < maXmachineHp && (Integer)this.entityData.get(METAL_RESERVE) > 0) {
            this.setMachineHeeauklth(this.getMachineHeeauklth() + 1.0F);
            this.entityData.set(METAL_RESERVE, (Integer)this.entityData.get(METAL_RESERVE) - 1);
         }

         if (this.hasEffect((MobEffect)Seffects.CORROSION.get())) {
            this.setMachineHeeauklth(this.getMachineHeeauklth() - 2.0F);
         }
      }

      if (this.tickCount % 200 == 0) {
         if (!this.isActive()) {
            this.callUponInfected();
         }

         this.searchBlocks();
      }

      if (this.tickCount % 100 == 0 && this.isDispenser() && this.isActive()) {
         LivingEntity target = this.getTarget();
         if (target != null && this.hasLineOfSight(target)) {
            this.performDispenserShot(target);
         }
      }

      if (this.tickCount % 60 == 0 && this.canRangeAttack()) {
         LivingEntity target = this.getTarget();
         if (target != null && this.hasLineOfSight(target)) {
            this.performRangedAttack(target, 0.0F);
         }
      }

   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      actualHurt(source, amount);
   }

   private void griefBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)0.5F, (double)0.0F, (double)0.5F).move((double)0.0F, (double)1.0F, (double)0.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (this.blockBreakingParameter(blockstate, blockpos)) {
            this.interactBlock(blockpos, this.level());
         }
      }

   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      float value = blockstate.getDestroySpeed(this.level(), blockpos);
      return this.tickCount % 20 == 0 && value > 0.0F && value <= (float)this.getBreaking();
   }

   public int getBreaking() {
      return (Integer)SConfig.SERVER.hyper_bd.get();
   }

   public boolean interactBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      return state.is(Utilities.biomass) ? level.setBlock(blockPos, ((net.minecraft.world.level.block.Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) : level.destroyBlock(blockPos, false, this);
   }

   protected SoundEvent getAmbientSound() {
      return this.isActive() ? (SoundEvent)Ssounds.CONSTRUCT_AMBIENT.get() : null;
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return !(this.getMachineHeeauklth() > 0.0F) && this.isActive() ? (SoundEvent)Ssounds.INF_DAMAGE.get() : SoundEvents.IRON_GOLEM_HURT;
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.IRON_GOLEM_STEP;
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (dataAccessor.equals(ACTIVE)) {
         this.refreshDimensions();
         if (this.isActive()) {
            this.setMachineHeeauklth((float)(maXmachineHp * (double)1.0F));
            this.setHealth(this.getMaxHealth());
         }
      }

   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F, (double)4.0F, (double)32.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState block = this.level().getBlockState(blockpos);
         if (this.metalAndValues.containsKey(block.getBlock().asItem()) && this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
            this.setTargetPos(blockpos);
            break;
         }
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isActive() ? super.getDimensions(pose) : super.getDimensions(pose).scale(1.0F, 0.4F);
   }

   public void callUponInfected() {
      AABB aabb = this.getBoundingBox().inflate((double)8.0F);

      for(Entity entity : this.level().getEntities(this, aabb, (entityx) -> entityx instanceof Infected && !(entityx instanceof Hyper))) {
         if (entity instanceof Infected infected1) {
            infected1.setSearchPos(this.getOnPos());
            if (infected1.distanceToSqr(this) < (double)30.0F) {
               this.setActive(true);
               infected1.discard();
               Level var7 = this.level();
               if (var7 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var7;
                  double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
                  double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
                  double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
                  serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
               }
               break;
            }
         }
      }

   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.ABNORMALS;
   }

   public boolean canBeSeenAsEnemy() {
      return super.canBeSeenAsEnemy() && this.isActive();
   }

   public void performDispenserShot(LivingEntity entity) {
      Arrow abstractarrow = new Arrow(this.level(), this);
      double d0 = entity.getX() - this.getX();
      double d1 = entity.getY(0.3333333333333333) - abstractarrow.getY();
      double d2 = entity.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      abstractarrow.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600));
      if (Math.random() < (double)0.4F) {
         abstractarrow.setSecondsOnFire(8);
      }

      abstractarrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
      this.playSound(SoundEvents.DISPENSER_LAUNCH, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(abstractarrow);
   }

   public void die(DamageSource p_21014_) {
      super.die(p_21014_);
      this.dropIron();
   }

   private void dropIron() {
      ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.IRON_INGOT, this.random.nextInt(1, 5)));
      this.level().addFreshEntity(itemEntity);
   }

   static {
      ACTIVE = SynchedEntityData.defineId(InfestedConstruct.class, EntityDataSerializers.BOOLEAN);
      DISPENSER = SynchedEntityData.defineId(InfestedConstruct.class, EntityDataSerializers.BOOLEAN);
      MACHINE_HEALTH = SynchedEntityData.defineId(InfestedConstruct.class, EntityDataSerializers.FLOAT);
      METAL_RESERVE = SynchedEntityData.defineId(InfestedConstruct.class, EntityDataSerializers.INT);
      maXmachineHp = (Double)SConfig.SERVER.inf_machine_hp.get();
   }

   @Override
   public LivingEntity entity() {
      return this;
   }

   public static class SearchAroundGoal extends Goal {
      private final InfestedConstruct construct;
      public int tryTicks;

      public SearchAroundGoal(InfestedConstruct construct) {
         this.construct = construct;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         return this.construct.getTargetPos() != null && this.construct.getTarget() == null;
      }

      protected void moveToBlock(BlockPos pos) {
         if (pos != null) {
            this.construct.navigation.moveTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)1.0F, (double)pos.getZ() + (double)0.5F, (double)1.0F);
         }

      }

      public void start() {
         this.moveToBlock(this.construct.getTargetPos());
         this.tryTicks = 0;
         super.start();
      }

      public boolean canContinueToUse() {
         return this.construct.getTarget() == null;
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
         BlockPos pos = this.construct.getTargetPos();
         if (pos != null && this.shouldRecalculatePath()) {
            this.moveToBlock(pos);
         }

         if (pos != null && pos.closerToCenterThan(this.construct.position(), (double)3.5F)) {
            this.assimilateMetal(pos, this.construct.level());
            this.construct.setTargetPos((BlockPos)null);
            this.construct.searchBlocks();
         }

      }

      public void assimilateMetal(BlockPos pos, Level level) {
         Item item = level.getBlockState(pos).getBlock().asItem();

         try {
            this.construct.setMetalReserve(this.construct.getMetalReserve() + (Integer)this.construct.metalAndValues.get(item));
         } catch (Exception var5) {
         }

         level.destroyBlock(pos, false, this.construct);
         this.construct.playSound(SoundEvents.IRON_GOLEM_REPAIR);
         if (item == Items.DISPENSER) {
            this.construct.setDispenser(true);
         }

      }
   }
}
