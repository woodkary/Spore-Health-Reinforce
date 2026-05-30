package com.Harbinger.Spore.Sentities.Experiments;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.AI.CustomMeleeAttackGoal;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Saugling extends Experiment {
   public static final EntityDataAccessor IS_HIDDEN;
   public static final EntityDataAccessor CHEST_POS;
   public static final EntityDataAccessor PRIMED;
   private int setTicksOut = 0;
   private int ticksOpen = 0;

   public Saugling(EntityType type, Level level) {
      super(type, level);
      this.navigation = new WallClimberNavigation(this, level);
      this.moveControl = new InfectedWallMovementControl(this);
   }

   public boolean isHidden() {
      return (Boolean)this.entityData.get(IS_HIDDEN);
   }

   public void setIsHidden(boolean val) {
      this.entityData.set(IS_HIDDEN, val);
   }

   public boolean isPrimed() {
      return (Boolean)this.entityData.get(PRIMED);
   }

   public void setPrimed(boolean val) {
      this.entityData.set(PRIMED, val);
   }

   public BlockPos getChestPos() {
      return (BlockPos)this.entityData.get(CHEST_POS);
   }

   public void setChestPos(BlockPos val) {
      this.entityData.set(CHEST_POS, val);
   }

   public List<String> getDropList() {
      return (List)SConfig.DATAGEN.saugling_loot.get();
   }

   public boolean hurt(DamageSource source, float amount) {
      return this.isHidden() ? false : super.hurt(source, amount);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_HIDDEN, false);
      this.entityData.define(CHEST_POS, BlockPos.ZERO);
      this.entityData.define(PRIMED, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("hidden", (Boolean)this.entityData.get(IS_HIDDEN));
      tag.putBoolean("primed", (Boolean)this.entityData.get(PRIMED));
      tag.putInt("chestPosX", ((BlockPos)this.entityData.get(CHEST_POS)).getX());
      tag.putInt("chestPosY", ((BlockPos)this.entityData.get(CHEST_POS)).getY());
      tag.putInt("chestPosZ", ((BlockPos)this.entityData.get(CHEST_POS)).getZ());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(IS_HIDDEN, tag.getBoolean("hidden"));
      this.entityData.set(PRIMED, tag.getBoolean("primed"));
      int x = tag.getInt("chestPosX");
      int y = tag.getInt("chestPosY");
      int z = tag.getInt("chestPosZ");
      this.entityData.set(CHEST_POS, new BlockPos(x, y, z));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (Double)SConfig.SERVER.sau_hp.get() * (Double)SConfig.SERVER.global_health.get()).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, (Double)SConfig.SERVER.sau_damage.get() * (Double)SConfig.SERVER.global_damage.get()).add(Attributes.ARMOR, (Double)SConfig.SERVER.sau_armor.get() * (Double)SConfig.SERVER.global_armor.get()).add(Attributes.FOLLOW_RANGE, (double)16.0F).add(Attributes.ATTACK_KNOCKBACK, (double)1.0F);
   }

   public int getSetTicksOut() {
      return this.setTicksOut;
   }

   public void setSetTicksOut(int setTicksOut) {
      this.setTicksOut = setTicksOut;
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(2, new HideInChestGoal(this));
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(4, new CustomMeleeAttackGoal(this, (double)1.5F, false) {
         protected double getAttackReachSqr(LivingEntity entity) {
            return (double)2.0F + (double)(entity.getBbWidth() * entity.getBbWidth());
         }
      });
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
   }

   public boolean isNoAi() {
      return this.isHidden();
   }

   public boolean isInvulnerable() {
      return this.isHidden();
   }

   private void leapAtTarget(LivingEntity target) {
      Vec3 $$0 = this.getDeltaMovement();
      Vec3 $$1 = new Vec3(target.getX() - this.getX(), target.getY() - this.getY(), this.getZ() - this.getZ());
      if ($$1.lengthSqr() > 1.0E-7) {
         $$1 = $$1.normalize().scale(0.4).add($$0.scale(0.2));
      }

      this.setDeltaMovement($$1.x, $$1.y, $$1.z);
   }

   private BlockPos findNearbyChest() {
      BlockPos mobPos = this.blockPosition();

      for(BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-8, -2, -8), mobPos.offset(8, 2, 8))) {
         if (this.level().getBlockState(pos).is(Blocks.CHEST)) {
            return pos.immutable();
         }
      }

      return BlockPos.ZERO;
   }

   public boolean isInvisible() {
      return this.isHidden();
   }

   public boolean checkChest(Level level) {
      return level.getBlockState(this.getChestPos()).is(Blocks.CHEST) && this.getChestPos() != BlockPos.ZERO;
   }

   public void aiStep() {
      super.aiStep();
      if (this.tickCount % 60 == 0) {
         this.setChestPos(this.findNearbyChest());
      }

      if (this.setTicksOut > 0) {
         --this.setTicksOut;
      }

      if (this.isHidden() && this.tickCount % 20 == 0) {
         if (!this.checkChest(this.level())) {
            this.setIsHidden(false);
            this.setPrimed(false);
         }

         if (!this.isPrimed()) {
            AABB aabb = this.getBoundingBox().inflate((double)3.0F);
            List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(LivingEntity.class, aabb, (entity) -> {
               boolean var10000;
               label27: {
                  if (entity.isAlive() && this.TARGET_SELECTOR.test(entity)) {
                     if (!(entity instanceof Player)) {
                        break label27;
                     }

                     Player player = (Player)entity;
                     if (!player.getAbilities().instabuild && !player.isSpectator()) {
                        break label27;
                     }
                  }

                  var10000 = false;
                  return var10000;
               }

               var10000 = true;
               return var10000;
            });
            if (!livingEntities.isEmpty()) {
               this.setPrimed(true);
               this.playSound((SoundEvent)Ssounds.SAUGLING_JUMPSCARE.get());
               this.setTarget((LivingEntity)livingEntities.get(this.random.nextInt(livingEntities.size())));
            }
         } else {
            this.setIsHidden(false);
            this.setPrimed(false);
            this.openChest(this.getChestPos());
            this.setSetTicksOut(100);
            LivingEntity target = this.getTarget();
            if (target != null) {
               this.leapAtTarget(target);
            }
         }
      }

      if (this.ticksOpen > 0) {
         if (this.ticksOpen == 1) {
            this.closeChest(this.getChestPos());
         }

         --this.ticksOpen;
      }

   }

   public void hideInChest() {
      this.ticksOpen = 50;
      this.setIsHidden(true);
      this.setPrimed(false);
      this.openChest(this.getChestPos());
      if (this.level().getBlockState(this.getChestPos().above()).isAir()) {
         this.teleportTo((double)this.getChestPos().getX() + (double)0.5F, (double)(this.getChestPos().getY() + 1), (double)this.getChestPos().getZ() + (double)0.5F);
      }

   }

   public void openChest(BlockPos pos) {
      BlockEntity entity = this.level().getBlockEntity(pos);
      if (entity instanceof ChestBlockEntity chestBlock) {
         this.playSound(SoundEvents.CHEST_OPEN);
         this.level().blockEvent(pos, chestBlock.getBlockState().getBlock(), 1, 1);
         this.level().updateNeighborsAt(pos, chestBlock.getBlockState().getBlock());
         this.level().updateNeighborsAt(pos.below(), chestBlock.getBlockState().getBlock());
      }

   }

   public void closeChest(BlockPos pos) {
      BlockEntity entity = this.level().getBlockEntity(pos);
      if (entity instanceof ChestBlockEntity chestBlock) {
         this.playSound(SoundEvents.CHEST_CLOSE);
         this.level().blockEvent(pos, chestBlock.getBlockState().getBlock(), 1, 0);
         this.level().updateNeighborsAt(pos, chestBlock.getBlockState().getBlock());
         this.level().updateNeighborsAt(pos.below(), chestBlock.getBlockState().getBlock());
      }

   }

   public boolean isDormant() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return this.isHidden() ? (SoundEvent)Ssounds.SAUGLING_CHEST_AMBIENT.get() : (SoundEvent)Ssounds.SAUGLING_AMBIENT.get();
   }

   public SoundEvent getHurtSound(DamageSource p_34327_) {
      return (SoundEvent)Ssounds.INF_DAMAGE.get();
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

   protected int calculateFallDamage(float p_21237_, float p_21238_) {
      return super.calculateFallDamage(p_21237_, p_21238_) - 10;
   }

   static {
      IS_HIDDEN = SynchedEntityData.defineId(Saugling.class, EntityDataSerializers.BOOLEAN);
      CHEST_POS = SynchedEntityData.defineId(Saugling.class, EntityDataSerializers.BLOCK_POS);
      PRIMED = SynchedEntityData.defineId(Saugling.class, EntityDataSerializers.BOOLEAN);
   }

   public static class HideInChestGoal extends Goal {
      private final Saugling mob;

      public HideInChestGoal(Saugling mob) {
         this.mob = mob;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         if (!this.mob.isHidden() && this.mob.getTarget() == null) {
            return this.mob.getChestPos() != BlockPos.ZERO && this.mob.getSetTicksOut() <= 0;
         } else {
            return false;
         }
      }

      public void start() {
         if (this.mob.getChestPos() != null && this.mob.getChestPos() != BlockPos.ZERO) {
            this.mob.setChestPos(this.mob.getChestPos());
            this.mob.getNavigation().moveTo((double)this.mob.getChestPos().getX(), (double)this.mob.getChestPos().getY(), (double)this.mob.getChestPos().getZ(), (double)1.0F);
         }

      }

      public void tick() {
         if (this.mob.getChestPos() != null && this.mob.getChestPos() != BlockPos.ZERO && this.mob.position().distanceToSqr(Vec3.atCenterOf(this.mob.getChestPos())) < (double)1.5F) {
            this.mob.hideInChest();
         }

      }
   }
}
