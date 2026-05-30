package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Seffects;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.ExtremelySusThings.Utilities;
import com.Harbinger.Spore.Sentities.Organoids.Umarmer;
import com.Harbinger.Spore.Sentities.Organoids.Usurper;
import com.Harbinger.Spore.Sentities.Utility.ScentEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OutpostWatcherBlockEntity extends BlockEntity implements AnimatedEntity {
   public int ticks;

   public OutpostWatcherBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.OUTPOST_WATCHER.get(), pos, state);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
   }

   public void tick() {
      if (this.ticks <= 720) {
         ++this.ticks;
      } else {
         this.ticks = 0;
      }

   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, OutpostWatcherBlockEntity e) {
      e.tick();
      if (e.getTicks() % 200 == 0 && level instanceof ServerLevel serverLevel) {
         List<ServerPlayer> players = serverLevel.players();
         if (players.isEmpty()) {
            return;
         }

         e.checkForPotentialTargets(level, blockPos);
      }

   }

   public int getTicks() {
      return this.ticks;
   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, OutpostWatcherBlockEntity e) {
      e.tick();
   }

   public void checkForPotentialTargets(Level level, BlockPos blockPos) {
      if (level.getDifficulty() != Difficulty.PEACEFUL) {
         int range = 2 * (Integer)SConfig.DATAGEN.outpost_range.get();
         AABB aabb = AABB.ofSize(new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), (double)range, (double)range, (double)range);
         List<LivingEntity> possibleTargets = level.getEntitiesOfClass(LivingEntity.class, aabb);
         List<ScentEntity> amountofScents = new ArrayList();

         for(LivingEntity entity : possibleTargets) {
            if (entity instanceof ScentEntity) {
               ScentEntity scent = (ScentEntity)entity;
               amountofScents.add(scent);
            }
         }

         for(LivingEntity entity : possibleTargets) {
            if (Utilities.TARGET_SELECTOR.Test(entity) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !entity.hasEffect((MobEffect)Seffects.SYMBIOSIS.get()) && entity.onGround()) {
               if (Math.random() < (double)0.3F && amountofScents.size() <= (Integer)SConfig.SERVER.scent_cap.get()) {
                  this.SummonScent(entity, level, entity.getX(), entity.getY(), entity.getZ());
               }

               if (Math.random() < (double)0.1F && level instanceof ServerLevel) {
                  ServerLevel serverLevel = (ServerLevel)level;
                  this.SummonOrganoids(serverLevel, entity.getX(), entity.getY(), entity.getZ(), Math.random() <= (double)0.5F, blockPos);
               }
            }
         }
      }

   }

   private void SummonScent(LivingEntity target, Level level, double x, double y, double z) {
      AABB aabb = target.getBoundingBox().inflate((double)3.0F);
      List<Entity> entityList = level.getEntities(target, aabb, (entity) -> {
         boolean var10000;
         if (entity instanceof LivingEntity livingEntity) {
            if (Utilities.TARGET_SELECTOR.Test(livingEntity)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      });
      ScentEntity scent = new ScentEntity((EntityType)Sentities.SCENT.get(), level);
      scent.setOvercharged(entityList.size() > 2);
      scent.moveTo(x, y, z);
      level.addFreshEntity(scent);
   }

   private void SummonOrganoids(ServerLevel level, double x, double y, double z, boolean range, BlockPos pos) {
      if (range) {
         Umarmer umarmer = new Umarmer((EntityType)Sentities.UMARMED.get(), level);
         umarmer.moveTo(x, y, z);
         umarmer.teleportTo(x, y, z);
         umarmer.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(pos)), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
         level.addFreshEntity(umarmer);
      } else {
         Usurper usurper = new Usurper((EntityType)Sentities.USURPER.get(), level);
         usurper.moveTo(x, y, z);
         usurper.teleportTo(x, y, z);
         usurper.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(pos)), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
         level.addFreshEntity(usurper);
      }

   }
}
