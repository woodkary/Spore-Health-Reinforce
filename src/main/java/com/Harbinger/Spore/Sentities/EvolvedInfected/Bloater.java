package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.AI.ReturnToWater;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.MovementControls.WaterXlandMovement;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

public class Bloater extends EvolvedInfected implements WaterInfected {
   public static final EntityDataAccessor TUMORS;
   private int explosionTicks = 0;

   public Bloater(EntityType type, Level level) {
      super(type, level);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.moveControl = new WaterXlandMovement(this);
      this.navigation = new HybridPathNavigation(this, this.level());
   }

   public void travel(Vec3 p_32858_) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(p_32858_);
      }

   }

   public float getStepHeight() {
      return this.isInFluidType() ? 2.0F : 1.0F;
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.bloater_melee_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.braio_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.bloater_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, (double)28.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public int getAmountOfTumors() {
      return (Integer)this.entityData.get(TUMORS);
   }

   public void setAmountOfTumors(int value) {
      this.entityData.set(TUMORS, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TUMORS, 4);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setAmountOfTumors(tag.getInt("tumors"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("tumors", this.getAmountOfTumors());
   }

   public void tick() {
      super.tick();
      if (this.explosionTicks > 0) {
         this.tickExplosion();
      }

      if (this.tickCount % 400 == 0 && this.getAmountOfTumors() < 4) {
         this.setAmountOfTumors(this.getAmountOfTumors() + 1);
      }

   }

   public void tickExplosion() {
      if (this.explosionTicks == 1) {
         this.playSound(SoundEvents.CREEPER_PRIMED);
      }

      ++this.explosionTicks;
      if (this.explosionTicks == 50) {
         this.explodeTumor();
         this.explosionTicks = 0;
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.bloater_loot.get();
   }

   public boolean doHurtTarget(Entity entity) {
      if (Math.random() < (double)0.5F && this.getAmountOfTumors() > 0) {
         this.tickExplosion();
      }

      if (entity.isInFluidType()) {
         entity.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, (double)-1.0F, (double)0.0F));
      }

      return super.doHurtTarget(entity);
   }

   public void explodeTumor() {
      AABB aabb = this.getBoundingBox().inflate((double)8.0F);

      for(Entity entity : this.level().getEntities(this, aabb, (entityx) -> {
         boolean var10000;
         if (entityx instanceof LivingEntity living) {
            if (this.TARGET_SELECTOR.test(living)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      })) {
         if (entity instanceof LivingEntity living) {
            if (!Utilities.helmetList().contains(living.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
               living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 2));
               living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            }
         }
      }

      this.playSound(SoundEvents.GENERIC_EXPLODE);
      this.setAmountOfTumors(this.getAmountOfTumors() - 1);
      Level level = this.level();
      if (level instanceof ServerLevel serverLevel) {
         double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
         double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)0.25F * (double)5.0F;
         double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
         serverLevel.sendParticles((SimpleParticleType)Sparticles.SPORE_PARTICLE.get(), x0, y0, z0, 12, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
      }

   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.3, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)(2.0F + entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(4, new ReturnToWater(this, 1.2));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.DROWNED_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.DROWNED_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   static {
      TUMORS = SynchedEntityData.defineId(Bloater.class, EntityDataSerializers.INT);
   }
}
