package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Core.utils.attack.SporeAttackUtil;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.FallenMultipart.StalhArm;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class Stahlmorder extends Calamity implements TrueCalamity {
   public static final EntityDataAccessor SWORD_ARM;
   public static final EntityDataAccessor MELEE_STATE;
   public static final EntityDataAccessor JUMP_OFFSET;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart swordArm;
   public final CalamityMultipart mouth;
   public AnimationState animationState = new AnimationState();
   public int animationOffset = 0;
   private final List<HitboxesForParts> innatePartList;

   public Stahlmorder(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.STAHL_RIGHT_LEG, HitboxesForParts.STAHL_LEFT_LEG, HitboxesForParts.STAHL_ARM_ARM, HitboxesForParts.STAHL_ARM_ARM2, HitboxesForParts.STAHL_MOUTH);
      this.swordArm = new CalamityMultipart(this, "swordArm", 3.5F, 3.5F);
      this.mouth = new CalamityMultipart(this, "mouth", 2.0F, 2.0F);
      this.subEntities = new CalamityMultipart[]{this.swordArm, this.mouth};
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
      this.moveControl = new InfectedWallMovementControl(this);
      this.setMaxUpStep(2.5F);
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SWORD_ARM, this.getMaxArmHp());
      this.entityData.define(MELEE_STATE, 0);
      this.entityData.define(JUMP_OFFSET, 0);
   }

   private Float getMaxArmHp() {
      return (float)((Double)SConfig.SERVER.sta_hp.get() / (double)4.0F);
   }

   public int getJumpOffset() {
      return (Integer)this.entityData.get(JUMP_OFFSET);
   }

   public void setJumpOffset(int val) {
      this.entityData.set(JUMP_OFFSET, val);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("sword_arm", (Float)this.entityData.get(SWORD_ARM));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(SWORD_ARM, tag.getFloat("sword_arm"));
   }

   public float getSwordArmHp() {
      return (Float)this.entityData.get(SWORD_ARM);
   }

   public void setSwordtArmHp(float i) {
      this.entityData.set(SWORD_ARM, i);
   }

   public void aiStep() {
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.mouth, new Vec3((double)1.5F, (double)3.5F, (double)0.0F));
      this.tickPart(this.swordArm, new Vec3((double)0.0F, (double)4.5F, (double)-5.0F));

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

   public void tick() {
      super.tick();
      if (this.tickCount % 20 == 0 && this.getHealth() == this.getMaxHealth() && this.getSwordArmHp() < this.getMaxArmHp()) {
         this.setSwordtArmHp(this.getSwordArmHp() + 1.0F);
      }

      if (this.animationOffset > 0) {
         if (this.level().isClientSide && this.animationOffset == 1) {
            this.animationState.stop();
         }

         --this.animationOffset;
      }

      if (this.getJumpOffset() > 0) {
         this.setJumpOffset(this.getJumpOffset() - 1);
      }

   }

   public void triggerAnimation(int states) {
      this.entityData.set(MELEE_STATE, states);
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.animationOffset = 20;
         this.animationState.start(this.tickCount);
      } else {
         super.handleEntityEvent(value);
      }

   }

   private int decideAnimation(LivingEntity target) {
      if (!(this.getSwordArmHp() > 0.0F) || !(this.getRandom().nextFloat() < 0.5F) && (target.getArmorValue() < 10 || !(this.getRandom().nextFloat() < 0.75F))) {
         return this.getRandom().nextBoolean() ? MELEE_STATES.SLAP.getValue() : MELEE_STATES.KICK.getValue();
      } else {
         return MELEE_STATES.SLASH.getValue();
      }
   }

   protected int calculateFallDamage(float fallDistance, float p_149390_) {
      if (fallDistance > 4.0F) {
         this.playSound((SoundEvent)Ssounds.LANDING.get());
      }

      return 0;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.STAHL_AMBIENT.get();
   }

   public int getAmbientSoundInterval() {
      return 100;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.sta_loot.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.RAVAGER_STEP;
   }

   public void onSyncedDataUpdated(EntityDataAccessor key) {
      if (MELEE_STATE.equals(key)) {
         AttributeInstance instance = this.getAttribute(Attributes.ATTACK_DAMAGE);
         if (instance != null) {
            if (this.getMeleeState() == MELEE_STATES.SLASH) {
               instance.setBaseValue((Double)SConfig.SERVER.sta_damage.get() * (Double)SConfig.SERVER.global_damage.get());
            } else if (this.getMeleeState() == MELEE_STATES.SLAP) {
               instance.setBaseValue((Double)SConfig.SERVER.sta_slap_damage.get() * (Double)SConfig.SERVER.global_damage.get());
            } else {
               instance.setBaseValue((Double)SConfig.SERVER.sta_kick_damage.get() * (Double)SConfig.SERVER.global_damage.get());
            }
         }
      }

      super.onSyncedDataUpdated(key);
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         SporeAttackUtil.INSTANCE.attack(living, this, (float) this.attributes.getValue(Attributes.ATTACK_DAMAGE));
         this.applyAttackEffect(living, (Integer)this.entityData.get(MELEE_STATE));
      }

      return super.doHurtTarget(entity);
   }

   private void applyAttackEffect(LivingEntity target, int animation) {
      switch (animation) {
         case 0:
            this.playSound((SoundEvent)Ssounds.STAHL_SLASH.get());
            target.addEffect(new MobEffectInstance((MobEffect)Seffects.CORROSION.get(), 600, 1));
            break;
         case 1:
            this.playSound((SoundEvent)Ssounds.STAHL_SLAP.get());
            target.knockback((double)4.0F, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            break;
         case 2:
            this.playSound((SoundEvent)Ssounds.STAHL_KICK.get());
            target.hurtMarked = true;
            target.setDeltaMovement(target.getDeltaMovement().add((double)0.0F, 0.8, (double)0.0F));
      }

   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if (calamityMultipart == this.mouth) {
         this.hurt(source, value * 1.25F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value);
      } else if (calamityMultipart == this.swordArm && this.getSwordArmHp() > 0.0F) {
         this.hurt(source, value * 1.5F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value*1.1f);
         float lostHealth = this.getSwordArmHp() - this.getDamageAfterArmorAbsorb(source, value);
         this.setSwordtArmHp(lostHealth > 0.0F ? lostHealth : (this.getSwordArmHp() != 0.0F ? this.summonDetashedPart() : 0.0F));
      }

      return true;
   }

   public float summonDetashedPart() {
      if (this.level().isClientSide) {
         return 0.0F;
      }

      Vec3 vec3 = (new Vec3((double)0.0F, (double)4.5F, (double)-5.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      StalhArm arm = new StalhArm((EntityType)Sentities.STAHL_ARM.get(), this.level());
      arm.moveTo(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
      this.level().addFreshEntity(arm);
      this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
      return 0.0F;
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.sta_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.sta_debuffs.get();
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

   public void recreateFromPacket(ClientboundAddEntityPacket entityPacket) {
      super.recreateFromPacket(entityPacket);
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.sta_dpsr.get();
   }

   public void registerGoals() {
      this.goalSelector.addGoal(3, new StaLeapGoal(this, 1.6F));
      this.goalSelector.addGoal(4, new StahlMeleeAttackGoal(this, (double)1.5F, false, (double)3.0F, 2.0F, (living) -> this.TARGET_SELECTOR.test(living)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Stahlmorder.this.getBbWidth();
            return (double)(f * 2.0F * f * 2.0F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.2));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this) {
         public boolean canContinueToUse() {
            return Stahlmorder.this.getJumpOffset() > 0 ? false : super.canContinueToUse();
         }
      });
      this.goalSelector.addGoal(8, new SporeBurstSupport(this) {
         public boolean canUse() {
            return Stahlmorder.this.getJumpOffset() > 0 ? false : super.canUse();
         }
      });
      this.goalSelector.addGoal(9, new RandomStrollGoal(this, (double)1.0F));
      super.registerGoals();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sta_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.sta_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sta_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public MELEE_STATES getMeleeState() {
      return MELEE_STATES.byId((Integer)this.entityData.get(MELEE_STATE) & 255);
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();
      if (this.getSwordArmHp() > 0.0F) {
         values.add(HitboxesForParts.STAHL_BLADE_ARM);
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
      SWORD_ARM = SynchedEntityData.defineId(Stahlmorder.class, EntityDataSerializers.FLOAT);
      MELEE_STATE = SynchedEntityData.defineId(Stahlmorder.class, EntityDataSerializers.INT);
      JUMP_OFFSET = SynchedEntityData.defineId(Stahlmorder.class, EntityDataSerializers.INT);
   }

   public static enum MELEE_STATES {
      SLASH(0),
      SLAP(1),
      KICK(2);

      private final int value;
      private static final MELEE_STATES[] BY_ID = (MELEE_STATES[])Arrays.stream(values()).sorted(Comparator.comparingInt(MELEE_STATES::getValue)).toArray((x$0) -> new MELEE_STATES[x$0]);

      private MELEE_STATES(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }

      public static MELEE_STATES byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      // $FF: synthetic method
      private static MELEE_STATES[] $values() {
         return new MELEE_STATES[]{SLASH, SLAP, KICK};
      }
   }

   public class StaLeapGoal extends Goal {
      private final Stahlmorder mob;
      private LivingEntity target;
      private final float yd;

      public StaLeapGoal(Stahlmorder p_25492_, float p_25493_) {
         this.mob = p_25492_;
         this.yd = p_25493_;
         this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
      }

      public boolean canUse() {
         if (Stahlmorder.this.getJumpOffset() > 0) {
            return false;
         } else {
            this.target = this.mob.getTarget();
            if (this.target == null) {
               return false;
            } else if (this.mob.isInWater()) {
               return false;
            } else {
               double d0 = this.mob.distanceToSqr(this.target);
               if (d0 > (double)32.0F) {
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
         return this.mob.onGround();
      }

      public void start() {
         if (this.target != null) {
            Vec3 vec31 = new Vec3(this.target.getX() - this.mob.getX(), (double)0.0F, this.target.getZ() - this.mob.getZ());
            if (vec31.lengthSqr() > 1.0E-7) {
               vec31 = vec31.normalize().scale((double)3.5F);
            }

            this.mob.getLookControl().setLookAt(this.target, 10.0F, (float)this.mob.getMaxHeadXRot());
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(vec31.x + (double)this.yd, (double)this.yd, vec31.z + (double)this.yd));
            Stahlmorder.this.setJumpOffset(200);
         }
      }
   }

   public static class StahlMeleeAttackGoal extends AOEMeleeAttackGoal {
      public int attackWindup = 0;
      public LivingEntity delayedTarget;

      public StahlMeleeAttackGoal(PathfinderMob mob, double speed, boolean p_25554_, double hitbox, float range, Predicate<LivingEntity> targets) {
         super(mob, speed, p_25554_, hitbox, range, targets);
      }

      public boolean canContinueToUse() {
         return this.attackWindup > 0 ? true : super.canContinueToUse();
      }

      protected void resetAttackCooldown() {
         this.ticksUntilNextAttack = this.adjustedTickDelay(40);
      }

      protected void checkAndPerformAttack(LivingEntity living, double at) {
         double d0 = this.getAttackReachSqr(living);
         PathfinderMob var7 = this.mob;
         if (var7 instanceof Stahlmorder stahlmorder) {
            if (this.ticksUntilNextAttack == 20 && at <= d0) {
               stahlmorder.animationOffset = 20;
               stahlmorder.level().broadcastEntityEvent(stahlmorder, (byte)4);
               stahlmorder.triggerAnimation(stahlmorder.decideAnimation(living));
               stahlmorder.applyAttackEffect(living, (Integer)stahlmorder.entityData.get(Stahlmorder.MELEE_STATE));
            }
         }

         if (at <= d0 && this.ticksUntilNextAttack <= 0 && this.mob.hasLineOfSight(living)) {
            this.resetAttackCooldown();
            var7 = this.mob;
            if (var7 instanceof Stahlmorder) {
               Stahlmorder s = (Stahlmorder)var7;
               this.startDelayedAttack(living, s);
            }
         }

      }

      public void startDelayedAttack(LivingEntity target, Stahlmorder s) {
         this.attackWindup = 15;
         this.delayedTarget = target;
         s.animationOffset = 20;
         s.level().broadcastEntityEvent(s, (byte)4);
         s.triggerAnimation(s.decideAnimation(target));
      }

      private void performDelayedAttack(LivingEntity living) {
         if (this.mob.hasLineOfSight(living)) {
            if (!(this.mob.distanceToSqr(living) > this.getAttackReachSqr(living))) {
               this.mob.swing(InteractionHand.MAIN_HAND);
               this.mob.doHurtTarget(living);
               AABB hitbox = living.getBoundingBox().inflate(this.box);

               for(LivingEntity en : this.mob.level().getEntitiesOfClass(LivingEntity.class, hitbox, this.victims)) {
                  this.mob.doHurtTarget(en);
               }

            }
         }
      }

      public void tick() {
         super.tick();
         if (this.attackWindup > 0) {
            --this.attackWindup;
            if (this.attackWindup == 1 && this.delayedTarget != null && this.delayedTarget.isAlive()) {
               this.performDelayedAttack(this.delayedTarget);
               this.delayedTarget = null;
            }
         }

      }
   }
}
