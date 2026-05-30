package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Utility.Specter;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class InfectedWanderingTrader extends Infected implements EvolvingInfected {
   public InfectedWanderingTrader(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_INVISIBILITY), SoundEvents.WANDERING_TRADER_DRINK_POTION, (p_35882_) -> !this.isInvisible() && this.isAggressive() && (Boolean)SConfig.SERVER.inf_van_potion.get()));
      this.goalSelector.addGoal(0, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HEALING), SoundEvents.WANDERING_TRADER_DRINK_POTION, (p_35882_) -> this.getHealth() < this.getMaxHealth() / 2.0F && !this.isAggressive() && (Boolean)SConfig.SERVER.inf_van_potion.get()) {
         public void start() {
            InfectedWanderingTrader.this.setHunger(0);
            super.start();
         }
      });
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
      return (List)SConfig.DATAGEN.inf_wan_loot.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_van_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_van_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_van_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)24.0F).add(Attributes.ATTACK_KNOCKBACK, 0.3);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.TRADER_AMBIENT.get();
   }

   protected SoundEvent getHurtSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound() {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public void baseTick() {
      super.baseTick();
      this.tickEvolution(this, (List)SConfig.SERVER.villager_ev.get(), ScamperVariants.VILLAGER);
   }

   public void Evolve(Infected livingEntity, List value, ScamperVariants variants) {
      if (this.getLinked()) {
         Specter specter = new Specter((EntityType)Sentities.SPECTER.get(), this.level());
         specter.setBiomass(this.getKills() + this.getEvoPoints());
         specter.setCustomName(this.getCustomName());
         specter.moveTo(this.getX(), this.getY(), this.getZ());
         this.level().addFreshEntity(specter);
         this.discard();
         Level var6 = this.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var6;
            double x0 = livingEntity.getX() - ((double)this.random.nextFloat() - 0.1) * 0.1;
            double y0 = livingEntity.getY() + ((double)this.random.nextFloat() - (double)0.25F) * 0.15 * (double)5.0F;
            double z0 = livingEntity.getZ() + ((double)this.random.nextFloat() - 0.1) * 0.1;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x0, y0, z0, 2, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F);
         }
      } else {
         EvolvingInfected.super.Evolve(livingEntity, value, variants);
      }

   }

   public String origin() {
      return "minecraft:wandering_trader";
   }
}
