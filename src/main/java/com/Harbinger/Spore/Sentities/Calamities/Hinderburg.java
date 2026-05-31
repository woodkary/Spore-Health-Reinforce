package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Sentities.FlyingInfected;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.AerialRangedGoal;
import com.Harbinger.Spore.Sentities.AI.FlyingWanderAround;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.HohlMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Projectile.ThrownTumor;
import com.Harbinger.Spore.Sentities.Utility.TumoroidNuke;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class Hinderburg extends Calamity implements FlyingInfected, TrueCalamity, RangedAttackMob {
   public static final EntityDataAccessor ADAPTATION;
   public static final EntityDataAccessor DROPPED_BOMBS;
   public static final EntityDataAccessor BOMB;
   private int bomb_timer = -1;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart lowerbody;
   public final CalamityMultipart forwardbody;
   public final CalamityMultipart rightcannon;
   public final CalamityMultipart leftcannon;
   public final CalamityMultipart mouth;
   private final List<HitboxesForParts> innatePartList;

   public Hinderburg(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.HINDEN_FRONT, HitboxesForParts.HINDEN_BACK, HitboxesForParts.MAW, HitboxesForParts.RIGHT_CANNON, HitboxesForParts.LEFT_CANNON);
      this.lowerbody = new CalamityMultipart(this, "lowerbody", 4.0F, 4.0F);
      this.forwardbody = new CalamityMultipart(this, "forwardbody", 4.0F, 4.0F);
      this.rightcannon = new CalamityMultipart(this, "rightcannon", 1.5F, 1.5F);
      this.leftcannon = new CalamityMultipart(this, "leftcannon", 1.5F, 1.5F);
      this.mouth = new CalamityMultipart(this, "mouth", 3.0F, 0.5F);
      this.subEntities = new CalamityMultipart[]{this.lowerbody, this.forwardbody, this.rightcannon, this.leftcannon, this.mouth};
      this.moveControl = new HindenMovementController(this);
      this.lookControl = new HindenLookControl(this);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
   }

   public boolean causeFallDamage(float p_147187_, float p_147188_, DamageSource p_147189_) {
      return false;
   }

   public void travel(Vec3 vec) {
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.1, (double)0.0F));
      }

      if (this.isEffectiveAi() && !this.onGround()) {
         this.moveRelative(0.1F, vec);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
      } else {
         super.travel(vec);
      }

   }

   public boolean canCalcify(Entity entity) {
      return false;
   }

   public boolean isNoGravity() {
      return true;
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
      if (this.tickCount % 20 == 0) {
         if (this.getKills() >= 50 && this.getDroppedBombs() >= 5 && !this.isAdapted()) {
            this.entityData.set(ADAPTATION, true);
         }

         if (this.isAdapted()) {
            AABB aabb = this.getBoundingBox().inflate((double)8.0F);

            for(Entity entity : this.level().getEntities(this, aabb)) {
               if (entity instanceof LivingEntity) {
                  LivingEntity living = (LivingEntity)entity;
                  if (!(living instanceof Infected) && !(living instanceof HohlMultipart) && !(living instanceof UtilityEntity) && !((List)SConfig.SERVER.blacklist.get()).contains(living.getEncodeId())) {
                     living.setSecondsOnFire(5);
                  }
               }
            }
         }
      }

      if (this.getBomb() < 2450) {
         int value = this.isAdapted() ? 2 : 1;
         this.setBomb(this.getBomb() + value);
      }

      if (this.getBombTimer() >= 0) {
         this.tickBomb();
         if (this.getBombTimer() == 1) {
            Entity entity = (Entity)(this.getTarget() != null ? this.getTarget() : this);
            entity.playSound((SoundEvent)Ssounds.HINDEN_NUKE.get());
         }

         if (this.getBombTimer() >= 80) {
            this.SummonNuke();
            this.bomb_timer = -1;
         }
      }

   }

   public int getBombTimer() {
      return this.bomb_timer;
   }

   public void tickBomb() {
      ++this.bomb_timer;
   }

   public int getDroppedBombs() {
      return (Integer)this.entityData.get(DROPPED_BOMBS);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.hinden_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FLYING_SPEED, 0.2).add(Attributes.ARMOR, (Double)SConfig.SERVER.hinden_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.hinden_damage.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if (calamityMultipart == this.mouth) {
         this.hurt(source, value * 2.0F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value);
      } else if (calamityMultipart != this.rightcannon && calamityMultipart != this.leftcannon) {
         this.hurt(source, value);
      } else {
         this.hurt(source, value * 3.0F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value * 2.0f);
      }

      return true;
   }

   public int chemicalRange() {
      return 32;
   }

   public List buffs() {
      return (List)SConfig.SERVER.hinden_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.hinden_debuffs.get();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.hindie_loot.get();
   }

   public void registerGoals() {
      this.goalSelector.addGoal(3, new AvoidEntityGoal(this, TumoroidNuke.class, 10.0F, (double)1.0F, 1.2));
      this.goalSelector.addGoal(5, new AerialRangedGoal(this, 1.3, this.isAdapted() ? 20 : 40, 16.0F, 5, 10) {
         public boolean canUse() {
            return super.canUse() && this.target != null && (this.target.onGround() || this.target.isInFluidType());
         }
      });
      this.goalSelector.addGoal(6, new AOEMeleeAttackGoal(this, (double)1.0F, true, (double)2.0F, 6.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      this.goalSelector.addGoal(9, new FlyingWanderAround(this, (double)0.5F));
      super.registerGoals();
   }

   public void ActivateAdaptation() {
      this.setKills(this.getKills() + 50);
      this.entityData.set(DROPPED_BOMBS, (Integer)this.entityData.get(DROPPED_BOMBS) + 5);
   }

   public void aiStep() {
      float f14 = this.getYRot() * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f14);
      float f15 = Mth.cos(f14);
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.forwardbody, (double)(f2 * -5.0F), (double)0.0F, (double)(f15 * 5.0F));
      this.tickPart(this.lowerbody, (double)(f2 * 5.0F), (double)0.0F, (double)(f15 * -5.0F));
      this.tickPart(this.mouth, (double)(f2 * -0.5F), (double)-0.5F, (double)(f15 * 0.5F));
      this.tickPart(this.rightcannon, new Vec3((double)0.0F, (double)0.0F, (double)4.0F), 0.3);
      this.tickPart(this.leftcannon, new Vec3((double)0.0F, (double)0.0F, (double)-4.0F), 0.3);

      for(int l = 0; l < this.subEntities.length; ++l) {
         this.subEntities[l].xo = avec3[l].x;
         this.subEntities[l].yo = avec3[l].y;
         this.subEntities[l].zo = avec3[l].z;
         this.subEntities[l].xOld = avec3[l].x;
         this.subEntities[l].yOld = avec3[l].y;
         this.subEntities[l].zOld = avec3[l].z;
      }

      super.aiStep();
      if (this.isAdapted()) {
         for(int i = 0; i < 360; ++i) {
            if (i % 40 == 0) {
               this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), Math.cos((double)i) * (double)0.25F, (double)0.25F, Math.sin((double)i) * (double)0.25F);
               this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), Math.sin((double)i) * (double)0.25F, (double)-0.25F, Math.cos((double)i) * (double)0.25F);
            }
         }
      }

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

   public boolean isAdapted() {
      return (Boolean)this.entityData.get(ADAPTATION);
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.hinden_dpsr.get();
   }

   public boolean tryToSummonNUKE(Entity entity) {
      if (entity != null && this.isArmed()) {
         double x = Math.abs(entity.getX()) - Math.abs(this.getX());
         double z = Math.abs(entity.getZ()) - Math.abs(this.getZ());
         return entity.getY() < this.getY() && Math.abs(x) < (double)10.0F && Math.abs(z) < (double)10.0F;
      } else {
         return false;
      }
   }

   public void SummonNuke() {
      TumoroidNuke tnt = new TumoroidNuke(this.level(), this);
      tnt.setOverclocked((Boolean)this.entityData.get(ADAPTATION));
      tnt.setBuster(Math.random() < 0.2);
      this.entityData.set(DROPPED_BOMBS, (Integer)this.entityData.get(DROPPED_BOMBS) + 1);
      this.level().addFreshEntity(tnt);
      this.setBomb(0);
   }

   public int getAmbientSoundInterval() {
      return 200;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HINDEN_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.RAVAGER_STEP;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BOMB, 0);
      this.entityData.define(DROPPED_BOMBS, 0);
      this.entityData.define(ADAPTATION, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("bomb", (Integer)this.entityData.get(BOMB));
      tag.putInt("dropped_bombs", (Integer)this.entityData.get(DROPPED_BOMBS));
      tag.putBoolean("adaptation", (Boolean)this.entityData.get(ADAPTATION));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(BOMB, tag.getInt("bomb"));
      this.entityData.set(DROPPED_BOMBS, tag.getInt("dropped_bombs"));
      this.entityData.set(ADAPTATION, tag.getBoolean("adaptation"));
   }

   public int getBomb() {
      return (Integer)this.entityData.get(BOMB);
   }

   public void setBomb(int i) {
      this.entityData.set(BOMB, i);
   }

   public boolean isArmed() {
      return this.getBomb() >= 2400;
   }

   public boolean tryToDigDown() {
      return super.tryToDigDown() || this.verticalCollision;
   }

   public void performRangedAttack(LivingEntity livingEntity, float p_33318_) {
      if (!this.level().isClientSide) {
         ThrownTumor tumor = new ThrownTumor(this.level(), this);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() - (double)1.5F;
         double dz = livingEntity.getZ() - this.getZ();
         Vec3 vec3;
         if (this.random.nextFloat() < 0.3F) {
            vec3 = (new Vec3((double)2.0F, 1.3, (double)5.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         } else if (this.random.nextFloat() < 0.3F) {
            vec3 = (new Vec3((double)2.0F, 1.3, (double)-5.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         } else {
            vec3 = (new Vec3((double)0.0F, (double)-2.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         }

         if (SConfig.SERVER.hinden_explosive_effects != null) {
            List<? extends String> ev = (List)SConfig.SERVER.hinden_explosive_effects.get();

            for(int i = 0; i < 1; ++i) {
               int randomIndex = this.random.nextInt(ev.size());
               ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
               MobEffect randomElement = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(randomElement1);
               tumor.setMobEffect(randomElement);
            }
         }

         tumor.setExplode(ExplosionInteraction.MOB);
         tumor.moveTo(this.getX() + vec3.x(), this.getY() + vec3.y(), this.getZ() + vec3.z());
         tumor.shoot(dx, dy - tumor.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
         this.level().addFreshEntity(tumor);
         this.setDeltaMovement(this.getDeltaMovement().add((new Vec3(dx, dy, dz)).normalize().scale(0.2)));
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   public String getMutation() {
      return this.isAdapted() ? "spore.entity.variant.overclocked" : super.getMutation();
   }

   public boolean getAdaptation() {
      return this.isAdapted();
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();

      for(HitboxesForParts hitboxes : this.innatePartList) {
         HitboxesForParts part = this.calculateChance(hitboxes, 0.75F);
         if (part != null) {
            values.add(part);
         }
      }

      return values;
   }

   static {
      ADAPTATION = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.BOOLEAN);
      DROPPED_BOMBS = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.INT);
      BOMB = SynchedEntityData.defineId(Hinderburg.class, EntityDataSerializers.INT);
   }

   private static class HindenMovementController extends MoveControl {
      private final Hinderburg mob;
      private int floatDuration;

      public HindenMovementController(Hinderburg mob) {
         super(mob);
         this.mob = mob;
      }

      public void tick() {
         if (this.operation == Operation.MOVE_TO && this.floatDuration-- <= 0) {
            this.floatDuration += this.mob.getRandom().nextInt(4) + 2;
            Vec3 vec3 = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            vec3 = vec3.normalize();
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(vec3.scale(0.1)));
         }

         if (this.operation == Operation.WAIT && !this.hasWanted() && this.mob.getTarget() == null) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add((double)0.0F, -0.01, (double)0.0F));
         }

      }
   }

   private static class HindenLookControl extends LookControl {
      public HindenLookControl(Mob mob) {
         super(mob);
      }

      public void tick() {
         super.tick();
         if (this.mob.getTarget() == null) {
            if (this.mob.tickCount % 40 == 0) {
               Vec3 vec3 = this.mob.getDeltaMovement();
               this.mob.setYRot(-((float)Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
               this.mob.yBodyRot = this.mob.getYRot();
            }
         } else {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity.distanceToSqr(this.mob) < (double)4096.0F) {
               double d1 = livingentity.getX() - this.mob.getX();
               double d2 = livingentity.getZ() - this.mob.getZ();
               this.mob.setYRot(-((float)Mth.atan2(d1, d2)) * (180F / (float)Math.PI));
               this.mob.yBodyRot = this.mob.getYRot();
            }
         }

      }
   }
}
