package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class Brot extends Hyper {
   private static final EntityDataAccessor DATA_SWELL_DIR;
   private int swell;
   private final int maxSwell = 40;
   private boolean leapt = false;

   public Brot(EntityType type, Level level) {
      super(type, level);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.brot_loot.get();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SWELL_DIR, -1);
   }

   public void addAdditionalSaveData(CompoundTag data) {
      super.addAdditionalSaveData(data);
      Objects.requireNonNull(this);
      data.putShort("Fuse", (short)40);
   }

   public int getSwellDir() {
      return (Integer)this.entityData.get(DATA_SWELL_DIR);
   }

   public void setSwellDir(int data) {
      this.entityData.set(DATA_SWELL_DIR, data);
   }

   public void setLeapt(boolean value) {
      this.leapt = value;
   }

   public boolean getLeapt() {
      return this.leapt;
   }

   private void chemAttack(LivingEntity pLivingEntity) {
      AABB boundingBox = pLivingEntity.getBoundingBox().inflate((double)12.0F);

      for(Entity entity : pLivingEntity.level().getEntities(pLivingEntity, boundingBox)) {
         if (entity instanceof LivingEntity livingEntity) {
            if (!Utilities.helmetList().contains(livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem()) && this.TARGET_SELECTOR.test(livingEntity)) {
               for(String str : (List<String>)SConfig.SERVER.brot_effects.get()) {
                  String[] string = str.split("\\|");
                  MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string[0]));
                  if (effect != null && !livingEntity.hasEffect(effect)) {
                     livingEntity.addEffect(new MobEffectInstance(effect, Integer.parseUnsignedInt(string[1]), Integer.parseUnsignedInt(string[2])));
                  }
               }
            }
         }
      }

   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new BrotLeapGoal(this));
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 5.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.brot_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.brot_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.brot_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void tick() {
      if (this.isAlive()) {
         if (this.tickCount % 20 == 0 && this.getLeapt() && this.onGround()) {
            this.setLeapt(false);
         }

         int i = this.getSwellDir();
         if (i > 0 && this.swell == 0) {
            this.playSound((SoundEvent)Ssounds.BRAIOMIL_ATTACK.get(), 1.0F, 0.5F);
         }

         this.swell += i;
         if (this.swell < 0) {
            this.swell = 0;
         }

         int var10000 = this.swell;
         Objects.requireNonNull(this);
         if (var10000 >= 40) {
            Objects.requireNonNull(this);
            this.swell = 40;
            this.setSwellDir(-1);
            this.chemAttack(this);
         }

         if (this.tickCount % 20 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceToSqr(target) < (double)120.0F) {
               this.setSwellDir(1);
            }
         }
      }

      super.tick();
   }

   public void aiStep() {
      super.aiStep();
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      Level world = this.level();
      if (this.swell >= 25) {
         for(int i = 0; i < 360; ++i) {
            if (i % 20 == 0) {
               world.addParticle(ParticleTypes.SMOKE, x, y + (double)1.0F, z, Math.cos((double)i) * 0.15, Math.sin((double)i) * Math.cos((double)i) * 0.15, Math.sin((double)i) * 0.15);
            }
         }
      }

   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.BROT_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   static {
      DATA_SWELL_DIR = SynchedEntityData.defineId(Brot.class, EntityDataSerializers.INT);
   }

   public static class BrotLeapGoal extends Goal {
      private final Brot mob;
      private LivingEntity target;

      public BrotLeapGoal(Brot value) {
         this.mob = value;
         this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
      }

      public boolean canUse() {
         if (this.mob.isVehicle()) {
            return false;
         } else {
            this.target = this.mob.getTarget();
            if (this.target == null) {
               return false;
            } else {
               double d0 = this.mob.distanceToSqr(this.target);
               if (!(d0 < (double)16.0F) && !(d0 > (double)48.0F)) {
                  if (!this.mob.onGround()) {
                     return false;
                  } else {
                     return this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
                  }
               } else {
                  return false;
               }
            }
         }
      }

      public boolean canContinueToUse() {
         return !this.mob.onGround();
      }

      public void start() {
         Vec3 vec3 = this.mob.getDeltaMovement();
         Vec3 vec31 = new Vec3(this.target.getX() - this.mob.getX(), (double)0.0F, this.target.getZ() - this.mob.getZ());
         if (vec31.lengthSqr() > 1.0E-7) {
            vec31 = vec31.normalize().scale(0.8).add(vec3.scale(0.3));
         }

         this.mob.setDeltaMovement(vec31.x, 0.6, vec31.z);
         this.mob.setLeapt(true);
      }
   }
}
