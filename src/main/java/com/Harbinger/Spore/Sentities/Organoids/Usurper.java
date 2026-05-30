package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.Projectile.AdaptableProjectile;
import com.Harbinger.Spore.Sentities.Projectile.VomitUsurperBall;
import com.Harbinger.Spore.Sentities.Variants.BulletParameters;
import com.Harbinger.Spore.Sentities.Variants.UsurperVariants;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Usurper extends Organoid implements RangedAttackMob, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor TIMER;
   private static final EntityDataAccessor BURST_CONTROL;

   public Usurper(EntityType type, Level level) {
      super(type, level);
   }

   public int getEmerge_tick() {
      return 60;
   }

   public int getBorrow_tick() {
      return 100;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.usurper_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.usurper_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TIMER, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
      this.entityData.define(BURST_CONTROL, 0);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, (double)0.0F, 5, 5, 1.5F) {
         public boolean canUse() {
            return super.canUse() && Usurper.this.getVariant() == UsurperVariants.SPRAY;
         }
      });
      this.goalSelector.addGoal(2, new ScatterShotRangedGoal(this, (double)0.0F, 40, 32.0F, 1, 4) {
         public boolean canUse() {
            return super.canUse() && Usurper.this.getVariant() == UsurperVariants.DEFAULT;
         }
      });
      this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public void setBurstControl(int value) {
      this.entityData.set(BURST_CONTROL, value);
   }

   public int getBurstControl() {
      return (Integer)this.entityData.get(BURST_CONTROL);
   }

   public void countDownBurstControl() {
      this.setBurstControl(this.getBurstControl() - 1);
   }

   public void tickBurstShotController() {
      LivingEntity target = this.getTarget();
      if (target != null) {
         this.getLookControl().setLookAt(target, 30.0F, 30.0F);
         if (this.getBurstControl() > -5) {
            this.countDownBurstControl();
         } else if (this.hasLineOfSight(target)) {
            this.setBurstControl(this.random.nextInt(3, 7));
         }

         if (this.getBurstControl() > 0) {
            this.performRangedAttack(target, 0.0F);
         }

      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.getTarget() == null && (Integer)this.entityData.get(TIMER) < 1200) {
            this.entityData.set(TIMER, (Integer)this.entityData.get(TIMER) + 1);
         } else if ((Integer)this.entityData.get(TIMER) >= 1200) {
            this.tickBurrowing();
         }

         if (this.tickCount % 20 == 0 && this.getVariant() == UsurperVariants.BURST) {
            this.tickBurstShotController();
         }
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.usurper_loot.get();
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         this.discard();
         burrowing = -1;
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public boolean hurt(DamageSource source, float value) {
      return this.isEmerging() ? false : super.hurt(source, value);
   }

   public boolean isNoAi() {
      return this.isBurrowing() || this.isEmerging();
   }

   public void performRangedAttack(LivingEntity livingEntity, float p_33318_) {
      if (this.getVariant() == UsurperVariants.SPRAY) {
         VomitUsurperBall.shoot(this, livingEntity, (float)((double)1.0F * (Double)SConfig.SERVER.global_damage.get()));
      } else {
         BulletParameters parameters = (BulletParameters)Util.getRandom(BulletParameters.values(), this.random);
         AdaptableProjectile projectile = new AdaptableProjectile(parameters, this.level(), this);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight();
         double dz = livingEntity.getZ() - this.getZ();
         projectile.moveTo(this.getX(), this.getY() + 1.2, this.getZ());
         projectile.shoot(dx, dy - projectile.getY() + Math.hypot(dx, dz) * (double)0.001F, dz, 1.5F, 3.0F);
         this.playSound((SoundEvent)Ssounds.SPIT.get());
         this.level().addFreshEntity(projectile);
      }

   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.USURPER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      UsurperVariants variant = (UsurperVariants)Util.getRandom(UsurperVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   public UsurperVariants getVariant() {
      return UsurperVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= UsurperVariants.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return UsurperVariants.values().length;
   }

   private void setVariant(UsurperVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public void onSyncedDataUpdated(@NotNull EntityDataAccessor dataValues) {
      super.onSyncedDataUpdated(dataValues);
      if (DATA_ID_TYPE_VARIANT.equals(dataValues)) {
         AttributeInstance range = this.getAttribute(Attributes.FOLLOW_RANGE);
         if (range != null) {
            if (this.getVariant() == UsurperVariants.SPRAY) {
               range.setBaseValue((double)8.0F);
            } else {
               range.setBaseValue((double)64.0F);
            }
         }
      }

   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Usurper.class, EntityDataSerializers.INT);
      TIMER = SynchedEntityData.defineId(Usurper.class, EntityDataSerializers.INT);
      BURST_CONTROL = SynchedEntityData.defineId(Usurper.class, EntityDataSerializers.INT);
   }
}
