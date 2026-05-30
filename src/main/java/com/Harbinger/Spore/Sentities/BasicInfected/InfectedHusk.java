package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class InfectedHusk extends Infected implements EvolvingInfected {
   public InfectedHusk(Level level) {
      super((EntityType)Sentities.INF_HUSK.get(), level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_husk_loot.get();
   }

   public boolean doHurtTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
      }

      return super.doHurtTarget(entity);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_husk_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_husk_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_husk_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   public void baseTick() {
      super.baseTick();
      this.tickEvolution(this, (List)SConfig.SERVER.husk_ev.get(), ScamperVariants.DEFAULT);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      RandomSource randomsource = p_33282_.getRandom();
      this.populateDefaultEquipmentSlots(randomsource, p_33283_);
      this.populateDefaultEquipmentEnchantments(randomsource, p_33283_);
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HUSK_AMBIENT.get();
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
      return "minecraft:husk";
   }
}
