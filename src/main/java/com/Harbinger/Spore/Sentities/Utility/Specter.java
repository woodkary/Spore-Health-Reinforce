package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
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
import net.minecraft.world.Container;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class Specter extends UtilityEntity implements Enemy, ArmorPersentageBypass, ColdWeakness {
   public static final EntityDataAccessor INVISIBLE;
   public static final EntityDataAccessor BIOMASS;
   public static final EntityDataAccessor STOMACH;
   public static final List states;
   private @Nullable BlockPos Targetpos;

   public Specter(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new HybridPathNavigation(this, this.level());
      this.setMaxUpStep(1.0F);
   }

   protected boolean canRide(Entity entity) {
      return !(entity instanceof Infected) && !(entity instanceof UtilityEntity) ? false : super.canRide(entity);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.specter_loot.get();
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

   public boolean dampensVibrations() {
      return this.isInvisible();
   }

   public boolean canBeSeenByAnyone() {
      return !this.isInvisible();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.specter_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.specter_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.specter_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)3.0F);
   }

   private void buffAI() {
      if (this.getHealth() < this.getMaxHealth() && !this.hasEffect(MobEffects.REGENERATION)) {
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, this.getHealth() < this.getMaxHealth() / 2.0F ? 1 : 0));
         this.setBiomass(this.getBiomass() - 1);
      }

   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new SearchAroundGoal(this));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, (double)1.0F));
      super.registerGoals();
   }

   public void setInvisible(boolean value) {
      this.entityData.set(INVISIBLE, value);
   }

   public boolean isInvisible() {
      return (Boolean)this.entityData.get(INVISIBLE);
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

   public @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(INVISIBLE, false);
      this.entityData.define(STOMACH, 0);
      this.entityData.define(BIOMASS, 0);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setInvisible(tag.getBoolean("invisible"));
      this.setBiomass(tag.getInt("biomass"));
      this.setStomach(tag.getInt("stomach"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("invisible", this.isInvisible());
      tag.putInt("biomass", this.getBiomass());
      tag.putInt("stomach", this.getStomach());
   }

   private boolean food(Container container) {
      return container.hasAnyMatching(ItemStack::isEdible);
   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F, (double)4.0F, (double)32.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState block = this.level().getBlockState(blockpos);
         BlockEntity blockEntity = this.level().getBlockEntity(blockpos);
         if (!states.contains(block)) {
            if (!(blockEntity instanceof Container)) {
               continue;
            }

            Container container = (Container)blockEntity;
            if (!this.food(container)) {
               continue;
            }
         }

         if (this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
            this.setTargetPos(blockpos);
            break;
         }
      }

   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 1));
         livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
      }

      return super.doHurtTarget(entity);
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
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

   public void tick() {
      super.tick();
      if (this.tickCount % 200 == 0) {
         this.searchBlocks();
         if ((float)this.getStomach() > 10.0F) {
            this.setBiomass(this.getBiomass() + 1);
            this.setStomach(this.getStomach() - 10);
         }
      }

      if (this.tickCount % 20 == 0 && this.getBiomass() > 0) {
         this.buffAI();
      }

      if (this.tickCount % 40 == 0 && this.horizontalCollision && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
         this.griefBlocks(this.getTarget());
      }

   }

   public void setTarget(@Nullable LivingEntity entity) {
      super.setTarget(entity);
      this.setInvisible(entity != null && entity.distanceToSqr(this) > (double)50.0F);
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

   protected SoundEvent getAmbientSound() {
      return this.isInvisible() ? null : (SoundEvent)Ssounds.SPECTER_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.EVOLVE_HURT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (INVISIBLE.equals(dataAccessor)) {
         this.playSound(this.isInvisible() ? (SoundEvent)Ssounds.SPECTER_CLOAK.get() : (SoundEvent)Ssounds.SPECTER_UNCLOAK.get());
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.hasEffect((MobEffect)Seffects.MARKER.get())) {
            return true;
         }
      }

      if (entity instanceof InventoryCarrier carrier) {
         if (carrier.getInventory() != null && carrier.getInventory().hasAnyMatching(ItemStack::isEdible)) {
            return true;
         }
      }

      return super.hasLineOfSight(entity);
   }

   public boolean interractWithBlock(BlockPos pos) {
      BlockEntity var3 = this.level().getBlockEntity(pos);
      if (var3 instanceof Container container) {
         if (this.food(container)) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
               ItemStack stack = container.getItem(i);
               FoodProperties properties = stack.getFoodProperties(this);
               if (stack.isEdible() && properties != null) {
                  int amount = stack.getCount() > 1 ? this.random.nextInt(stack.getCount()) : stack.getCount();
                  this.playSound(SoundEvents.GENERIC_EAT);
                  stack.shrink(amount);
                  this.setStomach(this.getStomach() + (int)((float)properties.getNutrition() + properties.getSaturationModifier()) * amount);
               }
            }

            return true;
         }
      }

      this.level().destroyBlock(pos, true, this);
      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)) {
         amount /= 2.0F;
      }

      if (source.getEntity() != null && this.random.nextFloat() < 0.1F) {
         ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), this.level());
         scent.moveTo(this.getX(), this.getY(), this.getZ());
         this.level().addFreshEntity(scent);
      }

      return super.hurt(source, amount);
   }

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return super.calculateFallDamage(p_21237_, p_21238_) - 15;
   }

   public float amountOfDamage(float value) {
      return (float)((Double)SConfig.SERVER.specter_damage.get() * (Double)SConfig.SERVER.global_damage.get() / (double)4.0F);
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.ABNORMALS;
   }

   public boolean interactBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      return state.is(Utilities.biomass) ? level.setBlock(blockPos, ((net.minecraft.world.level.block.Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) : level.destroyBlock(blockPos, false, this);
   }

   static {
      INVISIBLE = SynchedEntityData.defineId(Specter.class, EntityDataSerializers.BOOLEAN);
      BIOMASS = SynchedEntityData.defineId(Specter.class, EntityDataSerializers.INT);
      STOMACH = SynchedEntityData.defineId(Specter.class, EntityDataSerializers.INT);
      states = new ArrayList() {
         {
            this.add(Blocks.TORCH.defaultBlockState());
            this.add(Blocks.REDSTONE_TORCH.defaultBlockState());
            this.add(Blocks.TNT.defaultBlockState());
            this.add(((net.minecraft.world.level.block.Block)Sblocks.CDU.get()).defaultBlockState());
         }
      };
   }

   public static class SearchAroundGoal extends Goal {
      private final Specter specter;
      public int tryTicks;

      public SearchAroundGoal(Specter specter) {
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

      public boolean canContinueToUse() {
         return this.specter.getTarget() == null;
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
            this.specter.interractWithBlock(pos);
            this.openChest(pos);
            this.specter.setTargetPos((BlockPos)null);
            this.specter.searchBlocks();
         }

      }

      public void openChest(BlockPos pos) {
         BlockEntity entity = this.specter.level().getBlockEntity(pos);
         if (entity instanceof ChestBlockEntity chestBlock) {
            this.specter.playSound(SoundEvents.CHEST_OPEN);
            this.specter.level().blockEvent(pos, chestBlock.getBlockState().getBlock(), 1, 1);
            this.specter.level().updateNeighborsAt(pos, chestBlock.getBlockState().getBlock());
            this.specter.level().updateNeighborsAt(pos.below(), chestBlock.getBlockState().getBlock());
         }

      }
   }
}
