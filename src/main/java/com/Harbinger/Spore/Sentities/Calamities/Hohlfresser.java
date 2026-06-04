package com.Harbinger.Spore.Sentities.Calamities;

import com.Harbinger.Spore.Core.SAttributes;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Core.asmHooks.SporeEntityHeeaafastthManager;
import com.Harbinger.Spore.Sentities.HitboxesForParts;
import com.Harbinger.Spore.Sentities.TrueCalamity;
import com.Harbinger.Spore.Sentities.AI.AOEMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.CalamityInfectedCommand;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SporeBurstSupport;
import com.Harbinger.Spore.Sentities.AI.CalamitiesAI.SummonScentInCombat;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.HohlMultipart;
import com.Harbinger.Spore.Sentities.MovementControls.UndergroundMovementControl;
import com.Harbinger.Spore.Sentities.MovementControls.UndergroundPathNavigation;
import com.Harbinger.Spore.Sentities.Projectile.ThrownTumor;
import com.Harbinger.Spore.Sentities.Projectile.VomitHohlBall;
import com.Harbinger.Spore.Sentities.Utility.CorpseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;

public class Hohlfresser extends Calamity implements TrueCalamity, RangedAttackMob {
   private static final EntityDataAccessor CHILD_UUID;
   private static final EntityDataAccessor CHILD_ID;
   public static final EntityDataAccessor VULNERABLE;
   public static final EntityDataAccessor ADAPTED;
   public static final EntityDataAccessor UNDERGROUND;
   private static final EntityDataAccessor ORES;
   private float spin = 0.0F;
   private HohlMultipart[] parts = null;
   public final float[] ringBuffer = new float[64];
   public int ringBufferIndex = -1;
   private int ticksUnder;
   private static final Map<BlockState, Integer> cache;
   private int[] segments = new int[10];
   public static final TagKey ORE_TAG;
   public static final int FLAG_MINEABLE = 1;
   public static final int FLAG_HARD = 2;
   public static final int FLAG_WRONG = 4;
   private final List<HitboxesForParts> innatePartList;
   private final List<HitboxesForParts> tailHitboxes;

   public Hohlfresser(EntityType type, Level level) {
      super(type, level);
      this.innatePartList = List.of(HitboxesForParts.HOHL_JAW, HitboxesForParts.HOHL_HEAD);
      this.tailHitboxes = List.of(HitboxesForParts.HOHL_SEG1, HitboxesForParts.HOHL_SEG2, HitboxesForParts.HOHL_SEG3, HitboxesForParts.HOHL_TAIL);
      this.setMaxUpStep(2.0F);
      this.moveControl = new UndergroundMovementControl(this);
      this.navigation = new UndergroundPathNavigation(this, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ADAPTED, false);
      this.entityData.define(UNDERGROUND, false);
      this.entityData.define(VULNERABLE, 0);
      this.entityData.define(CHILD_UUID, Optional.empty());
      this.entityData.define(CHILD_ID, -1);
      this.entityData.define(ORES, 0.0F);
   }

   public float getSpin() {
      float speed = (float)Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
      this.spin += speed * 2.5E-4F * (float)this.tickCount;
      return this.spin;
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.hohl_loot.get();
   }

   public String getMutation() {
      return this.getAdaptation() ? "spore.entity.variant.engorged" : super.getMutation();
   }

   public boolean isPushable() {
      return false;
   }

   public void onSyncedDataUpdated(EntityDataAccessor key) {
      super.onSyncedDataUpdated(key);
      if (ORES.equals(key) && !this.getAdaptation() && this.getOres() > 50.0F && this.getKills() > 50) {
         this.setAdapted(true);
         this.refreshDimensions();
      }

   }

   public void setAdapted(boolean val) {
      if (val && !this.getAdaptation()) {
         AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
         AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
         AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
         float maxHealth = health != null ? (float)(health.getValue() * (double)2.0F) : SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(this) * 2.0F;
         if (health != null) {
            health.setBaseValue(maxHealth);
         }

         SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(this, maxHealth);
         if (armor != null) {
            armor.setBaseValue(armor.getValue() * (double)1.5F);
         }

         if (damage != null) {
            damage.setBaseValue(damage.getValue() * (double)1.25F);
         }
      }

      this.entityData.set(ADAPTED, val);
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canGoUnderground() {
      return !(Boolean)this.entityData.get(UNDERGROUND) && (Integer)this.entityData.get(VULNERABLE) <= 0;
   }

   public double getDamageCap() {
      return (Double)SConfig.SERVER.hohl_dpsr.get();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("adaptation", (Boolean)this.entityData.get(ADAPTED));
      tag.putBoolean("underground", (Boolean)this.entityData.get(UNDERGROUND));
      tag.putInt("vulnerable", (Integer)this.entityData.get(VULNERABLE));
      tag.putFloat("ores", (Float)this.entityData.get(ORES));
      if (this.getChildId() != null) {
         tag.putUUID("ChildUUID", this.getChildId());
      }

      tag.putIntArray("segmentIds", this.segments);
   }

   public boolean isInWall(LivingEntity mob) {
      float f = mob.getBbWidth() * 0.8F;
      AABB aabb = AABB.ofSize(mob.getEyePosition().add((double)0.0F, -0.05, (double)0.0F), (double)f, 1.0E-6, (double)f);
      return BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
         BlockState blockstate = mob.level().getBlockState(p_201942_);
         return !blockstate.isAir() && blockstate.isSuffocating(mob.level(), p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(mob.level(), p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
      });
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(ADAPTED, tag.getBoolean("adaptation"));
      this.entityData.set(UNDERGROUND, tag.getBoolean("underground"));
      this.entityData.set(VULNERABLE, tag.getInt("vulnerable"));
      this.entityData.set(ORES, tag.getFloat("ores"));
      if (tag.hasUUID("ChildUUID")) {
         this.setChildId(tag.getUUID("ChildUUID"));
      }

      this.segments = tag.getIntArray("segmentIds");
   }

   public void ActivateAdaptation() {
      this.setAdapted(true);
   }

   public boolean getAdaptation() {
      return (Boolean)this.entityData.get(ADAPTED);
   }

   public EntityDimensions getDimensions(Pose p_21047_) {
      float adapted = this.getAdaptation() ? 2.0F : 1.0F;
      return super.getDimensions(p_21047_).scale(adapted);
   }

   public void remove(RemovalReason reason) {
      if (!this.level().isClientSide && this.parts != null) {
         for(HohlMultipart hohlMultipart : this.parts) {
            hohlMultipart.discard();
         }
      }

      super.remove(reason);
   }

   protected void grief(AABB aabb) {
      if (!this.isUnderground() && this.tickCount % 20 == 0) {
         DamageSource source = this.getLastDamageSource();
         AABB box = source == null ? aabb : aabb.move(new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
         if (Math.random() < (double)0.2F) {
            this.handleDigIn();
         }

         super.grief(box);
      }

   }

   public boolean isUnderground() {
      return (Boolean)this.entityData.get(UNDERGROUND);
   }

   public void setUnderground(boolean val) {
      if (val) {
         this.playSound((SoundEvent)Ssounds.WORM_DIGGING.get());
         this.ticksUnder = 40;
      } else {
         this.entityData.set(VULNERABLE, 200);
      }

      this.entityData.set(UNDERGROUND, val);
      this.noPhysics = val;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.getAdaptation() && (source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE))) {
         amount /= 2.0F;
      }

      return super.hurt(source, amount);
   }

   public boolean hurt(CalamityMultipart calamityMultipart, DamageSource source, float value) {
      return this.hurt(source, value);
   }

   public int chemicalRange() {
      return 16;
   }

   public List buffs() {
      return (List)SConfig.SERVER.hohl_buffs.get();
   }

   public List debuffs() {
      return (List)SConfig.SERVER.hohl_debuffs.get();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.hohl_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.hohl_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.hohl_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)64.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_KNOCKBACK, (double)2.0F).add((Attribute)SAttributes.TOXICITY.get(), (double)0.0F).add((Attribute)SAttributes.REJUVENATION.get(), (double)0.0F).add((Attribute)SAttributes.LOCALIZATION.get(), (double)0.0F).add((Attribute)SAttributes.LACERATION.get(), (double)0.0F).add((Attribute)SAttributes.CORROSIVES.get(), (double)0.0F).add((Attribute)SAttributes.BALLISTIC.get(), (double)0.0F).add((Attribute)SAttributes.GRINDING.get(), (double)0.0F);
   }

   public HohlMultipart[] getHolfParts() {
      return this.parts;
   }

   @Nullable
   public UUID getChildId() {
      return (UUID)((Optional)this.entityData.get(CHILD_UUID)).orElse((Object)null);
   }

   public void setChildId(@Nullable UUID uniqueId) {
      this.entityData.set(CHILD_UUID, Optional.ofNullable(uniqueId));
   }

   public Entity getChild() {
      UUID id = this.getChildId();
      return id != null && !this.level().isClientSide ? ((ServerLevel)this.level()).getEntity(id) : null;
   }

   private int getSegments() {
      return this.getAdaptation() ? 10 : 5;
   }

   public void tick() {
      super.tick();
      if ((Integer)this.entityData.get(VULNERABLE) > 0) {
         this.entityData.set(VULNERABLE, (Integer)this.entityData.get(VULNERABLE) - 1);
      }

      if (!this.level().isClientSide) {
         if (this.shouldReplaceParts()) {
            this.rebuildPartsArray();
         }

         if (this.tickCount % 20 == 0 && this.parts != null && this.getAdaptation()) {
            float size = 1.2F;
            AttributeInstance hostInstance = this.getAttribute(Attributes.MAX_HEALTH);
            float hostMaxHealth = hostInstance != null ? (float)hostInstance.getBaseValue() : SporeEntityHeeaafastthManager.INSTANCE.getMaxHeeaafastth(this);

            for(int i = 0; i < this.parts.length; ++i) {
               size -= 0.05F;
               HohlMultipart hohlMultipart = this.parts[i];
               boolean isTail = i == this.parts.length - 1;
               hohlMultipart.setAdapted(this.getAdaptation());
               hohlMultipart.setSize(size * 1.4F);
               hohlMultipart.setIsTail(isTail);
               AttributeInstance instance = hohlMultipart.getAttribute(Attributes.MAX_HEALTH);
               if (instance != null && instance.getValue() != hostMaxHealth) {
                  instance.setBaseValue(hostMaxHealth);
               }

               SporeEntityHeeaafastthManager.INSTANCE.setMaxHeeaafastth(hohlMultipart, hostMaxHealth);
            }
         }

         Entity child = this.getChild();
         if (child == null) {
            this.createSegments();
         }

         this.updateSegmentPositions();
      }

      if (this.isUnderground()) {
         this.handleUnearthing();
      }

      if (this.tickCount % 20 == 0) {
         this.handleDigIn();
         this.refreshDimensions();
         if (this.parts != null) {
            for(HohlMultipart multipart : this.parts) {
               multipart.setHealth(this.getHealth());
            }
         }
      }

      if (this.ticksUnder > 0) {
         --this.ticksUnder;
      }

      if (this.tickCount % 20 == 0 && this.isMoving() && this.isUnderground() && this.getTarget() != null) {
         this.tryAndCrumbleBlocks();
      }

      if (this.tickCount % 80 == 0 && this.isUnderground() && this.isInWall(this)) {
         this.playSound((SoundEvent)Ssounds.WORM_DIGGING.get());
      }

      if (this.tickCount % 10 == 0) {
         this.handleShooting();
      }

   }

   private void rebuildPartsArray() {
      this.parts = new HohlMultipart[this.getSegments()];
      Entity var2 = this.getChild();
      if (var2 instanceof HohlMultipart firstChild) {
         this.parts[0] = firstChild;
         this.entityData.set(CHILD_ID, this.parts[0].getId());
         int i = 1;

         HohlMultipart current;
         for(current = firstChild; i < this.parts.length; ++i) {
            Entity var5 = current.getChild();
            if (!(var5 instanceof HohlMultipart)) {
               break;
            }

            HohlMultipart nextChild = (HohlMultipart)var5;
            this.parts[i] = nextChild;
            current = nextChild;
         }

         if (i < this.parts.length) {
            this.createMissingSegments(i, current);
         }
      }

   }

   private void createSegments() {
      float size = 1.0F;
      LivingEntity partParent = this;
      this.parts = new HohlMultipart[this.getSegments()];

      for(int i = 0; i < this.getSegments(); ++i) {
         int var = this.segments != null && this.segments.length >= this.getSegments() ? this.segments[i] : this.random.nextInt(3);
         size -= 0.1F;
         HohlMultipart part = new HohlMultipart((EntityType)Sentities.HOHLFRESSER_SEG.get(), this.level());
         part.setPos(this.getX(), this.getY(), this.getZ());
         part.setParent(partParent);
         part.setSize(size);
         part.setColor(this.getMutationColor());
         part.setVariant(var);
         part.setIsTail(i == this.getSegments() - 1);
         if (partParent == this) {
            this.setChildId(part.getUUID());
            this.entityData.set(CHILD_ID, part.getId());
         }

         if (partParent instanceof HohlMultipart partIndex) {
            partIndex.setChildId(part.getUUID());
         }

         partParent = part;
         this.level().addFreshEntity(part);
         this.parts[i] = part;
      }

   }

   private void createMissingSegments(int startIndex, LivingEntity lastParent) {
      float var10000;
      if (lastParent instanceof HohlMultipart hm) {
         var10000 = hm.getSize() - 0.1F;
      } else {
         var10000 = 0.9F;
      }

      float size = var10000;

      for(int i = startIndex; i < this.parts.length; ++i) {
         int var = this.segments != null && this.segments.length >= this.getSegments() ? this.segments[i] : this.random.nextInt(3);
         size -= this.getAdaptation() ? 0.05F : 0.1F;
         HohlMultipart part = new HohlMultipart((EntityType)Sentities.HOHLFRESSER_SEG.get(), this.level());
         part.setPos(lastParent.getX(), lastParent.getY(), lastParent.getZ());
         part.setParent(lastParent);
         part.setSize(size);
         part.setColor(this.getMutationColor());
         part.setVariant(var);
         part.setIsTail(i == this.parts.length - 1);
         if (lastParent instanceof HohlMultipart partIndex) {
            partIndex.setChildId(part.getUUID());
         }

         lastParent = part;
         this.level().addFreshEntity(part);
         this.parts[i] = part;
      }

   }

   private void updateSegmentPositions() {
      Vec3 prev = this.position();
      float xRot = this.getXRot();

      for(int i = 0; i < this.getSegments(); ++i) {
         if (this.parts[i] != null) {
            float yaw = this.getYawForPart(i);
            prev = this.parts[i].tickMultipartPosition(this.getId(), prev, xRot, this.getYRot(), yaw, true);
            xRot = this.parts[i].getXRot();
         }
      }

   }

   private boolean shouldReplaceParts() {
      if (this.parts != null && this.parts.length == this.getSegments()) {
         for(HohlMultipart part : this.parts) {
            if (part == null || !part.isAlive()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   void handleShooting() {
      LivingEntity living = this.getTarget();
      if (living != null && living.distanceToSqr(this) > (double)100.0F && this.hasSight(living)) {
         this.performRangedAttack(living, 0.0F);
      }

   }

   public boolean hasSight(Entity entity) {
      if (entity.level() != this.level()) {
         return false;
      } else {
         Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
         Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
         if (vec31.distanceTo(vec3) > (double)128.0F) {
            return false;
         } else {
            return this.level().clip(new ClipContext(vec3, vec31, Block.COLLIDER, Fluid.NONE, this)).getType() == Type.MISS;
         }
      }
   }

   public float getOres() {
      return (Float)this.entityData.get(ORES);
   }

   public void tryAndCrumbleBlocks() {
      if (!this.level().isClientSide) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)var2;
            if (!this.checkForNearbyPlayers(serverLevel)) {
               return;
            }
         }

         boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), this);
         if (canGrief) {
            AABB aabb = this.getBoundingBox().inflate((double)8.0F);

            for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
               BlockState state = this.level().getBlockState(blockpos);
               BlockState stateBelow = this.level().getBlockState(blockpos.below());
               boolean canFall = stateBelow.isAir() || stateBelow.liquid();
               if (canFall && Math.random() < (double)0.01F) {
                  double speed = (double)state.getDestroySpeed(this.level(), blockpos);
                  if (speed > (double)0.0F && speed <= (double)(Integer)SConfig.SERVER.calamity_bd.get()) {
                     this.level().removeBlock(blockpos, false);
                     FallingBlockEntity.fall(this.level(), blockpos, state);
                  }
               }

               if ((state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) && Math.random() < 0.2) {
                  this.level().setBlock(blockpos, Math.random() < (double)0.5F ? Blocks.DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState(), 3);
               }

               if (state.is(ORE_TAG) && Math.random() < (double)0.005F) {
                  this.entityData.set(ORES, (Float)this.entityData.get(ORES) + 1.0F);
                  this.level().setBlock(blockpos, blockpos.getY() < 0 ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), 3);
               }
            }

         }
      }
   }

   private boolean checkForNearbyPlayers(ServerLevel serverLevel) {
      List<ServerPlayer> playerList = serverLevel.getPlayers((p) -> true);
      if (playerList.isEmpty()) {
         return false;
      } else {
         for(ServerPlayer player : playerList) {
            if (player.distanceTo(this) < 400.0F) {
               return true;
            }
         }

         return false;
      }
   }

   public int analyzeBlock(BlockState state, BlockPos pos, Map<BlockState, Integer> cache) {
      return (Integer)cache.computeIfAbsent(state, (s) -> {
         double hardness = (double)s.getDestroySpeed(this.level(), pos);
         if (hardness == (double)-1.0F) {
            return 6;
         } else {
            boolean isMineable = s.isAir() || s.canBeReplaced() || s.is(BlockTags.MINEABLE_WITH_SHOVEL) || s.is(BlockTags.MINEABLE_WITH_PICKAXE) || !s.isSolidRender(this.level(), pos) || hardness == (double)0.0F;
            boolean isHard = hardness > (double)3.0F;
            boolean isWrong = !isMineable;
            int result = 0;
            if (isMineable) {
               result |= 1;
            }

            if (isHard) {
               result |= 2;
            }

            if (isWrong) {
               result |= 4;
            }

            return result;
         }
      });
   }

   private boolean checkBlocksUnder() {
      AABB aabb = this.getBoundingBox().move((double)0.0F, -0.6, (double)0.0F);

      for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState state = this.level().getBlockState(pos);
         int result = this.analyzeBlock(state, pos, cache);
         if ((result & 2) == 0 && (result & 1) != 0) {
            BlockState aboveState = this.level().getBlockState(pos.above());
            int aboveResult = this.analyzeBlock(aboveState, pos.above(), cache);
            if ((aboveResult & 1) != 0 && (aboveResult & 2) == 0) {
               continue;
            }

            return false;
         }

         return false;
      }

      return true;
   }

   public void handleDigIn() {
      if (!this.isUnderground() && (Integer)this.entityData.get(VULNERABLE) <= 0) {
         boolean tooDeep = (double)this.level().getMinBuildHeight() < this.getY() - (double)5.0F;
         boolean below = this.moveControl.getWantedY() < this.getY();
         boolean above = this.moveControl.getWantedY() > this.getY() + (double)1.0F;
         if ((below || above) && this.checkBlocksUnder() && tooDeep) {
            this.setUnderground(true);
         }
      }

   }

   public void handleUnearthing() {
      AABB aabb = this.getBoundingBox().inflate((double)1.0F, 1.4, (double)1.0F);
      int airAmount = 0;
      boolean meetsHardBlock = false;
      boolean meetsWrongBlock = false;

      for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState state = this.level().getBlockState(pos);
         int result = this.analyzeBlock(state, pos, cache);
         if (this.level().canSeeSky(pos) && this.ticksUnder <= 0) {
            ++airAmount;
         }

         if ((result & 4) != 0) {
            meetsWrongBlock = true;
            break;
         }

         if ((result & 2) != 0) {
            meetsHardBlock = true;
            break;
         }
      }

      if (airAmount >= 4 || meetsHardBlock || meetsWrongBlock) {
         this.setUnderground(false);
      }

   }

   private float getYawForPart(int i) {
      return this.getRingBuffer(4 + i * 2, 1.0F);
   }

   public float getRingBuffer(int bufferOffset, float partialTicks) {
      if (this.isDeadOrDying()) {
         partialTicks = 0.0F;
      }

      partialTicks = 1.0F - partialTicks;
      int i = this.ringBufferIndex - bufferOffset & 63;
      int j = this.ringBufferIndex - bufferOffset - 1 & 63;
      float d0 = this.ringBuffer[i];
      float d1 = this.ringBuffer[j] - d0;
      return Mth.wrapDegrees(d0 + d1 * partialTicks);
   }

   public boolean isMoving() {
      return Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z) > (double)0.0F;
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL);
   }

   public void registerGoals() {
      this.goalSelector.addGoal(4, new HohlChargeGoal(this, (double)0.5F, 300, 100.0F));
      this.goalSelector.addGoal(5, new HohlfresserMeleeAttack(this, (livingEntity) -> this.TARGET_SELECTOR.test(livingEntity)));
      this.goalSelector.addGoal(6, new CalamityInfectedCommand(this));
      this.goalSelector.addGoal(7, new SummonScentInCombat(this));
      this.goalSelector.addGoal(8, new SporeBurstSupport(this));
      super.registerGoals();
   }

   public boolean canDrownInFluidType(FluidType type) {
      return false;
   }

   private boolean checkVectorForSeeing(Entity target) {
      Vec3 startVec = this.position();
      Vec3 endVec = target.position();
      Vec3 direction = endVec.subtract(startVec).normalize();
      double distance = startVec.distanceTo(endVec);

      for(double i = (double)0.0F; i <= distance; i += (double)0.5F) {
         Vec3 current = startVec.add(direction.scale(i));
         BlockPos pos = BlockPos.containing(current);
         BlockState state = this.level().getBlockState(pos);
         int result = this.analyzeBlock(state, pos, cache);
         if ((result & 2) != 0 || (result & 1) == 0) {
            return false;
         }
      }

      return true;
   }

   public boolean hasLineOfSight(Entity entity) {
      if (this.getSearchArea() == BlockPos.ZERO) {
         if (this.isInWater()) {
            return true;
         } else {
            return this.checkVectorForSeeing(entity) || super.hasLineOfSight(entity);
         }
      } else {
         return super.hasLineOfSight(entity);
      }
   }

   public int getShootingAmount() {
      AttributeInstance instance = this.getAttribute((Attribute)SAttributes.BALLISTIC.get());
      if (instance != null && instance.getValue() > (double)0.0F) {
         int value = (int)(instance.getValue() * (double)3.0F);
         return this.random.nextInt(value + 1);
      } else {
         return 1;
      }
   }

   public void performRangedAttack(LivingEntity livingEntity, float v) {
      if (Math.random() < (double)0.1F) {
         for(int i = 0; i < this.getShootingAmount(); ++i) {
            this.shootTumor(livingEntity);
         }
      } else {
         float extraDamage = (float)((Double)SConfig.SERVER.hohl_r_damage.get() + (double)(this.getOres() * 0.2F));
         double maxDamage = (Double)SConfig.SERVER.hohl_damage.get() / (double)2.0F;
         double damage = maxDamage <= (double)extraDamage ? maxDamage : (double)extraDamage;
         VomitHohlBall.shoot(this, livingEntity, (float)damage, this.getOres() > 0.0F, this.getKills() > 0);
      }

   }

   void shootTumor(LivingEntity livingEntity) {
      if (!this.level().isClientSide) {
         ThrownTumor tumor = new ThrownTumor(this.level(), this);
         double dx = livingEntity.getX() - this.getX();
         double dy = livingEntity.getY() + (double)livingEntity.getEyeHeight();
         double dz = livingEntity.getZ() - this.getZ();
         tumor.setExplode(ExplosionInteraction.MOB);
         tumor.shoot(dx, dy - tumor.getY() + Math.hypot(dx, dz) * (double)0.05F, dz, 1.0F, 12.0F);
         this.level().addFreshEntity(tumor);
      }
   }

   public boolean doHurtTarget(Entity entity) {
      this.playSound((SoundEvent)Ssounds.SIEGER_BITE.get());
      return super.doHurtTarget(entity);
   }

   protected void onEffectAdded(MobEffectInstance instance, @Nullable Entity source) {
      super.onEffectAdded(instance, source);
      HohlMultipart[] parts = this.getHolfParts();
      if (parts != null) {
         for(HohlMultipart part : parts) {
            if (part == null) {
               return;
            }

            MobEffectInstance existing = part.getEffect(instance.getEffect());
            if (existing == null || existing.getDuration() < instance.getDuration() - 5) {
               part.addEffect(new MobEffectInstance(instance));
            }
         }

      }
   }

   protected void onEffectRemoved(MobEffectInstance instance) {
      super.onEffectRemoved(instance);
      if (this.getHolfParts() != null) {
         for(HohlMultipart hohlMultipart : this.getHolfParts()) {
            if (hohlMultipart == null) {
               return;
            }

            hohlMultipart.removeEffect(instance.getEffect());
         }

      }
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)Ssounds.HOHL_AMBIENT.get();
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.RAVAGER_STEP;
   }

   protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public List<HitboxesForParts> parts() {
      List<HitboxesForParts> values = new ArrayList<>(this.innatePartList);
      if (this.getHolfParts() != null) {
         for(HohlMultipart multipart : this.getHolfParts()) {
            values.add(this.CalculateParts(multipart));
         }
      }

      return values;
   }

   public void summonCorpsePart(int partCount, List distributedLoot, List partList) {
      AtomicInteger index = new AtomicInteger();

      for(int i = 0; i < partCount; ++i) {
         CorpseEntity partEntity = new CorpseEntity((EntityType)Sentities.CORPSE_PIECE.get(), this.level());

         for(ItemStack stack : (List<ItemStack>)distributedLoot.get(i)) {
            partEntity.addToInventory(stack);
         }

         partEntity.setColor(this.getMutationColor());
         partEntity.moveTo(this.calculateSegmentsPosition(i - 2));
         partEntity.setDeltaMovement(new Vec3((this.random.nextDouble() - this.random.nextDouble()) * 0.9, this.random.nextDouble() * 0.6 + 0.3, (this.random.nextDouble() - this.random.nextDouble()) * 0.9));
         partEntity.setOwnerAda(this.getAdaptation());
         partEntity.setCorpseType(((HitboxesForParts)partList.get(i)).getID());
         int var10002 = index.get();
         int var10003 = ((HitboxesForParts)partList.get(i)).getID();
         Objects.requireNonNull(index);
         partEntity.setInflation(this.tryToFindInflation(var10002, var10003, index::getAndIncrement));
         this.level().addFreshEntity(partEntity);
      }

   }

   private Vec3 calculateSegmentsPosition(int value) {
      return value >= 0 && this.getHolfParts() != null && this.getHolfParts().length >= value ? this.getHolfParts()[value].position() : this.position();
   }

   public float tryToFindInflation(int startPoint, int ID, Runnable runnable) {
      if (this.getHolfParts() == null) {
         return 1.0F;
      } else {
         int length = this.getHolfParts().length;
         if (length < startPoint) {
            return 1.0F;
         } else if (this.tailHitboxes.contains(HitboxesForParts.byId(ID))) {
            HohlMultipart multipart = this.getHolfParts()[startPoint];
            return multipart == null ? 1.0F : multipart.getSize();
         } else {
            runnable.run();
            return 1.0F;
         }
      }
   }

   private HitboxesForParts CalculateParts(HohlMultipart hohlMultipart) {
      boolean adapted = this.getAdaptation();
      if (hohlMultipart.isTail()) {
         return adapted ? HitboxesForParts.HOHL_ADA_TAIL : HitboxesForParts.HOHL_TAIL;
      } else if (hohlMultipart.getSegmentVariant() == HohlMultipart.SegmentVariants.MELEE) {
         return adapted ? HitboxesForParts.HOHL_ADA_SEG2 : HitboxesForParts.HOHL_SEG2;
      } else if (hohlMultipart.getSegmentVariant() == HohlMultipart.SegmentVariants.ORGAN) {
         return adapted ? HitboxesForParts.HOHL_ADA_SEG3 : HitboxesForParts.HOHL_SEG3;
      } else {
         return adapted ? HitboxesForParts.HOHL_ADA_SEG1 : HitboxesForParts.HOHL_SEG1;
      }
   }

   static {
      CHILD_UUID = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.OPTIONAL_UUID);
      CHILD_ID = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.INT);
      VULNERABLE = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.INT);
      ADAPTED = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.BOOLEAN);
      UNDERGROUND = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.BOOLEAN);
      ORES = SynchedEntityData.defineId(Hohlfresser.class, EntityDataSerializers.FLOAT);
      cache = new WeakHashMap();
      ORE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation("forge:ores"));
   }

   static class HohlfresserMeleeAttack extends AOEMeleeAttackGoal {
      public HohlfresserMeleeAttack(Hohlfresser mob, Predicate<LivingEntity> targets) {
         super(mob, (double)1.5F, false, (double)2.5F, 6.0F, targets);
      }

      protected double getAttackReachSqr(LivingEntity entity) {
         float f = this.mob.getBbWidth();
         return (double)(f * 1.5F * f + entity.getBbWidth());
      }
   }

   static class HohlChargeGoal extends Goal {
      private final Hohlfresser mob;
      private final double speed;
      private final int chargeDelay;
      private int chargeTimer = 0;
      private final float distance;

      HohlChargeGoal(Hohlfresser mob, double speed, int chargeDelay, float distance) {
         this.mob = mob;
         this.speed = speed;
         this.chargeDelay = chargeDelay;
         this.distance = distance;
      }

      public boolean canUse() {
         LivingEntity target = this.mob.getTarget();
         if (target != null && target.isAlive()) {
            if (this.chargeTimer < this.chargeDelay) {
               ++this.chargeTimer;
               return false;
            } else if (this.checkVectorForCharging(target)) {
               return true;
            } else {
               this.chargeTimer = 0;
               return false;
            }
         } else {
            this.chargeTimer = 0;
            return false;
         }
      }

      boolean jump(LivingEntity us, LivingEntity target) {
         return target.level().canSeeSky(target.getOnPos()) && us.level().canSeeSky(us.getOnPos());
      }

      public void start() {
         LivingEntity target = this.mob.getTarget();
         if (target != null && target.distanceTo(this.mob) < this.distance) {
            this.mob.setUnderground(true);
            Vec3 direction = target.position().subtract(this.mob.position());
            if (direction.lengthSqr() > 1.0E-7) {
               direction.normalize();
            }

            direction.scale(this.speed);
            this.mob.setDeltaMovement(direction.x, 0.3, direction.z);
         }

         this.chargeTimer = 0;
      }

      public boolean canContinueToUse() {
         return false;
      }

      private boolean checkVectorForCharging(Entity target) {
         Map<BlockState, Integer> cache = new HashMap();
         Vec3 startVec = this.mob.position();
         Vec3 endVec = target.position();
         Vec3 direction = endVec.subtract(startVec).normalize();
         double distance = startVec.distanceTo(endVec);

         for(double i = (double)0.0F; i <= distance; i += (double)0.5F) {
            Vec3 current = startVec.add(direction.scale(i));
            BlockPos pos = BlockPos.containing(current);
            BlockState state = this.mob.level().getBlockState(pos);
            int result = this.mob.analyzeBlock(state, pos, cache);
            if ((result & 2) != 0 || (result & 1) == 0) {
               return false;
            }
         }

         return true;
      }
   }
}
