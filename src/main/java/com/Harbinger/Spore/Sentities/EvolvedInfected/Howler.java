package com.Harbinger.Spore.Sentities.EvolvedInfected;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.ArmorPersentageBypass;
import com.Harbinger.Spore.Sentities.Carrier;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BasicInfected.Bairn;
import com.Harbinger.Spore.Sentities.Utility.Illusion;
import com.Harbinger.Spore.Sentities.Variants.HowlerVariants;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class Howler extends EvolvedInfected implements VariantKeeper, ArmorPersentageBypass {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final List sculkSummon;

   public Howler(EntityType type, Level level) {
      super(type, level);
   }

   protected void addRegularGoals() {
      super.addRegularGoals();
      this.goalSelector.addGoal(2, new HowlerAttackGoal(this, (double)1.5F));
      this.goalSelector.addGoal(3, new BansheeMeleeGoal(this, this.random));
      this.goalSelector.addGoal(3, new CustomMeleeAttackGoal(this, 1.2, true) {
         public boolean canUse() {
            return super.canUse() && Howler.this.getVariant() == HowlerVariants.DEFAULT;
         }
      });
      this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.how_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.how_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.how_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)24.0F).add(Attributes.ATTACK_KNOCKBACK, (double)3.0F);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.INF_PILLAGER_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public HowlerVariants getVariant() {
      return HowlerVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= HowlerVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return HowlerVariants.values().length;
   }

   private void setVariant(HowlerVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public void tick() {
      super.tick();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.inf_howler_loot.get();
   }

   public void ScreamAOE(Entity origin) {
      AABB area = origin.getBoundingBox().inflate((double)12.0F);

      for(Entity target : origin.level().getEntities(origin, area, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (target instanceof LivingEntity player) {
            if (Utilities.TARGET_SELECTOR.Test(player)) {
               if (this.getVariant() == HowlerVariants.FORLORN) {
                  player.addEffect(new MobEffectInstance((MobEffect)Seffects.UNEASY.get(), 3600, 0));
                  player.addEffect(new MobEffectInstance((MobEffect)Seffects.MADNESS.get(), 3600, 1));
               } else if (this.getVariant() == HowlerVariants.SWARMER) {
                  player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
               } else {
                  player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                  player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
               }
            }
         }
      }

   }

   public void ScreamBuffInfected(Entity origin) {
      AABB area = origin.getBoundingBox().inflate((double)6.0F);
      List<Entity> allies = origin.level().getEntities(origin, area);
      short var10000;
      switch (origin.level().getDifficulty()) {
         case EASY -> var10000 = 100;
         case NORMAL -> var10000 = 200;
         case HARD -> var10000 = 400;
         default -> var10000 = 0;
      }

      int duration = var10000;
      int amplifier = origin.level().getDifficulty() == Difficulty.HARD ? 1 : 0;
      List<? extends String> buffs = (List)SConfig.SERVER.howler_effects_buff.get();
      String randomBuff = (String)buffs.get(this.random.nextInt(buffs.size()));
      MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(randomBuff));
      if (effect != null) {
         for(Entity ally : allies) {
            if (ally instanceof Infected) {
               Infected infected = (Infected)ally;
               infected.addEffect(new MobEffectInstance(effect, duration, amplifier));
            }
         }
      }

      this.playSound((SoundEvent)Ssounds.HOWLER_GROWL.get());
   }

   public void SummonScream(LivingEntity caster, boolean isSkulk, boolean sculkAround) {
      ServerLevelAccessor levelAccessor = (ServerLevelAccessor)caster.level();
      Level level = caster.level();
      int dx = this.random.nextInt(-8, 9);
      int dz = this.random.nextInt(-8, 9);
      int dy = this.random.nextInt(0, 2);
      List<? extends String> summonPool = isSkulk && sculkAround ? sculkSummon : (List)SConfig.SERVER.howler_summon.get();
      String chosen = (String)summonPool.get(this.random.nextInt(summonPool.size()));
      ResourceLocation entityId = new ResourceLocation(chosen);
      EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(entityId);
      if (entityType != null) {
         Mob summoned = (Mob)entityType.create(level);
         if (summoned != null) {
            summoned.teleportRelative(caster.getX() + (double)dx, caster.getY() + (double)0.5F + (double)dy, caster.getZ() + (double)dz);
            summoned.finalizeSpawn(levelAccessor, level.getCurrentDifficultyAt(BlockPos.containing(caster.position())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
            level.addFreshEntity(summoned);
            this.playSound((SoundEvent)Ssounds.HOWLER_GROWL.get());
         }
      }

   }

   public void SummonSpecialScream(LivingEntity caster, LivingEntity target) {
      ServerLevelAccessor levelAccessor = (ServerLevelAccessor)caster.level();
      Level level = caster.level();
      int dx = this.random.nextInt(-8, 9);
      int dz = this.random.nextInt(-8, 9);
      int dy = this.random.nextInt(0, 2);
      if (this.getVariant() == HowlerVariants.FORLORN) {
         Illusion entityType = new Illusion((EntityType)Sentities.ILLUSION.get(), level);
         entityType.teleportRelative(caster.getX() + (double)dx, caster.getY() + (double)0.5F + (double)dy, caster.getZ() + (double)dz);
         entityType.setTargetId(target == null ? 0 : target.getId());
         entityType.setSeeAble(false);
         entityType.finalizeSpawn(levelAccessor, level.getCurrentDifficultyAt(BlockPos.containing(caster.position())), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
         level.addFreshEntity(entityType);
         this.playSound((SoundEvent)Ssounds.HOWLER_GROWL.get());
      }

      if (this.getVariant() == HowlerVariants.SWARMER) {
         Bairn entityType = new Bairn((EntityType)Sentities.BAIRN.get(), level);
         entityType.teleportRelative(caster.getX() + (double)dx, caster.getY() + (double)0.5F + (double)dy, caster.getZ() + (double)dz);
         entityType.finalizeSpawn(levelAccessor, level.getCurrentDifficultyAt(BlockPos.containing(caster.position())), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
         level.addFreshEntity(entityType);
         this.playSound((SoundEvent)Ssounds.HOWLER_GROWL.get());
      }

   }

   public void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public boolean dampensVibrations() {
      return this.getVariant() == HowlerVariants.SONIC;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public boolean checkForInfected(Entity origin) {
      AABB area = origin.getBoundingBox().inflate((double)4.0F);

      for(Entity entity : origin.level().getEntities(origin, area, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
         if (entity instanceof Infected && !((List)SConfig.SERVER.support.get()).contains(entity.getEncodeId()) && !(entity instanceof Carrier)) {
            return true;
         }

         if (entity instanceof Illusion && this.getVariant() == HowlerVariants.FORLORN) {
            return true;
         }
      }

      return false;
   }

   public float amountOfDamage(float value) {
      return this.getVariant() == HowlerVariants.BANSHEE ? (float)((Double)SConfig.SERVER.how_damage.get() * (Double)SConfig.SERVER.global_damage.get() / (double)2.0F) : 0.0F;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      HowlerVariants variant = (HowlerVariants)Util.getRandom(HowlerVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Spitter.class, EntityDataSerializers.INT);
      sculkSummon = List.of("sculkhorde:sculk_mite", "sculkhorde:sculk_mite_aggressor");
   }

   private class HowlerAttackGoal extends Goal {
      private final Howler mob;
      private final double speed;
      private int screamTimer = 0;

      private HowlerAttackGoal(Howler mob, double speedModifier) {
         this.mob = mob;
         this.speed = speedModifier;
      }

      public boolean canUse() {
         return this.mob.getTarget() != null && this.mob.getVariant() != HowlerVariants.BANSHEE;
      }

      public void tick() {
         if (this.screamTimer > 0) {
            --this.screamTimer;
         }

         LivingEntity target = this.mob.getTarget();
         if (target != null) {
            boolean isSkulk = this.mob.getVariant() == HowlerVariants.SONIC && (Boolean)SConfig.SERVER.skulk_target.get();
            this.mob.getLookControl().setLookAt(target, 10.0F, (float)this.mob.getMaxHeadXRot());
            double dist = this.mob.distanceToSqr(target);
            if (dist > (double)120.0F) {
               this.mob.getNavigation().moveTo(target, this.speed);
            } else if (this.screamTimer <= 0) {
               Howler.this.ScreamAOE(this.mob);
               if (Howler.this.checkForInfected(this.mob)) {
                  Howler.this.ScreamBuffInfected(this.mob);
               } else {
                  boolean skulk = ModList.get().isLoaded("sculkhorde");
                  int summons;
                  if (skulk) {
                     summons = Howler.this.random.nextInt(3, 9);
                  } else if (Howler.this.getVariant() == HowlerVariants.SWARMER) {
                     summons = Howler.this.random.nextInt(4, 7);
                  } else if (Howler.this.getVariant() == HowlerVariants.FORLORN) {
                     summons = Howler.this.random.nextInt(3, 5);
                  } else {
                     summons = Howler.this.random.nextInt(1, 3);
                  }

                  for(int i = 0; i < summons; ++i) {
                     if (Howler.this.getVariant() != HowlerVariants.SWARMER && Howler.this.getVariant() != HowlerVariants.FORLORN) {
                        Howler.this.SummonScream(this.mob, isSkulk, skulk);
                     } else {
                        Howler.this.SummonSpecialScream(this.mob, target);
                     }
                  }
               }

               if (this.mob.getVariant() == HowlerVariants.SONIC) {
                  this.shootSonicBoom(target, (float)((Double)SConfig.SERVER.how_damage.get() * (Double)SConfig.SERVER.global_damage.get()));
               }

               this.screamTimer = 120;
            }

         }
      }

      public void shootSonicBoom(LivingEntity target, float damage) {
         if (target != null && target.isAlive()) {
            this.mob.level().playSound((Player)null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0F, 1.0F);
            Level var4 = this.mob.level();
            if (var4 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var4;
               double dx = target.getX() - this.mob.getX();
               double dy = target.getY((double)0.5F) - this.mob.getEyeY();
               double dz = target.getZ() - this.mob.getZ();

               for(int i = 0; i < 10; ++i) {
                  double px = this.mob.getX() + dx * (double)i / (double)10.0F;
                  double py = this.mob.getEyeY() + dy * (double)i / (double)10.0F;
                  double pz = this.mob.getZ() + dz * (double)i / (double)10.0F;
                  serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
               }
            }

            target.hurt(this.mob.damageSources().sonicBoom(this.mob), damage);
            Vec3 push = (new Vec3(target.getX() - this.mob.getX(), target.getEyeY() - this.mob.getEyeY(), target.getZ() - this.mob.getZ())).normalize().scale((double)1.5F);
            target.push(push.x, push.y * (double)0.5F, push.z);
         }
      }
   }

   private static class BansheeMeleeGoal extends CustomMeleeAttackGoal {
      private int timeBeforeBigScream;
      private final RandomSource randomSource;

      public BansheeMeleeGoal(PathfinderMob mob, RandomSource randomSource) {
         super(mob, (double)1.75F, true);
         this.randomSource = randomSource;
      }

      public void tick() {
         super.tick();
         if (this.timeBeforeBigScream < 200) {
            ++this.timeBeforeBigScream;
         } else {
            this.callOrSummon();
            this.timeBeforeBigScream = 0;
            this.mob.playSound((SoundEvent)Ssounds.HOWLER_GROWL.get());
         }

      }

      public void callOrSummon() {
         List<Infected> brothers = this.getBrothers();
         if (!brothers.isEmpty() && brothers.size() >= 6) {
            for(Infected infected : brothers) {
               if (infected.isAlive() && infected.getTarget() == null) {
                  infected.setTarget(this.mob.getTarget());
               }
            }
         } else {
            Vec3 vec3 = Utilities.generatePositionAway(this.mob.position(), (double)16.0F);

            for(int i = 0; i < this.randomSource.nextInt(3, 10); ++i) {
               this.summonAtDistance(vec3);
            }
         }

      }

      public void summonAtDistance(Vec3 vec3) {
         List<? extends String> summonPool = (List)SConfig.SERVER.howler_summon.get();
         String chosen = (String)summonPool.get(this.randomSource.nextInt(summonPool.size()));
         ResourceLocation entityId = new ResourceLocation(chosen);
         EntityType<?> entityType = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(entityId);
         if (entityType != null) {
            Mob summoned = (Mob)entityType.create(this.mob.level());
            if (summoned != null) {
               Level var8 = this.mob.level();
               if (var8 instanceof ServerLevelAccessor) {
                  ServerLevelAccessor accessor = (ServerLevelAccessor)var8;
                  summoned.teleportTo(vec3.x, this.mob.getY() + (double)0.5F, vec3.z);
                  summoned.finalizeSpawn(accessor, accessor.getCurrentDifficultyAt(BlockPos.containing(this.mob.position())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
                  summoned.setTarget(this.mob.getTarget());
                  accessor.addFreshEntity(summoned);
               }
            }
         }

      }

      public List getBrothers() {
         return this.mob.level().getEntitiesOfClass(Infected.class, this.mob.getBoundingBox().inflate((double)32.0F, (double)8.0F, (double)32.0F));
      }

      public boolean canUse() {
         boolean var10000;
         if (super.canUse()) {
            PathfinderMob var2 = this.mob;
            if (var2 instanceof Howler) {
               Howler howler = (Howler)var2;
               if (howler.getVariant() == HowlerVariants.BANSHEE) {
                  var10000 = true;
                  return var10000;
               }
            }
         }

         var10000 = false;
         return var10000;
      }
   }
}
