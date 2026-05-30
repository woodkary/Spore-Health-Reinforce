package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Spotion;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
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
import org.jetbrains.annotations.Nullable;

public class Mephetic extends EvolvedInfected implements RangedAttackMob {
   private final List potions = new ArrayList();
   private int attackAnimationTick;
   private int throwAnimationTick;
   private int mouthAnimationTick;
   private int ticksBeforeThrown;

   public Mephetic(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.0F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(2, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_FIRE_RESISTANCE), SoundEvents.WITCH_DRINK, (p_35882_) -> this.isOnFire() && !this.hasEffect(MobEffects.FIRE_RESISTANCE) && (Boolean)SConfig.SERVER.use_potions.get()));
      this.goalSelector.addGoal(3, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER_BREATHING), SoundEvents.WITCH_DRINK, (p_35882_) -> this.isInWater() && !this.hasEffect(MobEffects.WATER_BREATHING) && (Boolean)SConfig.SERVER.use_potions.get()));
      this.goalSelector.addGoal(4, new DrinkPotionGoal(this, SoundEvents.WITCH_DRINK));
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
      return effectInstance.getEffect().isBeneficial() ? super.addEffect(effectInstance, entity) : false;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_mep_loot.get();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
   }

   public void handleEntityEvent(byte value) {
      if (value == 4) {
         this.attackAnimationTick = 10;
      } else if (value == 5) {
         this.mouthAnimationTick = 10;
      } else if (value == 6) {
         this.throwAnimationTick = 10;
      } else {
         super.handleEntityEvent(value);
      }

   }

   public boolean doHurtTarget(Entity entity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      if (entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 200, 1));
      }

      return super.doHurtTarget(entity);
   }

   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.throwAnimationTick > 0) {
         --this.throwAnimationTick;
      }

      if (this.mouthAnimationTick > 0) {
         --this.mouthAnimationTick;
      }

      if (this.ticksBeforeThrown <= 80) {
         ++this.ticksBeforeThrown;
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public int getThrowAnimationTick() {
      return this.throwAnimationTick;
   }

   public int getMouthAnimationTick() {
      return this.mouthAnimationTick;
   }

   public void tick() {
      super.tick();
      if (this.ticksBeforeThrown == 40) {
         this.loadPotions();
      }

      LivingEntity living = this.getTarget();
      if (this.ticksBeforeThrown >= 79 && living != null && this.hasLineOfSight(living) && living.distanceToSqr(this) > (double)20.0F) {
         this.throwPotions(living);
      }

      if (this.tickCount % 200 == 0 && living != null && this.hasLineOfSight(living) && living.distanceToSqr(this) > (double)60.0F) {
         this.throwLingeringPotions(living);
      }

   }

   private Potion getAttackPotion() {
      Random rand = new Random();
      List<? extends String> ev = (List)SConfig.SERVER.mep_potions.get();
      int randomIndex = rand.nextInt(ev.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
      Potion potion = (Potion)ForgeRegistries.POTIONS.getValue(randomElement1);
      LivingEntity living = this.getTarget();
      if (living != null && living.getMobType().equals(MobType.UNDEAD)) {
         if (potion == null) {
            return Potions.HEALING;
         }

         if (potion.equals(Potions.HARMING)) {
            return Potions.HEALING;
         }

         if (potion.equals(Potions.POISON)) {
            return (Potion)Spotion.MYCELIUM_POTION.get();
         }
      }

      return potion == null ? Potions.HARMING : potion;
   }

   public void loadPotions() {
      this.potions.clear();

      for(int i = 0; i < 3; ++i) {
         this.potions.add(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), this.getAttackPotion()));
      }

   }

   public void throwPotions(LivingEntity living) {
      this.throwAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)6);
      this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);

      for(int i = 0; i < this.potions.size(); ++i) {
         this.performRangedAttack(living, (float)i);
      }

      this.potions.clear();
      this.ticksBeforeThrown = 0;
   }

   public void throwLingeringPotions(LivingEntity living) {
      this.mouthAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)5);
      this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
      int u = this.random.nextInt(1, 6);

      for(int i = 0; i < u; ++i) {
         this.lingeringPotionThrow(living, this.getAttackPotion());
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.mep_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, (double)0.25F).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.mep_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.mep_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F);
   }

   public List getPotions() {
      return this.potions;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WITCH_AMBIENT.get();
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

   public void performRangedAttack(LivingEntity entity, float v) {
      Potion potion = PotionUtils.getPotion((ItemStack)this.potions.get((int)v));
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
      thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
      thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
      thrownpotion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
      this.level().addFreshEntity(thrownpotion);
   }

   public void lingeringPotionThrow(LivingEntity entity, Potion potion) {
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
      thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
      thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
      thrownpotion.shoot(d0, d1 + d3 * 0.4, d2, 1.0F, 8.0F);
      this.level().addFreshEntity(thrownpotion);
   }

   public static class DrinkPotionGoal extends Goal {
      private final Mob mob;
      private final ItemStack item;
      private final ItemStack strength;
      private final ItemStack speed;
      @javax.annotation.Nullable
      private final SoundEvent finishUsingSound;

      public DrinkPotionGoal(Mob p_25972_, @javax.annotation.Nullable SoundEvent p_25974_) {
         this.item = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING);
         this.strength = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH);
         this.speed = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS);
         this.mob = p_25972_;
         this.finishUsingSound = p_25974_;
      }

      public boolean canUse() {
         return this.mob.getHealth() < this.mob.getMaxHealth();
      }

      public boolean canContinueToUse() {
         return this.mob.isUsingItem();
      }

      public void start() {
         this.mob.setItemSlot(EquipmentSlot.MAINHAND, this.item.copy());
         this.mob.startUsingItem(InteractionHand.MAIN_HAND);
         if (Math.random() < 0.2) {
            this.mob.setItemSlot(EquipmentSlot.OFFHAND, Math.random() < (double)0.5F ? this.strength.copy() : this.speed.copy());
         }

      }

      public void stop() {
         if (!this.mob.getOffhandItem().equals(ItemStack.EMPTY)) {
            if (PotionUtils.getPotion(this.mob.getOffhandItem()).equals(Potions.STRENGTH)) {
               this.mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600));
            }

            if (PotionUtils.getPotion(this.mob.getOffhandItem()).equals(Potions.SWIFTNESS)) {
               this.mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600));
            }
         }

         this.mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         this.mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
         if (this.finishUsingSound != null) {
            this.mob.playSound(this.finishUsingSound, 1.0F, this.mob.getRandom().nextFloat() * 0.2F + 0.9F);
         }

      }
   }
}
