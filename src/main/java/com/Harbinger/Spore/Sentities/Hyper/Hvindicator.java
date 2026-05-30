package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.Projectile.ThrownItemProjectile;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;

public class Hvindicator extends Hyper implements RangedAttackMob {
   private static final EntityDataAccessor RIGHT_SKULL;
   private static final EntityDataAccessor LEFT_SKULL;
   private static final EntityDataAccessor TIME_AXE;
   private int attackAnimationTick;
   private int rangedAnimationTick;
   private int blockTime = 0;
   public AnimationState block_attack = new AnimationState();

   public Hvindicator(EntityType type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.hindicator_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.hindicator_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.hindicator_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("left_skull", (Boolean)this.entityData.get(LEFT_SKULL));
      tag.putBoolean("right_skull", (Boolean)this.entityData.get(RIGHT_SKULL));
      tag.putInt("time_axe", (Integer)this.entityData.get(TIME_AXE));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(LEFT_SKULL, tag.getBoolean("left_skull"));
      this.entityData.set(RIGHT_SKULL, tag.getBoolean("right_skull"));
      this.entityData.set(TIME_AXE, tag.getInt("time_axe"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(RIGHT_SKULL, false);
      this.entityData.define(LEFT_SKULL, false);
      this.entityData.define(TIME_AXE, 600);
   }

   public boolean hasRightSkull() {
      return (Boolean)this.entityData.get(RIGHT_SKULL);
   }

   public boolean hasLeftSkull() {
      return (Boolean)this.entityData.get(LEFT_SKULL);
   }

   public boolean hasAxe() {
      return (Integer)this.entityData.get(TIME_AXE) >= 600;
   }

   private void tickAxe() {
      if (!this.hasAxe()) {
         this.entityData.set(TIME_AXE, (Integer)this.entityData.get(TIME_AXE) + 1);
      }
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else if (value == 5) {
         this.block_attack.start(this.tickCount);
         this.blockTime = 10;
      } else if (value == 6) {
         this.rangedAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      if (entity instanceof Player player) {
         if (this.doesPlayerHaveShieldInHand(player, InteractionHand.MAIN_HAND) || this.doesPlayerHaveShieldInHand(player, InteractionHand.OFF_HAND)) {
            player.disableShield(true);
         }
      }

      return super.doHurtTarget(entity);
   }

   public boolean doesPlayerHaveShieldInHand(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      return stack.getItem() instanceof ShieldItem;
   }

   public void tick() {
      super.tick();
      this.tickAxe();
      this.setupAnimationStates();
      if (this.tickCount % 60 == 0 && this.hasAxe()) {
         LivingEntity target = this.getTarget();
         if (target != null && this.hasLineOfSight(target)) {
            this.rangedAnimationTick = 10;
            this.level().broadcastEntityEvent(this, (byte)6);
            this.performRangedAttack(target, 0.0F);
            this.entityData.set(TIME_AXE, 0);
         }
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.hindicator_loot.get();
   }

   private void setupAnimationStates() {
      if (this.blockTime > 0) {
         if (this.blockTime == 1) {
            this.block_attack.stop();
         }

         --this.blockTime;
      }

   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public boolean canDisableShield() {
      super.canDisableShield();
      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      float reduction = 1.0F;
      reduction = this.hasLeftSkull() ? reduction - 0.1F : reduction;
      reduction = this.hasRightSkull() ? reduction - 0.1F : reduction;
      if (source.is(DamageTypeTags.IS_PROJECTILE) && Math.random() < (double)0.5F) {
         if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.SHIELD_BLOCK);
            this.level().broadcastEntityEvent(this, (byte)5);
         }

         return false;
      } else if (source.getEntity() != null && Math.random() < (double)0.15F) {
         if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)5);
         }

         return false;
      } else {
         return super.hurt(source, amount * reduction);
      }
   }

   public void awardSkull(LivingEntity entity) {
      if ((entity instanceof Villager || entity instanceof AbstractIllager || entity instanceof Witch) && !this.hasLeftSkull()) {
         this.entityData.set(LEFT_SKULL, true);
      }

      if ((entity instanceof Zombie || entity instanceof Player) && !this.hasRightSkull()) {
         this.entityData.set(RIGHT_SKULL, true);
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public int getRangedAttackAnimationTick() {
      return this.rangedAnimationTick;
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.rangedAnimationTick > 0) {
         --this.rangedAnimationTick;
      }

   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      ThrownItemProjectile thrownItemProjectile = new ThrownItemProjectile(this.level(), this, (float)((Double)SConfig.SERVER.hindicator_ranged_damage.get() * (double)1.0F), new ItemStack(Items.IRON_AXE));
      thrownItemProjectile.setLivingEntityPredicate(this.TARGET_SELECTOR);
      double d0 = livingEntity.getX() - this.getX();
      double d1 = livingEntity.getY(0.3333333333333333) - thrownItemProjectile.getY();
      double d2 = livingEntity.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      thrownItemProjectile.shoot(d0, d1 + d3 * (double)0.2F, d2, 2.0F, 2.0F);
      this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(thrownItemProjectile);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HINDICATOR_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   static {
      RIGHT_SKULL = SynchedEntityData.defineId(Hvindicator.class, EntityDataSerializers.BOOLEAN);
      LEFT_SKULL = SynchedEntityData.defineId(Hvindicator.class, EntityDataSerializers.BOOLEAN);
      TIME_AXE = SynchedEntityData.defineId(Hvindicator.class, EntityDataSerializers.INT);
   }
}
