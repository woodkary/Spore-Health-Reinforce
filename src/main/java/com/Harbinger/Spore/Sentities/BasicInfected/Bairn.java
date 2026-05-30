package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.AI.InfectedConsumeFromRemains;
import com.Harbinger.Spore.Sentities.AI.InfectedPanicGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.BufferAI;
import com.Harbinger.Spore.Sentities.AI.LocHiv.FollowOthersGoal;
import com.Harbinger.Spore.Sentities.AI.LocHiv.LocalTargettingGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.MovementControls.WaterXlandMovement;
import com.Harbinger.Spore.Sentities.Utility.HyperClaw;
import com.Harbinger.Spore.Sentities.Variants.BairnSkins;
import com.google.common.base.Predicate;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class Bairn extends Infected implements VariantKeeper {
   private @Nullable BlockPos Targetpos;
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;

   public Bairn(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new WaterXlandMovement(this);
      this.navigation = new HybridPathNavigation(this, this.level());
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(2, new SearchAroundGoal(this));
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)1.5F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected void customServerAiStep() {
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this) && (Boolean)SConfig.SERVER.higher_thinking.get() && (this.getVariant() == BairnSkins.VILLAGER || this.getVariant() == BairnSkins.ZOMBIE_VILLAGER)) {
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      }

      super.customServerAiStep();
   }

   protected void addRegularGoals() {
      this.goalSelector.addGoal(3, new LocalTargettingGoal(this));
      this.goalSelector.addGoal(4, new BufferAI(this));
      this.goalSelector.addGoal(3, new OpenDoorGoal(this, true) {
         public boolean canUse() {
            return super.canUse() && (Bairn.this.getVariant() == BairnSkins.VILLAGER || Bairn.this.getVariant() == BairnSkins.ZOMBIE_VILLAGER);
         }

         public void start() {
            this.mob.swing(InteractionHand.MAIN_HAND);
            super.start();
         }
      });
      this.goalSelector.addGoal(5, new FollowOthersGoal(this, HyperClaw.class, (entity) -> this.getVehicle() == null) {
         public boolean canUse() {
            return Bairn.this.getVehicle() == null && super.canUse();
         }

         public boolean canContinueToUse() {
            LivingEntity living = Bairn.this.getFollowPartner();
            return living != null && living.distanceTo(Bairn.this) > 36.0F;
         }

         public void stop() {
            super.stop();
            LivingEntity living = Bairn.this.getFollowPartner();
            if (living != null) {
               Bairn.this.startRiding(living);
            }

         }
      });
      this.goalSelector.addGoal(5, new InfectedPanicGoal(this, (double)1.5F));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this) {
         public boolean canUse() {
            return super.canUse() && Bairn.this.getVariant() != BairnSkins.DROWNED;
         }
      });
      this.goalSelector.addGoal(7, new InfectedConsumeFromRemains(this));
   }

   public boolean canDrownInFluidType(FluidType type) {
      return this.getVariant() != BairnSkins.DROWNED && super.canDrownInFluidType(type);
   }

   public void travel(Vec3 input) {
      if (this.isEffectiveAi() && this.isInFluidType() && this.getVariant() == BairnSkins.DROWNED) {
         this.moveRelative(0.1F, input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(input);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.bairn_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.bairn_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.bairn_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.bairn_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.BAIRN.get();
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

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   private void setVariant(BairnSkins variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public BairnSkins getVariant() {
      return BairnSkins.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= BairnSkins.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return BairnSkins.values().length;
   }

   public @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      BairnSkins variant = (BairnSkins)Util.getRandom(BairnSkins.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)20.0F, (double)4.0F, (double)20.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockEntity blockEntity = this.level().getBlockEntity(blockpos);
         if (blockEntity instanceof Container container) {
            if (this.food(container) && this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
               this.setTargetPos(blockpos);
               break;
            }
         }
      }

   }

   public boolean hasLineOfSightBlocks(BlockPos pos) {
      BlockHitResult raytraceresult = this.level().clip(new ClipContext(this.getEyePosition(1.0F), new Vec3((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), Block.COLLIDER, Fluid.NONE, this));
      BlockPos position = raytraceresult.getBlockPos();
      return pos.equals(position) || this.level().isEmptyBlock(pos) || this.level().getBlockEntity(pos) == this.level().getBlockEntity(position);
   }

   private boolean food(Container container) {
      return container.hasAnyMatching((item) -> item.getFoodProperties(this) != null);
   }

   public boolean interractWithBlock(BlockPos pos) {
      BlockEntity var3 = this.level().getBlockEntity(pos);
      if (var3 instanceof Container container) {
         if (this.food(container)) {
            for(int i = 0; i < container.getContainerSize(); ++i) {
               ItemStack stack = container.getItem(i);
               FoodProperties properties = stack.getFoodProperties(this);
               if (properties != null) {
                  int amount = stack.getCount() > 1 ? this.random.nextInt(stack.getCount()) : stack.getCount();
                  this.playSound(SoundEvents.GENERIC_EAT);
                  stack.shrink(amount);
                  this.setKills(this.getKills() + (int)((float)properties.getNutrition() + properties.getSaturationModifier()) * amount);
               }
            }

            return true;
         }
      }

      this.level().destroyBlock(pos, true, this);
      return true;
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (this.getVariant() == BairnSkins.HUSK) {
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
         }

         if (this.getVariant() == BairnSkins.ZOMBIE_VILLAGER) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
         }

         living.hurtTime = 0;
         living.invulnerableTime = 0;
      }

      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 100 == 0) {
         this.searchBlocks();
      }

   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Bairn.class, EntityDataSerializers.INT);
   }

   public static class SearchAroundGoal extends Goal {
      private final Bairn specter;
      public int tryTicks;

      public SearchAroundGoal(Bairn specter) {
         this.specter = specter;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         return this.specter.getTargetPos() != null && this.specter.getTarget() == null;
      }

      protected void moveToBlock(BlockPos pos) {
         Path path = this.specter.navigation.createPath(pos, 1);
         if (path != null) {
            this.specter.getNavigation().moveTo(path, (double)1.0F);
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
