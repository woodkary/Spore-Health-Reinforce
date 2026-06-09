package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.SBlockEntities.ContainerBlockEntity;
import com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks;
import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.Harbinger.Spore.Sentities.Signal;
import com.Harbinger.Spore.Sentities.AI.HurtTargetGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.Utility.InfectionTendril;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Mound extends Organoid implements FoliageSpread {
   private static final EntityDataAccessor AGE;
   private static final EntityDataAccessor COUNTER;
   private static final EntityDataAccessor MAX_AGE;
   private static final EntityDataAccessor STRUCTURE;
   private static final EntityDataAccessor LINKED;
   public int maxCounter;
   private int attack_counter;

   public Mound(EntityType type, Level level) {
      super(type, level);
      this.maxCounter = (Integer)SConfig.SERVER.mound_cooldown.get();
      this.attack_counter = 0;
   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 30;
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return this.getLinked() && this.getMaxAge() <= 2;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.mound_loot.get();
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 20 == 0) {
         if (this.isAlive() && (Integer)this.entityData.get(AGE) < (Integer)this.entityData.get(MAX_AGE)) {
            this.getPersistentData().putInt("age", 1 + this.getPersistentData().getInt("age"));
            if (this.getPersistentData().getInt("age") >= (Integer)SConfig.SERVER.mound_age.get()) {
               this.getPersistentData().putInt("age", 0);
               this.entityData.set(AGE, (Integer)this.entityData.get(AGE) + 1);
            }
         }

         if ((Integer)this.entityData.get(COUNTER) < this.maxCounter) {
            this.setCounter(this.getCounter() + 1);
         }

         if (this.isAlive() && this.getCounter() >= this.maxCounter && !this.level().isClientSide) {
            double var10000;
            switch ((Integer)this.entityData.get(AGE)) {
               case 2 -> var10000 = (Double)SConfig.SERVER.mound_range_age2.get();
               case 3 -> var10000 = (Double)SConfig.SERVER.mound_range_age3.get();
               case 4 -> var10000 = (Double)SConfig.SERVER.mound_range_age4.get();
               default -> var10000 = (Double)SConfig.SERVER.mound_range_default.get();
            }

            double range = var10000;
            this.SpreadInfection(this.level(), range, this.getOnPos());
            this.setCounter(0);
            if (this.random.nextInt(10) == 0 && (Integer)this.entityData.get(AGE) >= 3 && this.checkForExtraTendrils(this, this.level())) {
               this.SpreadKin(this, this.level());
            }
         }

         if (this.getCounter() > this.maxCounter - 2 && this.getCounter() < this.maxCounter) {
            Level var2 = this.level();
            if (var2 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var2;
               double x0 = this.getX() - ((double)this.random.nextFloat() - 0.2) * 0.2;
               double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.5F) * (double)0.5F * (double)10.0F;
               double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.2) * 0.2;
               serverLevel.sendParticles((SimpleParticleType)Sparticles.SPORE_PARTICLE.get(), x0, y0, z0, 9, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
            }
         }

         if (this.getCounter() == this.maxCounter - 2) {
            this.playSound((SoundEvent)Ssounds.PUFF.get());
         }
      }

      if (this.isAlive() && this.attack_counter > 0) {
         --this.attack_counter;
      }

   }

   public int getAge() {
      return (Integer)this.entityData.get(AGE);
   }

   public void setAge(int e) {
      this.entityData.set(AGE, e);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("age", (Integer)this.entityData.get(AGE));
      tag.putInt("counter", (Integer)this.entityData.get(COUNTER));
      tag.putInt("max_age", (Integer)this.entityData.get(MAX_AGE));
      tag.putBoolean("structure", (Boolean)this.entityData.get(STRUCTURE));
      tag.putBoolean("linked", (Boolean)this.entityData.get(LINKED));
   }

   public int getAgeCounter() {
      return this.getPersistentData().getInt("age");
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(AGE, tag.getInt("age"));
      this.entityData.set(COUNTER, tag.getInt("counter"));
      this.entityData.set(MAX_AGE, tag.getInt("max_age"));
      this.entityData.set(STRUCTURE, tag.getBoolean("structure"));
      this.entityData.set(LINKED, tag.getBoolean("linked"));
   }

   public void setCounter(int counter) {
      this.entityData.set(COUNTER, counter);
   }

   public int getCounter() {
      return (Integer)this.entityData.get(COUNTER);
   }

   public int getMaxCounter() {
      return this.maxCounter;
   }

   public void setMaxAge(int maxAge) {
      this.entityData.set(MAX_AGE, maxAge);
   }

   public int getMaxAge() {
      return (Integer)this.entityData.get(MAX_AGE);
   }

   public void setLinked(boolean value) {
      this.entityData.set(LINKED, value);
   }

   public boolean getLinked() {
      return (Boolean)this.entityData.get(LINKED);
   }

   public void additionPlacers(Level level, BlockPos pos, double range) {
      AABB aabb = this.getBoundingBox().inflate(range);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = level.getBlockState(blockpos);
         BlockState above = level.getBlockState(blockpos.above());
         if (Math.random() < 0.1 && above.isAir() && blockstate.isSolidRender(level, blockpos) && (Boolean)this.entityData.get(STRUCTURE) && (Integer)this.entityData.get(AGE) >= (Integer)this.entityData.get(MAX_AGE) && this.distanceToSqr((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()) > (double)80.0F) {
            BlockState block4 = ((Block)ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("spore:block_st"))).getRandomElement(RandomSource.create()).orElse(Blocks.AIR)).defaultBlockState();
            level.setBlock(blockpos.above(), block4, 3);
            this.entityData.set(STRUCTURE, false);
         }
      }

   }

   public void additionIgnoreConfigPlacers(Level level, BlockPos pos, double range) {
      AABB aabb = this.getBoundingBox().inflate(range);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = level.getBlockState(blockpos);
         BlockState above = level.getBlockState(blockpos.above());
         if (Math.random() < 0.1 && above.isAir() && blockstate.isSolidRender(level, blockpos) && (Boolean)this.entityData.get(STRUCTURE) && (Integer)this.entityData.get(AGE) >= (Integer)this.entityData.get(MAX_AGE) && this.distanceToSqr((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()) > (double)80.0F) {
            BlockState block4 = ((Block)ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(new ResourceLocation("spore:block_st"))).getRandomElement(RandomSource.create()).orElse(Blocks.AIR)).defaultBlockState();
            level.setBlock(blockpos.above(), block4, 3);
            this.entityData.set(STRUCTURE, false);
         }
      }

      for(LivingEntity en : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
         if (!(en instanceof Infected) && !(en instanceof UtilityEntity) && !((List)SConfig.SERVER.blacklist.get()).contains(en.getEncodeId()) && en.getItemBySlot(EquipmentSlot.HEAD).getItem() != Sitems.GAS_MASK.get()) {
            en.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 1));
         }
      }

   }

   private void SpreadKin(Entity entity, Level level) {
      AABB aabb = entity.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.mound_tendril_checker.get());

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockState = level.getBlockState(blockpos);
         if (this.isStructureBlock(blockpos) || this.isChestWithFood(blockpos) && (Boolean)SConfig.SERVER.tendril_chest.get() || blockState.is((Block)Sblocks.REMAINS.get()) && (Boolean)SConfig.SERVER.tendril_corpse.get() || blockState.is(Blocks.SPAWNER) && (Boolean)SConfig.SERVER.tendril_spawner.get()) {
            InfectionTendril tendril = new InfectionTendril((EntityType)Sentities.TENDRIL.get(), level);
            tendril.setAgeM(this.getMaxAge() - 1);
            tendril.setSearchArea(blockpos);
            tendril.setPos(this.getX(), this.getY() + (double)0.5F, this.getZ());
            level.addFreshEntity(tendril);
            break;
         }
      }

   }

   private boolean isChestWithFood(BlockPos pos) {
      BlockEntity blockEntity = this.level().getBlockEntity(pos);
      if (blockEntity instanceof Container container) {
         if (!(container instanceof ContainerBlockEntity)) {
            return container.hasAnyMatching(ItemStack::isEdible);
         }
      }

      return false;
   }

   private boolean isStructureBlock(BlockPos pos) {
      BlockEntity blockEntity = this.level().getBlockEntity(pos);
      return blockEntity instanceof LivingStructureBlocks;
   }

   private boolean checkForExtraTendrils(Entity entity, Level level) {
      AABB aabb = entity.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.mound_tendril_checker.get());
      List<InfectionTendril> entities = level.getEntitiesOfClass(InfectionTendril.class, aabb);
      return entities.size() <= 4;
   }

   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      if (this.attack_counter == 0 && !((LivingEntity)this).level().isClientSide) {
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(((LivingEntity)this).level(), ((LivingEntity)this).getX(), ((LivingEntity)this).getY(), ((LivingEntity)this).getZ());
         areaeffectcloud.setOwner(this);
         areaeffectcloud.setRadius(2.0F);
         areaeffectcloud.setDuration(300);
         areaeffectcloud.setRadiusPerTick((1.5F * (float)(Integer)this.entityData.get(AGE) - areaeffectcloud.getRadius()) / (float)areaeffectcloud.getDuration());
         areaeffectcloud.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 1));
         ((LivingEntity)this).level().addFreshEntity(areaeffectcloud);
         this.playSound((SoundEvent)Ssounds.PUFF.get(), 0.5F, 0.5F);
         this.attack_counter = 300;
      }

      return super.hurt(p_21016_, p_21017_);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.mound_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.mound_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(AGE, 1);
      this.entityData.define(COUNTER, 0);
      this.entityData.define(MAX_AGE, 4);
      this.entityData.define(STRUCTURE, true);
      this.entityData.define(LINKED, false);
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      double health = (Double)SConfig.SERVER.mound_hp.get() * (double)(Integer)this.entityData.get(AGE) * (Double)SConfig.SERVER.global_health.get();
      double armor = (Double)SConfig.SERVER.mound_armor.get() * (double)(Integer)this.entityData.get(AGE) * (Double)SConfig.SERVER.global_armor.get();
      if (AGE.equals(dataAccessor)) {
         AttributeInstance hp = this.getAttribute(Attributes.MAX_HEALTH);
         AttributeInstance def = this.getAttribute(Attributes.ARMOR);
         if (hp != null) {
            hp.setBaseValue(health);
         }

         SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(this, (float)health);
         if (def != null) {
            def.setBaseValue(armor);
         }

         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public void registerGoals() {
      this.goalSelector.addGoal(2, (new HurtTargetGoal(this, (en) -> !((List)SConfig.SERVER.blacklist.get()).contains(en.getEncodeId()) && !(en instanceof UtilityEntity) && !(en instanceof Infected), new Class[]{Infected.class})).setAlertOthers(Infected.class));
   }

   public EntityDimensions getDimensions(Pose pose) {
      return super.getDimensions(pose).scale(this.getAge() >= 1 ? 1.0F * (float)this.getAge() : 1.0F);
   }

   public void die(DamageSource source) {
      if (this.getHealth()>0.0f) {
         return;
      }
      if (this.getLinked() && this.getAge() > 3 && source.getEntity() != null) {
         if (this.isInPowderSnow || this.Cold() || this.getLastDamageSource() != null && this.getLastDamageSource().is(DamageTypes.FREEZE)) {
            return;
         }

         List<Proto> protos = SporeSavedData.getHiveminds();
         if (protos.isEmpty()) {
            return;
         }

         Iterator var3 = protos.iterator();
         if (var3.hasNext()) {
            Proto proto = (Proto)var3.next();
            if (source.getDirectEntity() != null) {
               int var10000 = (int)source.getDirectEntity().getY();
            } else {
               int var7 = (int)this.getY();
            }

            proto.setSignal(new Signal(true, this.getOnPos()));
         }
      }

      for(int i = 0; i <= this.getAge(); ++i) {
         super.die(source);
      }

   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      this.setDefaultLinkage(this.level());
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   public void setDefaultLinkage(Level level) {
      if (level instanceof ServerLevel serverLevel) {
         SporeSavedData data = SporeSavedData.getDataLocation(serverLevel);
         if (data != null && data.getAmountOfHiveminds() >= (Integer)SConfig.SERVER.proto_spawn_world_mod.get()) {
            this.setLinked(true);
         }
      }

   }

   public int getEmerge_tick() {
      return 40;
   }

   public boolean isCloseCombatant() {
      return true;
   }

   static {
      AGE = SynchedEntityData.defineId(Mound.class, EntityDataSerializers.INT);
      COUNTER = SynchedEntityData.defineId(Mound.class, EntityDataSerializers.INT);
      MAX_AGE = SynchedEntityData.defineId(Mound.class, EntityDataSerializers.INT);
      STRUCTURE = SynchedEntityData.defineId(Mound.class, EntityDataSerializers.BOOLEAN);
      LINKED = SynchedEntityData.defineId(Mound.class, EntityDataSerializers.BOOLEAN);
   }
}
