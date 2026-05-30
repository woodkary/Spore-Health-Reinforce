package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoadRequest;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoaderHelper;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sblocks.BrainRemnants;
import com.Harbinger.Spore.Sblocks.CDUBlock;
import com.Harbinger.Spore.Sentities.CasingGenerator;
import com.Harbinger.Spore.Sentities.ChunkLoaderMob;
import com.Harbinger.Spore.Sentities.FoliageSpread;
import com.Harbinger.Spore.Sentities.Signal;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.NeuralProcessing.ProtoAIs.ProtoTargeting;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Utility.GastGeber;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class Proto extends Organoid implements CasingGenerator, FoliageSpread, ChunkLoaderMob {
   private static final EntityDataAccessor HOSTS;
   private static final EntityDataAccessor BIOMASS;
   public static final EntityDataAccessor NODE;
   private final List hypers = new ArrayList() {
      {
         this.add("spore:inquisitor");
         this.add("spore:grober");
         this.add("spore:wendigo");
         this.add("spore:hvindicator");
         this.add("spore:brot");
         this.add("spore:ogre");
         this.add("spore:hevoker");
      }
   };
   private int summonDefense = 0;
   private static final int INPUT_SIZE = 4;
   private static final int OUTPUT_SIZE = 4;
   private double[] weights;
   public List<String> team_1 = new ArrayList<>();
   public List<String> team_2 = new ArrayList<>();
   public List<String> team_3 = new ArrayList<>();
   public List<String> team_4 = new ArrayList<>();
   public List<String> team_5 = new ArrayList<>();
   private final Random random = new Random();
   @Nullable
   public Signal signal;

   public Proto(EntityType type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
      this.initializeValues();
   }

   private void fillDefaultTeams(List<String> team, List<String> CONFIG) {
      if (CONFIG.size() < 4) {
         throw new IllegalArgumentException("CONFIG must have at least 4 unique entries");
      } else {
         List<String> shuffledConfig = new ArrayList<>(CONFIG);
         Collections.shuffle(shuffledConfig, this.random);

         for(int i = 0; i < 4; ++i) {
            String config = (String)shuffledConfig.get(i);
            int add = this.isVariantKeeper(config);
            team.add(config + "_" + add);
         }

      }
   }

   protected void initializeValues() {
      this.weights = new double[16];

      for(int i = 0; i < this.weights.length; ++i) {
         this.weights[i] = this.getRandom().nextDouble();
      }

      this.fillDefaultTeams(this.team_1, (List)SConfig.SERVER.proto_summonable_troops.get());
      this.fillDefaultTeams(this.team_2, (List)SConfig.SERVER.proto_summonable_troops.get());
      this.fillDefaultTeams(this.team_3, (List)SConfig.SERVER.proto_summonable_troops.get());
      this.fillDefaultTeams(this.team_4, (List)SConfig.SERVER.proto_summonable_troops.get());
   }

   private int isVariantKeeper(String s) {
      ResourceLocation location = new ResourceLocation(s);
      Entity entity = ((EntityType)ForgeRegistries.ENTITY_TYPES.getValue(location)).create(this.level());
      int var10000;
      if (entity instanceof VariantKeeper keeper) {
         var10000 = this.getRandom().nextInt(keeper.amountOfMutations());
      } else {
         var10000 = -1;
      }

      return var10000;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.proto_loot.get();
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public double[] getWeights() {
      return this.weights;
   }

   public double getWeightsValue(int i) {
      return this.weights[i];
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.proto_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.proto_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.proto_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.FOLLOW_RANGE, (double)128.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)2.0F);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new ProtoTargeting(this));
      this.goalSelector.addGoal(4, new AOEMeleeAttackGoal(this, (double)0.0F, false, (double)2.5F, 8.0F, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public boolean isNunny() {
      return Objects.equals(this.getCustomName(), Component.literal("Nunny"));
   }

   private void generateCasing() {
      if (this.distanceToSqr((double)((BlockPos)this.entityData.get(NODE)).getX(), (double)((BlockPos)this.entityData.get(NODE)).getY(), (double)((BlockPos)this.entityData.get(NODE)).getZ()) > (double)100.0F) {
         if (Math.random() < 0.1) {
            this.entityData.set(NODE, this.getOnPos());
         }
      } else if (this.getBiomass() > 40) {
         this.generateChasing((BlockPos)this.entityData.get(NODE), this, 32, 2);
         this.generateChasing((BlockPos)this.entityData.get(NODE), this, 16);
         this.eatBiomass(5);
      }

   }

   private void scanForHosts() {
      List<Entity> entities = this.level().getEntities(this, this.seachbox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
      this.entityData.set(HOSTS, 0);

      for(Entity en : entities) {
         if (en instanceof Infected infected) {
            if (!infected.getLinked()) {
               infected.setLinked(true);
            }

            if (infected.getTarget() != null && !(infected.getY() < (double)0.0F) && !(infected.getHealth() < infected.getMaxHealth() / 2.0F)) {
               this.setHosts(this.getHosts() + 1);
            } else if (!this.level().isClientSide && this.harvestBiomassByDespawning(infected)) {
               this.setHosts(this.getHosts() + 1);
            }
         }

         if (en instanceof Mound mound) {
            if (!mound.getLinked()) {
               mound.setLinked(true);
            }

            this.setHosts(this.getHosts() + 1);
         }

         if ((Boolean)SConfig.SERVER.proto_raid.get() && Math.random() < (Double)SConfig.SERVER.proto_raid_chance.get() / (double)100.0F && (en instanceof Player || ((List)SConfig.SERVER.proto_sapient_target.get()).contains(en.getEncodeId()))) {
            int x = this.random.nextInt(-30, 30);
            int z = this.random.nextInt(-30, 30);
            Vigil vigil = new Vigil((EntityType)Sentities.VIGIL.get(), this.level());
            vigil.randomTeleport(en.getX() + (double)x, en.getY(), en.getZ() + (double)z, false);
            vigil.setProto(this);
            vigil.setVariant(2);
            vigil.tickEmerging();
            this.level().addFreshEntity(vigil);
            break;
         }
      }

   }

   public boolean harvestBiomassByDespawning(Infected living) {
      if (!(living instanceof GastGeber) && !(living instanceof Scamper)) {
         if (living.getKills() > 0) {
            this.addBiomass(living.getKills());
            living.setKills(0);
            return true;
         } else if (living instanceof Hyper && Math.random() < 1.0E-4) {
            this.addBiomass(30);
            living.discard();
            return false;
         } else if (living instanceof EvolvedInfected && Math.random() < 0.01) {
            this.addBiomass(15);
            living.discard();
            return false;
         } else if (Math.random() < 0.2) {
            this.addBiomass(5);
            living.discard();
            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
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

   private boolean checkForScent() {
      AABB hitbox = this.getBoundingBox().inflate((double)3.0F);
      List<ScentEntity> entities = this.level().getEntitiesOfClass(ScentEntity.class, hitbox);
      return entities.isEmpty();
   }

   private void SummonScent() {
      ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), this.level());
      scent.setOvercharged(true);
      scent.moveTo(this.getX(), this.getY(), this.getZ());
      this.level().addFreshEntity(scent);
   }

   public void addBiomass(int e) {
      this.entityData.set(BIOMASS, (Integer)this.entityData.get(BIOMASS) + e);
   }

   public void eatBiomass(int e) {
      this.entityData.set(BIOMASS, (Integer)this.entityData.get(BIOMASS) - e);
   }

   public int getBiomass() {
      return (Integer)this.entityData.get(BIOMASS);
   }

   public AABB seachbox() {
      return this.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.proto_range.get());
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.tickCount % 6000 == 0 && (Boolean)SConfig.SERVER.mound_foliage.get() && this.entityData.get(NODE) != BlockPos.ZERO) {
            this.SpreadInfection(this.level(), (Double)SConfig.SERVER.mound_range_age4.get() * (double)2.0F, (BlockPos)this.entityData.get(NODE));
            this.loadChunks();
         }

         if (this.tickCount % 200 == 0 && (Boolean)SConfig.SERVER.proto_casing.get()) {
            this.generateCasing();
         }

         if (this.tickCount % 1200 == 0) {
            this.scanForHosts();
            this.moraleBoost();
         }

         if (this.tickCount % 40 == 0) {
            this.griefBlocks();
            Entity target = this.getTarget();
            if (target != null && this.checkForScent()) {
               this.SummonScent();
            }
         }

         if (this.tickCount % 200 == 0) {
            this.addBiomass(1);
         }

         if (this.getSignal() != null && this.getSignal().active() && this.checkForCalamities(this.getSignal().pos())) {
            this.SummonConstructor(this.level(), this, this.getSignal().pos());
         }

         if (this.tickCount % 3000 == 0 && (Boolean)SConfig.SERVER.proto_madness.get()) {
            this.giveMadness(this);
         }

         if (this.summonDefense > 0) {
            --this.summonDefense;
         }

         LivingEntity target = this.getTarget();
         if (this.tickCount % 200 == 0 && target != null) {
            BlockPos pos = target.getOnPos();
            if (this.checkForOrganoids(target) && this.getBiomass() > 2 && !this.level().isClientSide) {
               int e = this.getRandom().nextInt(1, 5);

               for(int o = 0; o < e; ++o) {
                  this.summonMob(this.decide(this.inputs(target)), pos);
               }
            }
         }
      }

   }

   public double[] inputs(LivingEntity entity) {
      if (entity == null) {
         return new double[]{(double)1.0F, (double)1.0F, (double)1.0F, (double)1.0F};
      } else {
         double distance = entity.distanceToSqr(this) < (double)200.0F ? (double)1.0F : (double)0.0F;
         double isOnGround = entity.onGround() ? (double)1.0F : (double)0.0F;
         double hasAllotOfHealth = entity.getMaxHealth() >= 20.0F ? (double)1.0F : (double)0.0F;
         double hasAllotOfArmor = entity.getArmorValue() >= 20 ? (double)1.0F : (double)0.0F;
         return new double[]{distance, isOnGround, hasAllotOfHealth, hasAllotOfArmor};
      }
   }

   public Entity entityResourceLocation(int decision, List<String> string) {
      if (string.isEmpty()) {
         return null;
      } else {
         String[] id = ((String)string.get(decision)).split("_");
         ResourceLocation location = new ResourceLocation(id[0]);
         int variant = Integer.parseInt(id[1]);
         Entity entity = ((EntityType)ForgeRegistries.ENTITY_TYPES.getValue(location)).create(this.level());
         if (entity == null) {
            entity = new Mound((EntityType)Sentities.MOUND.get(), this.level());
         }

         if (entity instanceof VariantKeeper) {
            VariantKeeper keeper = (VariantKeeper)entity;
            if (variant > 0) {
               keeper.setVariant(variant);
            }
         }

         return entity;
      }
   }

   public List<String> getDecisionList(int decision) {
      List<String> var10000;
      switch (decision) {
         case 0 -> var10000 = this.team_1;
         case 1 -> var10000 = this.team_2;
         case 2 -> var10000 = this.team_3;
         case 3 -> var10000 = this.team_4;
         default -> var10000 = null;
      }

      return var10000;
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

   private void summonMob(int decision, BlockPos pos) {
      if (!pos.equals(BlockPos.ZERO)) {
         List<String> team = this.getDecisionList(decision);
         int i = this.getRandom().nextInt(team.size());
         BlockPos blockPos = pos;
         Entity summoned = this.entityResourceLocation(i, team);
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
            Level var9 = this.level();
            if (var9 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var9;
               organoid.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
            }
         }

         CompoundTag data = summoned.getPersistentData();
         data.putInt("hivemind", this.getId());
         data.putInt("decision", decision);
         data.putInt("member", decision);
         if (summoned instanceof LivingEntity) {
            LivingEntity mob = (LivingEntity)summoned;
            mob.randomTeleport((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), false);
         } else {
            summoned.teleportTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
         }

         if (this.checkTheGround(pos, summoned.level()) && summoned.position().distanceToSqr((double)0.0F, (double)0.0F, (double)0.0F) > (double)10.0F) {
            this.eatBiomass(2);
            this.level().addFreshEntity(summoned);
         }

      }
   }

   public void moraleBoost() {
      int e = 0;

      for(double weight : this.weights) {
         if (weight < (double)0.0F) {
            ++e;
         }
      }

      if (e > this.weights.length / 2) {
         for(int i = 0; i < this.weights.length; ++i) {
            this.weights[i] += this.getRandom().nextDouble();
         }
      }

   }

   protected void giveMadness(Proto proto) {
      AABB aabb = proto.getBoundingBox().inflate((double)128.0F);

      for(Entity entity : this.level().getEntities(this, aabb)) {
         if (entity instanceof LivingEntity living) {
            if (((List)SConfig.SERVER.proto_sapient_target.get()).contains(living.getEncodeId()) || living instanceof Player) {
               living.addEffect(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), 6000, 0, false, false));
            }
         }
      }

   }

   public void aiStep() {
      super.aiStep();
      if (this.isOnFire() && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
      } else if (this.getLastDamageSource() == this.damageSources().freeze()) {
         this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
      } else if (this.getHealth() < this.getMaxHealth() / 2.0F && !this.hasEffect(MobEffects.WEAKNESS) && !this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
         this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
      }

   }

   protected int calculateFallDamage(float p_149389_, float p_149390_) {
      return super.calculateFallDamage(p_149389_, p_149390_) - 60;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("biomass", (Integer)this.entityData.get(BIOMASS));
      tag.putInt("hosts", (Integer)this.entityData.get(HOSTS));
      tag.putInt("nodeX", ((BlockPos)this.entityData.get(NODE)).getX());
      tag.putInt("nodeY", ((BlockPos)this.entityData.get(NODE)).getY());
      tag.putInt("nodeZ", ((BlockPos)this.entityData.get(NODE)).getZ());
      ListTag weightsList = new ListTag();

      for(double weight : this.weights) {
         weightsList.add(DoubleTag.valueOf(weight));
      }

      tag.put("weights", weightsList);
      List<List<String>> teams = List.of(this.team_1, this.team_2, this.team_3, this.team_4, this.team_5);

      for(int i = 0; i < teams.size(); ++i) {
         ListTag teamTag = new ListTag();

         for(String member : (List<String>)teams.get(i)) {
            teamTag.add(StringTag.valueOf(member));
         }

         tag.put("team_" + (i + 1), teamTag);
      }

   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(BIOMASS, tag.getInt("biomass"));
      this.entityData.set(HOSTS, tag.getInt("hosts"));
      int x = tag.getInt("nodeX");
      int y = tag.getInt("nodeY");
      int z = tag.getInt("nodeZ");
      this.entityData.set(NODE, new BlockPos(x, y, z));
      ListTag weightsList = tag.getList("weights", 6);
      this.weights = new double[weightsList.size()];

      for(int i = 0; i < weightsList.size(); ++i) {
         this.weights[i] = weightsList.getDouble(i);
      }

      this.team_1.clear();
      this.team_2.clear();
      this.team_3.clear();
      this.team_4.clear();
      this.team_5.clear();
      List<List<String>> teams = List.of(this.team_1, this.team_2, this.team_3, this.team_4, this.team_5);

      for(int i = 0; i < teams.size(); ++i) {
         ListTag teamTag = tag.getList("team_" + (i + 1), 8);

         for(int j = 0; j < teamTag.size(); ++j) {
            ((List)teams.get(i)).add(teamTag.getString(j));
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BIOMASS, 100);
      this.entityData.define(HOSTS, 0);
      this.entityData.define(NODE, this.getOnPos());
   }

   public int getHosts() {
      return (Integer)this.entityData.get(HOSTS);
   }

   public void setHosts(int i) {
      this.entityData.set(HOSTS, i);
   }

   public boolean hurt(DamageSource source, float amount) {
      if ((double)amount > (Double)SConfig.SERVER.proto_dpsr.get() && (Double)SConfig.SERVER.proto_dpsr.get() > (double)0.0F) {
         return super.hurt(source, (float)((Double)SConfig.SERVER.proto_dpsr.get() * (double)1.0F));
      } else {
         if (source.getEntity() != null && Math.random() < (double)0.2F && this.summonDefense <= 0) {
            for(int i = 0; i < this.random.nextInt(1, 4); ++i) {
               this.SummonHelpers();
            }

            this.summonDefense = 160;
         }

         if (source.is(DamageTypes.FREEZE) && Math.random() < (double)0.2F) {
            this.SpreadInfection(this.level(), (Double)SConfig.SERVER.mound_range_age4.get() * (double)2.0F, (BlockPos)this.entityData.get(NODE));
         }

         return super.hurt(source, amount);
      }
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.PROTO_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public void die(DamageSource source) {
      super.die(source);
      Level var3 = this.level();
      if (var3 instanceof ServerLevel serverLevel) {
         this.spawnDeathParticles(serverLevel);
         this.cleanupChunkLoading();
      }

      this.spreadBlocksAroundDeath();
      this.affectNearbyEntities();
      this.discard();
   }

   private void spawnDeathParticles(ServerLevel level) {
      double x = this.getX() - ((double)this.random.nextFloat() - 0.1) * 1.2;
      double y = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)1.25F * (double)5.0F;
      double z = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 1.2;
      level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 4, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
   }

   private void cleanupChunkLoading() {
      ChunkPos chunk = this.chunkPosition();
      UUID var10000 = this.getUUID();
      String requestId = "hivemind_" + var10000 + "_" + chunk;
      ChunkLoaderHelper.removeRequest(requestId);
   }

   private void spreadBlocksAroundDeath() {
      AABB area = this.getBoundingBox().inflate((double)2.5F);
      BlockPos.betweenClosed(Mth.floor(area.minX), Mth.floor(area.minY), Mth.floor(area.minZ), Mth.floor(area.maxX), Mth.floor(area.maxY), Mth.floor(area.maxZ)).forEach(this::trySpreadBlockAt);
   }

   private void trySpreadBlockAt(BlockPos pos) {
      BlockState ground = this.level().getBlockState(pos);
      BlockState above = this.level().getBlockState(pos.above());
      if (!this.level().isClientSide() && ground.isSolidRender(this.level(), pos) && !above.isSolidRender(this.level(), pos)) {
         double chance = Math.random();
         if (chance < 0.9) {
            if (Math.random() < 0.7) {
               this.level().setBlock(pos.above(), ((Block)Sblocks.MYCELIUM_VEINS.get()).defaultBlockState(), 2);
            }

            if (Math.random() < 0.3) {
               this.level().setBlock(pos.above(), ((Block)Sblocks.BIOMASS_BLOCK.get()).defaultBlockState(), 2);
            }

            if (Math.random() < 0.1) {
               this.level().setBlock(pos.above(), ((Block)Sblocks.ROOTED_BIOMASS.get()).defaultBlockState(), 2);
            }

            if (Math.random() < 0.15) {
               this.level().setBlock(pos, (BlockState)((Block)Sblocks.BRAIN_REMNANTS.get()).defaultBlockState().setValue(BrainRemnants.OCCUPIED, true), 2);
            }
         }
      }

   }

   private void affectNearbyEntities() {
      AABB searchBox = AABB.ofSize(new Vec3(this.getX(), this.getY(), this.getZ()), (double)300.0F, (double)200.0F, (double)300.0F);
      List<Entity> nearbyEntities = this.level().getEntities(this, searchBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
      this.entityData.set(HOSTS, 0);

      for(Entity entity : nearbyEntities) {
         if (entity instanceof Infected infected) {
            if (infected.getLinked()) {
               infected.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1));
            }
         }

         if (entity instanceof Calamity calamity) {
            calamity.setSearchArea(this.getOnPos());
         }
      }

   }

   public void setSignal(@Nullable Signal signal) {
      this.signal = signal;
   }

   @Nullable
   public Signal getSignal() {
      return this.signal;
   }

   public void SummonConstructor(Level level, Entity entity, BlockPos pos) {
      RandomSource randomSource = RandomSource.create();
      int a = randomSource.nextInt(-12, 12);
      int b = randomSource.nextInt(-12, 12);
      int c = randomSource.nextInt(-4, 4);
      BlockPos blockPos = new BlockPos((int)entity.getX() + a, (int)entity.getY() + c, (int)entity.getZ() + b);
      BlockPos blockPosTop = blockPos.above();
      if (level instanceof ServerLevel serverLevel) {
         if (serverLevel.isEmptyBlock(blockPos) && (serverLevel.isEmptyBlock(blockPosTop) || serverLevel.getBlockState(blockPosTop).liquid()) && pos != null) {
            Womb.TERRAIN terrain = Womb.TERRAIN.GROUND_LEVEL;
            if (pos.getY() > 90) {
               terrain = Womb.TERRAIN.AIR_LEVEL;
            } else if (pos.getY() < 63) {
               terrain = Womb.TERRAIN.UNDERGROUND;
            } else if (this.checkForLiquids(pos)) {
               terrain = Womb.TERRAIN.WATER_LEVEL;
            }

            Womb creature = new Womb((EntityType)Sentities.RECONSTRUCTOR.get(), level, terrain, pos);
            creature.tickEmerging();
            creature.teleportRelative(entity.getX() + (double)a, entity.getY() + (double)c, entity.getZ() + (double)b);
            level.addFreshEntity(creature);
            level.getServer();

            for(ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
               player.playNotifySound((SoundEvent)Ssounds.CALAMITY_SPAWN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
               player.displayClientMessage(Component.translatable("calamity_summon_message"), false);
            }

            this.setSignal((Signal)null);
         }
      }

   }

   public void SummonHelpers() {
      int a = this.random.nextInt(-12, 12);
      int b = this.random.nextInt(-12, 12);
      int c = this.random.nextInt(4);
      Level var5 = this.level();
      if (var5 instanceof ServerLevel serverLevel) {
         int i = this.hypers.size();
         Verwa verwa = new Verwa((EntityType)Sentities.VERVA.get(), serverLevel);
         verwa.setStoredMob((String)this.hypers.get(this.random.nextInt(i)));
         verwa.teleportRelative(this.getX() + (double)a, this.getY() + (double)c, this.getZ() + (double)b);
         verwa.tickEmerging();
         this.level().addFreshEntity(verwa);
      }

   }

   private boolean checkForLiquids(BlockPos blockPos) {
      AABB aabb = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)14.0F, (double)14.0F, (double)14.0F);
      List<BlockPos> liquids = new ArrayList();

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         if (this.level().getBlockState(blockpos).getFluidState() != Fluids.EMPTY.defaultFluidState()) {
            liquids.add(blockpos);
         }
      }

      return liquids.size() > 6 && blockPos.getY() < 70;
   }

   public boolean checkForCalamities(BlockPos pos) {
      for(Entity en : this.level().getEntities(this, this.seachbox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (en instanceof Calamity calamity) {
            if (calamity.getSearchArea() == BlockPos.ZERO && Math.random() < (double)0.5F) {
               calamity.setSearchArea(pos);
               this.setSignal((Signal)null);

               for(ServerPlayer player : this.level().getServer().getPlayerList().getPlayers()) {
                  player.playNotifySound((SoundEvent)Ssounds.CALAMITY_INCOMING.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
                  player.displayClientMessage(Component.translatable("calamity_coming_message"), false);
               }

               return false;
            }
         }
      }

      return true;
   }

   public int getEmerge_tick() {
      return 120;
   }

   public int getNumberOfParticles() {
      return 6;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      if (serverLevelAccessor instanceof ServerLevel serverLevel) {
         if ((Boolean)SConfig.SERVER.teleport_hive.get()) {
            teleportToSurface(serverLevel, this);
         }
      }

      this.entityData.set(NODE, this.getOnPos());
      return super.finalizeSpawn(serverLevelAccessor, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   public void loadChunks() {
      if ((Boolean)SConfig.SERVER.proto_chunk.get()) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var2;
            serverLevel.getServer().execute(() -> {
               ChunkPos chunk = this.chunkPosition();
               UUID ownerId = this.getUUID();
               String id = "hivemind_" + ownerId + "_" + chunk.toString();
               ChunkLoadRequest request = new ChunkLoadRequest(serverLevel.dimension(), new ChunkPos[]{chunk}, 1, id, 12000L, ownerId);
               if (!ChunkLoaderHelper.ACTIVE_REQUESTS.containsValue(request)) {
                  ChunkLoaderHelper.addRequest(request);
               }
            });
         }
      }

   }

   public void onAddedToWorld() {
      super.onAddedToWorld();
      this.loadChunks();
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

   public int decide(double[] inputs) {
      if (this.weights == null || this.weights.length == 0) {
         this.initializeValues();
      }

      if (inputs != null && inputs.length != 0) {
         double[] outputs = new double[4];

         for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
               outputs[i] += inputs[j] * this.weights[i * 4 + j];
            }
         }

         return this.argmax(outputs);
      } else {
         return 0;
      }
   }

   private int argmax(double[] arr) {
      if (arr != null && arr.length != 0) {
         int maxIndex = 0;

         for(int i = 1; i < arr.length; ++i) {
            if (arr[i] > arr[maxIndex]) {
               maxIndex = i;
            }
         }

         return maxIndex;
      } else {
         return 0;
      }
   }

   public void punishForDecision(int decision, int member) {
      this.adjustWeightsForDecision(decision, -0.1);
      if (Math.random() < 0.05) {
         this.punishMember(this.getDecisionList(decision), member);
      }

   }

   public void praisedForDecision(int decision, int member) {
      this.adjustWeightsForDecision(decision, 0.05);
      if (Math.random() < 0.2) {
         this.awardMember(this.getDecisionList(decision), member);
      }

   }

   public void adjustWeightsForDecision(int decision, double penalty) {
      int startIndex = decision * 4;
      int endIndex = startIndex + 4;

      for(int i = startIndex; i < endIndex; ++i) {
         double[] var10000 = this.weights;
         var10000[i] += penalty;
         this.weights[i] = Math.max((double)-1.0F, Math.min((double)1.0F, this.weights[i]));
      }

   }

   private void punishMember(List<String> team, int member) {
      if (team != null && !team.isEmpty() && member >= 0 && member < team.size()) {
         String removed = (String)team.remove(member);
         String newMember = this.getUniqueReplacement(team, (List)SConfig.SERVER.proto_summonable_troops.get());
         if (newMember != null) {
            team.add(newMember);
         } else {
            team.add(removed);
         }

      }
   }

   private void awardMember(List<String> team, int member) {
      if (team != null && !team.isEmpty() && member >= 0 && member < team.size()) {
         String s = (String)team.get(member);
         if (!this.team_5.contains(s)) {
            this.team_5.add((String)team.get(member));
         }

      }
   }

   private String getUniqueReplacement(List<String> team, List<String> CONFIG) {
      List<String> possibleReplacements;
      if (this.team_5.isEmpty()) {
         possibleReplacements = new ArrayList<>(CONFIG);
      } else {
         possibleReplacements = this.team_5;
         possibleReplacements.removeAll(team);
         if (!possibleReplacements.isEmpty()) {
            String s = (String)possibleReplacements.get(this.random.nextInt(possibleReplacements.size()));
            this.team_5.remove(s);
            return s;
         }
      }

      List<String> mobsInTeam = new ArrayList<>();

      for(String s : team) {
         String[] string = s.split("_");
         mobsInTeam.add(string[0]);
      }

      possibleReplacements.removeAll(mobsInTeam);
      if (possibleReplacements.isEmpty()) {
         return null;
      } else {
         String member = (String)possibleReplacements.get(this.random.nextInt(possibleReplacements.size()));
         int add = this.isVariantKeeper(member);
         return member + "_" + add;
      }
   }

   public static void teleportToSurface(ServerLevel level, Mob entity) {
      BlockPos startPos = entity.blockPosition();
      if (!level.canSeeSky(startPos)) {
         int surfaceY = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, startPos.getX(), startPos.getZ());
         BlockPos surfacePos = new BlockPos(startPos.getX(), surfaceY, startPos.getZ());
         if (level.canSeeSky(surfacePos)) {
            BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());

            for(int i = 0; i < 8; ++i) {
               BlockState below = level.getBlockState(checkPos.below());
               BlockState current = level.getBlockState(checkPos);
               if (below.isSolid() && current.isAir()) {
                  entity.teleportTo((double)checkPos.getX() + (double)0.5F, (double)checkPos.getY(), (double)checkPos.getZ() + (double)0.5F);
                  return;
               }

               checkPos.move(Direction.DOWN);
            }
         }

         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(startPos.getX(), startPos.getY(), startPos.getZ());

         while(pos.getY() < level.getMaxBuildHeight()) {
            pos.move(Direction.UP);
            if (level.canSeeSky(pos)) {
               entity.teleportTo((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
               return;
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

   public String getChunkId() {
      UUID ownerId = this.getUUID();
      return "hivemind_" + ownerId + "_";
   }

   public boolean shouldLoadChunk() {
      return (Boolean)SConfig.SERVER.proto_chunk.get();
   }

   public int chunkLifeTicks() {
      return 12000;
   }

   static {
      HOSTS = SynchedEntityData.defineId(Proto.class, EntityDataSerializers.INT);
      BIOMASS = SynchedEntityData.defineId(Proto.class, EntityDataSerializers.INT);
      NODE = SynchedEntityData.defineId(Proto.class, EntityDataSerializers.BLOCK_POS);
   }
}
