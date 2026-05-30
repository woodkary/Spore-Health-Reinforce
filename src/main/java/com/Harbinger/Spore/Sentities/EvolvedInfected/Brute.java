package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Carrier;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.TransportInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Hyper.Ogre;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBlockProjectile;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Brute extends EvolvedInfected implements Carrier, RangedAttackMob, EvolvingInfected {
   private static final EntityDataAccessor DATA_CARRY_STATE;

   public Brute(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_brute_loot.get();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new TransportInfected(this, Mob.class, 1.1, (entity) -> ((List)SConfig.SERVER.ranged.get()).contains(entity.getEncodeId())));
      this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.3, 80, 48.0F) {
         public boolean canUse() {
            return super.canUse() && Brute.this.getCarriedBlock() != null && Brute.this.getTarget() != null && Brute.this.distanceToSqr(Brute.this.getTarget()) > (double)200.0F;
         }
      });
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.3, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public void setCarriedBlock(@Nullable BlockState state) {
      this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(state));
   }

   @Nullable
   public BlockState getCarriedBlock() {
      return (BlockState)((Optional)this.entityData.get(DATA_CARRY_STATE)).orElse((BlockState)null);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         tag.put("carriedBlockState", NbtUtils.writeBlockState(blockstate));
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      BlockState blockstate = null;
      if (tag.contains("carriedBlockState", 10)) {
         blockstate = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("carriedBlockState"));
         if (blockstate.isAir()) {
            blockstate = null;
         }
      }

      this.setCarriedBlock(blockstate);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CARRY_STATE, Optional.empty());
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (DATA_CARRY_STATE.equals(dataAccessor)) {
         if (this.getCarriedBlock() != null) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.getCarriedBlock().getBlock().asItem()));
         } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         }
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.brute_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.brute_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.brute_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void tick() {
      if (this.isAlive() && this.getTarget() != null && this.getRandom().nextInt(0, 6) == 3 && this.checkForInfected(this) && this.switchy()) {
         this.performRangedAttack(this);
      }

      super.tick();
      if (this.getCarriedBlock() == null && this.tickCount % 80 == 0 && !this.level().isClientSide()) {
         this.setCarriedBlock(this.blocky());
      }

      this.tickHyperEvolution(this);
   }

   private boolean switchy() {
      if (this.getTarget() == null) {
         return false;
      } else {
         double ze = this.distanceToSqr(this.getTarget());
         return ze > (double)60.0F && ze < (double)400.0F;
      }
   }

   boolean checkForInfected(Entity entity) {
      AABB boundingBox = entity.getBoundingBox().inflate(1.2);

      for(Entity en : entity.level().getEntities(entity, boundingBox)) {
         if (((List)SConfig.SERVER.can_be_carried.get()).contains(en.getEncodeId())) {
            return true;
         }
      }

      return false;
   }

   public void performRangedAttack(LivingEntity entity) {
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getTarget().getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getTarget().getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      AABB boundingBox = entity.getBoundingBox().inflate(1.2);

      for(Entity en : entity.level().getEntities(entity, boundingBox)) {
         if (en instanceof Mob && ((List)SConfig.SERVER.can_be_carried.get()).contains(en.getEncodeId())) {
            en.setDeltaMovement(d0 * -0.2, (d1 + d3) * 0.02, d2 * -0.2);
            ((Mob)en).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
         }
      }

   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_PILLAGER_AMBIENT.get();
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

   public void performRangedAttack(LivingEntity livingEntity, float p_33318_) {
      if (!this.level().isClientSide && this.getCarriedBlock() != null) {
         ThrownBlockProjectile thrownBlockProjectile = new ThrownBlockProjectile(this.level(), this, 10.0F, this.getCarriedBlock(), this.TARGET_SELECTOR);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.0F;
         double dz = livingEntity.getZ() - this.getZ();
         thrownBlockProjectile.moveTo(this.getX(), this.getY() + (double)1.5F, this.getZ());
         thrownBlockProjectile.shoot(dx, dy - thrownBlockProjectile.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
         this.level().addFreshEntity(thrownBlockProjectile);
         this.setCarriedBlock((BlockState)null);
      }

   }

   public BlockState blocky() {
      AABB aabb = this.getBoundingBox().inflate(0.2);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.getDestroySpeed(this.level(), blockpos) < 5.0F && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F) {
            this.level().destroyBlock(blockpos, false);
            return blockstate;
         }
      }

      return null;
   }

   public void HyperEvolve(LivingEntity living) {
      Ogre ogre = new Ogre((EntityType)Sentities.OGRE.get(), this.level());

      for(MobEffectInstance mobeffectinstance : this.getActiveEffects()) {
         ogre.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      ogre.setKills(this.getKills());
      ogre.setEvoPoints(this.getEvoPoints() - (Integer)SConfig.SERVER.min_kills_hyper.get());
      ogre.setCustomName(this.getCustomName());
      ogre.setPos(this.getX(), this.getY(), this.getZ());
      Level var7 = this.level();
      if (var7 instanceof ServerLevel serverLevel) {
         ogre.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      }

      this.level().addFreshEntity(ogre);
      this.discard();
      EvolvingInfected.super.HyperEvolve(living);
   }

   static {
      DATA_CARRY_STATE = SynchedEntityData.defineId(Brute.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
   }
}
