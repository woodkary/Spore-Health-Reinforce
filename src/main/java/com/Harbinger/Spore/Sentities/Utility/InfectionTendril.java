package com.Harbinger.Spore.Sentities.Utility;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.SBlockEntities.ContainerBlockEntity;
import com.Harbinger.Spore.SBlockEntities.LivingStructureBlocks;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.MovementControls.InfectedWallMovementControl;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectionTendril extends UtilityEntity {
   private static final EntityDataAccessor SEARCH_AREA;
   private static final EntityDataAccessor LIFE;
   private static final EntityDataAccessor MOUND_AGE;

   public InfectionTendril(EntityType type, Level level) {
      super(type, level);
      this.moveControl = new InfectedWallMovementControl(this);
      this.navigation = new WallClimberNavigation(this, level);
   }

   public boolean removeWhenFarAway(double p_21542_) {
      return false;
   }

   public int getLife() {
      return (Integer)this.entityData.get(LIFE);
   }

   public void setLife(int s) {
      this.entityData.set(LIFE, s);
   }

   public int getAgeM() {
      return (Integer)this.entityData.get(MOUND_AGE);
   }

   public void setAgeM(int s) {
      this.entityData.set(MOUND_AGE, s);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MOUND_AGE, 2);
      this.entityData.define(LIFE, 4800);
      this.entityData.define(SEARCH_AREA, BlockPos.ZERO);
   }

   public void setSearchArea(BlockPos blockPos) {
      this.entityData.set(SEARCH_AREA, blockPos);
   }

   BlockPos getSearchArea() {
      return (BlockPos)this.entityData.get(SEARCH_AREA);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("mound_age", this.getAgeM());
      tag.putInt("life", this.getLife());
      tag.putInt("AreaX", this.getSearchArea().getX());
      tag.putInt("AreaY", this.getSearchArea().getY());
      tag.putInt("AreaZ", this.getSearchArea().getZ());
      super.addAdditionalSaveData(tag);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      this.setAgeM(tag.getInt("mound_age"));
      this.setLife(tag.getInt("life"));
      int i = tag.getInt("AreaX");
      int j = tag.getInt("AreaY");
      int k = tag.getInt("AreaZ");
      this.setSearchArea(new BlockPos(i, j, k));
      super.readAdditionalSaveData(tag);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new GoToArea(this));
      super.registerGoals();
   }

   public void travel(Vec3 p_32858_) {
      if (this.isEffectiveAi() && this.isInWater()) {
         this.moveRelative(0.1F, p_32858_);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement());
      } else {
         super.travel(p_32858_);
      }

   }

   public boolean isInvulnerable() {
      return true;
   }

   public void tick() {
      super.tick();
      if (this.isAlive() && (Integer)this.entityData.get(LIFE) > 0) {
         this.entityData.set(LIFE, (Integer)this.entityData.get(LIFE) - 1);
      }

      if (this.getSearchArea() != BlockPos.ZERO && this.tickCount % 40 == 0) {
         if ((double)Math.abs(this.getSearchArea().getX()) - Math.abs(this.getX()) < (double)6.0F && (double)Math.abs(this.getSearchArea().getZ()) - Math.abs(this.getZ()) < (double)6.0F) {
            this.teleport();
         }

         if (!this.onGround() && this.horizontalCollision && this.verticalCollision) {
            this.teleport();
         }
      }

      if (this.tickCount % 40 == 0) {
         AABB aabb = this.getBoundingBox().inflate((double)8.0F);
         if (!this.level().getEntitiesOfClass(Player.class, aabb).isEmpty()) {
            this.discard();
         }
      }

   }

   public void aiStep() {
      if ((Integer)this.entityData.get(LIFE) == 0) {
         this.discard();
      }

      if (this.random.nextInt(0, 10) == 0) {
         this.Spread(this, this.level(), 0.3);
      }

      super.aiStep();
   }

   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   protected boolean teleport() {
      if (!this.level().isClientSide() && this.isAlive()) {
         double d0 = (double)this.getSearchArea().getX() + (double)this.random.nextInt(-8, 8);
         double d1 = (double)this.getSearchArea().getY();
         double d2 = (double)this.getSearchArea().getZ() + (double)this.random.nextInt(-8, 8);
         this.Spread(this, this.level(), 1.2);
         return this.randomTeleport(d0, d1, d2, false);
      } else {
         return false;
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)1.0F).add(Attributes.MOVEMENT_SPEED, 0.15);
   }

   private void Spread(Entity entity, Level level, double value) {
      AABB aabb = entity.getBoundingBox().inflate(value);

      for(BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
         BlockState nord = level.getBlockState(blockpos.north());
         BlockState south = level.getBlockState(blockpos.south());
         BlockState west = level.getBlockState(blockpos.west());
         BlockState east = level.getBlockState(blockpos.east());
         BlockState above = level.getBlockState(blockpos.above());
         BlockState below = level.getBlockState(blockpos.below());
         boolean nordT = !nord.isSolidRender(level, blockpos.north());
         boolean southT = !south.isSolidRender(level, blockpos.south());
         boolean westT = !west.isSolidRender(level, blockpos.west());
         boolean eastT = !east.isSolidRender(level, blockpos.east());
         boolean aboveT = !above.isSolidRender(level, blockpos.above());
         boolean belowT = !below.isSolidRender(level, blockpos.below());
         BlockState blockstate = level.getBlockState(blockpos);
         if (Math.random() < 0.02 && blockstate.isSolidRender(level, blockpos) && (nordT || southT || westT || eastT || aboveT || belowT)) {
            for(String str : (List<String>)SConfig.DATAGEN.block_infection.get()) {
               String[] string = str.split("\\|");
               ItemStack stack = new ItemStack((ItemLike)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[0])));
               if (stack != ItemStack.EMPTY && blockstate.getBlock().asItem() == stack.getItem()) {
                  ItemStack itemStack = new ItemStack((ItemLike)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string[1])));
                  if (itemStack != ItemStack.EMPTY) {
                     Item var27 = itemStack.getItem();
                     if (var27 instanceof BlockItem) {
                        BlockItem blockItem = (BlockItem)var27;
                        level.setBlock(blockpos, blockItem.getBlock().defaultBlockState(), 3);
                     }
                  }
               }
            }
         }

         if (above.isAir() && blockstate.isSolidRender(level, blockpos) && Math.random() < 0.1) {
            level.setBlock(blockpos.above(), ((Block)Sblocks.MYCELIUM_VEINS.get()).defaultBlockState(), 3);
         }

         BlockEntity blockEntity = this.level().getBlockEntity(blockpos);
         if (blockstate.is((Block)Sblocks.REMAINS.get())) {
            Mound mound = new Mound((EntityType)Sentities.MOUND.get(), level);
            mound.setMaxAge(this.getAgeM());
            mound.tickEmerging();
            mound.setPos((double)blockpos.getX() + (double)0.5F, (double)blockpos.getY(), (double)blockpos.getZ() + (double)0.5F);
            level.addFreshEntity(mound);
            level.removeBlock(blockpos, false);
            this.discard();
         } else {
            if (blockEntity instanceof Container) {
               Container container = (Container)blockEntity;
               if (this.isChestWithFood(container)) {
                  this.eatTheFood(container);
                  Mound mound = new Mound((EntityType)Sentities.MOUND.get(), level);
                  mound.setMaxAge(1);
                  mound.tickEmerging();
                  mound.setPos((double)blockpos.getX() + (double)0.5F, (double)(blockpos.getY() + 1), (double)blockpos.getZ() + (double)0.5F);
                  level.addFreshEntity(mound);
                  level.removeBlock(blockpos.above(), false);
                  this.discard();
                  continue;
               }
            }

            if (!blockstate.is((Block)Sblocks.HIVE_SPAWN.get()) && !blockstate.is((Block)Sblocks.BIOMASS_LUMP.get())) {
               if (blockstate.is(Blocks.SPAWNER)) {
                  level.setBlock(blockpos, ((Block)Sblocks.OVERGROWN_SPAWNER.get()).defaultBlockState(), 2);
                  this.discard();
               }
            } else if (blockEntity instanceof LivingStructureBlocks) {
               LivingStructureBlocks structureBlocks = (LivingStructureBlocks)blockEntity;
               structureBlocks.setKills(structureBlocks.getKills() + (Integer)SConfig.SERVER.mound_tendril_feed.get());
               this.discard();
            }
         }
      }

   }

   public boolean addEffect(MobEffectInstance p_182397_, @Nullable Entity p_182398_) {
      return false;
   }

   private boolean isChestWithFood(Container container) {
      return container instanceof ContainerBlockEntity ? false : container.hasAnyMatching(ItemStack::isEdible);
   }

   private void eatTheFood(Container container) {
      for(int i = 0; i < container.getContainerSize(); ++i) {
         ItemStack stack = container.getItem(i);
         if (stack.isEdible()) {
            stack.setCount(0);
         }
      }

   }

   static {
      SEARCH_AREA = SynchedEntityData.defineId(InfectionTendril.class, EntityDataSerializers.BLOCK_POS);
      LIFE = SynchedEntityData.defineId(InfectionTendril.class, EntityDataSerializers.INT);
      MOUND_AGE = SynchedEntityData.defineId(InfectionTendril.class, EntityDataSerializers.INT);
   }

   static class GoToArea extends Goal {
      InfectionTendril tendril;
      public int tryTicks;

      public GoToArea(InfectionTendril t) {
         this.tendril = t;
      }

      public boolean canUse() {
         return this.tendril.getSearchArea() != null;
      }

      protected void moveMobToBlock() {
         this.tendril.getNavigation().moveTo((double)((float)this.tendril.getSearchArea().getX()) + (double)0.5F, (double)(this.tendril.getSearchArea().getY() + 1), (double)((float)this.tendril.getSearchArea().getZ()) + (double)0.5F, (double)1.0F);
      }

      public void start() {
         this.moveMobToBlock();
         this.tryTicks = 0;
         super.start();
      }

      public void tick() {
         super.tick();
         ++this.tryTicks;
         if (this.tendril.getSearchArea() != null && this.shouldRecalculatePath()) {
            this.tendril.getNavigation().moveTo((double)this.tendril.getSearchArea().getX(), (double)this.tendril.getSearchArea().getY(), (double)this.tendril.getSearchArea().getZ(), (double)1.0F);
         }

      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 40 == 0;
      }
   }
}
