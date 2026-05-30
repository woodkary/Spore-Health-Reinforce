package com.Harbinger.Spore.SBlockEntities;

import com.Harbinger.Spore.Core.SblockEntities;
import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sentities;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Organoids.HiveTumor;
import com.Harbinger.Spore.Sentities.Utility.ArenaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BrainRemnantBlockEntity extends BlockEntity implements AnimatedEntity {
   public int ticks;
   public int ticksActivation;
   public int ticksOnFire = 0;
   private boolean onFire = false;
   private boolean active = false;

   public BrainRemnantBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SblockEntities.BRAIN_REMNANTS.get(), pos, state);
   }

   public BrainRemnantBlockEntity(BlockPos pos, BlockState state, boolean value, boolean active) {
      super((BlockEntityType)SblockEntities.BRAIN_REMNANTS.get(), pos, state);
      this.setOnFire(value);
      this.setActive(active);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.setOnFire(tag.getBoolean("fire"));
      this.setActive(tag.getBoolean("active"));
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putBoolean("fire", this.isOnFire());
      tag.putBoolean("active", this.isActive());
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean time) {
      this.active = time;
   }

   public boolean isOnFire() {
      return this.onFire;
   }

   public void setOnFire(boolean time) {
      this.onFire = time;
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, BrainRemnantBlockEntity e) {
      tickOnFire(level, pos, state, e);
      if (!level.isClientSide) {
         if (e.ticksActivation <= 1200) {
            ++e.ticksActivation;
         } else {
            e.ticksActivation = 0;
            if (Math.random() < 0.05 && checkForBrains(level, pos) && e.isActive()) {
               summonTumor(level, pos);
            }
         }
      }

   }

   public static boolean checkForBrains(Level level, BlockPos pos) {
      int count = 0;
      int range = 8;

      for(int x = -range; x <= range; ++x) {
         for(int y = -range; y <= range; ++y) {
            for(int z = -range; z <= range; ++z) {
               BlockPos checkPos = pos.offset(x, y, z);
               BlockState state = level.getBlockState(checkPos);
               if (state.is((Block)Sblocks.BRAIN_REMNANTS.get())) {
                  ++count;
                  if (count > 2) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static void summonTumor(Level level, BlockPos pos) {
      HiveTumor hiveTumor = new HiveTumor((EntityType)Sentities.HIVETUMOR.get(), level);
      hiveTumor.moveTo((double)pos.getX(), (double)(pos.getY() + 1), (double)pos.getZ());
      hiveTumor.tickEmerging();
      MinecraftServer server = level.getServer();
      if (server != null) {
         for(ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playNotifySound((SoundEvent)Ssounds.TUMOR_SPAWN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("tumor_summon_message"), false);
         }
      }

      if (level.addFreshEntity(hiveTumor)) {
         deleteNearbyBrains(level, pos, 8);
      }

   }

   public static void deleteNearbyBrains(Level level, BlockPos pos, int range) {
      for(int x = -range; x <= range; ++x) {
         for(int y = -range; y <= range; ++y) {
            for(int z = -range; z <= range; ++z) {
               BlockPos checkPos = pos.offset(x, y, z);
               BlockState state = level.getBlockState(checkPos);
               if (state.is((Block)Sblocks.BRAIN_REMNANTS.get())) {
                  level.destroyBlock(checkPos, false);
               }
            }
         }
      }

   }

   public static void clientTick(Level level, BlockPos pos, BlockState state, BrainRemnantBlockEntity e) {
      if (level.isClientSide) {
         if (e.ticks <= 12000) {
            ++e.ticks;
         } else {
            e.ticks = 0;
         }
      }

   }

   public static void tickOnFire(Level level, BlockPos pos, BlockState state, BrainRemnantBlockEntity entity) {
      if (entity.ticksOnFire > 0 && entity.isOnFire()) {
         ++entity.ticksOnFire;
         if (entity.ticksOnFire >= 50) {
            entity.ticksOnFire = 0;
            level.removeBlock(pos, false);
            level.explode((Entity)null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 2.0F, ExplosionInteraction.NONE);
            ArenaEntity arenaEntity = new ArenaEntity((EntityType)Sentities.ARENA_TENDRIL.get(), level);
            arenaEntity.moveTo((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            arenaEntity.tickEmerging();
            arenaEntity.recalculateHosts();
            level.addFreshEntity(arenaEntity);
         }
      }

   }

   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   public int getTicks() {
      return this.ticks;
   }
}
