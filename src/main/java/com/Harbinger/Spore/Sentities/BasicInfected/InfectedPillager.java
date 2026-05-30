package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Utility.Vanguard;
import com.Harbinger.Spore.Sentities.Variants.InfPillagerSkins;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class InfectedPillager extends Infected implements CrossbowAttackMob, EvolvingInfected, ArmedInfected, VariantKeeper {
   private static final EntityDataAccessor IS_CHARGING_CROSSBOW;
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;

   public InfectedPillager(EntityType type, Level level) {
      super(type, level);
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem p_33280_) {
      return p_33280_ == Items.CROSSBOW;
   }

   public boolean isChargingCrossbow() {
      return (Boolean)this.entityData.get(IS_CHARGING_CROSSBOW);
   }

   public void setChargingCrossbow(boolean p_33302_) {
      this.entityData.set(IS_CHARGING_CROSSBOW, p_33302_);
   }

   public void shootCrossbowProjectile(LivingEntity entity, ItemStack itemStack, Projectile projectile, float f) {
      if (projectile instanceof Arrow) {
         ((Arrow)projectile).addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 120));
      }

      this.shootCrossbowProjectile(this, entity, projectile, f, (float)((Double)SConfig.SERVER.inf_pil_range_damage.get() * (double)1.0F));
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_CHARGING_CROSSBOW, false);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_pillager_loot.get();
   }

   public boolean startRiding(Entity entity) {
      if (!this.getMainHandItem().equals(new ItemStack(Items.CROSSBOW))) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
      }

      return super.startRiding(entity);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RangedCrossbowAttackGoal(this, (double)1.0F, 8.0F));
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.4, true) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_pil_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_pil_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_pil_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public void onCrossbowAttackPerformed() {
      this.noActionTime = 0;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      if (Math.random() < (double)0.5F) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
      } else {
         this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

   }

   public void performRangedAttack(LivingEntity target, float distanceFactor) {
      ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, (item) -> item instanceof BowItem)));
      AbstractArrow abstractarrow = this.getArrow(itemstack, distanceFactor);
      if (this.getMainHandItem().getItem() instanceof BowItem) {
         abstractarrow = ((BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
      }

      double d0 = target.getX() - this.getX();
      double d1 = target.getY(0.3333333333333333) - abstractarrow.getY();
      double d2 = target.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      if (abstractarrow instanceof Arrow arrow) {
         arrow.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600));
      }

      abstractarrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
      this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(abstractarrow);
   }

   protected AbstractArrow getArrow(ItemStack p_32156_, float p_32157_) {
      return ProjectileUtil.getMobArrow(this, p_32156_, p_32157_);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      RandomSource randomsource = p_33282_.getRandom();
      this.populateDefaultEquipmentSlots(randomsource, p_33283_);
      this.populateDefaultEquipmentEnchantments(randomsource, p_33283_);
      this.setVariant(Math.random() < 0.2 ? InfPillagerSkins.CAPTAIN : InfPillagerSkins.DEFAULT);
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_PILLAGER_AMBIENT.get();
   }

   protected SoundEvent getHurtSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound() {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public void baseTick() {
      super.baseTick();
      this.tickEvolution(this, (List)SConfig.SERVER.pil_ev.get(), ScamperVariants.VILLAGER);
   }

   public String origin() {
      return "minecraft:pillager";
   }

   public void Evolve(Infected livingEntity, List value, ScamperVariants variants) {
      if (this.getLinked() && this.getVariant() == InfPillagerSkins.CAPTAIN) {
         Vanguard vanguard = new Vanguard((EntityType)Sentities.VANGUARD.get(), this.level());
         vanguard.setKills(this.getKills() + this.getEvoPoints());
         vanguard.setCustomName(this.getCustomName());
         vanguard.moveTo(this.getX(), this.getY(), this.getZ());
         Level var6 = this.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var6;
            DifficultyInstance instance = livingEntity.level().getCurrentDifficultyAt(new BlockPos((int)livingEntity.getX(), (int)livingEntity.getY(), (int)livingEntity.getZ()));
            vanguard.finalizeSpawn(serverLevel, instance, MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
            double x0 = livingEntity.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
            double y0 = livingEntity.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
            double z0 = livingEntity.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }

         this.level().addFreshEntity(vanguard);
         this.discard();
      } else {
         EvolvingInfected.super.Evolve(livingEntity, value, variants);
      }

   }

   private void setVariant(InfPillagerSkins variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public InfPillagerSkins getVariant() {
      return InfPillagerSkins.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= InfPillagerSkins.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return InfPillagerSkins.values().length;
   }

   static {
      IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(InfectedPillager.class, EntityDataSerializers.BOOLEAN);
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(InfectedPillager.class, EntityDataSerializers.INT);
   }
}
