package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Conductor extends EvolvedInfected {
   private int attackAnimationTick;
   protected List<AmbientSparks> sparks = new ArrayList<>();
   private static final EntityDataAccessor CHARGE;
   private static final EntityDataAccessor DATA_ID;
   private int beamTicks;

   public Conductor(EntityType p_33002_, Level p_33003_) {
      super(p_33002_, p_33003_);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public List<AmbientSparks> getSparks() {
      return this.sparks;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.conductor_loot.get();
   }

   public float getCharge() {
      return (Float)this.entityData.get(CHARGE);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.conductor_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.conductor_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.conductor_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      }

      if (value == 7) {
         this.beamTicks = 20;
      } else {
         super.handleEntityEvent(value);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CHARGE, 0.0F);
      this.entityData.define(DATA_ID, -1);
   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();
      if (this.isAggressive()) {
         if (this.getCharge() <= 20.0F) {
            this.entityData.set(CHARGE, (Float)this.entityData.get(CHARGE) + 0.1F);
         }
      } else if (this.getCharge() > 0.0F) {
         this.entityData.set(CHARGE, (Float)this.entityData.get(CHARGE) - 0.1F);
      }

      if (this.tickCount % 5 == 0 && this.getCharge() > 0.0F) {
         float range = Math.abs(this.getCharge() * 0.15F);
         List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate((double)(1.0F + range)));
         int charge = (int)range * 3;
         this.playSound((SoundEvent)Ssounds.ELECTRIC.get());

         for(int i = 0; i < this.random.nextInt(3 + charge); ++i) {
            Vec3 vec3 = Utilities.generatePositionAway(this.position().add((double)0.0F, 2.2, (double)0.0F), (double)(1.0F + range));
            Entity entity = nearbyEntities.isEmpty() ? null : (Entity)nearbyEntities.get(this.random.nextInt(nearbyEntities.size()));
            LivingEntity living = null;
            if (entity instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity;
               if (Utilities.TARGET_SELECTOR.Test(livingEntity) && this.hasLineOfSight(livingEntity)) {
                  living = livingEntity;
               }
            }

            if (this.level().isClientSide) {
               AmbientSparks ambientSparks = new AmbientSparks(vec3, living, this, this.random.nextInt(5, 10));
               this.sparks.add(ambientSparks);
            } else if (living != null) {
               this.entityData.set(CHARGE, (Float)this.entityData.get(CHARGE) - 1.0F);
               this.playSound((SoundEvent)Ssounds.ELECTRIC_SPARK.get());
               living.hurt(this.level().damageSources().lightningBolt(), (float)((Double)SConfig.SERVER.conductor_el_small_damage.get() * (Double)SConfig.SERVER.global_damage.get()));
               if (Math.random() < 0.2) {
                  living.setRemainingFireTicks(40);
               }
            }
         }
      }

      if (this.level().isClientSide && !this.sparks.isEmpty()) {
         Iterator<AmbientSparks> it = this.sparks.iterator();

         while(it.hasNext()) {
            AmbientSparks spark = (AmbientSparks)it.next();
            spark.TickSpark();
            if (spark.life > spark.maxLife) {
               it.remove();
            }
         }
      }

      if (!this.level().isClientSide && this.beamTicks <= 0 && this.getCharge() >= 10.0F) {
         LivingEntity target = this.getTarget();
         if (target != null && this.hasLineOfSight(target)) {
            this.setAttackId(target.getId());
            this.beamTicks = 20;
            this.level().broadcastEntityEvent(this, (byte)7);
            int voltageModifier = target instanceof IronGolem ? 3 : 1;
            target.hurt(this.level().damageSources().lightningBolt(), (float)((Double)SConfig.SERVER.conductor_el_discharge_damage.get() * (Double)SConfig.SERVER.global_damage.get() * (double)voltageModifier));
            target.setRemainingFireTicks(100);
            this.entityData.set(CHARGE, this.getCharge() - 10.0F);
            this.playSound((SoundEvent)Ssounds.ELECTRIC_DISCHARGE.get());
         }
      }

   }

   public int getAttackedId() {
      return (Integer)this.entityData.get(DATA_ID);
   }

   public void setAttackId(int i) {
      this.entityData.set(DATA_ID, i);
   }

   public int getBeamTicks() {
      return this.beamTicks;
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.beamTicks > 0) {
         --this.beamTicks;
         if (this.beamTicks <= 1) {
            this.setAttackId(0);
         }
      }

   }

   public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
      return effectInstance.getEffect().isBeneficial() ? super.addEffect(effectInstance, entity) : false;
   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.SCIENTIST_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   static {
      CHARGE = SynchedEntityData.defineId(Conductor.class, EntityDataSerializers.FLOAT);
      DATA_ID = SynchedEntityData.defineId(Conductor.class, EntityDataSerializers.INT);
   }

   public static class AmbientSparks {
      public int life;
      public final Vec3 Connection;
      public final @Nullable Entity living;
      public final Conductor owner;
      public final int maxLife;
      protected List<Vec3> connections = new ArrayList<>();
      protected final RandomSource randomSource = RandomSource.create();

      public AmbientSparks(Vec3 connection, @Nullable Entity living, Conductor owner, int maxLife) {
         this.Connection = connection;
         this.living = living;
         this.owner = owner;
         this.maxLife = maxLife;
      }

      private void rebuildChain(Vec3 start, Vec3 end) {
         double distance = start.distanceTo(end);
         int desiredSegments = Mth.clamp((int)(distance * (double)2.0F), 4, 40);
         if (desiredSegments != this.connections.size()) {
            this.connections.clear();

            for(int i = 0; i < desiredSegments; ++i) {
               double t = (double)i / (double)(desiredSegments - 1);
               this.connections.add(new Vec3(Mth.lerp(t, start.x, end.x), Mth.lerp(t, start.y, end.y), Mth.lerp(t, start.z, end.z)));
            }

         }
      }

      public List<Vec3> getConnections() {
         return this.connections;
      }

      public void TickSpark() {
         ++this.life;
         Vec3 vec3 = this.living == null ? this.Connection : this.living.position().add((double)0.0F, (double)(this.living.getBbHeight() / 2.0F), (double)0.0F);
         double rng = (double)(this.owner.random.nextFloat() - this.owner.random.nextFloat()) * 0.05;
         Vec3 basePosition = this.owner.position().add(rng, 2.2, rng);
         if (this.connections.isEmpty()) {
            this.rebuildChain(basePosition, vec3);
         }

         this.applyIK(this.connections, basePosition, vec3);
      }

      protected void moveSegmentTowards(List<Vec3> entities, int index, Vec3 target) {
         double random = (double)(this.randomSource.nextFloat() - this.randomSource.nextFloat()) * 0.1;
         entities.set(index, target.add(random, random, random));
      }

      public void applyIK(List<Vec3> entities, Vec3 basePos, Vec3 camera) {
         if (entities != null && entities.size() >= 3) {
            this.moveSegmentTowards(entities, entities.size() - 1, camera);

            for(int i = entities.size() - 2; i >= 0; --i) {
               Vec3 nextPos = entities.get(i + 1);
               Vec3 dir = entities.get(i).subtract(nextPos);
               float segmentLength = 1.0F;
               if (dir.lengthSqr() > (double)1.0E-4F) {
                  dir = dir.normalize().scale((double)segmentLength);
               } else {
                  dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
               }

               Vec3 solvedPos = nextPos.add(dir);
               this.moveSegmentTowards(entities, i, solvedPos);
            }

            entities.set(0, basePos);

            for(int i = 1; i < entities.size(); ++i) {
               Vec3 prevPos = entities.get(i - 1);
               Vec3 dir = entities.get(i).subtract(prevPos);
               float segmentLength = 1.0F;
               if (dir.lengthSqr() > (double)1.0E-4F) {
                  dir = dir.normalize().scale((double)segmentLength);
               } else {
                  dir = new Vec3((double)segmentLength, (double)0.0F, (double)0.0F);
               }

               Vec3 solvedPos = prevPos.add(dir);
               this.moveSegmentTowards(entities, i, solvedPos);
            }

         }
      }
   }
}
