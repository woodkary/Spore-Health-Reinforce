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
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.GazenWaterLeapGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.FallenMultipart.Licker;
import com.Harbinger.Spore.Sentities.Projectile.BileProjectile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fluids.FluidType;

public class Gazenbrecher extends Calamity implements WaterInfected, RangedAttackMob, TrueCalamity {
   public static final EntityDataAccessor ADAPTATION;
   public static final EntityDataAccessor TONGUE;
   private int radar;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart lowerbody;
   public final CalamityMultipart head;
   public final CalamityMultipart tongue;
   private final List<HitboxesForParts> innatePartList;

   public Gazenbrecher(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.GAZEN_TAIL, HitboxesForParts.GAZEN_HEAD, HitboxesForParts.GAZEN_LEFT_LEG, HitboxesForParts.GAZEN_RIGHT_LEG);
      this.lowerbody = new CalamityMultipart(this, "lowerbody", 3.0F, 3.0F);
      this.tongue = new CalamityMultipart(this, "tongue", 2.0F, 2.0F);
      this.head = new CalamityMultipart(this, "head", 3.0F, 3.0F);
      this.subEntities = new CalamityMultipart[]{this.lowerbody, this.head, this.tongue};
      this.setMaxUpStep(1.5F);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.gazen_loot.get();
   }

   public void travel(Vec3 vec) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
      } else {
         super.travel(vec);
      }

   }

   public void ActivateAdaptation() {
      this.entityData.set(ADAPTATION, 21);
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   public double setInflation() {
      return (double)1.0F;
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 1200 == 0 && this.getSearchArea() == BlockPos.ZERO && !this.isOcean(this.level().getBiome(this.getOnPos()))) {
         BlockPos pos = this.findOcean(this.level(), this.getOnPos());
         if (pos != null) {
            this.setSearchArea(pos);
         }
      }

      if (this.getHealth() >= this.getMaxHealth() && this.getTongueHp() < this.getMaxTongueHp() && this.tickCount % 40 == 0) {
         this.setTongueHp(this.getTongueHp() + 1.0F);
      }

      if (this.isInFluidType()) {
         if (this.getTarget() == null && this.radar >= 1200) {
            this.playSound((SoundEvent)Ssounds.SONAR.get());
            this.radar = 0;
            AABB boundingBox = this.getBoundingBox().inflate((double)64.0F);

            for(Entity entity : this.level().getEntities(this, boundingBox)) {
               if (!((List)SConfig.SERVER.whitelist.get()).contains(entity.getEncodeId())) {
                  if (!(entity instanceof Player)) {
                     continue;
                  }

                  Player player = (Player)entity;
                  if (player.getAbilities().instabuild) {
                     continue;
                  }
               }

               if (entity instanceof LivingEntity) {
                  LivingEntity livingEntity = (LivingEntity)entity;
                  if (livingEntity.isAlive()) {
                     this.playSound((SoundEvent)Ssounds.SIGNAL.get(), 2.0F, 1.0F);
                     this.setTarget(livingEntity);
                  }
               }
            }
         } else {
            ++this.radar;
         }
      }

      if (!this.isAdaptedToFire() && this.tickCount % 40 == 0 && this.isOnFire()) {
         this.entityData.set(ADAPTATION, (Integer)this.entityData.get(ADAPTATION) + 1);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TONGUE, this.getMaxTongueHp());
      this.entityData.define(ADAPTATION, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("tongue_hp", (Float)this.entityData.get(TONGUE));
      tag.putInt("adaptation", (Integer)this.entityData.get(ADAPTATION));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TONGUE, tag.getFloat("tongue_hp"));
      this.entityData.set(ADAPTATION, tag.getInt("adaptation"));
   }

   public float getTongueHp() {
      return (Float)this.entityData.get(TONGUE);
   }

   public void setTongueHp(float i) {
      this.entityData.set(TONGUE, i);
   }

   public float getMaxTongueHp() {
      return (float)((Double)SConfig.SERVER.gazen_hp.get() / (double)4.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.gazen_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.gazen_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.gazen_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public void aiStep() {
      float f14 = this.getYRot() * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f14);
      float f15 = Mth.cos(f14);
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.lowerbody, (double)(f2 * 2.5F), (double)0.0F, (double)(-f15 * 2.5F));
      this.tickPart(this.head, (double)(f2 * -3.0F), (double)0.0F, (double)(-f15 * -3.0F));
      this.tickPart(this.tongue, (double)(f2 * -5.0F), 0.3, (double)(-f15 * -5.0F));

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

   public CalamityMultipart[] getSubEntities() {
      return this.subEntities;
   }

   public boolean isMultipartEntity() {
      return true;
   }

   public PartEntity[] getParts() {
      return this.subEntities;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
      super.recreateFromPacket(p_218825_);
   }

   public boolean isAdaptedToFire() {
      return (Integer)this.entityData.get(ADAPTATION) > 20;
   }

   public int getAdaptationCount() {
      return (Integer)this.entityData.get(ADAPTATION);
   }

   public boolean fireImmune() {
      return this.isAdaptedToFire();
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.gazen_dpsr.get();
   }

   public void registerGoals() {
      this.goalSelector.addGoal(3, new ScatterShotRangedGoal(this, 1.3, 60, 32.0F, 1, 3) {
         public boolean canUse() {
            if (Gazenbrecher.this.getTongueHp() <= 0.0F) {
               return false;
            } else {
               return super.canUse() && (Gazenbrecher.this.calculateHeight() || Gazenbrecher.this.calculateDistance());
            }
         }
      });
      this.goalSelector.addGoal(4, new GazenWaterLeapGoal(this));
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)1.5F, false, (double)2.5F, 6.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Gazenbrecher.this.getBbWidth();
            return (double)(f * 2.0F * f * 2.0F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.2));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      super.registerGoals();
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if (calamityMultipart == this.tongue) {
         if (this.getTongueHp() > 0.0F && value > this.getTongueHp()) {
            if (this.getTongueHp() > 0.0F && value > this.getTongueHp()) {
               this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
               this.SummonDetashedTongue();
            }

            this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
         }

         this.hurt(source, value * 1.5F);
         this.setTongueHp(value > this.getTongueHp() ? 0.0F : this.getTongueHp() - value);
      } else {
         this.hurt(source, value);
      }

      return true;
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.gazen_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.gazen_debuffs.get();
   }

   boolean calculateHeight() {
      return this.getTarget() != null && this.getTarget().getY() > this.getY() && Math.abs(Math.abs(this.getTarget().getY()) - Math.abs(this.getY())) > (double)5.0F;
   }

   boolean calculateDistance() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)300.0F;
   }

   public boolean hasLineOfSight(Entity entity) {
      return !this.calculateDistance() && !this.calculateHeight() ? super.hasLineOfSight(entity) : true;
   }

   public void performRangedAttack(LivingEntity livingEntity, float p_33318_) {
      if (!this.level().isClientSide) {
         BileProjectile tumor = new BileProjectile(this.level(), this, this.TARGET_SELECTOR);
         Vec3 vec3 = (new Vec3((double)3.0F, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight();
         double dz = livingEntity.getZ() - this.getZ();
         if (this.isAdaptedToFire()) {
            tumor.setSecondsOnFire(10);
         }

         tumor.setDamage((float)((Double)SConfig.SERVER.gazen_ranged_damage.get() * (double)1.0F));
         tumor.moveTo(this.getX() + vec3.x, this.getY() + (double)1.0F, this.getZ() + vec3.z);
         tumor.shoot(dx, dy - tumor.getY() + Math.hypot(dx, dz) * (double)0.001F, dz, 2.0F, 6.0F);
         this.level().addFreshEntity(tumor);
      }

   }

   private void SummonDetashedTongue() {
      Licker licker = new Licker((EntityType)Sentities.LICKER.get(), this.level());
      Vec3 vec3 = (new Vec3((double)4.0F, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      licker.setBurned(this.isAdaptedToFire());
      licker.moveTo(this.getX() + vec3.x, this.getY() + 1.6, this.getZ() + vec3.z);
      licker.setYBodyRot(this.getYRot());
      this.level().addFreshEntity(licker);
   }

   public boolean doHurtTarget(Entity entity) {
      if (this.isAdaptedToFire()) {
         entity.setSecondsOnFire(10);
      }

      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return this.onGround() ? SoundEvents.RAVAGER_STEP : SoundEvents.GENERIC_SWIM;
   }

   protected SoundEvent getAmbientSound() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)200.0F ? null : (SoundEvent)Ssounds.GAZEN_AMBIENT.get();
   }

   public String getMutation() {
      return this.isAdaptedToFire() ? "spore.entity.variant.crispy" : super.getMutation();
   }

   public boolean getAdaptation() {
      return this.isAdaptedToFire();
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();
      if (this.getTongueHp() > 0.0F) {
         values.add(HitboxesForParts.LICKER);
      }

      for(HitboxesForParts hitboxes : this.innatePartList) {
         HitboxesForParts part = this.calculateChance(hitboxes, 0.85F);
         if (part != null) {
            values.add(part);
         }
      }

      return values;
   }

   static {
      ADAPTATION = SynchedEntityData.defineId(Gazenbrecher.class, EntityDataSerializers.INT);
      TONGUE = SynchedEntityData.defineId(Gazenbrecher.class, EntityDataSerializers.FLOAT);
   }
}
