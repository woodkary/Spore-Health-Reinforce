package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import com.Harbinger.Spore.Sentities.Variants.ProtectorVariants;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Protector extends EvolvedInfected implements ArmedInfected, HasUsableSlot, RangedAttackMob, VariantKeeper {
   public static final EntityDataAccessor SHIELDED;
   public static final EntityDataAccessor PEARLS;
   public static final EntityDataAccessor DAMAGE_POINTS;
   public static final EntityDataAccessor RESISTANCE_POINTS;
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   public int ticksUnShielded;
   private @Nullable BlockPos Targetpos;

   public Protector(EntityType p_33002_, Level p_33003_) {
      super(p_33002_, p_33003_);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new SearchAroundGoal(this));
      this.goalSelector.addGoal(3, new ProtectorMeleeGoal(this, (float)((Double)SConfig.SERVER.protector_damage.get() * (Double)SConfig.SERVER.global_damage.get())));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_protector_loot.get();
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.knight_damage(this) : super.getCustomDamage(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.protector_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.protector_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.protector_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public boolean getShielded() {
      return (Boolean)this.entityData.get(SHIELDED);
   }

   public void setShielded(boolean value) {
      this.entityData.set(SHIELDED, value);
   }

   public int getPearls() {
      return (Integer)this.entityData.get(PEARLS);
   }

   public void setPearls(int e) {
      this.entityData.set(PEARLS, e);
   }

   public int getDamagePoints() {
      return (Integer)this.entityData.get(DAMAGE_POINTS);
   }

   public void setDamagePoints(int e) {
      this.entityData.set(DAMAGE_POINTS, e);
   }

   public int getResistancePoints() {
      return (Integer)this.entityData.get(RESISTANCE_POINTS);
   }

   public void setResistancePoints(int e) {
      this.entityData.set(RESISTANCE_POINTS, e);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SHIELDED, false);
      this.entityData.define(PEARLS, 1);
      this.entityData.define(DAMAGE_POINTS, 0);
      this.entityData.define(RESISTANCE_POINTS, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      super.awardKillScore(entity, i, damageSource);
      if (entity instanceof EnderMan) {
         this.setPearls(this.getPearls() + this.random.nextInt(3));
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setShielded(tag.getBoolean("shield"));
      this.setPearls(tag.getInt("pearls"));
      this.setDamagePoints(tag.getInt("damage_points"));
      this.setResistancePoints(tag.getInt("resistance_points"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("shield", this.getShielded());
      tag.putInt("pearls", this.getPearls());
      tag.putInt("damage_points", this.getDamagePoints());
      tag.putInt("resistance_points", this.getResistancePoints());
      tag.putInt("Variant", this.getTypeVariant());
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.ADVENTURER_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public void tick() {
      super.tick();
      if (this.ticksUnShielded > 0) {
         --this.ticksUnShielded;
      }

      if (this.tickCount % 200 == 0) {
         this.setShielded(false);
      }

      if (this.tickCount % 40 == 0) {
         LivingEntity entity = this.getTarget();
         if (entity != null) {
            double distance = (double)this.distanceTo(entity);
            if (distance > (double)20.0F && this.getPearls() > 0 && this.hasLineOfSight(entity)) {
               this.performRangedAttack(entity, 0.0F);
            }
         }
      }

      if (this.tickCount % 200 == 0 && this.getVariant() == ProtectorVariants.COLLECTOR) {
         this.searchBlocks();
         this.spreadEffects();
      }

   }

   public boolean hurt(DamageSource source, float amount) {
      Entity entity = source.getEntity();
      LivingEntity target = this.getTarget();
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.equals(target)) {
            ItemStack stack = livingEntity.getMainHandItem();
            if (this.getShielded() && stack.canDisableShield(stack, this, livingEntity)) {
               this.ticksUnShielded = 200;
               this.playSound(SoundEvents.SHIELD_BREAK);
               this.setShielded(false);
            }

            if (this.getShielded() && this.isLookingAtMe(livingEntity)) {
               if (this.getVariant() == ProtectorVariants.STUBBED && !source.is(DamageTypes.THORNS) && livingEntity.distanceToSqr(this) < (double)100.0F) {
                  livingEntity.hurt(this.level().damageSources().thorns(this), amount * 0.1F);
               }

               if (this.getVariant() == ProtectorVariants.MOSS) {
                  livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                  livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
               }

               this.playSound(SoundEvents.SHIELD_BLOCK);
               return false;
            }

            return super.hurt(source, amount);
         }
      }

      if (entity != null && this.getShielded()) {
         this.playSound(SoundEvents.SHIELD_BREAK);
         this.ticksUnShielded = 100;
      }

      return super.hurt(source, amount);
   }

   public void onSyncedDataUpdated(List values) {
      super.onSyncedDataUpdated(values);
      if (values.equals(SHIELDED)) {
         AttributeInstance attributes = this.getAttribute(Attributes.MOVEMENT_SPEED);
         if (attributes != null) {
            attributes.setBaseValue(this.getShielded() ? 0.15 : 0.2);
         }
      }

      if (values.equals(DATA_ID_TYPE_VARIANT)) {
         double prot = (double)1.0F;
         double knock = (double)0.0F;
         AttributeInstance protection = this.getAttribute(Attributes.ARMOR);
         AttributeInstance knockbackResistence = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
         if (this.getVariant() == ProtectorVariants.BULK) {
            prot = 1.3;
            knock = (double)1.0F;
         }

         if (protection != null && knockbackResistence != null) {
            protection.setBaseValue((Double)SConfig.SERVER.protector_armor.get() * prot);
            knockbackResistence.setBaseValue(knock);
         }

         this.refreshDimensions();
      }

   }

   boolean isLookingAtMe(LivingEntity entity) {
      Vec3 lookVec = entity.getViewVector(1.0F).normalize();
      Vec3 toThis = (new Vec3(this.getX() - entity.getX(), this.getEyeY() - entity.getEyeY(), this.getZ() - entity.getZ())).normalize();
      double dot = lookVec.dot(toThis);
      double angleThreshold = 0.9;
      return dot > angleThreshold && entity.hasLineOfSight(this);
   }

   public boolean hasUsableSlot(EquipmentSlot slot) {
      return slot == EquipmentSlot.FEET || slot == EquipmentSlot.HEAD;
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      InfectedPlayer.createName(this, (List)SConfig.DATAGEN.name.get());
      InfectedPlayer.createItems(this, EquipmentSlot.HEAD, (List)SConfig.DATAGEN.player_h.get());
      InfectedPlayer.createItems(this, EquipmentSlot.FEET, (List)SConfig.DATAGEN.player_b.get());
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance instance, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      this.populateDefaultEquipmentSlots(this.random, instance);
      this.setVariant((ProtectorVariants)Util.getRandom(ProtectorVariants.values(), this.random));
      return super.finalizeSpawn(serverLevelAccessor, instance, p_21436_, p_21437_, p_21438_);
   }

   public EntityDimensions getDimensions(Pose pose) {
      float val = this.getVariant() == ProtectorVariants.BULK ? 1.2F : 1.0F;
      return super.getDimensions(pose).scale(val);
   }

   public void die(DamageSource source) {
      super.die(source);
      SporeSavedData.removeProtector(this);
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      if (!this.level().isClientSide) {
         ThrownEnderpearl pearl = new ThrownEnderpearl(this.level(), this);
         double d0 = livingEntity.getEyeY() - this.getEyeY();
         double d1 = livingEntity.getX() - this.getX();
         double d3 = livingEntity.getZ() - this.getZ();
         double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.2;
         pearl.setOwner(this);
         pearl.shoot(d1, d0 + d4, d3, 1.6F, 12.0F);
         this.playSound(SoundEvents.ENDER_PEARL_THROW, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
         this.level().addFreshEntity(pearl);
         this.setPearls(this.getPearls() - 1);
      }

   }

   public ProtectorVariants getVariant() {
      return ProtectorVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i >= 0 && i < ProtectorVariants.values().length ? i : 0);
   }

   public int amountOfMutations() {
      return ProtectorVariants.values().length;
   }

   private void setVariant(ProtectorVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   public boolean hasLineOfSightBlocks(BlockPos pos) {
      BlockHitResult raytraceresult = this.level().clip(new ClipContext(this.getEyePosition(1.0F), new Vec3((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), Block.COLLIDER, Fluid.NONE, this));
      BlockPos position = raytraceresult.getBlockPos();
      return pos.equals(position) || this.level().isEmptyBlock(pos) || this.level().getBlockEntity(pos) == this.level().getBlockEntity(position);
   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F, (double)4.0F, (double)32.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockEntity block = this.level().getBlockEntity(blockpos);
         if (block instanceof Container container) {
            if (this.items(container) && this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
               this.setTargetPos(blockpos);
               break;
            }
         }
      }

   }

   private boolean items(Container container) {
      for(int i = 0; i < container.getContainerSize(); ++i) {
         ItemStack stack = container.getItem(i);
         Item item = stack.getItem();
         if (item.getFoodProperties(stack, this) != null) {
            return true;
         }

         if (item instanceof TieredItem) {
            return true;
         }

         if (item instanceof ArmorItem) {
            return true;
         }
      }

      return false;
   }

   public void interactWithBlock(BlockPos pos) {
      BlockEntity var3 = this.level().getBlockEntity(pos);
      if (var3 instanceof Container container) {
         if (this.items(container)) {
            int food = 0;
            int damage = 0;
            int armor = 0;

            for(int i = 0; i < container.getContainerSize(); ++i) {
               ItemStack stack = container.getItem(i);
               FoodProperties properties = stack.getFoodProperties(this);
               Item item = stack.getItem();
               if (properties != null) {
                  int foodCalculation = 0;

                  for(int e = 0; e < stack.getCount(); ++e) {
                     foodCalculation = (int)((float)properties.getNutrition() + properties.getSaturationModifier() * 10.0F);
                  }

                  food = foodCalculation;
                  stack.shrink(stack.getCount());
               }

               if (item instanceof TieredItem) {
                  TieredItem tieredItem = (TieredItem)item;
                  damage = (int)tieredItem.getTier().getAttackDamageBonus();
                  stack.shrink(1);
               }

               if (item instanceof ArmorItem) {
                  ArmorItem tieredItem = (ArmorItem)item;
                  armor = tieredItem.getDefense();
                  stack.shrink(1);
               }
            }

            this.setKills(this.getKills() + food / 5);
            this.setDamagePoints(this.getDamagePoints() + damage);
            this.setResistancePoints(this.getResistancePoints() + armor);
         }
      }

   }

   public void spreadEffects() {
      AABB aabb = this.getBoundingBox().inflate((double)16.0F);

      for(Entity entity : this.level().getEntities(this, aabb)) {
         if (entity instanceof Infected infected) {
            if (!infected.hasEffect(MobEffects.DAMAGE_RESISTANCE) && this.getResistancePoints() > 0) {
               infected.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 36000, 0));
               this.setResistancePoints(this.getResistancePoints() - 1);
            }

            if (!infected.hasEffect(MobEffects.DAMAGE_BOOST) && this.getDamagePoints() > 0) {
               infected.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 36000, 0));
               this.setDamagePoints(this.getDamagePoints() - 1);
            }
         }
      }

   }

   static {
      SHIELDED = SynchedEntityData.defineId(Protector.class, EntityDataSerializers.BOOLEAN);
      PEARLS = SynchedEntityData.defineId(Protector.class, EntityDataSerializers.INT);
      DAMAGE_POINTS = SynchedEntityData.defineId(Protector.class, EntityDataSerializers.INT);
      RESISTANCE_POINTS = SynchedEntityData.defineId(Protector.class, EntityDataSerializers.INT);
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Protector.class, EntityDataSerializers.INT);
   }

   public static class ProtectorMeleeGoal extends CustomMeleeAttackGoal {
      private final float meleeDamage;

      public ProtectorMeleeGoal(Protector mob, float meleeDamage) {
         super(mob, 1.2, false);
         this.meleeDamage = meleeDamage;
      }

      protected double getAttackReachSqr(LivingEntity entity) {
         return (double)4.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
      }

      protected void checkAndPerformAttack(LivingEntity entity, double at) {
         PathfinderMob var5 = this.mob;
         if (var5 instanceof Protector protector) {
            double distance = (double)protector.distanceTo(entity);
            protector.setShielded(distance < (double)15.0F && protector.ticksUnShielded <= 0);
            if (protector.getShielded() && entity.getHealth() > this.meleeDamage) {
               double d0 = this.getAttackReachSqr(entity);
               if (at <= d0 && this.ticksUntilNextAttack <= 0 && protector.hasLineOfSight(entity)) {
                  this.resetAttackCooldown(20);
                  this.mob.playSound(SoundEvents.SHIELD_BLOCK);
                  this.mob.swing(InteractionHand.MAIN_HAND);
                  entity.hurtMarked = true;
                  if (entity instanceof Mob) {
                     Mob mob1 = (Mob)entity;
                     mob1.setTarget(this.mob);
                  }

                  boolean stud = protector.getVariant() == ProtectorVariants.STUBBED;
                  entity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 100, 0));
                  entity.knockback(stud ? 2.4 : (double)1.2F, (double)Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F))));
                  if (stud) {
                     protector.doHurtTarget(entity);
                     entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                  }
               }
            } else {
               super.checkAndPerformAttack(entity, at);
            }
         }

      }
   }

   public static class SearchAroundGoal extends Goal {
      private final Protector protector;
      public int tryTicks;

      public SearchAroundGoal(Protector protector) {
         this.protector = protector;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         if (this.protector.getVariant() != ProtectorVariants.COLLECTOR) {
            return false;
         } else {
            return this.protector.getTargetPos() != null && this.protector.getTarget() == null;
         }
      }

      protected void moveToBlock(BlockPos pos) {
         if (pos != null) {
            this.protector.navigation.moveTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)1.0F, (double)pos.getZ() + (double)0.5F, (double)1.0F);
         }

      }

      public void start() {
         this.moveToBlock(this.protector.getTargetPos());
         this.tryTicks = 0;
         super.start();
      }

      public boolean canContinueToUse() {
         return this.protector.getTarget() == null;
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
         BlockPos pos = this.protector.getTargetPos();
         if (pos != null && this.shouldRecalculatePath()) {
            this.moveToBlock(pos);
         }

         if (pos != null && pos.closerToCenterThan(this.protector.position(), (double)3.5F)) {
            this.protector.interactWithBlock(pos);
            this.openChest(pos);
            this.protector.setTargetPos((BlockPos)null);
            this.protector.searchBlocks();
         }

      }

      public void openChest(BlockPos pos) {
         BlockEntity entity = this.protector.level().getBlockEntity(pos);
         if (entity instanceof ChestBlockEntity chestBlock) {
            this.protector.playSound(SoundEvents.CHEST_OPEN);
            this.protector.level().blockEvent(pos, chestBlock.getBlockState().getBlock(), 1, 1);
            this.protector.level().updateNeighborsAt(pos, chestBlock.getBlockState().getBlock());
            this.protector.level().updateNeighborsAt(pos.below(), chestBlock.getBlockState().getBlock());
         }

      }
   }
}
