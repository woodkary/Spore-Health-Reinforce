package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.PullGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Variants.SlasherVariants;
import java.util.List;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class Slasher extends EvolvedInfected implements ArmorPersentageBypass, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private final WeakHashMap screwMap = new WeakHashMap();

   public Slasher(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sla_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.sla_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sla_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)20.0F).add(Attributes.ATTACK_KNOCKBACK, (double)0.0F);
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      if (this.getVariant() == SlasherVariants.PIERCER) {
         return SdamageTypes.slasher_piercing_damage(entity);
      } else {
         return Math.random() < 0.3 ? SdamageTypes.slasher_damage(this) : super.getCustomDamage(entity);
      }
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_slasher_loot.get();
   }

   public boolean canDisableShield() {
      return this.getVariant() == SlasherVariants.PIERCER ? true : super.canDisableShield();
   }

   public double getRanges() {
      return this.getVariant() == SlasherVariants.GRABBER ? (double)1.5F : (double)1.0F;
   }

   public boolean pull() {
      if (this.getVariant() != SlasherVariants.PIERCER && this.getVariant() != SlasherVariants.SMASHER) {
         LivingEntity living = this.getTarget();
         return living != null && this.distanceToSqr(living) < (double)32.0F * this.getRanges() && this.distanceToSqr(living) > (double)16.0F * this.getRanges() && this.hasLineOfSight(living);
      } else {
         return false;
      }
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(1, new PullGoal(this, (double)32.0F, (double)16.0F) {
         public boolean canUse() {
            return Slasher.this.pull();
         }

         public void start() {
            super.start();
            this.mob.playSound((SoundEvent)Ssounds.SLASHER_PULL.get());
            this.mob.level().broadcastEntityEvent(this.mob, (byte)4);
         }
      });
      this.goalSelector.addGoal(3, new OpenDoorGoal(this, true) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.higher_thinking.get();
         }

         public void start() {
            this.mob.swing(InteractionHand.MAIN_HAND);
            super.start();
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   protected void customServerAiStep() {
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this) && (Boolean)SConfig.SERVER.higher_thinking.get()) {
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      }

      super.customServerAiStep();
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         if (this.getVariant() == SlasherVariants.PIERCER && !player.isBlocking()) {
            player.getInventory().hurtArmor(SdamageTypes.slasher_piercing_damage(this), 35.0F, Inventory.ALL_ARMOR_SLOTS);
         }
      }

      if (this.getVariant() == SlasherVariants.SMASHER && entity instanceof LivingEntity livingEntity) {
         livingEntity.hurtMarked = true;
         livingEntity.knockback((double)2.0F, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
      }

      if (this.getVariant() == SlasherVariants.GRABBER && entity instanceof LivingEntity livingEntity) {
         if (!this.level().isClientSide && Math.random() < 0.15) {
            if (Math.random() < (double)0.5F) {
               this.dropItems(livingEntity, InteractionHand.MAIN_HAND, livingEntity.getOnPos());
            } else {
               this.dropItems(livingEntity, InteractionHand.OFF_HAND, livingEntity.getOnPos());
            }
         }
      }

      if (this.getVariant() == SlasherVariants.SCREW) {
         double defaultDamage = (Double)SConfig.SERVER.sla_damage.get() * (Double)SConfig.SERVER.global_damage.get();
         double damageMod = (double)1.0F;
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (this.screwMap.containsKey(living)) {
               damageMod = (Double)this.screwMap.get(living);
               double newMod = Math.min((double)2.5F, damageMod + (double)0.5F);
               this.screwMap.put(living, newMod);
               damageMod = newMod;
            } else {
               this.screwMap.put(living, (double)1.0F);
            }
         }

         AttributeInstance instance = this.getAttribute(Attributes.ATTACK_DAMAGE);
         if (instance != null) {
            instance.setBaseValue(defaultDamage * damageMod);
         }
      }

      this.playSound((SoundEvent)Ssounds.SLASHER_STAB.get());
      return super.doHurtTarget(entity);
   }

   public boolean hurt(DamageSource source, float amount) {
      Entity var4 = source.getDirectEntity();
      if (var4 instanceof LivingEntity livingEntity) {
         if (livingEntity.distanceToSqr(this) < (double)100.0F && !source.is(DamageTypes.THORNS) && this.getVariant() == SlasherVariants.SCREW) {
            livingEntity.hurt(this.level().damageSources().thorns(this), 5.0F);
         }
      }

      return super.hurt(source, amount);
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      super.awardKillScore(entity, i, damageSource);
      if (entity instanceof LivingEntity living) {
         this.screwMap.remove(living);
      }

   }

   private void dropItems(LivingEntity living, InteractionHand hand, BlockPos pos) {
      ItemStack stack = living.getItemInHand(hand);
      if (stack != ItemStack.EMPTY) {
         ItemEntity entity = new ItemEntity(this.level(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
         this.level().addFreshEntity(entity);
         living.setItemInHand(hand, ItemStack.EMPTY);
      }
   }

   public void defineSynchedData() {
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

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_DEATH.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      SlasherVariants variant = (SlasherVariants)Util.getRandom(SlasherVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public SlasherVariants getVariant() {
      return SlasherVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= SlasherVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return SlasherVariants.values().length;
   }

   private void setVariant(SlasherVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public float amountOfDamage(float value) {
      return this.getVariant() == SlasherVariants.PIERCER ? (float)((Double)SConfig.SERVER.sla_damage.get() * (Double)SConfig.SERVER.global_damage.get() / (double)2.0F) : 0.0F;
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Slasher.class, EntityDataSerializers.INT);
   }
}
