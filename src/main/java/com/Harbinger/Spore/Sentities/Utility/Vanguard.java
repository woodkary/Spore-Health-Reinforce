package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Spotion;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoadRequest;
import com.Harbinger.Spore.ExtremelySusThings.ChunkLoaderHelper;
import com.Harbinger.Spore.ExtremelySusThings.SporeSavedData;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.ChunkLoaderMob;
import com.Harbinger.Spore.Sentities.ColdEndurance;
import com.Harbinger.Spore.Sentities.ColdWeakness;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.FloatDiveGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Vanguard extends UtilityEntity implements CrossbowAttackMob, Enemy, ArmorPersentageBypass, ChunkLoaderMob, ColdWeakness {
   private static final EntityDataAccessor IS_CHARGING_CROSSBOW;
   private static final EntityDataAccessor KILLS;
   private static final EntityDataAccessor RAID_TIME_OUT;
   private static final EntityDataAccessor VILLAGE;
   private int attackAnimationTick;

   public Vanguard(EntityType type, Level level) {
      super(type, level);
      this.navigation = new WallClimberNavigation(this, level);
      this.setMaxUpStep(1.0F);
   }

   protected boolean canRide(Entity entity) {
      return !(entity instanceof Infected) && !(entity instanceof UtilityEntity) ? false : super.canRide(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.vanguard_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.vanguard_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.vanguard_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(1, new VanguardRangedCrossbowAttackGoal(this, 12.0F));
      this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, (double)1.0F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new VanguardFireGoal(this));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(6, new FloatDiveGoal(this));
      super.registerGoals();
   }

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return super.calculateFallDamage(p_21237_, p_21238_) - 15;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.VANGUARD_AMBIENT.get();
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.EVOLVE_HURT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
      return projectileWeapon == Items.CROSSBOW;
   }

   public void setKills(int val) {
      this.entityData.set(KILLS, val);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_CHARGING_CROSSBOW, false);
      this.entityData.define(KILLS, 0);
      this.entityData.define(RAID_TIME_OUT, 0);
      this.entityData.define(VILLAGE, BlockPos.ZERO);
   }

   public BlockPos getVillage() {
      return (BlockPos)this.entityData.get(VILLAGE);
   }

   public void setVillage(BlockPos pos) {
      this.entityData.set(VILLAGE, pos);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(KILLS, tag.getInt("kills"));
      this.entityData.set(RAID_TIME_OUT, tag.getInt("raid"));
      int x = tag.getInt("villageX");
      int y = tag.getInt("villageY");
      int z = tag.getInt("villageZ");
      this.setVillage(new BlockPos(x, y, z));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("kills", (Integer)this.entityData.get(KILLS));
      tag.putInt("raid", (Integer)this.entityData.get(RAID_TIME_OUT));
      tag.putInt("villageX", this.getVillage().getX());
      tag.putInt("villageY", this.getVillage().getY());
      tag.putInt("villageZ", this.getVillage().getZ());
   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      if (entity instanceof LivingEntity livingEntity) {
         livingEntity.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600, 0));
         livingEntity.hurtTime = 0;
         livingEntity.invulnerableTime = 0;
      }

      this.playSound((SoundEvent)Ssounds.VANGUARD_SLASH.get());
      return super.doHurtTarget(entity);
   }

   public void awardKillScore(Entity killed, int scoreValue, DamageSource source) {
      super.awardKillScore(killed, scoreValue, source);
      this.entityData.set(KILLS, (Integer)this.entityData.get(KILLS) + 1);
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.FIREWORKS)) {
         return false;
      } else {
         if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)) {
            amount /= 2.0F;
         }

         return super.hurt(source, amount);
      }
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
         this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else {
         super.handleEntityEvent(value);
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
      ItemStack itemstack = this.getMainHandItem();
      if (itemstack.is(Items.CROSSBOW)) {
         Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
         map.putIfAbsent(Enchantments.MULTISHOT, 1);
         map.putIfAbsent(Enchantments.QUICK_CHARGE, 3);
         EnchantmentHelper.setEnchantments(map, itemstack);
         this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
      }

   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      if (level instanceof ServerLevel serverLevel) {
         this.locateVillageOnSpawn(serverLevel);
         this.teleportToSurface(serverLevel, this);
      }

      this.populateDefaultEquipmentSlots(this.random, difficulty);
      return super.finalizeSpawn(level, difficulty, p_21436_, p_21437_, p_21438_);
   }

   private void griefBlocks() {
      AABB aabb = this.getBoundingBox().inflate((double)0.5F).move((double)0.0F, (double)0.5F, (double)0.0F);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (this.blockBreakingParameter(blockstate, blockpos)) {
            this.interactBlock(blockpos, this.level());
         }
      }

   }

   public boolean blockBreakingParameter(BlockState blockstate, BlockPos blockpos) {
      float value = blockstate.getDestroySpeed(this.level(), blockpos);
      return this.tickCount % 20 == 0 && value > 0.0F && value <= (float)this.getBreaking();
   }

   public int getBreaking() {
      return (Integer)SConfig.SERVER.hyper_bd.get();
   }

   public boolean interactBlock(BlockPos blockPos, Level level) {
      BlockState state = level.getBlockState(blockPos);
      return state.is(Utilities.biomass) ? level.setBlock(blockPos, ((Block)Sblocks.MEMBRANE_BLOCK.get()).defaultBlockState(), 3) : level.destroyBlock(blockPos, false, this);
   }

   public boolean isChargingCrossbow() {
      return (Boolean)this.entityData.get(IS_CHARGING_CROSSBOW);
   }

   public void setChargingCrossbow(boolean p_33302_) {
      this.entityData.set(IS_CHARGING_CROSSBOW, p_33302_);
   }

   public void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float v) {
      this.shootCrossbowProjectile(this, livingEntity, projectile, v, 3.2F);
   }

   public void onCrossbowAttackPerformed() {
      this.noActionTime = 0;
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      this.performCrossbowAttack(this, 3.2F);
   }

   public void tick() {
      super.tick();
      if (this.tickCount % 40 == 0 && this.horizontalCollision && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
         this.griefBlocks();
      }

      if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth() && !this.hasEffect(MobEffects.REGENERATION) && (Integer)this.entityData.get(KILLS) > 0) {
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
         this.entityData.set(KILLS, (Integer)this.entityData.get(KILLS) - 1);
      }

      if ((Integer)this.entityData.get(RAID_TIME_OUT) > 0) {
         this.entityData.set(RAID_TIME_OUT, (Integer)this.entityData.get(RAID_TIME_OUT) - 1);
      }

      if (this.tickCount % 40 == 0 && this.getVillage() != BlockPos.ZERO && this.getTarget() == null) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var2;
            this.tickMovement(serverLevel);
         }
      }

      if (this.tickCount % 20 == 0 && this.getVanguardRaid() <= 0 && this.compareTarget(this.getTarget())) {
         this.callReinforcements();
      }

   }

   public float amountOfDamage(float value) {
      return value * 0.25F;
   }

   public String getChunkId() {
      UUID uuid1 = this.getUUID();
      return "vanguard_" + uuid1 + "_";
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.vanguard_loot.get();
   }

   public boolean shouldLoadChunk() {
      return (Boolean)SConfig.SERVER.vanguard_chunk_load.get() && this.getVillage() != BlockPos.ZERO;
   }

   public int chunkLifeTicks() {
      return 600;
   }

   public ColdEndurance getEndurance() {
      return ColdEndurance.ABNORMALS;
   }

   public int getVanguardRaid() {
      return (Integer)this.entityData.get(RAID_TIME_OUT);
   }

   public void setVanguardRaid(int val) {
      this.entityData.set(RAID_TIME_OUT, val);
   }

   public boolean compareTarget(LivingEntity living) {
      if (living == null) {
         return false;
      } else {
         return ((List)SConfig.SERVER.proto_sapient_target.get()).contains(living.getEncodeId()) || living.getHealth() >= 100.0F || living instanceof Player;
      }
   }

   private void callReinforcements() {
      List<String> ids = new ArrayList();

      while(ids.size() < (Integer)SConfig.SERVER.vanguard_raid_size.get()) {
         for(String s : (List<String>)SConfig.SERVER.vanguard_members.get()) {
            String[] str = s.split("\\|");
            if (Math.random() < (double)((float)Integer.parseUnsignedInt(str[1]) / 100.0F)) {
               ids.add(str[0]);
               break;
            }
         }
      }

      for(String id : ids) {
         Vec3 vec3 = Utilities.generatePositionAway(this.position(), (double)15.0F);
         ResourceLocation entityId = new ResourceLocation(id);
         EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(entityId);
         if (entityType != null) {
            Entity entity = entityType.create(this.level());
            if (entity instanceof Mob) {
               Mob mob = (Mob)entity;
               Level var10 = this.level();
               if (var10 instanceof ServerLevelAccessor) {
                  ServerLevelAccessor accessor = (ServerLevelAccessor)var10;
                  mob.randomTeleport(vec3.x, this.getY(), vec3.z, false);
                  mob.finalizeSpawn(accessor, accessor.getCurrentDifficultyAt(BlockPos.containing(this.position())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
                  if (mob instanceof Infected) {
                     Infected infected = (Infected)mob;
                     infected.setSearchPos(this.getOnPos());
                     infected.setFollowPartner(this);
                     infected.setTarget(this.getTarget());
                     infected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                  }

                  accessor.addFreshEntity(mob);
               }
            }
         }
      }

      this.playSound((SoundEvent)Ssounds.VANGUARD_CALL.get());
      this.setVanguardRaid(6000);
   }

   private void tickMovement(ServerLevel serverLevel) {
      this.tryTeleportIfFar(serverLevel);
      this.moveTowardVillage();
      if (this.distanceToSqr(Vec3.atCenterOf(new Vec3i(this.getVillage().getX(), (int)this.position().y(), this.getVillage().getZ()))) < (double)100.0F) {
         this.playArrivalSound();
         this.setVillage(BlockPos.ZERO);
         this.removeChunkLoad();
      }

   }

   private void tryTeleportIfFar(ServerLevel serverLevel) {
      if (serverLevel.isLoaded(this.getVillage())) {
         double distSqr = this.distanceToSqr(Vec3.atCenterOf(this.getVillage()));
         if (!(distSqr < (double)40000.0F)) {
            BlockPos tp = this.findSafeGround(this.getVillage());
            if (tp != null) {
               this.teleportTo((double)tp.getX() + (double)0.5F, (double)tp.getY(), (double)tp.getZ() + (double)0.5F);
               this.addChunkLoad(tp, serverLevel);
            }

         }
      }
   }

   private void moveTowardVillage() {
      this.navigation.stop();
      Path path = this.navigation.createPath(this.getVillage(), 1);
      if (path != null) {
         this.getNavigation().moveTo(path, 1.2);
      }

   }

   private void playArrivalSound() {
      for(Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate((double)100.0F))) {
         p.playNotifySound((SoundEvent)Ssounds.VANGUARD_RAID.get(), SoundSource.MASTER, 1.0F, 1.0F);
      }

   }

   private BlockPos findSafeGround(BlockPos pos) {
      ServerLevel level = (ServerLevel)this.level();
      BlockPos.MutableBlockPos mutable = pos.mutable();

      for(int y = 0; y < 20; ++y) {
         if (level.getBlockState(mutable).isAir() && level.getBlockState(mutable.below()).isSolid()) {
            return mutable.immutable();
         }

         mutable.move(Direction.UP);
      }

      return null;
   }

   private void addChunkLoad(BlockPos pos, ServerLevel serverLevel) {
      ChunkPos chunk = new ChunkPos(pos);
      String id = "vanguard_" + this.getUUID();
      ChunkLoaderHelper.addRequest(new ChunkLoadRequest(serverLevel.dimension(), new ChunkPos[]{chunk}, 0, id, (long)this.chunkLifeTicks(), this.getUUID()));
   }

   private void removeChunkLoad() {
      String id = "vanguard_" + this.getUUID();
      ChunkLoaderHelper.removeRequest(id);
   }

   public boolean removeWhenFarAway(double value) {
      Level var4 = this.level();
      if (!(var4 instanceof ServerLevel serverLevel)) {
         return false;
      } else {
         SporeSavedData data = SporeSavedData.getDataLocation(serverLevel);
         return data != null && data.getAmountOfHiveminds() >= (Integer)SConfig.SERVER.proto_spawn_world_mod.get() && value > (double)256.0F;
      }
   }

   private void locateVillageOnSpawn(ServerLevel serverLevel) {
      List<Villager> villagers = serverLevel.getEntitiesOfClass(Villager.class, (new AABB(this.blockPosition())).inflate((double)256.0F));
      if (!villagers.isEmpty()) {
         Villager nearest = villagers.stream().min((a, b) -> Double.compare(this.distanceToSqr(a), this.distanceToSqr(b))).orElse(null);
         BlockPos villPos = nearest.blockPosition();
         this.setVillage(villPos);
      } else {
         int radius = 128;
         BlockPos foundVillage = serverLevel.findNearestMapStructure(StructureTags.VILLAGE, this.blockPosition(), radius, false);
         this.setVillage((BlockPos)Objects.requireNonNullElse(foundVillage, BlockPos.ZERO));
      }
   }

   public void teleportToSurface(Level level, Mob entity) {
      if (!level.canSeeSky(entity.blockPosition())) {
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(entity.getX()), level.getMaxBuildHeight(), Mth.floor(entity.getZ()));

         while(pos.getY() > level.getMinBuildHeight()) {
            pos.move(Direction.DOWN);
            BlockState state = level.getBlockState(pos);
            BlockState stateAbove = level.getBlockState(pos.above());
            if (state.isSolidRender(level, pos) && stateAbove.isAir()) {
               entity.teleportTo((double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.01, (double)pos.getZ() + (double)0.5F);
               return;
            }
         }

      }
   }

   static {
      IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Vanguard.class, EntityDataSerializers.BOOLEAN);
      KILLS = SynchedEntityData.defineId(Vanguard.class, EntityDataSerializers.INT);
      RAID_TIME_OUT = SynchedEntityData.defineId(Vanguard.class, EntityDataSerializers.INT);
      VILLAGE = SynchedEntityData.defineId(Vanguard.class, EntityDataSerializers.BLOCK_POS);
   }

   public static class VanguardFireGoal extends Goal {
      private final Vanguard vanguard;
      protected BlockPos targetPos;
      protected List<BlockPos> targetPositions = new ArrayList<>();
      protected List<BlockPos> firePositions = new ArrayList<>();
      private static final ItemStack FLINT;
      private int fireCooldown = 0;

      public VanguardFireGoal(Vanguard vanguard) {
         this.vanguard = vanguard;
      }

      boolean searchForFunnyBlocks() {
         this.targetPositions.clear();
         this.firePositions.clear();
         AABB aabb = this.vanguard.getBoundingBox().inflate((double)10.0F, (double)6.0F, (double)10.0F);
         boolean thereAreBurnAbleBlocks = false;

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockstate = this.vanguard.level().getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (block instanceof BedBlock || block instanceof FurnaceBlock) {
               this.targetPositions.add(blockpos.immutable());
            }

            if (block instanceof FireBlock) {
               this.firePositions.add(blockpos.immutable());
            }

            if (block.isFlammable(blockstate, this.vanguard.level(), blockpos, Direction.UP)) {
               thereAreBurnAbleBlocks = true;
            }
         }

         if (!this.targetPositions.isEmpty() && thereAreBurnAbleBlocks) {
            this.targetPos = (BlockPos)this.targetPositions.get(this.vanguard.getRandom().nextInt(this.targetPositions.size()));
            boolean hasFireNearby = this.firePositions.stream().anyMatch((pos) -> pos.distSqr(this.targetPos) < (double)36.0F);
            return !hasFireNearby;
         } else {
            return false;
         }
      }

      public boolean canUse() {
         return this.vanguard.tickCount % 40 == 0 ? this.searchForFunnyBlocks() : false;
      }

      public void start() {
         super.start();
         if (this.targetPos != null) {
            Path path = this.vanguard.navigation.createPath(this.targetPos, 1);
            if (path != null) {
               this.vanguard.navigation.moveTo(path, (double)1.0F);
            }
         }

         this.fireCooldown = 0;
      }

      public void tick() {
         super.tick();
         ++this.fireCooldown;
         if (this.targetPos != null) {
            double distanceSqr = this.targetPos.distToCenterSqr(this.vanguard.position());
            if (distanceSqr < (double)25.0F) {
               if (this.fireCooldown >= 20) {
                  this.setFire();
                  this.fireCooldown = 0;
                  Vec3 awayDirection = this.vanguard.position().subtract(Vec3.atCenterOf(this.targetPos)).normalize();
                  Vec3 awayPos = this.vanguard.position().add(awayDirection.scale((double)10.0F));
                  Path awayPath = this.vanguard.navigation.createPath(BlockPos.containing(awayPos), 1);
                  if (awayPath != null) {
                     this.vanguard.navigation.moveTo(awayPath, (double)1.5F);
                  }

                  this.stop();
               }
            } else if (this.vanguard.navigation.isDone()) {
               Path path = this.vanguard.navigation.createPath(this.targetPos, 1);
               if (path != null) {
                  this.vanguard.navigation.moveTo(path, (double)1.0F);
               }
            }
         }

      }

      public boolean canContinueToUse() {
         return this.targetPos != null && this.vanguard.tickCount % 400 != 0;
      }

      public void setFire() {
         if (this.targetPos != null) {
            AABB aabb = (new AABB(this.targetPos)).inflate((double)3.0F, (double)2.0F, (double)3.0F);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockState blockstate = this.vanguard.level().getBlockState(blockpos);
               if (this.vanguard.getRandom().nextFloat() < 0.3F) {
                  for(Direction direction : Direction.values()) {
                     BlockPos adjacentPos = blockpos.relative(direction);
                     BlockState adjacentState = this.vanguard.level().getBlockState(adjacentPos);
                     if (blockstate.isFlammable(this.vanguard.level(), blockpos, direction) && adjacentState.isAir()) {
                        BlockState fireState = Blocks.FIRE.defaultBlockState();
                        if (FireBlock.canBePlacedAt(this.vanguard.level(), adjacentPos, direction)) {
                           this.vanguard.level().setBlock(adjacentPos, fireState, 3);
                           break;
                        }
                     }
                  }
               }
            }

            this.vanguard.level().removeBlock(this.targetPos, true);
            this.vanguard.playSound((SoundEvent)Ssounds.VANGUARD_GRIEF.get());
         }
      }

      public void stop() {
         super.stop();
         this.targetPos = null;
         this.targetPositions.clear();
         this.firePositions.clear();
         this.vanguard.navigation.stop();
      }

      static {
         FLINT = new ItemStack(Items.FLINT_AND_STEEL);
      }
   }

   public static class VanguardRangedCrossbowAttackGoal extends Goal {
      private final Vanguard mob;
      private CrossbowState state;
      private final float attackRadiusSqr;
      private int attackDelay;
      private boolean fireworks;

      public VanguardRangedCrossbowAttackGoal(Vanguard mob, float attackRadius) {
         this.state = CrossbowState.UNCHARGED;
         this.mob = mob;
         this.attackRadiusSqr = attackRadius * attackRadius;
      }

      public boolean canUse() {
         return this.isHoldingCrossbow();
      }

      public boolean canContinueToUse() {
         return this.isHoldingCrossbow();
      }

      private boolean isHoldingCrossbow() {
         return this.mob.isHolding((i) -> i.getItem() instanceof CrossbowItem);
      }

      public void stop() {
         super.stop();
         if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
         }

         this.state = CrossbowState.UNCHARGED;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      private ItemStack createExplosiveRocket() {
         this.fireworks = true;
         ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
         CompoundTag fw = rocket.getOrCreateTagElement("Fireworks");
         fw.putByte("Flight", (byte)1);
         ListTag list = new ListTag();
         list.add(this.makeExplosionNBT(0, new int[]{3887386}, new int[]{4312372}));
         list.add(this.makeExplosionNBT(1, new int[]{15435844}, new int[]{14602026}));
         list.add(this.makeExplosionNBT(4, new int[]{2437522}, new int[]{2651799}));
         fw.put("Explosions", list);
         return rocket;
      }

      private CompoundTag makeExplosionNBT(int shape, int[] colors, int[] fades) {
         CompoundTag n = new CompoundTag();
         n.putByte("Type", (byte)shape);
         n.putIntArray("Colors", colors);
         n.putIntArray("FadeColors", fades);
         n.putBoolean("Trail", true);
         n.putBoolean("Flicker", true);
         return n;
      }

      private ItemStack getArrow() {
         this.fireworks = false;
         return PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), (Potion)Spotion.MYCELIUM_POTION.get());
      }

      private static void addChargedProjectile(ItemStack crossbow, ItemStack projectile) {
         CompoundTag tag = crossbow.getOrCreateTag();
         ListTag list;
         if (tag.contains("ChargedProjectiles", 9)) {
            list = tag.getList("ChargedProjectiles", 10);
         } else {
            list = new ListTag();
         }

         CompoundTag projTag = new CompoundTag();
         projectile.save(projTag);
         list.add(projTag);
         tag.put("ChargedProjectiles", list);
      }

      public void tick() {
         LivingEntity target = this.mob.getTarget();
         boolean hasLOS = target != null && this.mob.getSensing().hasLineOfSight(target);
         double dist = target == null ? (double)0.0F : this.mob.distanceToSqr(target);
         ItemStack crossbow = this.mob.getMainHandItem();
         if (crossbow.getItem() instanceof CrossbowItem) {
            CompoundTag tag = crossbow.getOrCreateTag();
            if (CrossbowItem.isCharged(crossbow) && this.state == CrossbowState.UNCHARGED) {
               this.state = CrossbowState.CHARGED;
               this.attackDelay = 5;
            }

            switch (this.state) {
               case UNCHARGED:
                  if (!CrossbowItem.isCharged(crossbow)) {
                     this.mob.startUsingItem(InteractionHand.MAIN_HAND);
                     this.state = CrossbowState.CHARGING;
                     this.mob.setChargingCrossbow(true);
                  }
                  break;
               case CHARGING:
                  if (!this.mob.isUsingItem()) {
                     this.state = CrossbowState.UNCHARGED;
                     return;
                  }

                  int useTicks = this.mob.getTicksUsingItem();
                  if (useTicks >= CrossbowItem.getChargeDuration(crossbow)) {
                     ItemStack projectile = Math.random() < 0.2 ? this.createExplosiveRocket() : this.getArrow();
                     addChargedProjectile(crossbow, projectile);
                     CrossbowItem.setCharged(crossbow, true);
                     this.mob.releaseUsingItem();
                     this.state = CrossbowState.CHARGED;
                     this.attackDelay = 5;
                  }
                  break;
               case CHARGED:
                  if (--this.attackDelay <= 0) {
                     this.state = CrossbowState.READY_TO_ATTACK;
                  }

                  this.mob.setChargingCrossbow(false);
                  break;
               case READY_TO_ATTACK:
                  if (target != null && hasLOS && dist <= (double)this.attackRadiusSqr) {
                     this.mob.performRangedAttack(target, 1.0F);
                     this.mob.playSound(this.fireworks ? (SoundEvent)Ssounds.VANGUARD_FIREWORKS.get() : (SoundEvent)Ssounds.VANGUARD_SHOOT.get());
                     CrossbowItem.setCharged(crossbow, false);
                     tag.remove("ChargedProjectiles");
                     this.state = CrossbowState.UNCHARGED;
                  }
            }

         }
      }

      static enum CrossbowState {
         UNCHARGED,
         CHARGING,
         CHARGED,
         READY_TO_ATTACK;

         // $FF: synthetic method
         private static CrossbowState[] $values() {
            return new CrossbowState[]{UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK};
         }
      }
   }
}
