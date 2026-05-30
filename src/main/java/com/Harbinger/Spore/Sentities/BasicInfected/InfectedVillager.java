package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Variants.InfVillagerSkins;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags.Biomes;
import org.jetbrains.annotations.Nullable;

public class InfectedVillager extends Infected implements EvolvingInfected, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;

   public InfectedVillager(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(3, new OpenDoorGoal(this, true) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.higher_thinking.get();
         }

         public void start() {
            this.mob.swing(InteractionHand.MAIN_HAND);
            super.start();
         }
      });
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_villager_loot.get();
   }

   protected void customServerAiStep() {
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this) && (Boolean)SConfig.SERVER.higher_thinking.get()) {
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      }

      super.customServerAiStep();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_vil_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_vil_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_vil_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   public void baseTick() {
      super.baseTick();
      this.tickEvolution(this, (List)SConfig.SERVER.villager_ev.get(), ScamperVariants.VILLAGER);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_VILLAGER_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public String origin() {
      return "minecraft:villager";
   }

   private void setVariant(InfVillagerSkins variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public InfVillagerSkins getVariant() {
      return InfVillagerSkins.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= InfVillagerSkins.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return InfVillagerSkins.values().length;
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
      Holder<Biome> biome = serverLevelAccessor.getBiome(this.getOnPos());
      if (biome.is(Biomes.IS_DESERT)) {
         this.setVariant(InfVillagerSkins.DESERT);
      }

      if (biome.is(net.minecraft.world.level.biome.Biomes.JUNGLE) || biome.is(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE)) {
         this.setVariant(InfVillagerSkins.JUNGLE);
      }

      if (biome.is(net.minecraft.world.level.biome.Biomes.SAVANNA) || biome.is(net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU)) {
         this.setVariant(InfVillagerSkins.SAVANNA);
      }

      if (biome.is(Biomes.IS_SWAMP)) {
         this.setVariant(InfVillagerSkins.SWAMP);
      }

      if (biome.is(Biomes.IS_CONIFEROUS)) {
         this.setVariant(InfVillagerSkins.TAIGA);
      }

      if (biome.is(Biomes.IS_SNOWY)) {
         this.setVariant(InfVillagerSkins.TUNDRA);
      }

      return super.finalizeSpawn(serverLevelAccessor, p_21435_, p_21436_, p_21437_, p_21438_);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(InfectedVillager.class, EntityDataSerializers.INT);
   }
}
