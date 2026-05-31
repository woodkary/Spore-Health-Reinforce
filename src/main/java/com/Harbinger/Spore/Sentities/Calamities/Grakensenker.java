package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkKrakenArm;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkKrakenLeg;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkVortexFunnel;
import com.Harbinger.Spore.Sentities.Projectile.HarpoonProjectile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class Grakensenker extends Calamity implements TrueCalamity, WaterInfected, RangedAttackMob {
   public static final EntityDataAccessor HEIGHT;
   public static final EntityDataAccessor WATER_TICKS;
   public static final EntityDataAccessor RIGHT_ARM_TIP;
   public static final EntityDataAccessor LEFT_ARM_TIP;
   public static final EntityDataAccessor RIGHT_ARM_ENTITY;
   public static final EntityDataAccessor LEFT_ARM_ENTITY;
   public static final EntityDataAccessor RIGHT_ARM_DELAY;
   public static final EntityDataAccessor LEFT_ARM_DELAY;
   public static final EntityDataAccessor VORTEX_VECTOR;
   public static final EntityDataAccessor VORTEX_TIMEOUT;
   public static final EntityDataAccessor WOOD;
   public static final EntityDataAccessor HOOK;
   public static final float MIN_HEIGHT = 0.0F;
   public static final float MAX_HEIGHT = 4.0F;
   private final IkKrakenLeg BackRightTentacle;
   private final IkKrakenLeg BackLeftTentacle;
   private final IkKrakenLeg MiddleRightTentacle;
   private final IkKrakenLeg MiddleLeftTentacle;
   private final IkKrakenLeg FrontRightTentacle;
   private final IkKrakenLeg FrontLeftTentacle;
   private final IkKrakenArm RightArmTentacle;
   private final IkKrakenArm LeftArmTentacle;
   private final IkVortexFunnel VortexFunnel;
   private final IkKrakenLeg[] TickTentacles;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart Body;
   public final CalamityMultipart Body2;
   public final CalamityMultipart RightHand;
   public final CalamityMultipart LeftHand;
   private static final Vector3f V0;
   private final List<HitboxesForParts> innatePartList;

   public Grakensenker(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.GRAKEN_FRONT_MAW, HitboxesForParts.GRAKEN_HINGE, HitboxesForParts.GRAKEN_BODY, HitboxesForParts.GRAKEN_BACK_MAW);
      this.Body = new CalamityMultipart(this, "body", 5.0F, 5.0F);
      this.Body2 = new CalamityMultipart(this, "body2", 3.0F, 3.0F);
      this.RightHand = new CalamityMultipart(this, "right", 1.5F, 1.5F);
      this.LeftHand = new CalamityMultipart(this, "left", 1.5F, 1.5F);
      this.BackRightTentacle = new IkKrakenLeg(this, 7, GrakenLegsModifiers.BACK_RIGHT_TENTACLE.bodySet, GrakenLegsModifiers.BACK_RIGHT_TENTACLE.offset, 4.0F, true);
      this.BackLeftTentacle = new IkKrakenLeg(this, 7, GrakenLegsModifiers.BACK_LEFT_TENTACLE.bodySet, GrakenLegsModifiers.BACK_LEFT_TENTACLE.offset, 4.0F, false);
      this.MiddleRightTentacle = new IkKrakenLeg(this, 7, GrakenLegsModifiers.MIDDLE_RIGHT_TENTACLE.bodySet, GrakenLegsModifiers.MIDDLE_RIGHT_TENTACLE.offset, 6.0F, false);
      this.MiddleLeftTentacle = new IkKrakenLeg(this, 7, GrakenLegsModifiers.MIDDLE_LEFT_TENTACLE.bodySet, GrakenLegsModifiers.MIDDLE_LEFT_TENTACLE.offset, 6.0F, true);
      this.FrontRightTentacle = new IkKrakenLeg(this, 10, GrakenLegsModifiers.FRONT_RIGHT_TENTACLE.bodySet, GrakenLegsModifiers.FRONT_RIGHT_TENTACLE.offset, 8.0F, true);
      this.FrontLeftTentacle = new IkKrakenLeg(this, 10, GrakenLegsModifiers.FRONT_LEFT_TENTACLE.bodySet, GrakenLegsModifiers.FRONT_LEFT_TENTACLE.offset, 8.0F, false);
      this.RightArmTentacle = new IkKrakenArm(this, 16, GrakenLegsModifiers.RIGHT_ARM.bodySet, GrakenLegsModifiers.RIGHT_ARM.offset, 4.0F, true);
      this.LeftArmTentacle = new IkKrakenArm(this, 16, GrakenLegsModifiers.LEFT_ARM.bodySet, GrakenLegsModifiers.LEFT_ARM.offset, 4.0F, false);
      this.VortexFunnel = new IkVortexFunnel(this);
      this.TickTentacles = new IkKrakenLeg[]{this.BackRightTentacle, this.BackLeftTentacle, this.MiddleRightTentacle, this.MiddleLeftTentacle, this.FrontRightTentacle, this.FrontLeftTentacle, this.RightArmTentacle, this.LeftArmTentacle, this.VortexFunnel};
      this.subEntities = new CalamityMultipart[]{this.Body, this.Body2, this.RightHand, this.LeftHand};
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
      this.navigation = new HybridPathNavigation(this, this.level());
      this.setMaxUpStep(1.5F);
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   public CalamityMultipart[] getSubEntities() {
      return this.subEntities;
   }

   public boolean isMultipartEntity() {
      return true;
   }

   public @Nullable PartEntity[] getParts() {
      return this.subEntities;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
      super.recreateFromPacket(p_218825_);
   }

   public String getMutation() {
      return this.getAdaptation() ? "spore.entity.variant.ship" : super.getMutation();
   }

   public IkKrakenLeg getBackRightTentacle() {
      return this.BackRightTentacle;
   }

   public IkKrakenLeg getBackLeftTentacle() {
      return this.BackLeftTentacle;
   }

   public IkKrakenLeg getMiddleRightTentacle() {
      return this.MiddleRightTentacle;
   }

   public IkKrakenLeg getMiddleLeftTentacle() {
      return this.MiddleLeftTentacle;
   }

   public IkKrakenLeg getFrontRightTentacle() {
      return this.FrontRightTentacle;
   }

   public IkKrakenLeg getFrontLeftTentacle() {
      return this.FrontLeftTentacle;
   }

   public IkKrakenLeg[] getTentacles() {
      return this.TickTentacles;
   }

   public IkKrakenArm getRightArmTentacle() {
      return this.RightArmTentacle;
   }

   public IkKrakenArm getLeftArmTentacle() {
      return this.LeftArmTentacle;
   }

   public IkVortexFunnel getVortexFunnel() {
      return this.VortexFunnel;
   }

   public void performRangedAttack(LivingEntity target, float v) {
      float yawRad = this.getYRot() * ((float)Math.PI / 180F);
      float spinRad = (float)this.getWaterTicks() * 0.05F;
      Vec3 offset = new Vec3((double)5.5F, (double)(4.0F + this.getExtendedHeight()), (double)1.0F);
      Vec3 pos = this.position().add(offset.yRot(-yawRad - ((float)Math.PI / 2F) + spinRad));
      HarpoonProjectile projectile = new HarpoonProjectile(this.level(), this, (float)((Double)SConfig.SERVER.graken_damage.get() * (double)0.5F));
      projectile.moveTo(pos.x, pos.y, pos.z);
      Vec3 look = this.getViewVector(1.0F);
      projectile.shoot(look.x, look.y, look.z, 3.0F, 0.0F);
      this.level().addFreshEntity(projectile);
      this.shootHook(false);
      this.playSound(SoundEvents.DISPENSER_LAUNCH);
   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9).add((double)0.0F, -0.1, (double)0.0F));
      } else {
         super.travel(vec);
      }

   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if (calamityMultipart == this.RightHand) {
         this.entityData.set(RIGHT_ARM_DELAY, 100);
      }

      if (calamityMultipart == this.LeftHand) {
         this.entityData.set(LEFT_ARM_DELAY, 100);
      }

      value = calamityMultipart == this.Body ? value * 3.0F : value;
      if(calamityMultipart == this.Body) {
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value * 2.0f);
      }
      return this.hurt(source, value);
   }

   protected boolean canAddPassenger(Entity passenger) {
      return true;
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.graken_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.graken_debuffs.get();
   }

   public float getExtendedHeight() {
      return (Float)this.entityData.get(HEIGHT);
   }

   public void setHeight(float value) {
      this.entityData.set(HEIGHT, value);
   }

   public int getWaterTicks() {
      return (Integer)this.entityData.get(WATER_TICKS);
   }

   public void setWaterTicks(int value) {
      this.entityData.set(WATER_TICKS, value);
   }

   public boolean isInDeepWater() {
      return (Integer)this.entityData.get(WATER_TICKS) >= 40;
   }

   public Vector3f getRightArm() {
      return (Vector3f)this.entityData.get(RIGHT_ARM_TIP);
   }

   public Vector3f getLeftArm() {
      return (Vector3f)this.entityData.get(LEFT_ARM_TIP);
   }

   public void setRightArm(Vector3f vector3f) {
      this.entityData.set(RIGHT_ARM_TIP, vector3f);
   }

   public void setLeftArm(Vector3f vector3f) {
      this.entityData.set(LEFT_ARM_TIP, vector3f);
   }

   public int getRightArmDelay() {
      return (Integer)this.entityData.get(RIGHT_ARM_DELAY);
   }

   public int getLeftArmDelay() {
      return (Integer)this.entityData.get(LEFT_ARM_DELAY);
   }

   public BlockPos getVortexVector() {
      return (BlockPos)this.entityData.get(VORTEX_VECTOR);
   }

   public int getVortexTimeOut() {
      return (Integer)this.entityData.get(VORTEX_TIMEOUT);
   }

   public void setVortexVector(BlockPos vector3f) {
      this.entityData.set(VORTEX_VECTOR, vector3f);
   }

   public boolean hasVortex() {
      return this.entityData.get(VORTEX_VECTOR) != BlockPos.ZERO;
   }

   public void setVortexTimeout(int value) {
      this.entityData.set(VORTEX_TIMEOUT, value);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(HEIGHT, 0.0F);
      this.entityData.define(WATER_TICKS, 0);
      this.entityData.define(RIGHT_ARM_TIP, V0);
      this.entityData.define(LEFT_ARM_TIP, V0);
      this.entityData.define(RIGHT_ARM_ENTITY, -1);
      this.entityData.define(LEFT_ARM_ENTITY, -1);
      this.entityData.define(RIGHT_ARM_DELAY, 0);
      this.entityData.define(LEFT_ARM_DELAY, 0);
      this.entityData.define(VORTEX_VECTOR, BlockPos.ZERO);
      this.entityData.define(VORTEX_TIMEOUT, 0);
      this.entityData.define(WOOD, 0);
      this.entityData.define(HOOK, true);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("height", (Float)this.entityData.get(HEIGHT));
      tag.putInt("water", (Integer)this.entityData.get(WATER_TICKS));

      for(int e = 0; e < this.TickTentacles.length; ++e) {
         this.TickTentacles[e].writeVariants(tag, e);
      }

      tag.putInt("VX", this.getVortexVector().getX());
      tag.putInt("VY", this.getVortexVector().getY());
      tag.putInt("VZ", this.getVortexVector().getZ());
      tag.putInt("timeOut", this.getVortexTimeOut());
      tag.putInt("wood", (Integer)this.entityData.get(WOOD));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(HEIGHT, tag.getFloat("height"));
      this.entityData.set(WATER_TICKS, tag.getInt("water"));

      for(int e = 0; e < this.TickTentacles.length; ++e) {
         this.TickTentacles[e].readVariants(tag, e);
      }

      int x = tag.getInt("VX");
      int y = tag.getInt("VY");
      int z = tag.getInt("VZ");
      this.setVortexVector(new BlockPos(x, y, z));
      this.setVortexTimeout(tag.getInt("timeOut"));
      this.entityData.set(WOOD, tag.getInt("wood"));
   }

   public boolean shotHook() {
      return (Boolean)this.entityData.get(HOOK);
   }

   public void shootHook(boolean val) {
      this.entityData.set(HOOK, val);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(1.0F, 1.0F + this.getExtendedHeight() * 0.5F);
   }

   public void applyYaw(CalamityMultipart part, Vec3 offset) {
      float yawRad = this.getYRot() * ((float)Math.PI / 180F);
      float spinRad = (float)this.getWaterTicks() * 0.05F;
      Vec3 pos = this.position().add(offset.yRot(-yawRad - ((float)Math.PI / 2F) + spinRad));
      part.setPos(pos.x, pos.y, pos.z);
   }

   public void aiStep() {
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.applyYaw(this.Body, new Vec3((double)-4.5F, (double)5.0F + (double)this.getExtendedHeight(), (double)0.0F));
      this.applyYaw(this.Body2, new Vec3((double)-2.5F, (double)2.0F + (double)this.getExtendedHeight(), (double)0.0F));
      this.RightHand.setPos((double)this.getRightArm().x, (double)this.getRightArm().y, (double)this.getRightArm().z);
      this.LeftHand.setPos((double)this.getLeftArm().x, (double)this.getLeftArm().y, (double)this.getLeftArm().z);

      for(int l = 0; l < this.subEntities.length; ++l) {
         this.subEntities[l].xo = avec3[l].x;
         this.subEntities[l].yo = avec3[l].y;
         this.subEntities[l].zo = avec3[l].z;
         this.subEntities[l].xOld = avec3[l].x;
         this.subEntities[l].yOld = avec3[l].y;
         this.subEntities[l].zOld = avec3[l].z;
      }

      super.aiStep();
   }

   public void setRightArmEntity(int id) {
      this.entityData.set(RIGHT_ARM_ENTITY, id);
   }

   public void setLeftArmEntity(int id) {
      this.entityData.set(LEFT_ARM_ENTITY, id);
   }

   private void validateArms() {
      if (!this.level().isClientSide()) {
         int rightId = (Integer)this.entityData.get(RIGHT_ARM_ENTITY);
         int leftId = (Integer)this.entityData.get(LEFT_ARM_ENTITY);
         if (rightId != -1) {
            label29: {
               Entity e = this.level().getEntity(rightId);
               if (e instanceof LivingEntity) {
                  LivingEntity le = (LivingEntity)e;
                  if (le.isAlive() && le.isPassengerOfSameVehicle(this)) {
                     break label29;
                  }
               }

               this.entityData.set(RIGHT_ARM_ENTITY, -1);
            }
         }

         if (leftId != -1) {
            Entity e = this.level().getEntity(leftId);
            if (e instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)e;
               if (le.isAlive() && le.isPassengerOfSameVehicle(this)) {
                  return;
               }
            }

            this.entityData.set(LEFT_ARM_ENTITY, -1);
         }

      }
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return this.onGround() ? SoundEvents.RAVAGER_STEP : SoundEvents.GENERIC_SWIM;
   }

   protected SoundEvent getAmbientSound() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)200.0F ? null : (SoundEvent)Ssounds.KRAKEN_GROWL.get();
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 1200 == 0 && this.getSearchArea() == BlockPos.ZERO && !this.isOcean(this.level().getBiome(this.getOnPos()))) {
         BlockPos pos = this.findOcean(this.level(), this.getOnPos());
         if (pos != null) {
            this.setSearchArea(pos);
         }
      }

      this.updateHeight();
      this.handleVortexBehavior();
      if (this.getVortexTimeOut() > 0) {
         this.setVortexTimeout(this.getVortexTimeOut() - 1);
      }

      for(IkKrakenLeg leg : this.TickTentacles) {
         leg.refreshLegStandingPoint();
         leg.applyIK();
      }

      if (this.tickCount % 10 == 0) {
         this.tryGrab(this.getRightArm(), true, this.getRightArmDelay() <= 0);
         this.tryGrab(this.getLeftArm(), false, this.getLeftArmDelay() <= 0);
      }

      if (this.tickCount % 20 == 0) {
         this.validateArms();
         if (this.isInDeepWater() && !this.hasVortex() && this.getVortexTimeOut() <= 0 && this.getTarget() == null && this.getSearchArea() == BlockPos.ZERO) {
            BlockPos vec3 = this.findVortexCenter(this.level());
            if (vec3 != null) {
               this.setVortexVector(vec3);
            }
         }
      }

      if (this.hasVortex()) {
         int range = 8;

         for(int i = 0; i <= 2 * range; ++i) {
            for(int k = 0; k <= 2 * range; ++k) {
               double distance = (double)Mth.sqrt((float)((i - range) * (i - range) + (k - range) * (k - range)));
               if ((Math.abs(i) != 2 || Math.abs(k) != 2) && distance < (double)range + (double)0.5F && Math.random() < 0.1) {
                  BlockPos vector3f = this.getVortexVector().offset(i - range, 0, k - range);
                  this.level().addParticle(ParticleTypes.BUBBLE, (double)vector3f.getX(), (double)(vector3f.getY() + 1), (double)vector3f.getZ(), (double)0.0F, 0.05, (double)0.0F);
               }
            }
         }

         this.applyVortexForces();
      }

      if (this.getRightArmDelay() > 0) {
         this.entityData.set(RIGHT_ARM_DELAY, this.getRightArmDelay() - 1);
      }

      if (this.getLeftArmDelay() > 0) {
         this.entityData.set(LEFT_ARM_DELAY, this.getLeftArmDelay() - 1);
      }

      if (this.isInWater()) {
         LivingEntity target = this.getTarget();
         Vec3 vec3 = target == null ? this.getDeltaMovement() : target.position();
         if (vec3.horizontalDistanceSqr() > (double)2.5E-7F) {
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            float pitch = (float)(Mth.atan2(dy, horizontal) * (180D / Math.PI));
            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yBodyRot = lerpRotation(this.yRotO, this.getYRot());
         }
      }

      if (this.tickCount % 600 == 0 && !this.shotHook()) {
         AABB aabb = this.getBoundingBox().inflate((double)8.0F);
         List<HarpoonProjectile> harpoons = this.level().getEntitiesOfClass(HarpoonProjectile.class, aabb);
         if (harpoons.isEmpty()) {
            this.shootHook(true);
         }
      }

      if (this.tickCount % 40 == 0 && this.shotHook() && this.getAdaptation() && !this.isInDeepWater()) {
         LivingEntity living = this.getTarget();
         if (living != null && living.hasLineOfSight(living) && !this.isVehicle()) {
            this.performRangedAttack(living, 0.0F);
         }
      }

   }

   protected static float lerpRotation(float currentRotation, float targetRotation) {
      while(targetRotation - currentRotation < -180.0F) {
         currentRotation -= 360.0F;
      }

      while(targetRotation - currentRotation >= 180.0F) {
         currentRotation += 360.0F;
      }

      return Mth.lerp(0.2F, currentRotation, targetRotation);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.getAdaptation()) {
         amount *= 0.8F;
      }

      this.setVortexTimeout(1200);
      this.setVortexVector(BlockPos.ZERO);
      return super.hurt(source, amount);
   }

   private void tryGrab(Vector3f handPos, boolean right, boolean canGrab) {
      boolean active = right ? this.isRightArmFull() : this.isLeftArmFull();
      if (!active || !canGrab) {
         AABB aabb = new AABB((double)handPos.x - (double)2.0F, (double)handPos.y - (double)2.0F, (double)handPos.z - (double)2.0F, (double)handPos.x + (double)2.0F, (double)handPos.y + (double)2.0F, (double)handPos.z + (double)2.0F);
         List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, aabb, (e) -> e.isAlive() && e != this && !e.isPassenger() && !e.isInvulnerable() && Utilities.TARGET_SELECTOR.Test(e) && TargetingConditions.forCombat().test(this, e));
         if (!targets.isEmpty()) {
            LivingEntity living = (LivingEntity)targets.get(0);
            if (right) {
               this.setRightArmEntity(living.getId());
            } else {
               this.setLeftArmEntity(living.getId());
            }

            living.startRiding(this);
         }
      }
   }

   public boolean isRightArmFull() {
      return (Integer)this.entityData.get(RIGHT_ARM_ENTITY) != -1;
   }

   public boolean isLeftArmFull() {
      return (Integer)this.entityData.get(LEFT_ARM_ENTITY) != -1;
   }

   public int getRightArmEntity() {
      return (Integer)this.entityData.get(RIGHT_ARM_ENTITY);
   }

   public int getLeftArmEntity() {
      return (Integer)this.entityData.get(LEFT_ARM_ENTITY);
   }

   protected void positionRider(Entity passenger, MoveFunction callback) {
      float tall = passenger.getBbHeight() / 2.0F;
      passenger.setPose(Pose.STANDING);
      if (passenger.getId() == (Integer)this.entityData.get(RIGHT_ARM_ENTITY)) {
         Vector3f pos = this.getRightArm();
         callback.accept(passenger, (double)pos.x, (double)(pos.y - tall), (double)pos.z);
         this.strangleVictim(passenger);
         if (passenger.distanceToSqr(this.position().add((double)0.0F, (double)this.getExtendedHeight(), (double)0.0F)) <= (double)4.0F) {
            this.setRightArmEntity(-1);
         }
      } else if (passenger.getId() == (Integer)this.entityData.get(LEFT_ARM_ENTITY)) {
         Vector3f pos = this.getLeftArm();
         callback.accept(passenger, (double)pos.x, (double)(pos.y - tall), (double)pos.z);
         this.strangleVictim(passenger);
         if (passenger.distanceToSqr(this.position().add((double)0.0F, (double)this.getExtendedHeight(), (double)0.0F)) <= (double)4.0F) {
            this.setLeftArmEntity(-1);
         }
      } else {
         callback.accept(passenger, this.getX(), this.getY() + (double)this.getExtendedHeight(), this.getZ());
         if (this.tickCount % 20 == 0 && passenger instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)passenger;
            this.doHurtTarget(living);
         }
      }

   }

   public void strangleVictim(Entity entity) {
      if (entity instanceof LivingEntity living) {
         int air = living.getAirSupply();
         --air;
         living.setAirSupply(air);
      }

   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.graken_dpsr.get();
   }

   protected void removePassenger(Entity passenger) {
      super.removePassenger(passenger);
      if (passenger.getId() == (Integer)this.entityData.get(RIGHT_ARM_ENTITY)) {
         this.entityData.set(RIGHT_ARM_ENTITY, -1);
      }

      if (passenger.getId() == (Integer)this.entityData.get(LEFT_ARM_ENTITY)) {
         this.entityData.set(LEFT_ARM_ENTITY, -1);
      }

   }

   public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      if (passenger.getId() == (Integer)this.entityData.get(RIGHT_ARM_ENTITY)) {
         return new Vec3(this.getRightArm());
      } else if (passenger.getId() == (Integer)this.entityData.get(LEFT_ARM_ENTITY)) {
         return new Vec3(this.getLeftArm());
      } else {
         this.entityData.set(RIGHT_ARM_DELAY, 100);
         this.entityData.set(LEFT_ARM_DELAY, 100);
         return super.getDismountLocationForPassenger(passenger);
      }
   }

   public void updateHeight() {
      if (!this.level().isClientSide) {
         float current = this.getExtendedHeight();
         boolean deep = this.inDeepWater(this.getOnPos());
         if (deep) {
            if (this.getWaterTicks() <= 180) {
               this.setWaterTicks(this.getWaterTicks() + 5);
            }
         } else if (this.getWaterTicks() > 0) {
            this.setWaterTicks(this.getWaterTicks() - 5);
         }

         boolean deepWater = this.isInDeepWater();
         double wantedY = this.moveControl.getWantedY() + (double)2.0F;
         boolean wantsLowStance = wantedY < this.getY() + (double)this.getBbHeight() && this.horizontalCollision;
         float target;
         if (!wantsLowStance && !deepWater) {
            target = current + 0.08F;
         } else {
            target = current - 0.05F;
         }

         target = Math.max(0.0F, Math.min(4.0F, target));
         if (Math.abs(target - current) > 0.01F) {
            this.setHeight(target);
         }

      }
   }

   protected boolean inDeepWater(BlockPos pos) {
      BlockPos firstPos = pos.offset(-3, 2, -3);
      BlockPos secondPos = pos.offset(3, 5, 3);
      return BlockPos.betweenClosedStream(firstPos, secondPos).allMatch(this::checkForFluid);
   }

   boolean checkForFluid(BlockPos pos) {
      BlockState state = this.level().getBlockState(pos);
      FluidState fluidstate = state.getFluidState();
      return state.getCollisionShape(this.level(), pos).isEmpty() && fluidstate.is(FluidTags.WATER);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.graken_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.graken_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.graken_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (HEIGHT.equals(dataAccessor)) {
         this.setMaxUpStep((float)((double)1.5F + (double)this.getExtendedHeight()));
         this.refreshDimensions();
      }

      if (SEARCH_AREA.equals(dataAccessor) && this.getSearchArea() != BlockPos.ZERO) {
         this.setVortexTimeout(1200);
         this.setVortexVector(BlockPos.ZERO);
      }

      if (WATER_TICKS.equals(dataAccessor) && this.hasVortex() && !this.isInDeepWater()) {
         this.setVortexTimeout(1200);
         this.setVortexVector(BlockPos.ZERO);
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.graken_loot.get();
   }

   public void registerGoals() {
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)1.5F, false, (double)2.5F, 6.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         public boolean canUse() {
            return !Grakensenker.this.hasVortex() && super.canUse();
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Grakensenker.this.getBbWidth();
            return (double)(f * 2.0F * f * 2.0F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.2));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      super.registerGoals();
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();

      for(HitboxesForParts hitboxes : this.innatePartList) {
         HitboxesForParts part = this.calculateChance(hitboxes, 0.85F);
         if (part != null) {
            values.add(part);
         }
      }

      return values;
   }

   public Vec3 applyYaw(Vec3 offset) {
      float yawRad = this.getYRot() * ((float)Math.PI / 180F);
      float spinRad = (float)this.getWaterTicks() * 0.05F;
      return offset.yRot(-yawRad - ((float)Math.PI / 2F) + spinRad);
   }

   public @Nullable BlockPos findVortexCenter(Level level) {
      if (!this.isInWater()) {
         return null;
      } else {
         Vec3 random = this.applyYaw(new Vec3((double)this.getRandom().nextInt(3, 7), (double)0.0F, (double)this.getRandom().nextInt(-5, 5)));
         Vec3 base = this.position().add(random);

         for(int dy = 0; dy <= 32; ++dy) {
            BlockPos pos = BlockPos.containing(base.x, base.y + (double)dy, base.z);
            BlockState water = level.getBlockState(pos);
            BlockState air = level.getBlockState(pos.above());
            if (water.is(Blocks.WATER) && air.isAir()) {
               if (dy <= 12) {
                  return null;
               }

               return pos;
            }
         }

         return null;
      }
   }

   public void handleVortexBehavior() {
      if (this.hasVortex()) {
         if (this.getVortexTimeOut() <= 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
            this.lookAtVortex(this.getVortexVector());
         }
      }
   }

   private void lookAtVortex(BlockPos target) {
      double dx = (double)target.getX() - this.getX();
      double dz = (double)target.getZ() - this.getZ();
      double dy = (double)target.getY() - this.getEyeY();
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
      float pitch = (float)(-(Math.atan2(dy, dist) * (180D / Math.PI)));
      this.setYRot(yaw);
      this.setXRot(pitch);
      this.yBodyRot = yaw;
      this.yHeadRot = yaw;
   }

   public void applyVortexForces() {
      if (!this.level().isClientSide) {
         Vec3[] funnelPoints = this.getVortexFunnel().getEntities();
         if (funnelPoints != null && funnelPoints.length >= 2) {
            Vec3 base = funnelPoints[0];

            for(int i = 0; i < funnelPoints.length; ++i) {
               Vec3 segmentPos = funnelPoints[i];
               double distanceFromBase = (double)i / (double)(funnelPoints.length - 1);
               double radius = (double)1.0F + distanceFromBase * (double)4.0F + (double)i / (double)4.0F;
               AABB area = getAabb(distanceFromBase, segmentPos, radius);

               for(Entity entity : this.level().getEntitiesOfClass(Entity.class, area, (e) -> {
                  boolean var10000;
                  label23: {
                     label22: {
                        if (e instanceof LivingEntity living) {
                           if (living != this && Utilities.TARGET_SELECTOR.Test(living) && TargetingConditions.forCombat().test(this, living)) {
                              break label22;
                           }
                        }

                        if (!(e instanceof Boat)) {
                           break label23;
                        }
                     }

                     if (e.isInWater()) {
                        var10000 = true;
                        return var10000;
                     }
                  }

                  var10000 = false;
                  return var10000;
               })) {
                  if (entity.isVehicle() && this.getVortexVector().distToCenterSqr(entity.position()) < (double)120.0F) {
                     entity.ejectPassengers();
                  }

                  this.applyVortexForceToEntity(entity, segmentPos, radius, i, funnelPoints.length, base);
               }
            }

         }
      }
   }

   protected void grief(AABB aabb) {
      boolean flag = false;

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.is(Utilities.biomass)) {
            flag = this.level().setBlock(blockpos, ((Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) || flag;
            this.breakCounter = 0;
         } else if (blockstate.getDestroySpeed(this.level(), blockpos) < (float)this.getDestroySpeed() && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            if (blockstate.is(BlockTags.PLANKS) || blockstate.is(BlockTags.LOGS) || blockstate.is(BlockTags.WOODEN_FENCES)) {
               this.entityData.set(WOOD, (Integer)this.entityData.get(WOOD) + 1);
            }

            flag = this.level().destroyBlock(blockpos, false, this) || flag;
            this.breakCounter = 0;
         }
      }

   }

   public boolean getAdaptation() {
      return (Integer)this.entityData.get(WOOD) >= 20;
   }

   public void ActivateAdaptation() {
      this.entityData.set(WOOD, 20);
   }

   private static @NotNull AABB getAabb(double distanceFromBase, Vec3 segmentPos, double radius) {
      double verticalScale = (double)1.0F + ((double)1.0F - distanceFromBase) * (double)2.0F;
      double horizontalScale = 0.8 + distanceFromBase * 1.2;
      return new AABB(segmentPos.x - radius * horizontalScale, segmentPos.y - radius * verticalScale * (double)0.5F, segmentPos.z - radius * horizontalScale, segmentPos.x + radius * horizontalScale, segmentPos.y + radius * verticalScale * (double)1.5F, segmentPos.z + radius * horizontalScale);
   }

   private void applyVortexForceToEntity(Entity entity, Vec3 center, double radius, int segmentIndex, int totalSegments, Vec3 basePosition) {
      Vec3 entityPos = entity.position();
      Vec3 toCenter = center.subtract(entityPos);
      double distance = toCenter.length();
      if (!(distance < 0.2) && !(distance > radius)) {
         double normalizedDistance = distance / radius;
         double funnelPosition = (double)segmentIndex / (double)(totalSegments - 1);
         double baseMultiplier = Math.pow((double)1.0F - funnelPosition, (double)2.0F);
         double pullStrength;
         if (segmentIndex == 0) {
            pullStrength = (double)0.25F * ((double)1.0F + normalizedDistance * (double)0.5F);
         } else if (segmentIndex < totalSegments / 3) {
            pullStrength = 0.18 * (0.8 + normalizedDistance * 0.4);
         } else {
            pullStrength = 0.12 * (0.6 + normalizedDistance * 0.3);
         }

         Vec3 radialPull = toCenter.normalize().scale(pullStrength);
         Vec3 spinDir = (new Vec3(-toCenter.z, (double)0.0F, toCenter.x)).normalize();
         double spinStrength = 0.8 * ((double)1.0F - normalizedDistance) * (0.3 + baseMultiplier * 0.7);
         Vec3 spinForce = spinDir.scale(spinStrength);
         Vec3 toBase = basePosition.subtract(entityPos);
         double distanceToBase = toBase.length();
         double flowStrength = getFlowStrength(segmentIndex, totalSegments, distanceToBase);
         Vec3 flowForce = toBase.normalize().scale(flowStrength);
         double sinkStrength;
         if (segmentIndex > totalSegments - 4) {
            sinkStrength = 0.01 * ((double)1.0F - funnelPosition);
         } else {
            sinkStrength = 0.01 * baseMultiplier;
         }

         Vec3 sinkForce = new Vec3((double)0.0F, -sinkStrength * ((double)1.0F - normalizedDistance), (double)0.0F);
         Vec3 totalForce;
         if (segmentIndex == 0) {
            totalForce = radialPull.scale((double)2.0F).add(flowForce.scale((double)0.5F)).add(spinForce.scale(0.3));
         } else if (segmentIndex < 4) {
            totalForce = flowForce.scale((double)1.5F).add(radialPull).add(spinForce.scale(0.7)).add(sinkForce.scale((double)0.5F));
         } else {
            totalForce = flowForce.add(radialPull.scale(0.8)).add(spinForce).add(sinkForce.scale(0.8));
         }

         Vec3 motion = entity.getDeltaMovement().add(totalForce);
         double maxSpeed;
         if (segmentIndex == 0) {
            maxSpeed = entity instanceof Boat ? 0.2 : 0.4;
         } else if (segmentIndex < 4) {
            maxSpeed = entity instanceof Boat ? 0.3 : 0.6;
         } else {
            maxSpeed = entity instanceof Boat ? (double)0.5F : (double)1.0F;
         }

         if (motion.length() > maxSpeed) {
            motion = motion.normalize().scale(maxSpeed);
         }

         if (segmentIndex <= 5 && this.shouldConsumeEntity(entity, center)) {
            this.consumeEntity(entity);
         } else {
            entity.setDeltaMovement(motion);
            entity.hurtMarked = true;
         }
      }
   }

   private static double getFlowStrength(int segmentIndex, int totalSegments, double distanceToBase) {
      double flowStrength;
      if (segmentIndex == 0) {
         flowStrength = 0.02;
      } else if (segmentIndex < 3) {
         flowStrength = 0.3 * ((double)1.0F - distanceToBase / (double)10.0F);
      } else if (segmentIndex < totalSegments / 2) {
         flowStrength = 0.15;
      } else {
         flowStrength = 0.08;
      }

      return flowStrength;
   }

   private boolean shouldConsumeEntity(Entity entity, Vec3 baseCenter) {
      if (!entity.isAlive()) {
         return false;
      } else if (entity.isPassenger()) {
         return false;
      } else {
         double distSq = entity.position().distanceToSqr(baseCenter);
         return distSq < 10.240000000000002 && (entity instanceof LivingEntity || entity instanceof Boat);
      }
   }

   private void consumeEntity(Entity entity) {
      entity.setDeltaMovement(Vec3.ZERO);
      entity.fallDistance = 0.0F;
      entity.stopRiding();
      Vec3 basePos = this.getVortexFunnel().getEntities()[0];
      entity.setPos(basePos.x, basePos.y, basePos.z);
      if (entity instanceof Boat boat) {
         this.entityData.set(WOOD, (Integer)this.entityData.get(WOOD) + 5);
         boat.discard();
      } else {
         entity.startRiding(this);
      }

   }

   static {
      HEIGHT = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.FLOAT);
      WATER_TICKS = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      RIGHT_ARM_TIP = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.VECTOR3);
      LEFT_ARM_TIP = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.VECTOR3);
      RIGHT_ARM_ENTITY = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      LEFT_ARM_ENTITY = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      RIGHT_ARM_DELAY = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      LEFT_ARM_DELAY = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      VORTEX_VECTOR = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.BLOCK_POS);
      VORTEX_TIMEOUT = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      WOOD = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.INT);
      HOOK = SynchedEntityData.defineId(Grakensenker.class, EntityDataSerializers.BOOLEAN);
      V0 = new Vector3f();
   }

   static enum GrakenLegsModifiers {
      BACK_LEFT_TENTACLE(new Vec3((double)-3.0F, (double)3.5F, (double)0.75F), new Vec3((double)-6.0F, (double)-1.0F, (double)6.0F), new Vec3((double)1.0F, (double)-3.0F, (double)4.0F)),
      BACK_RIGHT_TENTACLE(new Vec3((double)-3.0F, (double)3.5F, (double)-0.75F), new Vec3((double)-6.0F, (double)-1.0F, (double)-6.0F), new Vec3((double)1.0F, (double)-3.0F, (double)-4.0F)),
      MIDDLE_LEFT_TENTACLE(new Vec3((double)-1.0F, (double)2.0F, (double)0.75F), new Vec3((double)0.0F, (double)-1.0F, (double)6.0F), new Vec3((double)0.0F, (double)1.0F, (double)7.0F)),
      MIDDLE_RIGHT_TENTACLE(new Vec3((double)-1.0F, (double)2.0F, (double)-0.75F), new Vec3((double)0.0F, (double)-1.0F, (double)-6.0F), new Vec3((double)0.0F, (double)1.0F, (double)-7.0F)),
      FRONT_LEFT_TENTACLE(new Vec3((double)-2.0F, (double)3.0F, (double)0.75F), new Vec3((double)9.0F, (double)-1.0F, (double)6.0F), new Vec3((double)8.0F, (double)1.0F, (double)4.0F)),
      FRONT_RIGHT_TENTACLE(new Vec3((double)-2.0F, (double)3.0F, (double)-0.75F), new Vec3((double)9.0F, (double)-1.0F, (double)-6.0F), new Vec3((double)8.0F, (double)1.0F, (double)-4.0F)),
      LEFT_ARM(new Vec3((double)0.0F, (double)3.0F, (double)1.0F), new Vec3((double)8.0F, (double)2.5F, (double)6.0F), new Vec3((double)16.0F, (double)4.5F, (double)8.0F)),
      RIGHT_ARM(new Vec3((double)0.0F, (double)3.0F, (double)-1.0F), new Vec3((double)8.0F, (double)2.5F, (double)-6.0F), new Vec3((double)16.0F, (double)4.5F, (double)-8.0F));

      private final Vec3 bodySet;
      private final Vec3 offset;
      private final Vec3 underwaterOffset;

      private GrakenLegsModifiers(Vec3 bodySet, Vec3 offset, Vec3 underwaterOffset) {
         this.bodySet = bodySet;
         this.offset = offset;
         this.underwaterOffset = underwaterOffset;
      }

      // $FF: synthetic method
      private static GrakenLegsModifiers[] $values() {
         return new GrakenLegsModifiers[]{BACK_LEFT_TENTACLE, BACK_RIGHT_TENTACLE, MIDDLE_LEFT_TENTACLE, MIDDLE_RIGHT_TENTACLE, FRONT_LEFT_TENTACLE, FRONT_RIGHT_TENTACLE, LEFT_ARM, RIGHT_ARM};
      }
   }
}
