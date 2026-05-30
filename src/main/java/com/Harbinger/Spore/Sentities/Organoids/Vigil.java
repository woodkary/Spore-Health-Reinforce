package com.Harbinger.Spore.Sentities.Organoids;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Sparticles;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Signal;
import com.Harbinger.Spore.Sentities.VariantKeeper;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import com.Harbinger.Spore.Sentities.Variants.VigilVariants;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class Vigil extends Organoid implements TraceableEntity, VariantKeeper {
   private static final EntityDataAccessor TRIGGER;
   private static final EntityDataAccessor WAVE_SIZE;
   private static final EntityDataAccessor TIMER;
   private static final EntityDataAccessor DATA_ID_TYPE_VARIANT;
   private int summon_counter;
   @Nullable
   private Mob proto;

   public Vigil(EntityType type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.vigil_loot.get();
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(WAVE_SIZE, 0);
      this.entityData.define(TIMER, 0);
      this.entityData.define(TRIGGER, 0);
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public int getEmerge_tick() {
      return this.isStalker() ? 90 : 180;
   }

   public int getBorrow_tick() {
      return this.isStalker() ? 100 : 200;
   }

   public void tickBurrowing() {
      int burrowing = (Integer)this.entityData.get(BORROW);
      if (burrowing > this.getBorrow_tick()) {
         burrowing = -1;
         if (this.isStalker() && this.getTarget() != null) {
            this.ReEmerge();
         } else {
            if (this.getVariant() == VigilVariants.TROLL) {
               Level var3 = this.level();
               if (var3 instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)var3;
                  if (this.getLastAttacker() instanceof Player || this.getTarget() instanceof Player) {
                     this.pickAndPlaceMessage(serverLevel, this.getOnPos().above());
                  }
               }
            }

            this.discard();
            this.TimeToLeave();
         }
      }

      this.entityData.set(BORROW, burrowing + 1);
   }

   public boolean isStalker() {
      return this.getVariant() == VigilVariants.STALKER;
   }

   public int getTrigger() {
      return (Integer)this.entityData.get(TRIGGER);
   }

   public void setTrigger(int i) {
      this.entityData.set(TRIGGER, i);
   }

   public int getWaveSize() {
      return (Integer)this.entityData.get(WAVE_SIZE);
   }

   public void setWaveSize(int i) {
      this.entityData.set(WAVE_SIZE, i);
   }

   public int getTimer() {
      return (Integer)this.entityData.get(TIMER);
   }

   @Nullable
   public void setProto(Mob entity) {
      this.proto = entity;
   }

   public boolean isNoAi() {
      return this.isBurrowing() || this.isEmerging();
   }

   public void tick() {
      if (this.getTarget() == null && (Integer)this.entityData.get(TIMER) < 6000) {
         if ((Integer)this.entityData.get(TIMER) % 300 == 0) {
            this.setTrigger(0);
            this.setWaveSize(0);
         }

         this.entityData.set(TIMER, (Integer)this.entityData.get(TIMER) + 1);
      } else if ((Integer)this.entityData.get(TIMER) >= 6000) {
         this.escape();
      }

      if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) < (double)150.0F || this.getTrigger() >= 4) {
         this.setTrigger(this.getTrigger() <= 0 ? 1 : this.getTrigger());
         this.escape();
      }

      super.tick();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("trigger", (Integer)this.entityData.get(TRIGGER));
      tag.putInt("timer", (Integer)this.entityData.get(TIMER));
      tag.putInt("wave_size", (Integer)this.entityData.get(WAVE_SIZE));
      tag.putInt("Variant", this.getTypeVariant());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(TRIGGER, tag.getInt("trigger"));
      this.entityData.set(TIMER, tag.getInt("timer"));
      this.entityData.set(WAVE_SIZE, tag.getInt("wave_size"));
      this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
   }

   public void ReEmerge() {
      this.entityData.set(TIMER, 0);
      this.randomTeleport(this.getX() + (double)this.random.nextInt(-30, 30), this.getY(), this.getZ() + (double)this.random.nextInt(-30, 30), false);
      this.entityData.set(EMERGE, -1);
      this.tickEmerging();
   }

   public boolean hurt(DamageSource p_21016_, float p_21017_) {
      if (this.isEmerging()) {
         return false;
      } else {
         this.escape();
         return super.hurt(p_21016_, p_21017_);
      }
   }

   public void escape() {
      if (this.onGround()) {
         this.tickBurrowing();
      }

   }

   boolean checkForScents() {
      AABB aabb = this.getBoundingBox().inflate((double)16.0F);
      List<ScentEntity> entities = this.level().getEntitiesOfClass(ScentEntity.class, aabb);
      return entities.size() < (Integer)SConfig.SERVER.scent_cap.get();
   }

   public void TimeToLeave() {
      int i = (Integer)this.entityData.get(TRIGGER);
      if (i == 1) {
         this.SummonScent(this, this.level(), false);
      } else if (i == 2) {
         this.SummonScent(this, this.level(), true);
      } else if (i >= 3) {
         AABB searchbox = this.getBoundingBox().inflate((double)(Integer)SConfig.SERVER.proto_range.get());

         for(Entity en : this.level().getEntities(this, searchbox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (en instanceof Proto) {
               Proto proto = (Proto)en;
               proto.setSignal(new Signal(true, new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ())));
               break;
            }
         }
      }

      this.punishHivemind();
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.VIGIL_AMBIENT.get();
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
   }

   private void SummonScent(Entity entity, Level level, boolean value) {
      if (this.checkForScents()) {
         ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), level);
         scent.moveTo(entity.getX(), entity.getY(), entity.getZ());
         scent.setOvercharged(value);
         level.addFreshEntity(scent);
      }

   }

   public @org.jetbrains.annotations.Nullable Entity getOwner() {
      return this.proto;
   }

   public VigilVariants getVariant() {
      return VigilVariants.byId(this.getTypeVariant() & 255);
   }

   public int getTypeVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public void setVariant(int i) {
      if (i <= VigilVariants.values().length && i >= 0) {
         this.entityData.set(DATA_ID_TYPE_VARIANT, i);
      } else {
         this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
      }

   }

   public int amountOfMutations() {
      return VigilVariants.values().length;
   }

   public void pickAndPlaceMessage(ServerLevel serverLevel, BlockPos pos) {
      if (!pos.equals(BlockPos.ZERO) && (serverLevel.getBlockState(pos).isAir() || serverLevel.getBlockState(pos).getBlock() instanceof BushBlock)) {
         String key = "spore.proto.message." + this.random.nextInt(10);
         Component translated = Component.translatable(key);
         String[] words = translated.getString().split(" ");
         List<String> lines = new ArrayList();
         StringBuilder currentLine = new StringBuilder();

         for(String word : words) {
            if (currentLine.length() + word.length() + 1 <= 15) {
               if (currentLine.length() > 0) {
                  currentLine.append(" ");
               }

               currentLine.append(word);
            } else {
               lines.add(currentLine.toString());
               currentLine = new StringBuilder(word);
               if (lines.size() == 4) {
                  break;
               }
            }
         }

         if (lines.size() < 4 && currentLine.length() > 0) {
            lines.add(currentLine.toString());
         }

         while(lines.size() < 4) {
            lines.add("");
         }

         this.placeSignWithText(serverLevel, pos, (String[])lines.toArray(new String[0]));
      }
   }

   public void placeSignWithText(ServerLevel world, BlockPos pos, String[] lines) {
      BlockState signState = Blocks.OAK_SIGN.defaultBlockState();
      world.setBlockAndUpdate(pos, signState);
      BlockEntity be = world.getBlockEntity(pos);
      if (be instanceof SignBlockEntity sign) {
         SignText newText = sign.getFrontText();
         int v = Math.min(lines.length, 4);

         for(int i = 0; i < v; ++i) {
            String centered = this.centerLine(lines[i], 15);
            Component line = Component.literal(centered);
            newText = newText.setMessage(i, line, line);
         }

         sign.setText(newText, true);
         sign.setChanged();
      }
   }

   private String centerLine(String text, int width) {
      if (text.length() >= width) {
         return text.substring(0, width);
      } else {
         int pad = (width - text.length()) / 2;
         String var10000 = " ".repeat(pad);
         return var10000 + text;
      }
   }

   private void setVariant(VigilVariants variant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
   }

   public void SummonInfected() {
      int l = this.getTrigger();
      List<? extends String> summons;
      if (l <= 1) {
         summons = (List)SConfig.SERVER.vigil_base_wave.get();
      } else if (l == 2) {
         summons = (List)SConfig.SERVER.vigil_middle_wave.get();
      } else {
         summons = (List)SConfig.SERVER.vigil_max_wave.get();
      }

      this.awardHivemind();
      LivingEntity target = this.getTarget();
      if (target != null && this.getTrigger() > 0) {
         Level var5 = this.level();
         if (var5 instanceof ServerLevelAccessor) {
            ServerLevelAccessor world = (ServerLevelAccessor)var5;
            RandomSource rand = RandomSource.create();
            int randomIndex = rand.nextInt(summons.size());
            ResourceLocation randomElement1 = new ResourceLocation((String)summons.get(randomIndex));
            EntityType<?> randomElement = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(randomElement1);
            Mob waveentity = (Mob)randomElement.create(this.level());

            assert waveentity != null;

            waveentity.setPos(this.getX(), this.getY(), this.getZ());
            waveentity.finalizeSpawn(world, this.level().getCurrentDifficultyAt(new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ())), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
            if (waveentity.getTarget() == null && target.isAlive() && !target.isInvulnerable()) {
               waveentity.setTarget(target);
            }

            this.level().addFreshEntity(waveentity);
         }
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.vigil_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.vigil_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F);
   }

   public void aiStep() {
      super.aiStep();
      if (this.getWaveSize() > 0 && this.summon_counter <= 20) {
         ++this.summon_counter;
      } else if (this.getWaveSize() > 0 && this.summon_counter >= 20) {
         this.summon_counter = 0;
         this.SummonInfected();
         this.setWaveSize(this.getWaveSize() - 1);
      }

      if (this.getWaveSize() > 0) {
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         RandomSource randomsource = this.getRandom();

         for(int l = 0; l < 3; ++l) {
            int i = randomsource.nextInt(-3, 3);
            int i1 = randomsource.nextInt(-3, 3);
            Level var12 = this.level();
            if (var12 instanceof ServerLevel) {
               ServerLevel serverLevel = (ServerLevel)var12;
               serverLevel.sendParticles((SimpleParticleType)Sparticles.SPORE_PARTICLE.get(), x + (double)i, y + this.random.nextDouble(), z + (double)i1, 1, (double)0.0F, 0.3, (double)0.0F, (double)0.15F);
            }
         }
      }

   }

   protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
      Entity var4 = this.getOwner();
      if (var4 instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
      }

      return super.mobInteract(player, interactionHand);
   }

   protected void registerGoals() {
      this.addTargettingGoals();
      this.goalSelector.addGoal(2, new WatchTargetGoat(this));
      this.goalSelector.addGoal(2, new WatcherMobSummon(this));
      this.goalSelector.addGoal(2, new WatcherMobCall(this));
      this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
      super.registerGoals();
   }

   public int getNumberOfParticles() {
      return 4;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.isStalker() ? super.getDimensions(pose).scale(1.2F) : super.getDimensions(pose);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_146746_, DifficultyInstance p_146747_, MobSpawnType p_146748_, @Nullable SpawnGroupData p_146749_, @Nullable CompoundTag p_146750_) {
      VigilVariants variant = (VigilVariants)Util.getRandom(VigilVariants.values(), this.random);
      this.setVariant(variant);
      return super.finalizeSpawn(p_146746_, p_146747_, p_146748_, p_146749_, p_146750_);
   }

   public String getMutation() {
      return this.getTypeVariant() != 0 ? this.getVariant().getName() : super.getMutation();
   }

   static {
      TRIGGER = SynchedEntityData.defineId(Vigil.class, EntityDataSerializers.INT);
      WAVE_SIZE = SynchedEntityData.defineId(Vigil.class, EntityDataSerializers.INT);
      TIMER = SynchedEntityData.defineId(Vigil.class, EntityDataSerializers.INT);
      DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Busser.class, EntityDataSerializers.INT);
   }

   private static class WatchTargetGoat extends Goal {
      private final Vigil vigil;

      public WatchTargetGoat(Vigil vigil1) {
         this.vigil = vigil1;
      }

      public boolean canUse() {
         return this.vigil.getTarget() != null;
      }

      public void tick() {
         super.tick();
         Entity target = this.vigil.getTarget();
         if (target != null && this.vigil.hasLineOfSight(target)) {
            this.vigil.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (target instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)target;
               if (!player.hasEffect((MobEffect)Seffects.UNEASY.get())) {
                  player.addEffect(new MobEffectInstance((MobEffect)Seffects.UNEASY.get(), 6000, 0));
               }

               player.displayClientMessage(Component.translatable("vigil.message"), true);
            }
         }

      }

      public void start() {
         super.start();
         if (this.vigil.getTarget() != null && this.vigil.proto != null && this.vigil.proto.getTarget() == null) {
            this.vigil.proto.setTarget(this.vigil.getTarget());
         }

      }
   }

   private static class WatcherMobCall extends Goal {
      private final Vigil vigil;
      private int timer;

      private WatcherMobCall(Vigil vigil) {
         this.vigil = vigil;
      }

      public boolean canUse() {
         LivingEntity living = this.vigil.getTarget();
         return this.vigil.tickCount % 40 == 0 && living != null && this.checkForInfected(living) && this.vigil.getVariant() == VigilVariants.RINGER;
      }

      public boolean canContinueToUse() {
         return this.timer < 200;
      }

      boolean checkForInfected(Entity entity) {
         AABB boundingBox = entity.getBoundingBox().inflate((double)16.0F);

         for(Entity en : entity.level().getEntities(entity, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (en instanceof Infected) {
               return false;
            }
         }

         return true;
      }

      public void start() {
         super.start();
         Level level = this.vigil.level();
         AABB aabb = this.vigil.getBoundingBox().inflate((double)64.0F);
         List<Infected> infecteds = level.getEntitiesOfClass(Infected.class, aabb, (entity) -> !(entity instanceof Hyper) && !(entity instanceof Experiment));
         BlockPos pos = this.vigil.getOnPos();
         Vec3 position = this.vigil.position();
         boolean above = level.canSeeSky(pos);

         for(Infected infected : infecteds) {
            Vec3 vec3 = Utilities.generatePositionAway(position, (double)30.0F);
            infected.randomTeleport(vec3.x, vec3.y, vec3.z, false);
            if (above) {
               infected.teleportToSurface(this.vigil.level(), infected);
            }

            infected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            infected.setSearchPos(pos);
         }

         this.vigil.playSound(SoundEvents.BELL_RESONATE);
      }

      public void tick() {
         super.tick();
         ++this.timer;
      }
   }

   private static class WatcherMobSummon extends Goal {
      private final Vigil vigil;

      private WatcherMobSummon(Vigil vigil) {
         this.vigil = vigil;
      }

      public boolean canUse() {
         if (this.vigil.getWaveSize() <= 0 && this.vigil.getVariant() != VigilVariants.RINGER) {
            LivingEntity living = this.vigil.getTarget();
            return this.vigil.tickCount % 10 == 0 && living != null && this.checkForInfected(living);
         } else {
            return false;
         }
      }

      boolean checkForInfected(Entity entity) {
         AABB boundingBox = entity.getBoundingBox().inflate((double)16.0F);

         for(Entity en : entity.level().getEntities(entity, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            if (en instanceof Infected) {
               return false;
            }
         }

         return true;
      }

      public void start() {
         super.start();
         if (this.vigil.getTarget() != null && this.vigil.getWaveSize() <= 0) {
            LivingEntity target = this.vigil.getTarget();
            int l = (Integer)SConfig.SERVER.vigil_wave_size.get();
            int e = target.getMaxHealth() > (float)(l * 5) ? l : (int)(target.getMaxHealth() / 5.0F) + target.getArmorValue() / 4;
            this.vigil.setWaveSize(e);
            this.vigil.setTrigger(this.vigil.getTrigger() + 1);
         }

      }
   }
}
