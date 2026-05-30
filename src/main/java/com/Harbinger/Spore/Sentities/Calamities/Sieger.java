package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.FallenMultipart.SiegerTail;
import com.Harbinger.Spore.Sentities.Projectile.ThrownTumor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class Sieger extends Calamity implements RangedAttackMob, TrueCalamity {
   public static final EntityDataAccessor TAIL_HP;
   public static final EntityDataAccessor ADAPTATION;
   private final CalamityMultipart[] subEntities;
   public final CalamityMultipart lowerbody;
   public final CalamityMultipart head;
   public final CalamityMultipart tail;
   public final CalamityMultipart tail2;
   private final List<HitboxesForParts> innatePartList;

   public Sieger(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.SIEGER_BODY, HitboxesForParts.SIEGER_JAW, HitboxesForParts.SIEGER_RIGHT_LEG, HitboxesForParts.SIEGER_LEFT_LEG, HitboxesForParts.SIEGER_BACK_RIGHT_LEG, HitboxesForParts.SIEGER_BACK_LEFT_LEG);
      this.lowerbody = new CalamityMultipart(this, "lowerbody", 3.0F, 3.0F);
      this.tail = new CalamityMultipart(this, "tail", 1.5F, 1.5F);
      this.tail2 = new CalamityMultipart(this, "tail", 1.5F, 1.5F);
      this.head = new CalamityMultipart(this, "head", 1.4F, 1.4F);
      this.subEntities = new CalamityMultipart[]{this.lowerbody, this.tail, this.tail2, this.head};
      this.setMaxUpStep(1.5F);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.sieger_loot.get();
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
      if (this.getHealth() >= this.getMaxHealth() && this.getTailHp() < this.getMaxTailHp() && this.tickCount % 40 == 0) {
         this.setTailHp(this.getTailHp() + 1.0F);
      }

      if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
         this.entityData.set(ADAPTATION, (Integer)this.entityData.get(ADAPTATION) + 1);
      }

   }

   public void aiStep() {
      float f14 = this.getYRot() * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f14);
      float f15 = Mth.cos(f14);
      Vec3[] avec3 = new Vec3[this.subEntities.length];

      for(int j = 0; j < this.subEntities.length; ++j) {
         avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
      }

      if (this.getTailHp() > 0.0F) {
         this.tickPart(this.tail, new Vec3((double)-1.5F, (double)7.0F, (double)0.0F));
      } else {
         this.tickPart(this.tail, (double)(f2 * 2.0F), (double)1.0F, (double)(-f15 * 2.0F));
      }

      if (this.getTailHp() > 0.0F) {
         this.tickPart(this.tail2, new Vec3((double)-3.0F, (double)4.0F, (double)0.0F));
      } else {
         this.tickPart(this.tail2, (double)(f2 * 2.0F), (double)1.0F, (double)(-f15 * 2.0F));
      }

      this.tickPart(this.head, (double)(f2 * -2.5F), 1.4, (double)(-f15 * -2.5F));
      this.tickPart(this.lowerbody, (double)(f2 * 3.0F), (double)0.0F, (double)(-f15 * 3.0F));

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

   boolean calculateHeight() {
      return this.getTarget() != null && this.getTarget().getY() > this.getY() && Math.abs(Math.abs(this.getTarget().getY()) - Math.abs(this.getY())) > (double)5.0F;
   }

   boolean calculateDistance() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)400.0F;
   }

   public boolean hasLineOfSight(Entity entity) {
      return !this.calculateDistance() && !this.calculateHeight() ? super.hasLineOfSight(entity) : true;
   }

   private int[] ammoAmount() {
      int[] values = new int[2];
      if (this.isAdapted()) {
         values[0] = 5;
         values[1] = 8;
      } else {
         values[0] = 3;
         values[1] = 6;
      }

      return values;
   }

   public void registerGoals() {
      this.goalSelector.addGoal(3, new ScatterShotRangedGoal(this, (double)1.5F, 80, 48.0F, this.ammoAmount()[0], this.ammoAmount()[1]) {
         public boolean canUse() {
            if (Sieger.this.getTailHp() <= 0.0F) {
               return false;
            } else {
               return super.canUse() && (Sieger.this.calculateHeight() || Sieger.this.calculateDistance());
            }
         }
      });
      this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)1.5F, false, (double)2.5F, 6.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)) {
         protected double getAttackReachSqr(LivingEntity entity) {
            float f = Sieger.this.getBbWidth();
            return (double)(f * 3.0F * f * 3.0F + entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.2));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      this.goalSelector.addGoal(9, new RandomStrollGoal(this, (double)1.0F));
      super.registerGoals();
   }

   public boolean canDisableShield() {
      return true;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sieger_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, (double)0.25F).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.sieger_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sieger_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   protected SoundEvent getAmbientSound() {
      return this.getTarget() != null && this.distanceToSqr(this.getTarget()) > (double)200.0F ? null : (SoundEvent)Ssounds.SIEGER_AMBIENT.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.RAVAGER_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
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

   public boolean hurt(DamageSource source, float amount) {
      return super.hurt(source, this.isAdapted() ? amount * 0.7F : amount);
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.sieger_dpsr.get();
   }

   public void performRangedAttack(LivingEntity livingEntity, float p_33318_) {
      if (!this.level().isClientSide) {
         ThrownTumor tumor = new ThrownTumor(this.level(), this);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight() + (double)5.0F;
         double dz = livingEntity.getZ() - this.getZ();
         if (SConfig.SERVER.sieger_explosive_effects != null) {
            List<? extends String> ev = (List)SConfig.SERVER.sieger_explosive_effects.get();

            for(int i = 0; i < 1; ++i) {
               int randomIndex = this.random.nextInt(ev.size());
               ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
               MobEffect randomElement = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(randomElement1);
               tumor.setMobEffect(randomElement);
            }
         }

         tumor.setExplode(ExplosionInteraction.MOB);
         tumor.moveTo(this.getX(), this.getY() + 8.2, this.getZ());
         tumor.shoot(dx, dy - tumor.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 2.0F, 12.0F);
         this.level().addFreshEntity(tumor);
      }

   }

   public boolean isAdapted() {
      return (Integer)this.entityData.get(ADAPTATION) >= 900;
   }

   public void ActivateAdaptation() {
      this.entityData.set(ADAPTATION, 900);
   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TAIL_HP, this.getMaxTailHp());
      this.entityData.define(ADAPTATION, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putFloat("tail_hp", (Float)this.entityData.get(TAIL_HP));
      tag.putInt("adaptation", (Integer)this.entityData.get(ADAPTATION));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TAIL_HP, tag.getFloat("tail_hp"));
      this.entityData.set(ADAPTATION, tag.getInt("adaptation"));
   }

   public float getTailHp() {
      return (Float)this.entityData.get(TAIL_HP);
   }

   public void setTailHp(float i) {
      this.entityData.set(TAIL_HP, i);
   }

   public float getMaxTailHp() {
      return (float)((Double)SConfig.SERVER.sieger_hp.get() / (double)4.0F);
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      if (calamityMultipart == this.tail || calamityMultipart == this.tail2) {
         this.hurt(source, this.isAdapted() ? value : value * 2.0F);
         float lostHealth = this.getTailHp() - this.getDamageAfterArmorAbsorb(source, value);
         this.setTailHp(lostHealth > 0.0F ? lostHealth : (this.getTailHp() != 0.0F ? this.SummonDetashedTail() : 0.0F));
      }

      if (calamityMultipart == this.head) {
         this.hurt(source, value * 0.75F);
      } else {
         this.hurt(source, value);
      }

      return true;
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.sieger_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.sieger_debuffs.get();
   }

   private float SummonDetashedTail() {
      SiegerTail siegerTail = new SiegerTail((EntityType)Sentities.SIEGER_TAIL.get(), this.level());
      Vec3 vec3 = (new Vec3(-1.7, (double)0.0F, (double)0.0F)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      siegerTail.setWar(this.isAdapted());
      siegerTail.moveTo(this.getX() + vec3.x, this.getY() + 1.6, this.getZ() + vec3.z);
      this.level().addFreshEntity(siegerTail);
      this.playSound((SoundEvent)Ssounds.LIMB_SLASH.get());
      return 0.0F;
   }

   public String getMutation() {
      return this.isAdapted() ? "spore.entity.variant.war_torn" : super.getMutation();
   }

   public boolean getAdaptation() {
      return this.isAdapted();
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList();
      if (this.getTailHp() > 0.0F) {
         values.add(HitboxesForParts.SIEGER_TAIL);
      }

      for(HitboxesForParts hitboxes : this.innatePartList) {
         HitboxesForParts part = this.calculateChance(hitboxes, 0.75F);
         if (part != null) {
            values.add(part);
         }
      }

      return values;
   }

   static {
      TAIL_HP = SynchedEntityData.defineId(Sieger.class, EntityDataSerializers.FLOAT);
      ADAPTATION = SynchedEntityData.defineId(Sieger.class, EntityDataSerializers.INT);
   }
}
