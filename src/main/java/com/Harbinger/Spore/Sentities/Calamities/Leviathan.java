package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.LeviathanMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkLeviFin;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkLeviLeg;
import com.Harbinger.Spore.Sentities.Projectile.AcidBall;
import com.Harbinger.Spore.Sentities.Projectile.DrownedFleshBomb;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fluids.FluidType;

public class Leviathan extends Calamity implements TrueCalamity, WaterInfected, RangedAttackMob {
   private static final int SEGMENT_COUNT = 2;
   private static final EntityDataAccessor CHILD_UUID;
   private static final EntityDataAccessor SPRAY_COOLDOWN;
   private final CalamityMultipart[] subEntities;
   private LeviathanMultipart firstSegment;
   public final CalamityMultipart head;
   private final IkLeviLeg[] legs;
   private final IkLeviFin[] fins;
   public final float[] ringBuffer = new float[64];
   public int ringBufferIndex = -1;
   private int attackAnimationTick;
   private int rangeAttackAnimationTick;
   private final List<HitboxesForParts> innatePartList;

   public Leviathan(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.LEVI_BODY, HitboxesForParts.LEVI_SEGMENT, HitboxesForParts.LEVI_TAIL, HitboxesForParts.LEVI_RIGHT_JAW, HitboxesForParts.LEVI_LEFT_JAW);
      IkLeviLeg frontRightLeg = new IkLeviLeg(this, 6, LEG_POSITIONS.FRONT_RIGHT_TENTACLE.bodySet, LEG_POSITIONS.FRONT_RIGHT_TENTACLE.offset, 6.0F);
      IkLeviLeg frontLeftLeg = new IkLeviLeg(this, 6, LEG_POSITIONS.FRONT_LEFT_TENTACLE.bodySet, LEG_POSITIONS.FRONT_LEFT_TENTACLE.offset, 6.0F);
      IkLeviLeg backRightLeg = new IkLeviLeg(this, 5, LEG_POSITIONS.BACK_RIGHT_TENTACLE.bodySet, LEG_POSITIONS.BACK_RIGHT_TENTACLE.offset, 4.0F);
      IkLeviLeg backLeftLeg = new IkLeviLeg(this, 5, LEG_POSITIONS.BACK_LEFT_TENTACLE.bodySet, LEG_POSITIONS.BACK_LEFT_TENTACLE.offset, 4.0F);
      this.legs = new IkLeviLeg[]{frontLeftLeg, frontRightLeg, backLeftLeg, backRightLeg};
      IkLeviFin rightFin = new IkLeviFin(this, 4, LEG_POSITIONS.RIGHT_ARM.bodySet, LEG_POSITIONS.RIGHT_ARM.offset, 5.0F);
      IkLeviFin leftFin = new IkLeviFin(this, 4, LEG_POSITIONS.LEFT_ARM.bodySet, LEG_POSITIONS.LEFT_ARM.offset, 5.0F);
      this.fins = new IkLeviFin[]{rightFin, leftFin};
      this.head = new CalamityMultipart(this, "head", 3.0F, 3.0F);
      this.subEntities = new CalamityMultipart[]{this.head};
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
      this.setMaxUpStep(1.5F);
   }

   public IkLeviLeg[] getLegs() {
      return this.legs;
   }

   public IkLeviFin[] getFins() {
      return this.fins;
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.2F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85).add((double)0.0F, this.onGround() ? (double)0.01F : (double)0.0F, (double)0.0F));
      } else {
         super.travel(vec);
      }

   }

   public int getRangeAttackAnimationTick() {
      return this.rangeAttackAnimationTick;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);

      for(int e = 0; e < this.legs.length; ++e) {
         this.legs[e].writeVariants(tag, e);
      }

      tag.putInt("spray_cooldown", (Integer)this.entityData.get(SPRAY_COOLDOWN));
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);

      for(int e = 0; e < this.legs.length; ++e) {
         this.legs[e].readVariants(tag, e);
      }

      this.entityData.set(SPRAY_COOLDOWN, tag.getInt("spray_cooldown"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CHILD_UUID, Optional.empty());
      this.entityData.define(SPRAY_COOLDOWN, 0);
   }

   public void setSprayCooldown(int val) {
      this.entityData.set(SPRAY_COOLDOWN, val);
   }

   public int getSprayCooldown() {
      return (Integer)this.entityData.get(SPRAY_COOLDOWN);
   }

   @Nullable
   public UUID getChildId() {
      return (UUID)((Optional)this.entityData.get(CHILD_UUID)).orElse((Object)null);
   }

   public void setChildId(@Nullable UUID id) {
      this.entityData.set(CHILD_UUID, Optional.ofNullable(id));
   }

   @Nullable
   public LeviathanMultipart getFirstSegment() {
      if (this.firstSegment == null && !this.level().isClientSide) {
         UUID id = this.getChildId();
         if (id != null) {
            Entity e = ((ServerLevel)this.level()).getEntity(id);
            if (e instanceof LeviathanMultipart) {
               LeviathanMultipart part = (LeviathanMultipart)e;
               this.firstSegment = part;
            }
         }
      }

      return this.firstSegment;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.levi_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.levi_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.levi_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)0.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public void aiStep() {
      float f14 = this.getYRot() * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f14);
      float f15 = Mth.cos(f14);
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.head, (double)(f2 * -3.0F), (double)0.0F, (double)(-f15 * -3.0F));

      for(int l = 0; l < this.subEntities.length; ++l) {
         this.subEntities[l].xo = avec3[l].x;
         this.subEntities[l].yo = avec3[l].y;
         this.subEntities[l].zo = avec3[l].z;
         this.subEntities[l].xOld = avec3[l].x;
         this.subEntities[l].yOld = avec3[l].y;
         this.subEntities[l].zOld = avec3[l].z;
      }

      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.rangeAttackAnimationTick > 0) {
         --this.rangeAttackAnimationTick;
      }

   }

   public CalamityMultipart[] getSubEntities() {
      return this.subEntities;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.leviathan_loot.get();
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() != null && amount >= 10.0F && Math.random() < 0.2) {
         this.explodeSegments();
         this.playSound((SoundEvent)Ssounds.CALAMITY_DEATH.get());
      }

      return super.hurt(source, amount);
   }

   public boolean isMultipartEntity() {
      return true;
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public boolean canShoot(LivingEntity living) {
      if (living == null) {
         return false;
      } else {
         if (this.getSprayCooldown() <= 0) {
            if (this.isInWater()) {
               this.setSprayCooldown(20);
               return false;
            }

            if (this.hasLineOfSight(living) && this.distanceToSqr(living) > (double)50.0F) {
               this.rangeAttackAnimationTick = 20;
               this.level().broadcastEntityEvent(this, (byte)5);
               return true;
            }
         }

         return false;
      }
   }

   public @org.jetbrains.annotations.Nullable PartEntity[] getParts() {
      return this.subEntities;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
      super.recreateFromPacket(p_218825_);
   }

   protected void onEffectAdded(MobEffectInstance instance, @Nullable Entity source) {
      super.onEffectAdded(instance, source);
      if (this.firstSegment != null) {
         MobEffectInstance existing = this.firstSegment.getEffect(instance.getEffect());
         if (existing == null || existing.getDuration() < instance.getDuration() - 5) {
            this.firstSegment.addEffect(new MobEffectInstance(instance));
         }

      }
   }

   protected void onEffectRemoved(MobEffectInstance instance) {
      super.onEffectRemoved(instance);
      if (this.firstSegment != null) {
         this.firstSegment.removeEffect(instance.getEffect());
      }
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      value = this.rangeAttackAnimationTick > 0 ? value * 2.0F : value * 0.5F;
      this.hurt(source, value);
      return false;
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.levi_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.levi_debuffs.get();
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.levi_dpsr.get();
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      AcidBall.shoot(this, livingEntity, (float)((Double)SConfig.SERVER.levi_damage.get() * (double)0.25F * (Double)SConfig.SERVER.global_damage.get()) * 2.0F);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else if (value == 5) {
         this.rangeAttackAnimationTick = 20;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   public void tick() {
      super.tick();

      for(IkLeviLeg leg : this.legs) {
         leg.refreshLegStandingPoint();
         leg.applyIK();
      }

      for(IkLeviFin leg : this.fins) {
         leg.applyIK();
      }

      if (++this.ringBufferIndex == 64) {
         this.ringBufferIndex = 0;
      }

      this.ringBuffer[this.ringBufferIndex] = this.getYRot();
      if (!this.level().isClientSide) {
         if (this.shouldSpawnChain()) {
            this.createChain();
         }

         this.updateChain();
      }

      if (this.getSprayCooldown() > 0) {
         this.setSprayCooldown(this.getSprayCooldown() - 1);
      }

      LivingEntity target = this.getTarget();
      if (this.tickCount % 5 == 0 && this.canShoot(target) && target != null) {
         for(int i = 0; i < this.random.nextInt(4) + this.getExtraShots(); ++i) {
            this.performRangedAttack(target, 0.0F);
         }

         this.setSprayCooldown(40);
      }

      if (this.tickCount % 1200 == 0 && this.getSearchArea() == BlockPos.ZERO && !this.isOcean(this.level().getBiome(this.getOnPos()))) {
         BlockPos pos = this.findOcean(this.level(), this.getOnPos());
         if (pos != null) {
            this.setSearchArea(pos);
         }
      }

   }

   public int getExtraShots() {
      AttributeInstance instance = this.getAttribute((Attribute)SAttributes.BALLISTIC.get());
      if (instance != null) {
         double level = instance.getValue();
         return level < (double)1.0F ? 0 : (int)((double)3.0F * level);
      } else {
         return 0;
      }
   }

   private boolean shouldSpawnChain() {
      LeviathanMultipart part = this.getFirstSegment();
      return part == null || !part.isAlive();
   }

   private void createChain() {
      LeviathanMultipart previous = null;

      for(int i = 0; i < 2; ++i) {
         LeviathanMultipart part = new LeviathanMultipart((EntityType)Sentities.LEVIATHAN_SEG.get(), this.level());
         part.setPos(this.getX(), this.getY(), this.getZ());
         part.setParent((Entity)(i == 0 ? this : previous));
         part.setColor(this.getMutationColor());
         part.setTail(i == 1);
         this.level().addFreshEntity(part);
         if (i == 0) {
            this.setChildId(part.getUUID());
            this.firstSegment = part;
         } else {
            previous.setChildId(part.getUUID());
         }

         previous = part;
      }

   }

   protected SoundEvent getAmbientSound() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)200.0F ? null : (SoundEvent)Ssounds.LEVIATHAN_AMBIENT.get();
   }

   public void registerGoals() {
      this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)1.5F, false, (double)2.5F, 6.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Leviathan.this.getBbWidth();
            return (double)(f * 3.0F * f * 3.0F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, (double)1.0F, 10));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      super.registerGoals();
   }

   private void updateChain() {
      LeviathanMultipart part = this.getFirstSegment();
      if (part != null) {
         Vec3 anchor = this.position();
         float xRot = this.getXRot();
         float yRot = this.getYRot();

         for(int index = 0; part != null; ++index) {
            float yaw = this.getRingBuffer(4 + index * 2, 1.0F);
            anchor = part.tickMultipartPosition(this.getId(), anchor, xRot, yRot, yaw, index == 0);
            xRot = part.getXRot();
            Entity child = part.getChild();
            LeviathanMultipart var10000;
            if (child instanceof LeviathanMultipart) {
               LeviathanMultipart next = (LeviathanMultipart)child;
               var10000 = next;
            } else {
               var10000 = null;
            }

            part = var10000;
         }

      }
   }

   public float getRingBuffer(int offset, float partialTicks) {
      if (this.isDeadOrDying()) {
         partialTicks = 0.0F;
      }

      partialTicks = 1.0F - partialTicks;
      int i = this.ringBufferIndex - offset & 63;
      int j = this.ringBufferIndex - offset - 1 & 63;
      float d0 = this.ringBuffer[i];
      float d1 = Mth.wrapDegrees(this.ringBuffer[j] - d0);
      return d0 + d1 * partialTicks;
   }

   public void remove(RemovalReason reason) {
      super.remove(reason);

      LeviathanMultipart var10000;
      for(LeviathanMultipart part = this.getFirstSegment(); part != null; part = var10000) {
         Entity next = part.getChild();
         part.discard();
         if (next instanceof LeviathanMultipart l) {
            var10000 = l;
         } else {
            var10000 = null;
         }
      }

   }

   public void explodeSegments() {
      this.explodeTumorsAround(this.position());
      LeviathanMultipart part = this.getFirstSegment();
      if (part != null) {
         this.explodeTumorsAround(part.position());
         Entity entity = part.getChild();
         if (entity instanceof LeviathanMultipart) {
            this.explodeTumorsAround(entity.position());
         }
      }

   }

   public void explodeTumorsAround(Vec3 pos) {
      for(int i = 0; i < this.random.nextInt(3, 7); ++i) {
         DrownedFleshBomb fleshBomb = new DrownedFleshBomb(this.level());
         int e = ((List)SConfig.SERVER.levi_explosive_effects.get()).size();
         fleshBomb.setEffect((String)((List)SConfig.SERVER.levi_explosive_effects.get()).get(this.random.nextInt(e)));
         fleshBomb.moveTo(pos);
         fleshBomb.setDeltaMovement(new Vec3((this.random.nextDouble() - this.random.nextDouble()) * 0.9, this.random.nextDouble() * 0.6 + 0.3, (this.random.nextDouble() - this.random.nextDouble()) * 0.9));
         this.level().addFreshEntity(fleshBomb);
      }

   }

   public void die(DamageSource source) {
      super.die(source);

      LeviathanMultipart var10000;
      for(LeviathanMultipart part = this.getFirstSegment(); part != null; part = var10000) {
         Entity next = part.getChild();
         part.discard();
         if (next instanceof LeviathanMultipart l) {
            var10000 = l;
         } else {
            var10000 = null;
         }
      }

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

   static {
      CHILD_UUID = SynchedEntityData.defineId(Leviathan.class, EntityDataSerializers.OPTIONAL_UUID);
      SPRAY_COOLDOWN = SynchedEntityData.defineId(Leviathan.class, EntityDataSerializers.INT);
   }

   static enum LEG_POSITIONS {
      BACK_LEFT_TENTACLE(new Vec3((double)-2.0F, (double)1.0F, (double)0.75F), new Vec3((double)-4.0F, (double)0.0F, (double)3.0F)),
      BACK_RIGHT_TENTACLE(new Vec3((double)-2.0F, (double)1.0F, (double)-0.75F), new Vec3((double)-4.0F, (double)0.0F, (double)-3.0F)),
      FRONT_LEFT_TENTACLE(new Vec3((double)0.0F, (double)1.5F, (double)0.75F), new Vec3((double)4.0F, (double)0.0F, (double)3.0F)),
      FRONT_RIGHT_TENTACLE(new Vec3((double)0.0F, (double)1.5F, (double)-0.75F), new Vec3((double)4.0F, (double)0.0F, (double)-3.0F)),
      LEFT_ARM(new Vec3((double)0.0F, (double)1.0F, (double)0.75F), new Vec3((double)-1.0F, (double)0.5F, (double)6.0F)),
      RIGHT_ARM(new Vec3((double)0.0F, (double)1.0F, (double)-0.75F), new Vec3((double)-1.0F, (double)0.5F, (double)-6.0F));

      private final Vec3 bodySet;
      private final Vec3 offset;

      private LEG_POSITIONS(Vec3 bodySet, Vec3 offset) {
         this.bodySet = bodySet;
         this.offset = offset;
      }

      // $FF: synthetic method
      private static LEG_POSITIONS[] $values() {
         return new LEG_POSITIONS[]{BACK_LEFT_TENTACLE, BACK_RIGHT_TENTACLE, FRONT_LEFT_TENTACLE, FRONT_RIGHT_TENTACLE, LEFT_ARM, RIGHT_ARM};
      }
   }
}
