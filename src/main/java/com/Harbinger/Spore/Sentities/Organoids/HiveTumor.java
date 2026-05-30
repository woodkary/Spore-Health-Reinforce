package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sblocks.CDUBlock;
import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class HiveTumor extends Organoid implements FoliageSpread {
   private static final EntityDataAccessor BIOMASS;
   private static final EntityDataAccessor GROWTH;
   private static final EntityDataAccessor SCARED;

   public HiveTumor(EntityType type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
   }

   private void griefBlocks() {
      if (this.getLastDamageSource() == this.damageSources().inWall() && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
         AABB aabb = this.getBoundingBox().inflate(0.2, (double)0.0F, 0.2);
         boolean flag = false;

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (blockstate.getDestroySpeed(this.level(), blockpos) < 10.0F && blockstate.getDestroySpeed(this.level(), blockpos) > 0.0F) {
               flag = this.level().destroyBlock(blockpos, true, this) || flag;
            }
         }
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.tumor_loot.get();
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 20 == 0) {
         this.grow(1);
      }

      if (this.tickCount % 40 == 0) {
         this.griefBlocks();
         LivingEntity living = this.getTarget();
         if (living != null && this.checkForOrganoids(living) && (Integer)this.entityData.get(BIOMASS) >= 10) {
            this.summonMob(living.getOnPos());
         }
      }

      if (this.tickCount % 200 == 0 || this.tickCount % 20 == 0 && this.isScared()) {
         this.addBiomass(1);
      }

      if (this.tickCount % 3000 == 0 && (Boolean)SConfig.SERVER.htumor_madness.get()) {
         this.giveMadness();
      }

      if (!this.level().isClientSide) {
         if (this.tickCount % 6000 == 0 && (Boolean)SConfig.SERVER.mound_foliage.get()) {
            this.SpreadInfection(this.level(), (Double)SConfig.SERVER.mound_range_age4.get() * (double)2.0F, this.getOnPos());
         }

         if ((Integer)this.entityData.get(GROWTH) >= (Integer)SConfig.SERVER.htumor_timer.get()) {
            Level var2 = this.level();
            if (var2 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var2;
               List<Proto> protos = SporeSavedData.getHiveminds();
               if (!protos.isEmpty()) {
                  for(Proto proto : protos) {
                     if (proto.distanceTo(this) <= (float)(Integer)SConfig.SERVER.proto_range.get()) {
                        proto.addBiomass(1000);
                        this.discard();
                        break;
                     }
                  }
               } else {
                  this.SummonProto(serverLevel);
               }
            }
         }
      }

      if (this.tickCount % 1200 == 0) {
         this.scanForHosts();
      }

      if (this.isScared()) {
         this.entityData.set(SCARED, (Integer)this.entityData.get(SCARED) - 1);
      }

      if (this.isScared()) {
         for(int i = 0; i < 360; ++i) {
            if (i % 20 == 0) {
               this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.2, this.getZ(), Math.cos((double)i) * 0.15, (this.random.nextDouble() - this.random.nextDouble()) * 0.3, Math.sin((double)i) * 0.15);
            }
         }
      }

   }

   public void SummonProto(ServerLevel serverLevel) {
      Proto proto = new Proto((EntityType)Sentities.PROTO.get(), this.level());
      proto.moveTo((double)this.getOnPos().getX(), (double)this.getOnPos().getY(), (double)this.getOnPos().getZ());
      proto.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      this.level().addFreshEntity(proto);
      this.entityData.set(GROWTH, 0);
      this.discard();
   }

   protected void giveMadness() {
      AABB aabb = this.getBoundingBox().inflate((double)64.0F);

      for(Entity entity : this.level().getEntities(this, aabb)) {
         if (entity instanceof LivingEntity living) {
            if (((List)SConfig.SERVER.htumor_sapient_target.get()).contains(living.getEncodeId()) || living instanceof Player) {
               living.addEffect(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), 3000, 0, false, false));
            }
         }
      }

   }

   public void addBiomass(int e) {
      this.entityData.set(BIOMASS, (Integer)this.entityData.get(BIOMASS) + e);
   }

   public void eatBiomass(int e) {
      this.entityData.set(BIOMASS, (Integer)this.entityData.get(BIOMASS) - e);
   }

   public void grow(int e) {
      this.entityData.set(GROWTH, (Integer)this.entityData.get(GROWTH) + e);
   }

   protected boolean checkForOrganoids(Entity entity) {
      AABB aabb = entity.getBoundingBox().inflate((double)12.0F);
      List<Entity> entities = this.level().getEntities(this, aabb, (entity1) -> entity1 instanceof Organoid);
      return entities.size() <= 4;
   }

   private boolean checkTheGround(BlockPos pos, Level level) {
      for(int i = 0; i < 3; ++i) {
         BlockState state = level.getBlockState(pos.below(i));
         if (state.getDestroySpeed(level, pos.below(i)) > 4.0F || state.isAir()) {
            return false;
         }
      }

      return true;
   }

   private void summonMob(BlockPos pos) {
      if (!pos.equals(BlockPos.ZERO)) {
         int i = this.getRandom().nextInt(((List)SConfig.SERVER.htumor_summonable_troops.get()).size());
         BlockPos blockPos = pos;
         EntityType<?> type = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation((String)((List)SConfig.SERVER.htumor_summonable_troops.get()).get(i)));
         if (type != null) {
            Entity summoned = type.create(this.level());
            if (summoned instanceof Organoid) {
               Organoid organoid = (Organoid)summoned;
               blockPos = organoid.isCloseCombatant() ? pos : BlockPos.containing(Utilities.generatePositionAway(new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), (double)this.random.nextInt(8, 16)));
               organoid.tickEmerging();
            }

            if (summoned instanceof Vigil) {
               Vigil organoid = (Vigil)summoned;
               organoid.setProto(this);
            }

            if (summoned instanceof Mound) {
               Mound organoid = (Mound)summoned;
               organoid.setMaxAge(1);
            }

            if (summoned instanceof Verwa) {
               Verwa organoid = (Verwa)summoned;
               Level var8 = this.level();
               if (var8 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var8;
                  organoid.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
               }
            }

            if (summoned instanceof LivingEntity) {
               LivingEntity mob = (LivingEntity)summoned;
               mob.randomTeleport((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), false);
            } else {
               summoned.teleportTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
            }

            if (summoned instanceof VariantKeeper) {
               VariantKeeper keeper = (VariantKeeper)summoned;
               keeper.setVariant(this.random.nextInt(keeper.amountOfMutations()));
            }

            if (this.checkTheGround(pos, summoned.level()) && summoned.position().distanceToSqr((double)0.0F, (double)0.0F, (double)0.0F) > (double)10.0F) {
               this.eatBiomass(10);
               this.level().addFreshEntity(summoned);
            }

         }
      }
   }

   public void SpreadFoliageAndConvert(Level level, BlockState blockstate, BlockPos blockpos) {
      FoliageSpread.super.SpreadFoliageAndConvert(level, blockstate, blockpos);
      if (blockstate.getBlock().equals(Sblocks.CDU.get())) {
         CDUBlock.replaceCDU(blockpos, level);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BIOMASS, 0);
      this.entityData.define(GROWTH, 0);
      this.entityData.define(SCARED, 0);
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      compound.putInt("biomass", (Integer)this.entityData.get(BIOMASS));
      compound.putInt("growth", (Integer)this.entityData.get(GROWTH));
      compound.putInt("scarred", (Integer)this.entityData.get(SCARED));
   }

   public boolean isScared() {
      return (Integer)this.entityData.get(SCARED) > 0;
   }

   public void setScaredTicks(int i) {
      this.entityData.set(SCARED, i);
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      this.entityData.set(BIOMASS, compound.getInt("biomass"));
      this.entityData.set(GROWTH, compound.getInt("growth"));
      this.entityData.set(SCARED, compound.getInt("scarred"));
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.TUMOR_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public int getEmerge_tick() {
      return 120;
   }

   public int getNumberOfParticles() {
      return 6;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.htumor_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.htumor_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.htumor_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)2.0F);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new HiveTumorPanicGoal(this));
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)0.0F, false, (double)2.5F, 8.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public boolean hasLineOfSight(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.hasEffect((MobEffect)Seffects.MARKER.get())) {
            return true;
         } else if ((livingEntity instanceof Player || ((List)SConfig.SERVER.proto_sapient_target.get()).contains(livingEntity.getEncodeId())) && !livingEntity.hasEffect((MobEffect)Seffects.SYMBIOSIS.get())) {
            return true;
         } else {
            return livingEntity.getMaxHealth() > 30.0F ? true : super.hasLineOfSight(entity);
         }
      } else {
         return super.hasLineOfSight(entity);
      }
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 60;
   }

   public AABB seachbox() {
      return this.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.htumor_range.get());
   }

   private void scanForHosts() {
      for(Entity en : this.level().getEntities(this, this.seachbox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (en instanceof Infected infected) {
            if (!infected.getLinked()) {
               infected.setLinked(true);
            }
         }

         if (en instanceof Mound mound) {
            if (!mound.getLinked()) {
               mound.setLinked(true);
            }
         }
      }

   }

   static {
      BIOMASS = SynchedEntityData.defineId(HiveTumor.class, EntityDataSerializers.INT);
      GROWTH = SynchedEntityData.defineId(HiveTumor.class, EntityDataSerializers.INT);
      SCARED = SynchedEntityData.defineId(HiveTumor.class, EntityDataSerializers.INT);
   }

   private static class HiveTumorPanicGoal extends Goal {
      private final HiveTumor hiveTumor;

      private HiveTumorPanicGoal(HiveTumor hiveTumor) {
         this.hiveTumor = hiveTumor;
      }

      public boolean canUse() {
         if (this.hiveTumor.isScared()) {
            return true;
         } else {
            LivingEntity living = this.hiveTumor.getTarget();
            if (this.hiveTumor.getHealth() <= this.hiveTumor.getMaxHealth() / 2.0F) {
               return true;
            } else if (living == null) {
               return false;
            } else {
               return living.getMaxHealth() >= 100.0F || living.getArmorValue() >= 20;
            }
         }
      }

      public void start() {
         this.hiveTumor.setScaredTicks(6000);
      }

      public void tick() {
         super.tick();
         if (this.hiveTumor.tickCount % 40 == 0) {
            this.Targeting();
         }

      }

      public boolean canContinueToUse() {
         return this.hiveTumor.isScared();
      }

      private void Targeting() {
         LivingEntity target = this.hiveTumor.getTarget();
         if (target != null && target.isAlive()) {
            AABB boundingBox = this.hiveTumor.getBoundingBox().inflate((double)128.0F);

            for(Entity entity : this.hiveTumor.level().getEntities(this.hiveTumor, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
               if (entity instanceof Infected) {
                  Infected infected = (Infected)entity;
                  if (infected.getTarget() == null && !target.isInvulnerable()) {
                     infected.setTarget(target);
                  }
               }
            }

         }
      }
   }
}
