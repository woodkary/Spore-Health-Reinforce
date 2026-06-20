package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Recipes.EntityContainer;
import com.Harbinger.Spore.Recipes.WombRecipe;
import com.Harbinger.Spore.Screens.AssimilationMenu;
import com.Harbinger.Spore.Sentities.AdaptableEntity;
import com.Harbinger.Spore.Sentities.BaseEntities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Womb extends Organoid implements MenuProvider, AdaptableEntity, IDieWithDiscardEntity {
   private static final EntityDataAccessor<Integer> COUNTER;
   private static final EntityDataAccessor<Integer> BIOMASS;
   private static final EntityDataAccessor<Integer> STATE;
   private static final EntityDataAccessor<BlockPos> LOCATION;
   private static final EntityDataAccessor<Boolean> SPECIAL_DEAD;
   private int breakCounter;
   private final List<String> attributeIDs = new ArrayList<>();
   private int eatingTicks = 0;
   private int pendingAdaptationCounts=0;
   private Vec3 lastLegalPosition=Vec3.ZERO;

   public Womb(EntityType type, Level level, TERRAIN terrain, BlockPos pos) {
      super(type, level);
      this.entityData.set(STATE, terrain.value);
      this.setLocation(pos);
      this.setLegalPosition(Vec3.ZERO);
   }

   public Womb(EntityType type, Level level) {
      super(type, level);
      this.entityData.set(STATE, 0);
      this.setLocation(BlockPos.ZERO);
      this.setLegalPosition(Vec3.ZERO);
   }

   @Override
   public void actuallyHurt(DamageSource source, float amount) {
      if(!source.is(DamageTypes.FREEZE)){
         this.pendingAdaptationCounts=Math.min(this.pendingAdaptationCounts+1,10);
      }
      super.actuallyHurt(source, amount);
   }

   public List<String> getAttributeIDs() {
      return this.attributeIDs;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.womb_loot.get();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(COUNTER, 0);
      this.entityData.define(BIOMASS, 0);
      this.entityData.define(STATE, 0);
      this.entityData.define(LOCATION, BlockPos.ZERO);
      this.entityData.define(SPECIAL_DEAD, false);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WOMB_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public void tick() {
      if (this.entityData.get(BIOMASS) >= (Integer)SConfig.SERVER.reconstructor_biomass.get()) {
         this.summon(this, false);
      }
      tickLegalPosition();
      super.tick();

      if (this.entityData.get(COUNTER) < (Integer)SConfig.SERVER.recontructor_clock.get() * 20) {
         this.entityData.set(COUNTER, (Integer)this.entityData.get(COUNTER) + 1);
      } else {
         this.entityData.set(COUNTER, 0);
         this.entityData.set(BIOMASS, (Integer)this.entityData.get(BIOMASS) + 1);
      }

      if (this.random.nextInt(100) == 0) {
         this.CallNearbyInfected();
      }

      if (this.random.nextInt(40) == 0) {
         this.AssimilateNearbyInfected();
      }

      if (this.random.nextInt(20) == 0 && this.isEating()) {
         this.playSound(SoundEvents.GENERIC_EAT);
      }

      if (this.eatingTicks > 0) {
         --this.eatingTicks;
      }

   }

   public void setBiomass(int biomass) {
      this.entityData.set(BIOMASS, biomass);
   }

   public int getBiomass() {
      return (Integer)this.entityData.get(BIOMASS);
   }

   public void setLocation(BlockPos pos) {
      this.entityData.set(LOCATION, pos);
   }

   public BlockPos getLocation() {
      return (BlockPos)this.entityData.get(LOCATION);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Counter", (Integer)this.entityData.get(COUNTER));
      tag.putInt("Biomass", (Integer)this.entityData.get(BIOMASS));
      tag.putInt("State", (Integer)this.entityData.get(STATE));
      tag.putInt("LocationX", this.getLocation().getX());
      tag.putInt("LocationY", this.getLocation().getY());
      tag.putInt("LocationZ", this.getLocation().getZ());
      ListTag teamTag = new ListTag();

      for(String member : this.attributeIDs) {
         teamTag.add(StringTag.valueOf(member));
      }

      tag.put("mutations", teamTag);
      tag.putInt("adaptationCount",getAdaptationCount());
      addAdditionalLegalPositionData(tag);
   }

   public boolean isEating() {
      return this.eatingTicks > 0;
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(COUNTER, tag.getInt("Counter"));
      this.entityData.set(BIOMASS, tag.getInt("Biomass"));
      this.entityData.set(STATE, tag.getInt("State"));
      int i = tag.getInt("LocationX");
      int j = tag.getInt("LocationY");
      int k = tag.getInt("LocationZ");
      this.setLocation(new BlockPos(i, j, k));
      this.attributeIDs.clear();
      ListTag teamTag = tag.getList("mutations", 8);

      for(int l = 0; l < teamTag.size(); ++l) {
         this.attributeIDs.add(teamTag.getString(l));
      }
      if(tag.contains("adaptationCount")) {
         setAdaptationCount(tag.getInt("adaptationCount"));
      }
      readAdditionalLegalPositionData(tag);
   }

   private void CallNearbyInfected() {
      if (!this.level().isClientSide) {
         AABB hitbox = this.getBoundingBox().inflate((double)50.0F);

         for(Entity en : this.level().getEntities(this, hitbox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (en instanceof Infected) {
               Infected infected = (Infected)en;
               infected.setSearchPos(new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ()));
            }
         }
      }

   }

   public Optional getCurrentRecipe(Entity entity) {
      EntityContainer container = new EntityContainer(entity);
      return this.level().getRecipeManager().getRecipeFor(WombRecipe.WombRecipeType.INSTANCE, container, this.level());
   }

   public void addMutation(WombRecipe recipe) {
      this.attributeIDs.add(recipe.getAttribute());
   }

   private void AssimilateNearbyInfected() {
      if (!this.level().isClientSide) {
         for(Entity en : this.level().getEntities(this, this.getBoundingBox().inflate(0.1), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (en instanceof Infected) {
               Infected infected = (Infected)en;
               this.setBiomass(this.getBiomass() + this.calculateAssimilation(infected) + infected.getKills());
               Optional<WombRecipe> recipe = this.getCurrentRecipe(infected);
               recipe.ifPresent(this::addMutation);
               infected.discard();
               Level var7 = this.level();
               if (var7 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var7;
                  double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
                  double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * (double)0.25F * (double)5.0F;
                  double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
                  serverLevel.sendParticles((SimpleParticleType)Sparticles.BLOOD_PARTICLE.get(), x0, y0, z0, 8, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
               }

               this.playSound(SoundEvents.GENERIC_EAT);
               this.eatingTicks += 80;
               break;
            }
         }
      }

   }

   public int calculateAssimilation(Entity entity) {
      int value = (Integer)SConfig.SERVER.reconstructor_assimilation.get();
      if (entity instanceof Hyper) {
         return value * 4;
      } else {
         return entity instanceof EvolvedInfected ? value * 2 : value;
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor dataAccessor) {
      if (BIOMASS.equals(dataAccessor)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(dataAccessor);
   }

   public EntityDimensions getDimensions(Pose pose) {
      int age = 1;
      if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 4 && this.getBiomass() < (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         age = 2;
      } else if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         age = 3;
      }

      return super.getDimensions(pose).scale((float)age);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
              .add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.reconstructor_hp.get() * (Double)SConfig.SERVER.global_health.get())
              .add(Attributes.ARMOR, (Double)SConfig.SERVER.reconstructor_armor.get() * (Double)SConfig.SERVER.global_armor.get())
              .add(Attributes.FOLLOW_RANGE, (double)16.0F)
              .add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public void aiStep() {
      super.aiStep();
      if (this.breakCounter < 40) {
         ++this.breakCounter;
      } else if (this.getLastDamageSource() == this.damageSources().inWall() && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
         AABB aabb = this.getBoundingBox().inflate(0.2, (double)0.0F, 0.2);
         boolean flag = false;

         for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (blockstate.getDestroySpeed(this.level(), blockpos) < 10.0F && blockstate.getDestroySpeed(this.level(), blockpos) > 0.0F) {
               flag = this.level().destroyBlock(blockpos, true, this) || flag;
               this.breakCounter = 0;
            }
         }
      }

   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.getBiomass() > 1) {
         int age = 1;
         if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 4 && this.getBiomass() < (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
            age = 2;
         } else if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
            age = 3;
         }

         if (age > 1) {
            double maxHealth = (Double)SConfig.SERVER.mound_hp.get() * (double)age * (Double)SConfig.SERVER.global_health.get();
            AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
               health.setBaseValue(maxHealth);
            }

            SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(this, (float)maxHealth);
            AttributeInstance armor = this.getAttribute(Attributes.ARMOR);

            if (armor != null) {
               armor.setBaseValue((Double)SConfig.SERVER.mound_armor.get() * (double)age * (Double)SConfig.SERVER.global_armor.get());
            }
         }
      }

   }
   private void summon(Entity entity, boolean value) {
      summon(entity, value,true);
   }

   private void summon(Entity entity, boolean value,boolean discard) {
      if (Math.random() <= (double)0.3F) {
         this.entityData.set(STATE, this.random.nextInt(TERRAIN.values().length));
      }

      List<? extends String> variantList = this.getVariant().getList();
      if (!variantList.isEmpty()) {
         ResourceLocation entityId = new ResourceLocation((String)variantList.get(this.random.nextInt(variantList.size())));
         EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(entityId);
         if (entityType != null) {
            Mob spawnedEntity = (Mob)entityType.create(this.level());
            if (spawnedEntity != null) {
               if(!hasLegalPosition()){
                  this.setPos(this.lastLegalPosition());
               }
               Vec3 origin = new Vec3((double)this.getLocation().getX(), (double)this.getLocation().getY(), (double)this.getLocation().getZ());
               Vec3 current = this.position();
               double maxDistance = (double)200.0F;
               if (origin.distanceTo(current) > maxDistance) {
                  Vec3 newPos = Utilities.generatePositionAway(origin, (double)100.0F);
                  spawnedEntity.teleportRelative(newPos.x, newPos.y, newPos.z);
               } else {
                  spawnedEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
               }

               if (spawnedEntity instanceof Calamity calamity) {
                   calamity.setSearchArea(this.getLocation());

                  for(String attrId : this.attributeIDs) {
                     ResourceLocation attrLocation = new ResourceLocation(attrId);
                     Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrLocation);
                     if (attribute != null) {
                        AttributeInstance instance = calamity.getAttribute(attribute);
                        if (attribute == Attributes.MAX_HEALTH) {
                           double maxHealth = instance != null ? instance.getValue() + (double)1.0F : SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(calamity) + 1.0F;
                           if (instance != null) {
                              instance.setBaseValue(maxHealth);
                           }

                           SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(calamity, (float)maxHealth);
                        } else if (instance != null) {
                           double e = instance.getValue();
                           instance.setBaseValue(e + (double)1.0F);
                        }
                     }
                  }
                  if(this.pendingAdaptationCounts>0){
                     calamity.setAdaptationCount(this.pendingAdaptationCounts);
                  }

                  if (value) {
                     calamity.setSecondsOnFire(3);
                     float halfMaxHealth = SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(calamity);
                     SporeEntityHeeaafastthManager.INSTANCE.setHeeaafastth(calamity, halfMaxHealth);
                     //calamity.setHealth(halfMaxHealth);
                  }
               }

               Level var21 = this.level();
               if (var21 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var21;
                  double x0 = this.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
                  double y0 = this.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
                  double z0 = this.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
                  serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
                  spawnedEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
               }

               this.level().addFreshEntity(spawnedEntity);
               if(discard) {
                  this.discard();
               }
            }
         }
      }
   }
   @javax.annotation.Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_33283_, MobSpawnType p_33284_, @javax.annotation.Nullable SpawnGroupData p_33285_, @javax.annotation.Nullable CompoundTag p_33286_) {
      this.syncAtFinalizeSpawn();
      return super.finalizeSpawn(serverLevelAccessor, p_33283_, p_33284_, p_33285_, p_33286_);
   }
   @Override
   public void tickDeath() {
      if (this.getHealth() > 0.0f) {
         return;
      }
      if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         this.summon(this, true,true);
      }
      super.tickDeath();
   }
   public void die(DamageSource p_21014_) {
      if (this.getHealth()>0.0f) {
         return;
      }
      if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         this.summon(this, true,true);
      }
      super.die(p_21014_);
   }

   public int getEmerge_tick() {
      return 60;
   }

   public TERRAIN getVariant() {
      return TERRAIN.byId((Integer)this.entityData.get(STATE) & 255);
   }

   public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return new AssimilationMenu(i, inventory);
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         if (serverPlayer.getAbilities().instabuild && !this.level().isClientSide) {
            NetworkHooks.openScreen(serverPlayer, this, this.blockPosition());
            return InteractionResult.SUCCESS;
         }
      }

      return super.mobInteract(player, hand);
   }

   static {
      COUNTER = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
      BIOMASS = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
      STATE = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.INT);
      LOCATION = SynchedEntityData.defineId(Womb.class, EntityDataSerializers.BLOCK_POS);
      SPECIAL_DEAD=SynchedEntityData.defineId(Womb.class, EntityDataSerializers.BOOLEAN);
   }

   @Override
   public int getAdaptationCount() {
      return pendingAdaptationCounts;
   }

   @Override
   public void setAdaptationCount(int adaptationCount) {
      pendingAdaptationCounts = adaptationCount;
   }
   @Override
   public LivingEntity self(){
      return this;
   }

   @Override
   public boolean isSpecialDefasd() {
      return this.entityData.get(SPECIAL_DEAD);
   }

   @Override
   public boolean hasLegalPosition() {
      if(Double.isNaN(this.position.x)||Double.isNaN(this.position.y)||Double.isNaN(this.position.z)) {
         return false;
      }
      return this.lastLegalPosition().distanceTo(this.position) <= 5000.0F;
   }

   @Override
   public Vec3 lastLegalPosition() {
      return this.lastLegalPosition;
   }

   @Override
   public void setLegalPosition(Vec3 position) {
      this.lastLegalPosition = position;
   }
   //specialDie不带有discard，防止递归
   @Override
   public void specialDie(DamageSource source) {
      this.entityData.set(SPECIAL_DEAD, true);
      if (this.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         this.summon(this, true,false);
      }
   }


   public enum TERRAIN {
      GROUND_LEVEL(0, (List)SConfig.SERVER.reconstructor_terrain.get()),
      WATER_LEVEL(1, (List)SConfig.SERVER.reconstructor_water.get()),
      AIR_LEVEL(2, (List)SConfig.SERVER.reconstructor_air.get()),
      UNDERGROUND(3, (List)SConfig.SERVER.reconstructor_underground.get());

      private final int value;
      private final List list;
      private static final TERRAIN[] BY_ID = (TERRAIN[])Arrays.stream(values()).sorted(Comparator.comparingInt(TERRAIN::getValue)).toArray((x$0) -> new TERRAIN[x$0]);

      private TERRAIN(int v, List l) {
         this.value = v;
         this.list = l;
      }

      public int getValue() {
         return this.value;
      }

      public List getList() {
         return this.list;
      }

      public static TERRAIN byId(int id) {
         return BY_ID[id % BY_ID.length];
      }

      // $FF: synthetic method
      private static TERRAIN[] $values() {
         return new TERRAIN[]{GROUND_LEVEL, WATER_LEVEL, AIR_LEVEL, UNDERGROUND};
      }
   }
}
