package com.Harbinger.Spore.Sentities.Hyper;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.PullGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.Utility.HyperClaw;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;

public class Hevoker extends Hyper {
   private static final EntityDataAccessor DEAD;
   private static final EntityDataAccessor HAS_ARM;
   private static final EntityDataAccessor TIME_REGROW;
   private final HevokerPart[] subEntities;
   private final HevokerPart totem = new HevokerPart(this, "totem", 0.5F, 0.5F);
   private final HevokerPart arm1 = new HevokerPart(this, "right_arm1", 0.5F, 0.5F);
   private final HevokerPart arm2 = new HevokerPart(this, "right_arm2", 0.5F, 0.5F);
   private final HevokerPart arm3 = new HevokerPart(this, "right_arm3", 0.5F, 0.5F);
   private final HevokerPart arm4 = new HevokerPart(this, "right_arm4", 0.5F, 0.5F);
   private int reviveTimer = 0;
   private int attackAnimationTick;
   private boolean value = true;

   public Hevoker(EntityType type, Level level) {
      super(type, level);
      this.subEntities = new HevokerPart[]{this.totem, this.arm1, this.arm2, this.arm3, this.arm4};
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   public boolean isMultipartEntity() {
      return true;
   }

   public HevokerPart[] getSubEntities() {
      return this.subEntities;
   }

   public PartEntity[] getParts() {
      return this.subEntities;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
      super.recreateFromPacket(p_218825_);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("fake_death", this.isFakeDead());
      tag.putBoolean("arm", this.hasArm());
      tag.putInt("regrow", this.getTimeRegrow());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setFakeDead(tag.getBoolean("fake_death"));
      this.setArm(tag.getBoolean("arm"));
      this.setTimeRegrow(tag.getInt("regrow"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DEAD, false);
      this.entityData.define(HAS_ARM, true);
      this.entityData.define(TIME_REGROW, 0);
   }

   public boolean isFakeDead() {
      return (Boolean)this.entityData.get(DEAD);
   }

   public void setFakeDead(boolean value) {
      this.entityData.set(DEAD, value);
   }

   public boolean hasArm() {
      return (Boolean)this.entityData.get(HAS_ARM);
   }

   public void setArm(boolean value) {
      this.entityData.set(HAS_ARM, value);
   }

   public void setTimeRegrow(int value) {
      this.entityData.set(TIME_REGROW, value);
   }

   public void tickTimeRegrow() {
      this.entityData.set(TIME_REGROW, (Integer)this.entityData.get(TIME_REGROW) + 1);
   }

   public int getTimeRegrow() {
      return (Integer)this.entityData.get(TIME_REGROW);
   }

   public void tick() {
      super.tick();
      if (this.reviveTimer > 0) {
         if (this.reviveTimer == 1) {
            this.reviveBody();
         }

         --this.reviveTimer;
      }

      if (this.isFakeDead()) {
         this.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
      }

      if (!this.hasArm() && this.tickCount % 20 == 0) {
         this.tickTimeRegrow();
         if (this.getTimeRegrow() >= 300) {
            this.setArm(true);
            this.setTimeRegrow(0);
         }
      }

   }

   public void reviveBody() {
      float hp = (float)((Double)SConfig.SERVER.hevoker_hp.get() * (Double)SConfig.SERVER.global_health.get() / (double)4.0F);
      this.setHealth(hp);
      ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);
      DamageSource source = this.getLastDamageSource();
      if (source != null) {
         ForgeHooks.onLivingUseTotem(this, source, stack, InteractionHand.MAIN_HAND);
      }

      this.setFakeDead(false);
      this.playSound(SoundEvents.TOTEM_USE);
      this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 800, 1));
      this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
      this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.hevoker_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.hevoker_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.hevoker_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.hevoker_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   private boolean switchy() {
      LivingEntity living = this.getTarget();
      if (this.isFakeDead()) {
         return false;
      } else if (living != null && this.canSee(living)) {
         double ze = this.distanceToSqr(living);
         return ze > (double)200.0F && ze < (double)600.0F && (Boolean)this.entityData.get(HAS_ARM) && this.tickCount % 5 == 0;
      } else {
         return false;
      }
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(3, new AOEMeleeAttackGoal(this, 1.2, true, 1.2, 3.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         public boolean canUse() {
            return Hevoker.this.isFakeDead() ? false : super.canUse();
         }

         protected void checkAndPerformAttack(LivingEntity entity, double p_25558_) {
            if (!Hevoker.this.isFakeDead()) {
               super.checkAndPerformAttack(entity, p_25558_);
            }

         }
      });
      this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8) {
         public boolean canUse() {
            return Hevoker.this.isFakeDead() ? false : super.canUse();
         }
      });
      this.goalSelector.addGoal(2, new PullGoal(this, (double)32.0F, (double)8.0F) {
         public boolean canUse() {
            return Hevoker.this.switchy();
         }

         public void start() {
            super.start();
            this.mob.playSound((SoundEvent)Ssounds.HEXEN_SUCK.get());
         }
      });
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
         public boolean canUse() {
            return Hevoker.this.isFakeDead() ? false : super.canUse();
         }
      });
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      return super.doHurtTarget(entity);
   }

   public void aiStep() {
      super.aiStep();
      this.moveHitBoxesAround();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public boolean isInvulnerable() {
      return super.isInvulnerable() || this.isFakeDead();
   }

   public boolean isAttackable() {
      return this.isFakeDead() ? false : super.isAttackable();
   }

   public boolean canSee(Entity entity) {
      if (entity.level() != this.level()) {
         return false;
      } else {
         Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
         Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
         if (vec31.distanceTo(vec3) > (double)128.0F) {
            return false;
         } else {
            return this.level().clip(new ClipContext(vec3, vec31, Block.COLLIDER, Fluid.NONE, this)).getType() == Type.MISS;
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!this.isInPowderSnow && !source.is(DamageTypes.FREEZE) && !(amount > 100.0F)) {
         if (this.isFakeDead()) {
            return false;
         } else if (!this.isFakeDead() && amount > this.getHealth() && !this.isInPowderSnow) {
            this.setFakeDead(true);
            this.setHealth(1.0F);
            this.reviveTimer = 200;
            return true;
         } else {
            if (Math.random() < 0.2) {
               this.performTelekineticThrow();
            }

            return super.hurt(source, amount);
         }
      } else {
         return super.hurt(source, amount);
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (DEAD.equals(dataAccessor)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isFakeDead() ? super.getDimensions(pose).scale(2.2F, 0.25F) : super.getDimensions(pose);
   }

   public boolean hurt(HevokerPart hevokerArm, DamageSource source, float amount) {
      if (Math.random() < 0.2 && this.hasArm() && !this.level().isClientSide && (hevokerArm == this.arm1 || hevokerArm == this.arm2 || hevokerArm == this.arm3 || hevokerArm == this.arm4)) {
         this.SummonClaw();
         this.setArm(false);
      }

      return this.isFakeDead() && hevokerArm == this.totem ? this.hurt(source, Float.MAX_VALUE) : this.hurt(source, amount);
   }

   protected void tickPart(HevokerPart part, Vec3 vec3i) {
      Vec3 vec3 = vec3i.yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      part.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
   }

   public void SummonClaw() {
      Vec3 vec3 = (new Vec3(0.3, (double)0.5F, -0.8)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      HyperClaw claw = new HyperClaw((EntityType)Sentities.HEVOKER_ARM.get(), this.level());
      claw.moveTo(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
      this.level().addFreshEntity(claw);
      this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
   }

   public void moveHitBoxesAround() {
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.arm1, this.isFakeDead() ? new Vec3((double)0.0F, (double)0.0F, (double)-0.5F) : new Vec3(0.3, (double)0.5F, -0.8));
      this.tickPart(this.arm2, this.isFakeDead() ? new Vec3((double)0.5F, (double)0.0F, (double)-1.0F) : new Vec3(0.3, (double)1.0F, -0.8));
      this.tickPart(this.arm3, this.isFakeDead() ? new Vec3(0.6, (double)0.0F, (double)-1.5F) : new Vec3(0.3, (double)1.5F, -0.8));
      this.tickPart(this.arm4, this.isFakeDead() ? new Vec3(0.6, (double)0.0F, (double)-2.0F) : new Vec3(0.3, (double)2.0F, -0.8));
      this.tickPart(this.totem, this.isFakeDead() ? new Vec3(-0.2, (double)0.5F, (double)0.0F) : new Vec3((double)0.5F, 1.8, (double)0.0F));

      for(int l = 0; l < this.subEntities.length; ++l) {
         this.subEntities[l].xo = avec3[l].x;
         this.subEntities[l].yo = avec3[l].y;
         this.subEntities[l].zo = avec3[l].z;
         this.subEntities[l].xOld = avec3[l].x;
         this.subEntities[l].yOld = avec3[l].y;
         this.subEntities[l].zOld = avec3[l].z;
      }

   }

   public InteractionResult interact(HevokerPart hevokerPart, Player player, InteractionHand hand) {
      if (this.isFakeDead() && hevokerPart == this.totem && this.reviveTimer > 20 && this.value) {
         this.hurt(this.damageSources().playerAttack(player), Float.MAX_VALUE);
         this.createTotem();
         this.value = false;
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   public void createTotem() {
      ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);
      ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
      this.level().addFreshEntity(entity);
   }

   protected SoundEvent getAmbientSound() {
      return this.isFakeDead() ? null : (SoundEvent)Ssounds.HEVOKER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   public void performTelekineticThrow() {
      AABB aabb = this.getBoundingBox().inflate((double)8.0F);
      List<Entity> entities = this.level().getEntities(this, aabb, (entityx) -> {
         boolean var10000;
         if (entityx instanceof LivingEntity living) {
            if (this.TARGET_SELECTOR.test(living)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      if (entities.size() > 1) {
         for(Entity entity : entities) {
            entity.setDeltaMovement(entity.getDeltaMovement().add((double)0.0F, (double)1.0F, (double)0.0F));
         }

         this.swing(InteractionHand.MAIN_HAND);
      }

      this.playSound((SoundEvent)Ssounds.HEXEN_BLOW.get());
   }

   static {
      DEAD = SynchedEntityData.defineId(Hevoker.class, EntityDataSerializers.BOOLEAN);
      HAS_ARM = SynchedEntityData.defineId(Hevoker.class, EntityDataSerializers.BOOLEAN);
      TIME_REGROW = SynchedEntityData.defineId(Hevoker.class, EntityDataSerializers.INT);
   }
}
