package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedPlayer;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class Nuclealave extends EvolvedInfected implements RangedAttackMob, ArmedInfected, HasUsableSlot {
   public Nuclealave(EntityType type, Level level) {
      super(type, level);
      this.setMaxUpStep(1.0F);
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.nucke_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.nuckelave_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.nuckelave_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.nuckelave_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      InfectedPlayer.createName(this, (List)SConfig.DATAGEN.name.get());
      InfectedPlayer.createItems(this, EquipmentSlot.HEAD, (List)SConfig.DATAGEN.player_h.get());
      InfectedPlayer.createItems(this, EquipmentSlot.CHEST, (List)SConfig.DATAGEN.player_c.get());
      InfectedPlayer.createItems(this, EquipmentSlot.LEGS, (List)SConfig.DATAGEN.player_l.get());
      InfectedPlayer.createItems(this, EquipmentSlot.FEET, (List)SConfig.DATAGEN.player_b.get());
      InfectedPlayer.createItems(this, EquipmentSlot.MAINHAND, (List)SConfig.DATAGEN.player_hm.get());
      InfectedPlayer.createItems(this, EquipmentSlot.OFFHAND, (List)SConfig.DATAGEN.player_ho.get());
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance instance, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      this.populateDefaultEquipmentSlots(this.random, instance);
      return super.finalizeSpawn(serverLevelAccessor, instance, p_21436_, p_21437_, p_21438_);
   }

   public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
      super.awardKillScore(entity, i, damageSource);
      if (entity instanceof LivingEntity living) {
         for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = living.getItemBySlot(slot);
            if (this.getItemBySlot(slot) == ItemStack.EMPTY && stack != ItemStack.EMPTY) {
               this.setItemSlot(slot, stack);
            }
         }
      }

   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new RangedAttackGoal(this, (double)1.1F, 50, 20.0F) {
         public boolean canUse() {
            return Nuclealave.this.canDoRangedAttacks() && super.canUse();
         }

         public void start() {
            super.start();
            Nuclealave.this.setAggressive(true);
         }

         public void stop() {
            super.stop();
            Nuclealave.this.setAggressive(false);
         }
      });
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.3, false) {
         public boolean canUse() {
            return !Nuclealave.this.canDoRangedAttacks() && super.canUse();
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public boolean canDoRangedAttacks() {
      LivingEntity living = this.getTarget();
      if (!(this.getMainHandItem().getItem() instanceof BowItem) && !(this.getOffhandItem().getItem() instanceof BowItem)) {
         return false;
      } else {
         return living != null && living.distanceToSqr(this) > (double)100.0F;
      }
   }

   private boolean hasFireCapabilities() {
      return this.getMainHandItem().getItem() instanceof FlintAndSteelItem || this.getOffhandItem().getItem() instanceof FlintAndSteelItem;
   }

   private boolean canShield() {
      return this.getMainHandItem().canPerformAction(ToolActions.SHIELD_BLOCK) || this.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK);
   }

   private ItemStack getShieldInHand() {
      return this.getMainHandItem().getItem() instanceof ShieldItem ? this.getMainHandItem() : this.getOffhandItem();
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.canShield()) {
         Entity var4 = source.getEntity();
         if (var4 instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)var4;
            if (Math.random() < (double)0.35F) {
               this.playSound(SoundEvents.SHIELD_BLOCK);
               this.getShieldInHand().hurtAndBreak((int)amount, living, (e) -> {
               });
               return false;
            }
         }
      }

      return super.hurt(source, amount);
   }

   public boolean doHurtTarget(Entity entity) {
      if (this.hasFireCapabilities()) {
         entity.setSecondsOnFire(10);
      }

      return super.doHurtTarget(entity);
   }

   protected AbstractArrow getArrow(ItemStack p_32156_, float p_32157_) {
      return ProjectileUtil.getMobArrow(this, p_32156_, p_32157_);
   }

   public void performRangedAttack(LivingEntity entity, float v) {
      ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, (item) -> item instanceof BowItem)));
      AbstractArrow abstractarrow = this.getArrow(itemstack, v);
      if (this.getMainHandItem().getItem() instanceof BowItem) {
         abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
      }

      double d0 = entity.getX() - this.getX();
      double d1 = entity.getY(0.3333333333333333) - abstractarrow.getY();
      double d2 = entity.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      if (abstractarrow instanceof Arrow arrow) {
         arrow.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600));
         if (this.hasFireCapabilities()) {
            arrow.setSecondsOnFire(10);
         }
      }

      abstractarrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
      this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(abstractarrow);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.ADVENTURER_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.HORSE_STEP;
   }

   public boolean hasUsableSlot(EquipmentSlot slot) {
      return true;
   }
}
