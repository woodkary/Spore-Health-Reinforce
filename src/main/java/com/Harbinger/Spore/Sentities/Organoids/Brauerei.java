package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Spotion;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.ScatterShotRangedGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import com.Harbinger.Spore.Sentities.Variants.BraureiVariants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class Brauerei extends Organoid implements RangedAttackMob, VariantKeeper {
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private static final EntityDataAccessor TIMER;
   private static final EntityDataAccessor COLOR;
   private static final EntityDataAccessor DATA_PARTICLE;
   private @Nullable MobEffect effect;

   public Brauerei(EntityType type, Level level) {
      super(type, level);
   }

   public int getEmerge_tick() {
      return 200;
   }

   public int getBorrow_tick() {
      return 200;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putInt("color", (Integer)this.entityData.get(COLOR));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(COLOR, tag.getInt("color"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(TIMER, 0);
      this.entityData.define(COLOR, 0);
      this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public MobEffect getEffect() {
      return this.effect;
   }

   public void setEffect(MobEffect effect) {
      this.entityData.set(COLOR, effect.getColor());
      this.effect = effect;
   }

   public int getTimer() {
      return (Integer)this.entityData.get(TIMER);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.tickCount % 20 == 0) {
         if (this.getTarget() == null && (Integer)this.entityData.get(TIMER) < 300) {
            this.entityData.set(TIMER, (Integer)this.entityData.get(TIMER) + 1);
         } else if ((Integer)this.entityData.get(TIMER) >= 300) {
            this.tickBurrowing();
         }
      }

      if (this.tickCount % 300 == 0) {
         if (this.getVariant() == BraureiVariants.HAZARD) {
            this.setEffect((MobEffect)this.debuff_List().get(this.random.nextInt(this.debuff_List().size())));
         } else {
            this.setEffect((MobEffect)this.testList().get(this.random.nextInt(this.testList().size())));
         }

         if (this.getEffect() != null) {
            if (this.getVariant() == BraureiVariants.HAZARD) {
               this.spreadDeBuffs(this, this.getEffect());
            } else {
               this.spreadBuffs(this, this.getEffect());
            }
         }
      }

      if ((Integer)this.entityData.get(COLOR) != 0) {
         ParticleOptions particleoptions = (ParticleOptions)this.entityData.get(DATA_PARTICLE);
         if (particleoptions.getType() == ParticleTypes.ENTITY_EFFECT) {
            int k = this.getColor();
            double d5 = (double)((float)(k >> 16 & 255) / 255.0F);
            double d6 = (double)((float)(k >> 8 & 255) / 255.0F);
            double d7 = (double)((float)(k & 255) / 255.0F);

            for(int i = 0; i < 4; ++i) {
               this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), d5, d6, d7);
            }
         }
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

   private List testList() {
      List<MobEffect> contents = new ArrayList();

      for(String str : (List<String>)SConfig.SERVER.braurei_buffs.get()) {
         MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(str));
         if (effect != null) {
            contents.add(effect);
         }
      }

      if (contents.isEmpty()) {
         contents.add(MobEffects.REGENERATION);
      }

      return contents;
   }

   private List debuff_List() {
      List<MobEffect> contents = new ArrayList();

      for(String str : (List<String>)SConfig.SERVER.braurei_debuffs.get()) {
         MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(str));
         if (effect != null) {
            contents.add(effect);
         }
      }

      if (contents.isEmpty()) {
         contents.add((MobEffect)Seffects.MYCELIUM.get());
      }

      return contents;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.braurei_loot.get();
   }

   protected void spreadBuffs(LivingEntity entity, MobEffect effect) {
      this.awardHivemind();
      AABB aabb = entity.getBoundingBox().inflate((double)32.0F);

      for(Entity testEntity : entity.level().getEntities(entity, aabb, (livingx) -> livingx instanceof Infected || livingx instanceof UtilityEntity)) {
         if (testEntity instanceof LivingEntity living) {
            int level = entity.level().getDifficulty() == Difficulty.HARD ? 1 : 0;
            living.addEffect(new MobEffectInstance(effect, 600, level));
         }
      }

   }

   protected void spreadDeBuffs(LivingEntity entity, MobEffect effect) {
      this.awardHivemind();
      AABB aabb = entity.getBoundingBox().inflate((double)32.0F);

      for(Entity testEntity : entity.level().getEntities(entity, aabb, (livingx) -> {
         boolean var10000;
         if (livingx instanceof LivingEntity livingEntity) {
            if (this.TARGET_SELECTOR.test(livingEntity) && !Utilities.helmetList().contains(livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      })) {
         if (testEntity instanceof LivingEntity living) {
            int level = entity.level().getDifficulty() == Difficulty.HARD ? 1 : 0;
            living.addEffect(new MobEffectInstance(effect, 600, level));
         }
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.braurei_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.braurei_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)20.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public int getColor() {
      return (Integer)this.entityData.get(COLOR);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(3, new ScatterShotRangedGoal(this, (double)0.0F, 80, 20.0F, 1, 3));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   private List getPotion() {
      List<Potion> values = new ArrayList();
      values.add((Potion)Spotion.MARKER_POTION.get());
      values.add((Potion)Spotion.MYCELIUM_POTION.get());
      values.add((Potion)Spotion.CORROSION_POTION_STRONG.get());
      values.add(Potions.WEAKNESS);
      values.add(Potions.STRONG_POISON);
      return values;
   }

   public boolean hurt(DamageSource source, float value) {
      return this.isEmerging() ? false : super.hurt(source, value);
   }

   public void performRangedAttack(LivingEntity entity, float p_33318_) {
      Vec3 vec3 = entity.getDeltaMovement();
      double d0 = entity.getX() + vec3.x - this.getX();
      double d1 = entity.getEyeY() - (double)1.1F - this.getY();
      double d2 = entity.getZ() + vec3.z - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
      thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), (Potion)this.getPotion().get(this.random.nextInt(this.getPotion().size()))));
      thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
      thrownpotion.shoot(d0, d1 + d3 * 0.2, d2, 0.75F, 8.0F);
      if (!this.isSilent()) {
         this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
      }

      this.level().addFreshEntity(thrownpotion);
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.BRAUREI_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   public BraureiVariants getVariant() {
      return BraureiVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, i <= BraureiVariants.values().length && i >= 0 ? i : 0);
   }

   public int amountOfMutations() {
      return BraureiVariants.values().length;
   }

   private void setVariant(BraureiVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @javax.annotation.Nullable SpawnGroupData p_146749_, @javax.annotation.Nullable CompoundTag p_146750_) {
      BraureiVariants variant = (BraureiVariants)Util.getRandom(BraureiVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   static {
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.INT);
      TIMER = SynchedEntityData.defineId(Brauerei.class, EntityDataSerializers.INT);
      COLOR = SynchedEntityData.defineId(Brauerei.class, EntityDataSerializers.INT);
      DATA_PARTICLE = SynchedEntityData.defineId(Brauerei.class, EntityDataSerializers.PARTICLE);
   }
}
