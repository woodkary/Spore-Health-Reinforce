package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.BileProjectile;
import com.Harbinger.Spore.Sentities.Projectile.ThrownTumor;
import com.Harbinger.Spore.Sentities.Projectile.Vomit;
import com.Harbinger.Spore.Sentities.Variants.SpitterVariants;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;

public class Spitter extends EvolvedInfected implements RangedAttackMob, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;

   public Spitter(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.1, this.getShootingPerVariant(), (float)this.getRangePerVariant()) {
         public boolean canUse() {
            return Spitter.this.switchyFar() && super.canUse();
         }
      });
      this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.1, 5, 5.0F) {
         public boolean canUse() {
            return Spitter.this.switchyClose() && super.canUse();
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_spitter_loot.get();
   }

   private boolean switchyFar() {
      if (this.getTarget() != null && (this.getTypeVariant() == 0 || this.getTypeVariant() == 3)) {
         double ze = this.distanceToSqr(this.getTarget());
         return ze > (double)32.0F;
      } else {
         return true;
      }
   }

   private boolean switchyClose() {
      if (this.getTarget() != null && (this.getTypeVariant() == 0 || this.getTypeVariant() == 3)) {
         double ze = this.distanceToSqr(this.getTarget());
         return ze < (double)32.0F;
      } else {
         return false;
      }
   }

   private int getShootingPerVariant() {
      byte var10000;
      switch (this.getVariant()) {
         case BILE -> var10000 = 50;
         case EXPLOSIVE -> var10000 = 60;
         default -> var10000 = 40;
      }

      return var10000;
   }

   private int getRangePerVariant() {
      return this.getVariant() == SpitterVariants.SNIPER ? 32 : 16;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.spit_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ARMOR, (Double)SConfig.SERVER.spit_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public void onSyncedDataUpdated(EntityDataAccessor accessor) {
      super.onSyncedDataUpdated(accessor);
      if (accessor.equals(DATA_ID_TYPE_VARIANT)) {
         AttributeInstance instance = this.getAttribute(Attributes.FOLLOW_RANGE);
         if (instance != null) {
            instance.setBaseValue(this.getTypeVariant() == 4 ? (double)64.0F : (double)32.0F);
         }
      }

   }

   public void performRangedAttack(LivingEntity target, float f) {
      if (!this.level().isClientSide) {
         double dx = target.getX() - this.getX();
         double dy = target.getY() + (double)target.getEyeHeight() - (double)1.0F;
         double dz = target.getZ() - this.getZ();
         double dist = this.distanceToSqr(target);
         switch (this.getTypeVariant()) {
            case 1 -> this.shootTumor(dx, dy, dz);
            case 2 -> this.shootBile(dx, dy, dz);
            case 3 -> this.shootCloseOrFar(target, dist, true);
            case 4 -> AcidBall.shoot(this, target, (float)((Double)SConfig.SERVER.spit_damage_l.get() * (Double)SConfig.SERVER.global_damage.get()) * 1.5F);
            default -> this.shootCloseOrFar(target, dist, false);
         }

      }
   }

   private void shootTumor(double dx, double dy, double dz) {
      ThrownTumor tumor = new ThrownTumor(this.level(), this);
      tumor.setMobEffect((MobEffect)Seffects.CORROSION.get());
      tumor.setExplode(ExplosionInteraction.NONE);
      tumor.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
      tumor.shoot(dx, dy - tumor.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
      this.level().addFreshEntity(tumor);
   }

   private void shootBile(double dx, double dy, double dz) {
      BileProjectile bile = new BileProjectile(this.level(), this, this.TARGET_SELECTOR);
      bile.setDamage((float)((Double)SConfig.SERVER.spit_damage_l.get() * (Double)SConfig.SERVER.global_damage.get()));
      bile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
      bile.shoot(dx, dy - bile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
      this.level().addFreshEntity(bile);
   }

   private void shootCloseOrFar(LivingEntity target, double dist, boolean doubleShot) {
      if (dist < (double)32.0F) {
         float dmg = (float)((Double)SConfig.SERVER.spit_damage_c.get() * (Double)SConfig.SERVER.global_damage.get());
         Vomit.shoot(this, target, dmg);
         if (doubleShot) {
            Vomit.shoot(this, target, dmg);
         }
      } else {
         float dmg = (float)((Double)SConfig.SERVER.spit_damage_l.get() * (Double)SConfig.SERVER.global_damage.get());
         AcidBall.shoot(this, target, dmg);
         if (doubleShot) {
            AcidBall.shoot(this, target, dmg);
         }

         this.playSound(SoundEvents.SLIME_JUMP, 1.0F, 0.5F);
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
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
      this.setVariant((SpitterVariants)Util.getRandom(SpitterVariants.values(), this.random));
      return super.finalizeSpawn(level, difficulty, type, group, tag);
   }

   public SpitterVariants getVariant() {
      return SpitterVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i >= 0 && i < SpitterVariants.values().length ? i : 0);
   }

   public int amountOfMutations() {
      return SpitterVariants.values().length;
   }

   private void setVariant(SpitterVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public boolean hasLineOfSight(Entity entity) {
      return !entity.isInFluidType() && super.hasLineOfSight(entity);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Spitter.class, EntityDataSerializers.INT);
   }
}
