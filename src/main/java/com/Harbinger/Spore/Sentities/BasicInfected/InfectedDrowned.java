package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.WaterInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.HybridPathNavigation;
import com.Harbinger.Spore.Sentities.AI.ReturnToWater;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.MovementControls.WaterXlandMovement;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

public class InfectedDrowned extends Infected implements WaterInfected, EvolvingInfected {
   public InfectedDrowned(EntityType type, Level level) {
      super(type, level);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.moveControl = new WaterXlandMovement(this);
      this.navigation = new HybridPathNavigation(this, this.level());
   }

   public void travel(Vec3 p_32858_) {
      if (this.isEffectiveAi() && this.isInFluidType()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
      } else {
         super.travel(p_32858_);
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_drow_loot.get();
   }

   public float getStepHeight() {
      return this.isInFluidType() ? 2.0F : 1.0F;
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   protected void customServerAiStep() {
      if (!this.isInWater() && this.tickCount % 20 == 0) {
         AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);

         assert speed != null;

         speed.setBaseValue(0.15);
      }

   }

   protected void registerGoals() {
      this.goalSelector.addGoal(4, new ReturnToWater(this, 1.2));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, (double)1.0F));
      this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)3.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      super.registerGoals();
   }

   public void tick() {
      super.tick();
      this.tickEvolution(this, (List)SConfig.SERVER.drowned_ev.get(), ScamperVariants.DROWNED);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.DROWNED_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.DROWNED_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_dr_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_dr_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_dr_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)48.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   public String origin() {
      return "minecraft:drowned";
   }
}
