package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.Harbinger.Spore.Sentities.Utility.GastGeber;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

public class Scamper extends EvolvedInfected implements WaterInfected, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   public static final EntityDataAccessor AGE;
   public int deployClock = 0;
   public boolean deploying;

   public Scamper(EntityType type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
      this.navigation = new HybridPathNavigation(this, this.level());
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public boolean isDeploying() {
      return this.deploying;
   }

   public void setDeploying(boolean deploying) {
      this.deploying = deploying;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.sca_loot.get();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("age", (Integer)this.entityData.get(AGE));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(AGE, tag.getInt("age"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(AGE, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void setAge(int e) {
      this.entityData.set(AGE, e);
   }

   public int getAge() {
      return (Integer)this.entityData.get(AGE);
   }

   public boolean canDrownInFluidType(FluidType type) {
      return this.getVariant() == ScamperVariants.DROWNED ? false : super.canDrownInFluidType(type);
   }

   public void travel(Vec3 vec3) {
      if (this.isEffectiveAi() && this.isInFluidType() && this.getVariant() == ScamperVariants.DROWNED) {
         this.moveRelative(0.1F, vec3);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         if (this.isInFluidType() && this.getVariant() != ScamperVariants.DROWNED) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, 0.01, (double)0.0F));
         }

         super.travel(vec3);
      }

   }

   public void tick() {
      if (this.isAlive() && this.deployClock > 0) {
         --this.deployClock;
      }

      if (this.deployClock == 0) {
         this.deploying = false;
      }

      if (this.isAlive() && this.tickCount % 20 == 0) {
         this.setAge(this.getAge() + 1);
         if (this.getAge() >= (Integer)SConfig.SERVER.scamper_age.get() && !this.level().isClientSide) {
            if ((!this.getLinked() || !(Math.random() < (double)0.3F)) && this.getEvoPoints() < (Integer)SConfig.SERVER.min_kills_hyper.get()) {
               int chance = this.random.nextInt(1, 3);
               int age = this.random.nextInt(1, 4);
               if ((Boolean)SConfig.SERVER.scamper_summon.get()) {
                  this.Summon(4);
               }

               for(int i = 0; i < chance; ++i) {
                  if ((Boolean)SConfig.SERVER.scamper_summon.get()) {
                     this.Summon(age);
                  }
               }

               if ((Boolean)SConfig.SERVER.scent_spawn.get()) {
                  this.SummonScent();
               }

               this.level().setBlock(new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ()), ((Block)Sblocks.REMAINS.get()).defaultBlockState(), 2);
            } else {
               GastGeber geber = new GastGeber((EntityType)Sentities.GASTGABER.get(), this.level());
               geber.setKills(this.getKills() + this.getEvoPoints());
               geber.moveTo(this.position());
               this.level().addFreshEntity(geber);
            }

            Level level = this.level();
            if (level instanceof ServerLevel serverLevel) {
               double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
               double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
               double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
               serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
            }

            this.discard();
         }
      }

      super.tick();
   }

   private void Summon(int i) {
      RandomSource randomSource = RandomSource.create();
      Mound mound = new Mound((EntityType)Sentities.MOUND.get(), this.level());
      int vecx = randomSource.nextInt(-3, 3);
      int vecz = randomSource.nextInt(-3, 3);
      mound.moveTo(this.getX() + (double)vecx, this.getY(), this.getZ() + (double)vecz);
      mound.setMaxAge(i);
      mound.setLinked(this.getLinked());
      mound.tickEmerging();
      mound.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
      this.level().addFreshEntity(mound);
   }

   private void SummonScent() {
      ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), this.level());
      scent.moveTo(this.getX(), this.getY() + (double)0.4F, this.getZ());
      this.level().addFreshEntity(scent);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.2, true));
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.scamper_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.scamper_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.scamper_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)8.0F).add(Attributes.ATTACK_KNOCKBACK, (double)0.0F);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.SCAMPER_AMBIENT.get();
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

   public ScamperVariants getVariant() {
      return ScamperVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= ScamperVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return ScamperVariants.values().length;
   }

   public void setVariant(ScamperVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public boolean doHurtTarget(Entity entity) {
      if (super.doHurtTarget(entity)) {
         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 1), this);
            if (this.deployClock == 0 && !this.isDeploying()) {
               this.setDeploying(true);
               setcloud(this);
               this.deployClock = 800;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void setcloud(LivingEntity entity) {
      if (!entity.level().isClientSide) {
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(entity.level(), entity.getX(), entity.getY(), entity.getZ());
         areaeffectcloud.setOwner(entity);
         areaeffectcloud.setRadius(2.0F);
         areaeffectcloud.setDuration(600);
         areaeffectcloud.setRadiusPerTick((4.0F - areaeffectcloud.getRadius()) / (float)areaeffectcloud.getDuration());
         areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 1));
         entity.level().addFreshEntity(areaeffectcloud);
      }

   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.INT);
      AGE = SynchedEntityData.defineId(Scamper.class, EntityDataSerializers.INT);
   }
}
