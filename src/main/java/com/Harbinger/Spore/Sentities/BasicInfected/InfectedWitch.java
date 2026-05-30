package com.Harbinger.Spore.Sentities.BasicInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Damage.SdamageTypes;
import com.Harbinger.Spore.Sentities.EvolvingInfected;
import com.Harbinger.Spore.Sentities.AI.BuffAlliesGoal;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.RangedBuff;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectedWitch extends Infected implements RangedAttackMob, RangedBuff, EvolvingInfected {
   private Potion potion = null;

   public InfectedWitch(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HEALING), SoundEvents.WITCH_DRINK, (p_35882_) -> this.getHealth() < this.getMaxHealth() / 2.0F && (Boolean)SConfig.SERVER.use_potions.get()) {
         public void start() {
            InfectedWitch.this.setHunger(0);
            super.start();
         }
      });
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         public boolean canUse() {
            return super.canUse() && (this.mob.getRandom().nextInt(0, 10) == 8 || !(Boolean)SConfig.SERVER.use_potions.get());
         }

         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)1.5F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(1, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_FIRE_RESISTANCE), SoundEvents.WITCH_DRINK, (p_35882_) -> this.isOnFire() && this.getLastDamageSource() != null && this.getLastDamageSource() == this.damageSources().inFire() && !this.hasEffect(MobEffects.FIRE_RESISTANCE) && (Boolean)SConfig.SERVER.use_potions.get()));
      this.goalSelector.addGoal(4, new BuffAlliesGoal(this, Infected.class, 1.3, 35, 45, 3.0F, (entity) -> entity.hasEffect((MobEffect)Seffects.STARVATION.get()) && !(entity instanceof InfectedWitch)) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.use_potions.get() && this.mob.getLastHurtByMob() == null;
         }
      });
      this.goalSelector.addGoal(4, new BuffAlliesGoal(this, Mob.class, 1.3, (Integer)SConfig.SERVER.buff_potion_meter.get(), (Integer)SConfig.SERVER.at_potion_meter.get(), 3.0F, (entity) -> ((List)SConfig.SERVER.evolved.get()).contains(entity.getEncodeId()) && !(entity instanceof InfectedWitch)) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.use_potions.get() && this.mob.getLastHurtByMob() == null;
         }
      });
      this.goalSelector.addGoal(4, new BuffAlliesGoal(this, Infected.class, 1.3, (Integer)SConfig.SERVER.buff_potion_meter.get(), (Integer)SConfig.SERVER.buff_potion_meter.get(), 3.0F, (livingEntity) -> !(livingEntity instanceof InfectedWitch)) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.use_potions.get() && this.mob.getLastHurtByMob() == null;
         }
      });
      this.goalSelector.addGoal(6, new RangedAttackGoal(this, (double)1.0F, (Integer)SConfig.SERVER.at_potion_meter.get(), 10.0F) {
         public boolean canUse() {
            return super.canUse() && (Boolean)SConfig.SERVER.use_potions.get();
         }
      });
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_witch_loot.get();
   }

   public void tick() {
      super.tick();
      if (this.isAlive() && this.potion != null && this.getMainHandItem() != PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.potion)) {
         this.setItemSlot(EquipmentSlot.OFFHAND, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.potion));
      }

      this.tickEvolution(this, (List)SConfig.SERVER.wit_ev.get(), ScamperVariants.VILLAGER);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.inf_witch_melee_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.inf_witch_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.inf_witch_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, (double)16.0F);
   }

   public DamageSource getCustomDamage(LivingEntity entity) {
      return Math.random() < 0.3 ? SdamageTypes.infected_witch_damage(this) : super.getCustomDamage(entity);
   }

   public void performRangedAttack(LivingEntity entity, float f) {
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      if (d3 >= (double)4.0F && this.getAttackPotion() != null) {
         this.potion = this.getAttackPotion();
      } else if (entity.getMobType().equals(MobType.UNDEAD)) {
         this.potion = Potions.HEALING;
      } else {
         this.potion = Potions.HARMING;
      }

      ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
      thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.potion));
      thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
      thrownpotion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
      if (!this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
      }

      this.level().addFreshEntity(thrownpotion);
   }

   public void performRangedBuff(LivingEntity entity, float f) {
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      if (entity.getHealth() < entity.getMaxHealth() && !entity.isOnFire()) {
         this.potion = Potions.REGENERATION;
      } else if (entity.isOnFire() && !entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         this.potion = Potions.FIRE_RESISTANCE;
      } else if (d3 <= (double)4.0F && this.getBuffPotion() != null) {
         this.potion = this.getBuffPotion();
      } else {
         this.potion = Potions.HEALING;
      }

      ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
      thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.potion));
      thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
      thrownpotion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
      if (!this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
      }

      this.level().addFreshEntity(thrownpotion);
   }

   private Potion getBuffPotion() {
      Random rand = new Random();
      List<? extends String> ev = (List)SConfig.SERVER.buffing_potions.get();
      int randomIndex = rand.nextInt(ev.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
      return (Potion)ForgeRegistries.POTIONS.getValue(randomElement1);
   }

   private Potion getAttackPotion() {
      Random rand = new Random();
      List<? extends String> ev = (List)SConfig.SERVER.harming_potions.get();
      int randomIndex = rand.nextInt(ev.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
      return (Potion)ForgeRegistries.POTIONS.getValue(randomElement1);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WITCH_AMBIENT.get();
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
      return "minecraft:witch";
   }
}
