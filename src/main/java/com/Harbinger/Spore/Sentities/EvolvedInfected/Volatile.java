package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.VolatileSwellGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class Volatile extends EvolvedInfected {
   private static final EntityDataAccessor DATA_SWELL_DIR;
   private int swell;

   public Volatile(EntityType type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new VolatileSwellGoal(this));
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)6.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_volatile_loot.get();
   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SWELL_DIR, -1);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.vola_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.vola_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.vola_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)32.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public boolean hurt(DamageSource source, float amount) {
      List<? extends String> ev = (List)SConfig.SERVER.vola_buffs.get();
      int randomIndex = this.random.nextInt(ev.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
      MobEffect randomElement = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(randomElement1);
      if (randomElement != null) {
         this.addEffect(new MobEffectInstance(randomElement, 600, 0));
      }

      return super.hurt(source, amount);
   }

   public boolean doHurtTarget(Entity entity) {
      List<? extends String> ev = (List)SConfig.SERVER.vola_debuffs.get();
      int randomIndex = this.random.nextInt(ev.size());
      ResourceLocation randomElement1 = new ResourceLocation((String)ev.get(randomIndex));
      MobEffect randomElement = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(randomElement1);
      if (randomElement != null && entity instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(randomElement, 600, 0));
      }

      return super.doHurtTarget(entity);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.WITCH_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public int getSwellDir() {
      return (Integer)this.entityData.get(DATA_SWELL_DIR);
   }

   public void tick() {
      super.tick();
      if (this.isAlive()) {
         int i = this.getSwellDir();
         if (i > 0 && this.swell == 0) {
            this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            this.gameEvent(GameEvent.PRIME_FUSE);
         }

         this.swell += i;
         if (this.swell < 0) {
            this.swell = 0;
         }

         if (this.swell >= 40) {
            this.swell = 40;
            this.explodeVolatile();
         }
      }

   }

   private void explodeVolatile() {
      if (!this.level().isClientSide) {
         ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(this.level(), this) ? ExplosionInteraction.MOB : ExplosionInteraction.NONE;
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)(Integer)SConfig.SERVER.volatile_explosion.get(), explosion$blockinteraction);
         this.discard();
         AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
         areaeffectcloud.setRadius(5.5F);
         areaeffectcloud.setRadiusOnUse(-0.5F);
         areaeffectcloud.setWaitTime(20);

         for(String string : (List<String>)SConfig.SERVER.vola_debuffs.get()) {
            MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
            if (effect != null) {
               areaeffectcloud.addEffect(new MobEffectInstance(effect, 600, 1));
            }
         }

         areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
         areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
         this.level().addFreshEntity(areaeffectcloud);
      }

   }

   public void setSwellDir(int i) {
      this.entityData.set(DATA_SWELL_DIR, i);
   }

   static {
      DATA_SWELL_DIR = SynchedEntityData.defineId(Volatile.class, EntityDataSerializers.INT);
   }
}
