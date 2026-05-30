package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Wendigo extends Hyper {
   private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
   private static final UUID SPEED_MODIFIER_STALKING_UUID = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890");
   private static final AttributeModifier SPEED_MODIFIER_STALKING;
   private static final AttributeModifier SPEED_MODIFIER_ATTACKING;
   public static final EntityDataAccessor IS_STALKING;
   public static final EntityDataAccessor STALKING_TIMEOUT;
   public static final EntityDataAccessor CAMO;
   public static final EntityDataAccessor IS_SPRINTING;

   public Wendigo(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.setMaxUpStep(1.0F);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("stalking", this.isStalking());
      tag.putInt("camo", this.getCamo());
      tag.putInt("stalking_timeout", this.getStalkingTimeout());
      tag.putInt("sprinting", this.getIsSprinting());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setIsStalking(tag.getBoolean("stalking"));
      this.setCamo(tag.getInt("camo"));
      this.setStalkingTimeout(tag.getInt("stalking_timeout"));
      this.setIsSprinting(tag.getInt("sprinting"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_STALKING, false);
      this.entityData.define(STALKING_TIMEOUT, 0);
      this.entityData.define(CAMO, 0);
      this.entityData.define(IS_SPRINTING, 0);
   }

   public void setIsStalking(boolean value) {
      this.entityData.set(IS_STALKING, value);
   }

   public boolean isStalking() {
      return (Boolean)this.entityData.get(IS_STALKING);
   }

   public void setCamo(int value) {
      this.entityData.set(CAMO, value);
   }

   public int getCamo() {
      return (Integer)this.entityData.get(CAMO);
   }

   public void setStalkingTimeout(int value) {
      this.entityData.set(STALKING_TIMEOUT, value);
   }

   public int getStalkingTimeout() {
      return (Integer)this.entityData.get(STALKING_TIMEOUT);
   }

   public void setIsSprinting(int value) {
      this.entityData.set(IS_SPRINTING, value);
   }

   public int getIsSprinting() {
      return (Integer)this.entityData.get(IS_SPRINTING);
   }

   public boolean isCrouching() {
      return this.isStalking();
   }

   public double getJumpRange() {
      return (double)300.0F;
   }

   boolean isLookingAtMe(LivingEntity entity) {
      Vec3 vec3 = entity.getViewVector(1.0F).normalize();
      Vec3 vec31 = new Vec3(this.getX() - entity.getX(), this.getEyeY() - entity.getEyeY(), this.getZ() - entity.getZ());
      double d0 = vec31.length();
      vec31 = vec31.normalize();
      double d1 = vec3.dot(vec31);
      return d1 > (double)1.0F - 0.025 / d0 && entity.hasLineOfSight(this);
   }

   boolean canStartStalking(LivingEntity livingEntity) {
      if (this.getStalkingTimeout() > 0) {
         return false;
      } else if (livingEntity.distanceToSqr(this) < this.getJumpRange()) {
         return false;
      } else if (this.isLookingAtMe(livingEntity)) {
         this.setStalkingTimeout(20);
         return false;
      } else {
         return true;
      }
   }

   public void setTarget(@Nullable LivingEntity livingEntity) {
      super.setTarget(livingEntity);
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance != null) {
         if (livingEntity != null && this.canStartStalking(livingEntity)) {
            if (!attributeinstance.hasModifier(SPEED_MODIFIER_STALKING)) {
               attributeinstance.addTransientModifier(SPEED_MODIFIER_STALKING);
            }
         } else {
            attributeinstance.removeModifier(SPEED_MODIFIER_STALKING);
         }

         this.setIsStalking(attributeinstance.hasModifier(SPEED_MODIFIER_STALKING));
      }

   }

   public List LureList() {
      List<SoundEvent> values = new ArrayList();
      values.add(SoundEvents.VILLAGER_AMBIENT);
      values.add(SoundEvents.WANDERING_TRADER_AMBIENT);
      values.add(SoundEvents.ENDERMAN_AMBIENT);
      values.add(SoundEvents.ENDERMAN_SCREAM);
      values.add(SoundEvents.PILLAGER_AMBIENT);
      return values;
   }

   public void playSoundsNearTarget(Entity target) {
      SoundEvent soundEvent = (SoundEvent)this.LureList().get(this.random.nextInt(this.LureList().size()));
      BlockPos pos = target.getOnPos();
      target.level().playSound((Player)null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), soundEvent, target.getSoundSource(), 1.0F, 1.0F);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.wendigo_loot.get();
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public void aiStep() {
      super.aiStep();
      AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (attributeinstance != null) {
         if (!this.isStalking() && this.getIsSprinting() > 0) {
            if (!attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
               attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
         } else {
            attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
         }
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.wendigo_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.wendigo_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.wendigo_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         this.undressTarget(livingEntity);
      }

      return super.doHurtTarget(entity);
   }

   private void undressTarget(LivingEntity livingEntity) {
      this.dropItem(livingEntity, livingEntity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
      this.dropItem(livingEntity, livingEntity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
      this.dropItem(livingEntity, livingEntity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
      this.dropItem(livingEntity, livingEntity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
   }

   private void dropItem(LivingEntity livingEntity, ItemStack stack, EquipmentSlot slot) {
      if (Math.random() < (double)0.02F && shouldDropItemsPerSlot(slot)) {
         if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.addItem(stack);
         } else {
            ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
            this.level().addFreshEntity(entity);
            livingEntity.setItemSlot(slot, ItemStack.EMPTY);
         }
      }

   }

   public static boolean shouldDropItemsPerSlot(EquipmentSlot slot) {
      return (Boolean)SConfig.SERVER.wendigo_disarmor.get();
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 200 == 0 && this.isStalking()) {
         Entity entity = this.getTarget();
         if (entity != null) {
            this.playSoundsNearTarget(entity);
         }
      }

      if (this.tickCount % 20 == 0) {
         if (this.getStalkingTimeout() > 0) {
            this.setStalkingTimeout(this.getStalkingTimeout() - 1);
         }

         if (this.getIsSprinting() > 0) {
            this.setIsSprinting(this.getIsSprinting() - 1);
         }
      }

   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() != null) {
         this.setStalkingTimeout(20);
      }

      return super.hurt(source, amount);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (IS_STALKING.equals(dataAccessor)) {
         if (this.getTarget() != null && this.getIsSprinting() <= 0) {
            this.setIsSprinting(10);
            this.playSound((SoundEvent)Ssounds.WENDIGO_SCREECH.get());
         }

         this.setCamo(this.isStalking() ? this.getBiomeTint() : 0);
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public int getBiomeTint() {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY());
      int k = Mth.floor(this.getZ());
      BlockPos blockpos = new BlockPos(i, j, k);
      Biome biome = (Biome)this.level().getBiome(blockpos).value();
      return biome.getFoliageColor();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WENDIGO_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isStalking() ? super.getDimensions(pose).scale(2.2F, 0.35F) : super.getDimensions(pose);
   }

   static {
      SPEED_MODIFIER_STALKING = new AttributeModifier(SPEED_MODIFIER_STALKING_UUID, "Crawling speed slowdown", (double)-0.15F, Operation.ADDITION);
      SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Sprinting speed", (double)0.15F, Operation.ADDITION);
      IS_STALKING = SynchedEntityData.defineId(Wendigo.class, EntityDataSerializers.BOOLEAN);
      STALKING_TIMEOUT = SynchedEntityData.defineId(Wendigo.class, EntityDataSerializers.INT);
      CAMO = SynchedEntityData.defineId(Wendigo.class, EntityDataSerializers.INT);
      IS_SPRINTING = SynchedEntityData.defineId(Wendigo.class, EntityDataSerializers.INT);
   }
}
