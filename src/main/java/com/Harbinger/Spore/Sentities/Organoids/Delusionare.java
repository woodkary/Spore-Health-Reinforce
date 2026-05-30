package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.ArmedInfected;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.Utility.Illusion;
import com.Harbinger.Spore.Sentities.Variants.DelusionerVariants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Delusionare extends Organoid implements VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor SPELL_TIME;
   private static final EntityDataAccessor SPELL_ID;
   private static final List protectionEnchants;
   private static final List speed;

   public Delusionare(EntityType type, Level level) {
      super(type, level);
   }

   public int getEmerge_tick() {
      return 100;
   }

   public int getBorrow_tick() {
      return 100;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("spell_timer", (Integer)this.entityData.get(SPELL_TIME));
      tag.putInt("spell_id", (Integer)this.entityData.get(SPELL_ID));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(SPELL_TIME, tag.getInt("spell_timer"));
      this.entityData.set(SPELL_ID, tag.getInt("spell_id"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(SPELL_TIME, 0);
      this.entityData.define(SPELL_ID, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public int getSpellById() {
      return (Integer)this.entityData.get(SPELL_ID);
   }

   public boolean isCasting() {
      return (Integer)this.entityData.get(SPELL_TIME) > 0;
   }

   public void setSpellTime(int value) {
      this.entityData.set(SPELL_TIME, value);
   }

   public void setSpellId(int value) {
      this.entityData.set(SPELL_ID, value);
   }

   protected void tickSpell() {
      this.entityData.set(SPELL_TIME, (Integer)this.entityData.get(SPELL_TIME) + 1);
      if ((Integer)this.entityData.get(SPELL_TIME) == 40) {
         this.playSound((SoundEvent)Ssounds.DELUSIONER_CASTING.get());
      }

      if ((Integer)this.entityData.get(SPELL_TIME) > 80) {
         this.castSpell(this.getSpellById());
         this.entityData.set(SPELL_TIME, 0);
      }

   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.delusioner_loot.get();
   }

   public void tick() {
      super.tick();
      if ((Integer)this.entityData.get(SPELL_TIME) > 0) {
         this.tickSpell();
      }

      if (this.tickCount % 1200 == 0 && this.getTarget() == null) {
         this.tickBurrowing();
      }

   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         this.discard();
         burrowing = -1;
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.delusioner_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.delusioner_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.addTargettingGoals();
      this.goalSelector.addGoal(4, new CastMagicGoal(this));
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.DELUSIONER_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public void castSpell(int value) {
      LivingEntity entity = this.getTarget();
      if (entity != null) {
         if (this.getVariant() == DelusionerVariants.DEFAULT) {
            if (value == Spells.SUMMON_ILLUSION.getId()) {
               for(int i = 0; i < this.random.nextInt(2, 5); ++i) {
                  Illusion illusion = new Illusion((EntityType)Sentities.ILLUSION.get(), this.level());
                  illusion.setSeeAble(true);
                  illusion.moveTo(this.getX(), this.getY(), this.getZ());
                  Level var6 = this.level();
                  if (var6 instanceof ServerLevel) {
                     ServerLevel serverLevel = (ServerLevel)var6;
                     illusion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
                  }

                  illusion.setTarget(entity);
                  this.level().addFreshEntity(illusion);
               }
            }

            if (value == Spells.CAST_ARROWS.getId() && this.hasLineOfSight(entity)) {
               for(int i = 0; i < this.random.nextInt(3, 7); ++i) {
                  int randomX = this.random.nextInt(-4, 4);
                  int randomZ = this.random.nextInt(-4, 4);
                  Arrow arrow = new Arrow(EntityType.ARROW, this.level());
                  arrow.moveTo(entity.getX() + (double)randomX, entity.getY() + (double)3.0F, entity.getZ() + (double)randomZ);
                  arrow.setOwner(this);
                  if (Math.random() < 0.3) {
                     arrow.setSecondsOnFire(4);
                  }

                  double d0 = entity.getX() - arrow.getX();
                  double d1 = entity.getY(0.3333333333333333) - arrow.getY();
                  double d2 = entity.getZ() - arrow.getZ();
                  double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                  arrow.addEffect(new MobEffectInstance((MobEffect)Seffects.MYCELIUM.get(), 600));
                  arrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
                  this.level().addFreshEntity(arrow);
               }
            }

            if (value == Spells.CAST_INVISIBILITY.getId()) {
               AABB aabb = this.getBoundingBox().inflate((double)32.0F);

               for(Entity entity1 : this.level().getEntities(this, aabb)) {
                  if (entity1 instanceof Infected) {
                     Infected infected = (Infected)entity1;
                     infected.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600));
                  }
               }
            }

            if (value == Spells.CAST_TELEPORTATION.getId()) {
               AABB aabb = this.getBoundingBox().inflate((double)32.0F);

               for(Entity entity1 : this.level().getEntities(this, aabb)) {
                  double randomX = entity.getX() + (double)this.random.nextInt(-4, 4);
                  double randomZ = entity.getZ() + (double)this.random.nextInt(-4, 4);
                  if (entity1 instanceof Infected) {
                     Infected infected = (Infected)entity1;
                     infected.randomTeleport(randomX, entity.getY(), randomZ, true);
                     infected.playSound(SoundEvents.ENDERMAN_TELEPORT);
                  }
               }
            }
         } else {
            if (value == Spells.CAST_FIREBALL.getId()) {
               int amount = this.random.nextInt(1, 4);

               for(int i = 0; i < amount; ++i) {
                  Vec3 look = this.getLookAngle();
                  Vec3 spawnPos = this.position().add(look.scale((double)0.5F)).add((double)0.0F, 1.2, (double)0.0F);
                  SmallFireball fireball = new SmallFireball(this.level(), this, (double)0.0F, (double)0.0F, (double)0.0F);
                  fireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                  double dx = entity.getX() - spawnPos.x;
                  double dy = entity.getY(0.33) - spawnPos.y;
                  double dz = entity.getZ() - spawnPos.z;
                  float accuracy = (float)(14 - this.level().getDifficulty().getId() * 4);
                  dx += this.random.nextGaussian() * 0.15;
                  dy += this.random.nextGaussian() * 0.15;
                  dz += this.random.nextGaussian() * 0.15;
                  fireball.shoot(dx, dy, dz, 1.6F, accuracy);
                  this.level().addFreshEntity(fireball);
               }
            }

            if (value == Spells.CAST_LIGHTING.getId()) {
               if (!this.hasLineOfSight(entity)) {
                  return;
               }

               LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
               AABB aabb = entity.getBoundingBox().inflate((double)8.0F);
               List<Entity> entities = this.level().getEntities(entity, aabb, (e) -> {
                  boolean var10000;
                  if (e instanceof PowerableMob powerableMob) {
                     if (!powerableMob.isPowered()) {
                        var10000 = true;
                        return var10000;
                     }
                  }

                  var10000 = false;
                  return var10000;
               });
               if (entities.isEmpty()) {
                  double randomX = (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
                  double randomZ = (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
                  Vec3 vec3 = entity.position().add(randomX, (double)0.0F, randomZ);
                  bolt.moveTo(vec3);
               } else {
                  Entity powerMob = (Entity)entities.get(this.random.nextInt(entities.size()));
                  bolt.moveTo(powerMob.position());
               }

               this.level().addFreshEntity(bolt);
            }

            if (value == Spells.CAST_PROTECTION.getId()) {
               AABB aabb = this.getBoundingBox().inflate((double)8.0F);

               for(Entity mob : this.level().getEntities(this, aabb)) {
                  if (mob instanceof LivingEntity) {
                     LivingEntity living = (LivingEntity)mob;
                     if (living instanceof Infected) {
                        if (living instanceof ArmedInfected) {
                           for(EquipmentSlot slot : EquipmentSlot.values()) {
                              Enchantment enchantment = (Enchantment)protectionEnchants.get(this.random.nextInt(protectionEnchants.size()));
                              ItemStack stack = living.getItemBySlot(slot);
                              if (stack.getItem() instanceof ArmorItem) {
                                 Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                                 enchants.put(enchantment, enchantment.getMaxLevel());
                                 EnchantmentHelper.setEnchantments(enchants, stack);
                              }
                           }
                        } else {
                           living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, this.random.nextInt(0, 2), false, false, false));
                        }
                     }
                  }
               }
            }

            if (value == Spells.CAST_ENDURANCE.getId()) {
               AABB aabb = this.getBoundingBox().inflate((double)8.0F);

               for(Entity mob : this.level().getEntities(this, aabb)) {
                  if (mob instanceof LivingEntity) {
                     LivingEntity living = (LivingEntity)mob;
                     if (living instanceof Infected) {
                        if (living instanceof ArmedInfected) {
                           Enchantment enchantment = (Enchantment)speed.get(this.random.nextInt(speed.size()));
                           ItemStack stack = living.getItemBySlot(EquipmentSlot.FEET);
                           if (stack.getItem() instanceof ArmorItem) {
                              Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                              enchants.put(enchantment, enchantment.getMaxLevel());
                              EnchantmentHelper.setEnchantments(enchants, stack);
                           }
                        } else {
                           living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, this.random.nextInt(0, 3), false, false, false));
                        }
                     }
                  }
               }
            }
         }

         this.awardHivemind();
      }
   }

   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide && this.isCasting()) {
         for(int i = 0; i < 2; ++i) {
            this.level().addParticle(ParticleTypes.ENCHANT, this.getRandomX((double)0.5F), this.getRandomY() - (double)0.25F, this.getRandomZ((double)0.5F), (this.random.nextDouble() - (double)0.5F) * (double)2.0F, -this.random.nextDouble(), (this.random.nextDouble() - (double)0.5F) * (double)2.0F);
         }
      }

   }

   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      return this.isEmerging() ? false : super.hurt(p_21016_, p_21017_);
   }

   public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, MobSpawnType p_33284_, @Nullable SpawnGroupData p_33285_, @Nullable CompoundTag p_33286_) {
      DelusionerVariants variant = (DelusionerVariants)Util.getRandom(DelusionerVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_33282_, p_33283_, p_33284_, p_33285_, p_33286_);
   }

   public DelusionerVariants getVariant() {
      return DelusionerVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= DelusionerVariants.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return DelusionerVariants.values().length;
   }

   private void setVariant(DelusionerVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Delusionare.class, EntityDataSerializers.INT);
      SPELL_TIME = SynchedEntityData.defineId(Delusionare.class, EntityDataSerializers.INT);
      SPELL_ID = SynchedEntityData.defineId(Delusionare.class, EntityDataSerializers.INT);
      protectionEnchants = List.of(Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION);
      speed = List.of(Enchantments.FROST_WALKER, Enchantments.FALL_PROTECTION, Enchantments.DEPTH_STRIDER);
   }

   public static class CastMagicGoal extends Goal {
      Delusionare delusionare;

      public CastMagicGoal(Delusionare delusionare) {
         this.delusionare = delusionare;
      }

      public boolean canUse() {
         if (this.delusionare.isCasting()) {
            return false;
         } else {
            return this.delusionare.getTarget() != null;
         }
      }

      private boolean hasAlliesAround() {
         AABB aabb = this.delusionare.getBoundingBox().inflate((double)8.0F);
         List<Entity> possible_allies = this.delusionare.level().getEntities(this.delusionare, aabb);
         List<Infected> allies = new ArrayList();

         for(Entity entity : possible_allies) {
            if (entity instanceof Infected infected) {
               allies.add(infected);
            }
         }

         return allies.size() > 2;
      }

      private boolean isFar() {
         LivingEntity entity = this.delusionare.getTarget();
         if (entity == null) {
            return false;
         } else {
            return this.delusionare.distanceToSqr(entity) > (double)140.0F;
         }
      }

      public void start() {
         super.start();
         if (this.hasAlliesAround() && this.isFar()) {
            this.delusionare.setSpellId(3);
         }

         if (this.hasAlliesAround() && !this.isFar()) {
            this.delusionare.setSpellId(2);
         }

         if (!this.hasAlliesAround() && this.isFar()) {
            this.delusionare.setSpellId(1);
         }

         if (!this.hasAlliesAround() && !this.isFar()) {
            this.delusionare.setSpellId(0);
         }

         this.delusionare.setSpellTime(1);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }
   }

   public static enum Spells {
      SUMMON_ILLUSION(0),
      CAST_ARROWS(1),
      CAST_INVISIBILITY(2),
      CAST_TELEPORTATION(3),
      CAST_FIREBALL(0),
      CAST_LIGHTING(1),
      CAST_PROTECTION(2),
      CAST_ENDURANCE(3);

      private final int id;

      private Spells(int id) {
         this.id = id;
      }

      public int getId() {
         return this.id;
      }

      // $FF: synthetic method
      private static Spells[] $values() {
         return new Spells[]{SUMMON_ILLUSION, CAST_ARROWS, CAST_INVISIBILITY, CAST_TELEPORTATION, CAST_FIREBALL, CAST_LIGHTING, CAST_PROTECTION, CAST_ENDURANCE};
      }
   }
}
