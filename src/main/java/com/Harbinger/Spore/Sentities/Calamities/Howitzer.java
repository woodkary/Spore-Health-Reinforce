package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.LeapGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.FallenMultipart.HowitzerArm;
import com.Harbinger.Spore.Sentities.Projectile.FleshBomb;
import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Howitzer extends Calamity implements TrueCalamity, RangedAttackMob {
   public static final EntityDataAccessor<Float> RIGHT_ARM;
   public static final EntityDataAccessor<Float> LEFT_ARM;
   public static final EntityDataAccessor<Integer> ORES;
   public static final EntityDataAccessor<Integer> NUKE;
   public static final EntityDataAccessor<Integer> SELF_DETONATION;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart rightArm;
   public final CalamityMultipart leftArm;
   public final CalamityMultipart mouth;
   public int getLeapTime = 0;
   private @Nullable BlockPos Targetpos;
   private final List<HitboxesForParts> innatePartList;

   public Howitzer(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.HOWI_CANNON1, HitboxesForParts.HOWI_CANNON2, HitboxesForParts.HOWI_CANNON3, HitboxesForParts.HOWI_LEFT_LEG, HitboxesForParts.HOWI_RIGHT_LEG, HitboxesForParts.HOWI_SACK);
      this.rightArm = new CalamityMultipart(this, "rightarm", 2.0F, 4.0F);
      this.leftArm = new CalamityMultipart(this, "leftarm", 2.0F, 4.0F);
      this.mouth = new CalamityMultipart(this, "mouth", 4.0F, 3.0F);
      this.subEntities = new CalamityMultipart[]{this.rightArm, this.leftArm, this.mouth};
      this.setMaxUpStep(1.5F);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
   }

   public void setId(int p_20235_) {
      super.setId(p_20235_);

      for(int i = 0; i < this.subEntities.length; ++i) {
         this.subEntities[i].setId(p_20235_ + i + 1);
      }

   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.howit_dpsr.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.howit_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.howit_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.howit_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)128.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }
   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      if(this.getSelfDetonation()>0&&!source.is(DamageTypes.FREEZE)){
         AttributeInstance instance = this.getAttribute(SAttributes.BALLISTIC.get());
         if (instance != null) {
            double level = instance.getValue();
            amount*= (float) (1.0-level/64.0);
         }
      }
      super.actuallyHurt(source, amount);
   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound((SoundEvent)Ssounds.LANDING.get(), 0.5F, 0.5F);
      return super.doHurtTarget(entity);
   }

   public void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(3, new LeapGoal(this, 0.9F) {
         public boolean canUse() {
            return Howitzer.this.getGetLeapTime() <= 0 && Howitzer.this.hasBothArms() && Howitzer.this.isInMeleeRange() && super.canUse();
         }

         public void start() {
            super.start();
            Howitzer.this.setLeapTicks(200);
         }
      });
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)1.0F, true, (double)2.0F, 5.0F, (e) -> this.TARGET_SELECTOR.test(e)) {
         public boolean canUse() {
            return Howitzer.this.isInMeleeRange() && super.canUse();
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Howitzer.this.getBbWidth();
            return (double)(f * 1.5F * f * 1.5F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new HowitzerRangedAttackGoal(this, (double)1.0F, 80, 64.0F, 1, 5) {
         public boolean canUse() {
            return !Howitzer.this.isInMeleeRange() && super.canUse();
         }
      });
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.2));
      this.goalSelector.addGoal(5, new SearchAroundGoal(this));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      this.goalSelector.addGoal(9, new RandomStrollGoal(this, (double)1.0F));
   }

   public void aiStep() {
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      this.tickPart(this.mouth, Vec3.ZERO, (double)5.0F);
      if (this.getRightArmHp() > 0.0F) {
         this.tickPart(this.rightArm, new Vec3(-3.85, (double)0.0F, (double)4.0F));
      } else {
         this.tickPart(this.rightArm, Vec3.ZERO);
         this.rightArm.getBoundingBox().inflate((double)1.0F, 0.3, (double)1.0F);
      }

      if (this.getLeftArmHp() > 0.0F) {
         this.tickPart(this.leftArm, new Vec3(3.85, (double)0.0F, (double)-4.0F));
      } else {
         this.tickPart(this.leftArm, Vec3.ZERO);
         this.leftArm.getBoundingBox().inflate((double)1.0F, 0.3, (double)1.0F);
      }

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
   private void releaseFinalNuke(){
      NukeEntity nukeEntity = new NukeEntity((EntityType)Sentities.NUKE.get(), this.level());
      nukeEntity.setInitRange(3.0F);
      nukeEntity.setRange((float)((Double)SConfig.SERVER.nuke_range.get() * (double)2.0F));
      nukeEntity.setInitDuration(0);
      nukeEntity.setDuration((Integer)SConfig.SERVER.nuke_time.get());
      nukeEntity.setDamage((float)((Double)SConfig.SERVER.nuke_damage.get() * (double)1.0F));
      nukeEntity.isFinalAttack(true);
      nukeEntity.livingEntityPredicate = this.TARGET_SELECTOR;
      nukeEntity.setPos(this.getX(), this.getY(), this.getZ());
      this.level().addFreshEntity(nukeEntity);
      Level var3 = this.level();
      if (var3 instanceof ServerLevel) {
         ServerLevel serverLevel = (ServerLevel)var3;
         Utilities.explodeCircle(serverLevel, this, this.getOnPos(), 15.0, (float)((Double)SConfig.SERVER.howit_damage.get() * 1.0), 8.0, (entity) -> {
             if (entity instanceof LivingEntity livingEntity) {
                return this.TARGET_SELECTOR.test(livingEntity);
            }

             return false;
         });
      }
   }
   public void tickDetonation() {
      if (this.entityData.get(SELF_DETONATION) >= 30) {
         releaseFinalNuke();
         this.discard();
      } else {
         this.entityData.set(SELF_DETONATION, this.entityData.get(SELF_DETONATION) + 1);
      }

   }

   public int getSelfDetonation() {
      return (Integer)this.entityData.get(SELF_DETONATION);
   }

   public boolean hasBothArms() {
      return this.getRightArmHp() > 0.0F && this.getLeftArmHp() > 0.0F;
   }

   public boolean isInMeleeRange() {
      LivingEntity living = this.getTarget();
      return living != null && this.distanceToSqr(living) < (double)200.0F;
   }

   public int getGetLeapTime() {
      return this.getLeapTime;
   }

   public void setLeapTicks(int i) {
      this.getLeapTime = i;
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

   public void recreateFromPacket(ClientboundAddEntityPacket entityPacket) {
      super.recreateFromPacket(entityPacket);
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if(this.getSelfDetonation()>0&&!source.is(DamageTypes.FREEZE)){
         AttributeInstance instance = this.getAttribute(SAttributes.BALLISTIC.get());
         if (instance != null) {
            double level = instance.getValue();
            value*= (float) (1.0-level/64.0);
            return hurt(source, value);
         }
      }
      if (calamityMultipart == this.mouth) {
         this.hurt(source, value * 2.0F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value * 0.8f);
      } else if (calamityMultipart == this.rightArm) {
         this.hurt(source, value * 1.5F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value * 0.4f);
         float lostHealth = this.getRightArmHp() - this.getDamageAfterArmorAbsorb(source, value);
         this.setRightArmHp(lostHealth > 0.0F ? lostHealth : (this.getRightArmHp() != 0.0F ? this.summonDetashedPart(true) : 0.0F));
      } else if (calamityMultipart == this.leftArm) {
         this.hurt(source, value * 1.5F);
         SporeEntityHeeaafastthManager.INSTANCE.hurrt(this, source, value * 0.4f);
         float lostHealth = this.getLeftArmHp() - this.getDamageAfterArmorAbsorb(source, value);
         this.setLeftArmHp(lostHealth > 0.0F ? lostHealth : (this.getLeftArmHp() != 0.0F ? this.summonDetashedPart(false) : 0.0F));
      } else {
         this.hurt(source, value);
      }

      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      if(this.getSelfDetonation()>0&&!source.is(DamageTypes.FREEZE)){
         AttributeInstance instance = this.getAttribute(SAttributes.BALLISTIC.get());
         if (instance != null) {
            double level = instance.getValue();
            amount*= (float) (1.0-level/64.0);
         }
      }
      if (source.getEntity() != null && this.random.nextFloat() < 0.2F) {
         this.setTarget(null);
      }

      if (source.is(DamageTypes.FREEZE)) {
         this.entityData.set(NUKE, 0);
      }

      if (this.getHealth() <= 50.0F && this.isRadioactive() && this.getSelfDetonation() <= 0) {
         this.tickDetonation();
      }

      return super.hurt(source, amount);
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.howit_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.howit_debuffs.get();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(RIGHT_ARM, this.getMaxArmHp());
      this.entityData.define(LEFT_ARM, this.getMaxArmHp());
      this.entityData.define(ORES, 0);
      this.entityData.define(NUKE, 0);
      this.entityData.define(SELF_DETONATION, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("right_arm", (Float)this.entityData.get(RIGHT_ARM));
      tag.putFloat("left_arm", (Float)this.entityData.get(LEFT_ARM));
      tag.putInt("ores", (Integer)this.entityData.get(ORES));
      tag.putInt("nuke", (Integer)this.entityData.get(NUKE));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(RIGHT_ARM, tag.getFloat("right_arm"));
      this.entityData.set(LEFT_ARM, tag.getFloat("left_arm"));
      this.entityData.set(ORES, tag.getInt("ores"));
      this.entityData.set(NUKE, tag.getInt("nuke"));
   }

   public float getRightArmHp() {
      return (Float)this.entityData.get(RIGHT_ARM);
   }

   public void setRightArmHp(float i) {
      this.entityData.set(RIGHT_ARM, i);
   }

   public float getLeftArmHp() {
      return (Float)this.entityData.get(LEFT_ARM);
   }

   public void setLeftArmHp(float i) {
      this.entityData.set(LEFT_ARM, i);
   }

   public float getMaxArmHp() {
      return (float)((Double)SConfig.SERVER.howit_hp.get() / (double)4.0F);
   }

   public boolean hasLineOfSight(Entity entity) {
      return !this.canEntitySeeTheSky(entity) && !(entity.distanceToSqr(this) < (double)200.0F) ? super.hasLineOfSight(entity) : true;
   }

   private boolean canEntitySeeTheSky(Entity entity) {
      return entity.level().canSeeSky(entity.getOnPos());
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      if (this.getLeapTime > 140) {
         this.damageStomp(this.level(), this.getOnPos(), (double)12.0F, (double)8.0F);
      }

      return super.calculateFallDamage(p_149389_, p_149390_) - 25;
   }

   public void tick() {
      super.tick();
      if (this.getGetLeapTime() > 0) {
         --this.getLeapTime;
      }

      if (this.tickCount % 20 == 0 && this.getHealth() == this.getMaxHealth()) {
         if (this.getRightArmHp() < this.getMaxArmHp()) {
            this.setRightArmHp(this.getRightArmHp() + 1.0F);
         }

         if (this.getLeftArmHp() < this.getMaxArmHp()) {
            this.setLeftArmHp(this.getLeftArmHp() + 1.0F);
         }
      }

      if (this.tickCount % 20 == 0) {
         this.createBomb();
         if (this.isRadioactive()) {
            this.spreadRadiation();
         }
      }

      if (this.tickCount % 200 == 0) {
         this.searchBlocks();
      }

      if (this.isRadioactive() && this.getSelfDetonation() > 0) {
         if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            this.tickDetonation();
         }

         for(int i = 0; i < 360; ++i) {
            if (i % 40 == 0) {
               this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + (double)2.0F, this.getZ(), Math.cos((double)i) * (double)0.25F, (double)0.25F, Math.sin((double)i) * (double)0.25F);
               this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + (double)2.0F, this.getZ(), Math.sin((double)i) * (double)0.25F, (double)-0.25F, Math.cos((double)i) * (double)0.25F);
            }
         }
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.howit_loot.get();
   }

   public float summonDetashedPart(boolean isRight) {
      if (this.level().isClientSide) {
         return 0.0F;
      }

      double offset = isRight ? (double)3.0F : (double)-3.0F;
      Vec3 vec3 = (new Vec3((double)0.0F, (double)0.0F, offset)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      HowitzerArm arm = new HowitzerArm((EntityType)Sentities.HOWIT_ARM.get(), this.level());
      arm.setRight(isRight);
      arm.setNuclear(this.isRadioactive());
      arm.moveTo(this.getX() + vec3.x, this.getY() + 1.6, this.getZ() + vec3.z);
      this.level().addFreshEntity(arm);
      this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
      return 0.0F;
   }

   protected void damageStomp(Level level, BlockPos pos, double range, double damageRange) {
      AABB aabb = this.getBoundingBox().inflate(damageRange);
      List<Entity> entities = level.getEntities(this, aabb, (entity) -> {
         boolean var10000;
         if (entity instanceof LivingEntity living) {
            if (this.TARGET_SELECTOR.test(living)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      if (level instanceof ServerLevel serverLevel) {
         for(int i = 0; (double)i <= (double)2.0F * range; ++i) {
            for(int j = 0; (double)j <= (double)2.0F * range; ++j) {
               for(int k = 0; (double)k <= (double)2.0F * range; ++k) {
                  double distance = (double)Mth.sqrt((float)(((double)i - range) * ((double)i - range) + ((double)j - range) * ((double)j - range) + ((double)k - range) * ((double)k - range)));
                  if ((Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) && distance < range + (double)0.5F) {
                     BlockPos blockpos = pos.offset(i - (int)range, j - (int)range, k - (int)range);
                     BlockState state = level.getBlockState(blockpos);
                     boolean airBelow = level.getBlockState(blockpos.below()).isAir();
                     if (airBelow && state.getDestroySpeed(level, pos) >= 0.0F && Math.random() < 0.3 && !state.isAir()) {
                        FallingBlockEntity.fall(serverLevel, blockpos, state);
                        serverLevel.removeBlock(blockpos, false);
                     }
                  }
               }
            }
         }
      }

      for(Entity entity : entities) {
         if (entity instanceof LivingEntity living) {
            for(int i = 0; i < 2; ++i) {
               this.doHurtTarget(living);
               living.hurtTime = 0;
               living.invulnerableTime = 0;
            }
         }
      }

      this.playSound((SoundEvent)Ssounds.LANDING.get());
   }

   private FleshBomb.BombType compareEntity(LivingEntity living, int burnable) {
      AABB aabb = living.getBoundingBox().inflate((double)4.0F);
      List<Entity> extra_targets = this.level().getEntities(living, aabb, (entity) -> {
         boolean var10000;
         if (entity instanceof LivingEntity livingEntity) {
            if (this.TARGET_SELECTOR.test(livingEntity)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      if (!this.isRadioactive() || !this.hasNuke() || !(living.getMaxHealth() >= 100.0F) && living.getArmorValue() < 20) {
         if (burnable > 8) {
            return Math.random() < (double)0.3F ? FleshBomb.BombType.BILE : FleshBomb.BombType.FLAME;
         } else if (((List)SConfig.SERVER.corrosion.get()).contains(living.getEncodeId())) {
            return FleshBomb.BombType.ACID;
         } else {
            return extra_targets.size() <= 1 && living.getArmorValue() < 10 ? FleshBomb.BombType.BASIC : FleshBomb.BombType.BILE;
         }
      } else {
         this.entityData.set(NUKE, 0);
         return FleshBomb.BombType.NUCLEAR;
      }
   }

   public boolean isRadioactive() {
      return (Integer)this.entityData.get(ORES) >= 100;
   }

   public void ActivateAdaptation() {
      super.ActivateAdaptation();
      this.entityData.set(ORES, 100);
   }

   public void performRangedAttack(LivingEntity entity, float val) {
      float damage = (float)((Double)SConfig.SERVER.howit_ranged_damage.get() * (Double)SConfig.SERVER.global_damage.get());
      FleshBomb bomb = new FleshBomb(this.level(), this, damage, this.compareEntity(entity, (int)val), this.random.nextInt(4, 7));
      bomb.setLivingEntityPredicate(this.TARGET_SELECTOR);
      bomb.setCarrier(Math.random() < (double)0.2F);
      bomb.setTarget(entity);
      double dx = entity.getX() - this.getX();
      double dz = entity.getZ() - this.getZ();
      double dy = entity.getY() - this.getY();
      float value = this.random.nextFloat() * 0.5F;
      bomb.moveTo(this.getX() + (double)value, this.getY() + (double)7.0F, this.getZ() + (double)value);
      bomb.shoot(dx * (double)0.085F, (double)6.5F + Math.hypot(dx, dz) * (double)0.02F + (dy > (double)0.0F ? dy : (double)0.0F), dz * (double)0.085F, 2.0F, 14.0F);
      this.level().addFreshEntity(bomb);
      this.playSound((SoundEvent)Ssounds.FALLING_BOMB.get());
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HOWITZER_AMBIENT.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.RAVAGER_STEP;
   }

   protected void grief(AABB aabb) {
      boolean flag = false;

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.is(Utilities.biomass)) {
            flag = this.level().setBlock(blockpos, ((Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) || flag;
            this.breakCounter = 0;
         } else if (blockstate.getDestroySpeed(this.level(), blockpos) < (float)this.getDestroySpeed() && blockstate.getDestroySpeed(this.level(), blockpos) >= 0.0F && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            if (blockstate.is(TagKey.create(Registries.BLOCK, new ResourceLocation("forge:ores")))) {
               this.entityData.set(ORES, (Integer)this.entityData.get(ORES) + 1);
            }

            flag = this.level().destroyBlock(blockpos, false, this) || flag;
            this.breakCounter = 0;
         }
      }

   }

   public boolean hasLineOfSightBlocks(BlockPos pos) {
      BlockHitResult raytraceresult = this.level().clip(new ClipContext(this.getEyePosition(1.0F), new Vec3((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), ClipContext.Block.COLLIDER, Fluid.NONE, this));
      BlockPos position = raytraceresult.getBlockPos();
      return pos.equals(position) || this.level().isEmptyBlock(pos) || this.level().getBlockEntity(pos) == this.level().getBlockEntity(position);
   }

   public void searchBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)32.0F, (double)4.0F, (double)32.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState block = this.level().getBlockState(blockpos);
         if (block.is(TagKey.create(Registries.BLOCK, new ResourceLocation("forge:ores"))) && this.hasLineOfSightBlocks(blockpos) && this.random.nextFloat() < 0.5F) {
            this.setTargetPos(blockpos);
            break;
         }
      }

   }

   private @Nullable BlockPos getTargetPos() {
      return this.Targetpos;
   }

   public void setTargetPos(@Nullable BlockPos pos) {
      this.Targetpos = pos;
   }

   public boolean hasNuke() {
      return (Integer)this.entityData.get(NUKE) > 60;
   }

   protected void createBomb() {
      if (this.isRadioactive() && !this.hasNuke()) {
         this.entityData.set(NUKE, (Integer)this.entityData.get(NUKE) + 1);
      }

   }

   public String getMutation() {
      return this.isRadioactive() ? "spore.entity.variant.irradiated" : super.getMutation();
   }

   public void spreadRadiation() {
      for(Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate((double)12.0F))) {
         if (entity instanceof LivingEntity living) {
            if (this.TARGET_SELECTOR.test(living)) {
               this.addEffect(living);
            }
         }
      }

   }

   public void addEffect(LivingEntity living) {
      if (ModList.get().isLoaded("alexscaves")) {
         MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("alexscaves:irradiated"));
         if (effect != null) {
            living.addEffect(new MobEffectInstance(effect, 400, 0));
         }
      } else {
         living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
      }

   }

   public boolean getAdaptation() {
      return this.isRadioactive();
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();
      if (this.getRightArmHp() > 0.0F) {
         values.add(HitboxesForParts.HOWI_RIGHT_ARM);
      }

      if (this.getLeftArmHp() > 0.0F) {
         values.add(HitboxesForParts.HOWI_LEFT_ARM);
      }

      for(HitboxesForParts hitboxes : this.innatePartList) {
         HitboxesForParts part = this.calculateChance(hitboxes, 0.65F);
         if (part != null) {
            values.add(part);
         }
      }

      return values;
   }

   static {
      RIGHT_ARM = SynchedEntityData.defineId(Howitzer.class, EntityDataSerializers.FLOAT);
      LEFT_ARM = SynchedEntityData.defineId(Howitzer.class, EntityDataSerializers.FLOAT);
      ORES = SynchedEntityData.defineId(Howitzer.class, EntityDataSerializers.INT);
      NUKE = SynchedEntityData.defineId(Howitzer.class, EntityDataSerializers.INT);
      SELF_DETONATION = SynchedEntityData.defineId(Howitzer.class, EntityDataSerializers.INT);
   }

   public static class HowitzerRangedAttackGoal extends ScatterShotRangedGoal {
      private double pathedTargetX;
      private double pathedTargetY;
      private double pathedTargetZ;
      private int ticksUntilNextPathRecalculation;
      private boolean holdingPosition;

      public HowitzerRangedAttackGoal(RangedAttackMob mob, double speed, int interval, float range, int min, int max) {
         super(mob, speed, interval, range, min, max);
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      private int getBurnable(LivingEntity target) {
         AABB aabb = target.getBoundingBox().inflate((double)4.0F);
         List<BlockPos> burnable_material = new ArrayList();

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            if (this.mob.level().getBlockState(blockpos).isFlammable(this.mob.level(), blockpos, Direction.UP)) {
               burnable_material.add(blockpos);
            }
         }

         return burnable_material.size();
      }

      public void stop() {
         super.stop();
         this.holdingPosition = false;
         this.ticksUntilNextPathRecalculation = 0;
         this.pathedTargetX = (double)0.0F;
         this.pathedTargetY = (double)0.0F;
         this.pathedTargetZ = (double)0.0F;
      }

      public void tick() {
         double d0 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
         boolean flag = this.mob.getSensing().hasLineOfSight(this.target);
         if (flag) {
            ++this.seeTime;
         } else {
            this.seeTime = 0;
         }

         if (!(d0 > (double)this.attackRadiusSqr) && this.seeTime >= 5) {
            if (!this.holdingPosition) {
               this.mob.getNavigation().stop();
               this.holdingPosition = true;
            }

            this.ticksUntilNextPathRecalculation = 0;
         } else {
            this.holdingPosition = false;
            this.updateNavigation(d0);
         }

         this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
         if (--this.attackTime == 0) {
            if (!flag) {
               return;
            }

            RandomSource randomSource = RandomSource.create();
            int shot = randomSource.nextInt(this.minShots, this.maxShots + this.getExtraShots());
            float f = (float)Math.sqrt(d0) / this.attackRadius;
            float f1 = (float)this.getBurnable(this.target);

            for(int i = 0; i < shot; ++i) {
               this.rangedAttackMob.performRangedAttack(this.target, f1);
            }

            this.attackTime = Mth.floor(f * (float)this.attackInterval + (float)this.attackInterval);
         } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(d0) / (double)this.attackRadius, (double)this.attackInterval, (double)this.attackInterval));
         }

      }

      private void updateNavigation(double distanceToTargetSqr) {
         this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
         if (this.ticksUntilNextPathRecalculation > 0 && !this.mob.getNavigation().isDone() && this.target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) < (double)4.0F) {
            return;
         }

         this.pathedTargetX = this.target.getX();
         this.pathedTargetY = this.target.getY();
         this.pathedTargetZ = this.target.getZ();
         this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
         if (distanceToTargetSqr > (double)1024.0F) {
            this.ticksUntilNextPathRecalculation += 10;
         } else if (distanceToTargetSqr > (double)256.0F) {
            this.ticksUntilNextPathRecalculation += 5;
         }

         if (!this.mob.getNavigation().moveTo(this.target, this.speedModifier)) {
            this.ticksUntilNextPathRecalculation += 15;
         }

         this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
      }
   }

   public static class SearchAroundGoal extends Goal {
      private final Howitzer howitzer;
      public int tryTicks;

      public SearchAroundGoal(Howitzer specter) {
         this.howitzer = specter;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         return this.howitzer.getTargetPos() != null && this.howitzer.getTarget() == null;
      }

      protected void moveToBlock(BlockPos pos) {
         if (pos != null) {
            this.howitzer.navigation.moveTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)1.0F, (double)pos.getZ() + (double)0.5F, (double)1.0F);
         }

      }

      public void start() {
         this.moveToBlock(this.howitzer.getTargetPos());
         this.tryTicks = 0;
         super.start();
      }

      public boolean canContinueToUse() {
         return this.howitzer.getTarget() == null;
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         BlockPos pos = this.howitzer.getTargetPos();
         if (pos != null && this.shouldRecalculatePath()) {
            this.moveToBlock(pos);
         }

         if (pos != null && pos.closerToCenterThan(this.howitzer.position(), (double)7.0F)) {
            this.howitzer.level().destroyBlock(pos, false, this.howitzer);
            this.howitzer.entityData.set(Howitzer.ORES, (Integer)this.howitzer.entityData.get(Howitzer.ORES) + 1);
            this.howitzer.setTargetPos((BlockPos)null);
            this.howitzer.searchBlocks();
         }

      }
   }
}
