package com.Harbinger.Spore.ExtremelySusThings;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ChunkLoadRequest {
   private ChunkPos[] chunkPositionsToLoad;
   private int priority;
   private final String requestID;
   private final long tickAmount;
   private long ticksUntilExpiration;
   private final UUID ownerUUID;
   private final ResourceKey dimension;

   public ChunkLoadRequest(ResourceKey dimension, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration, UUID ownerUUID) {
      this.chunkPositionsToLoad = chunkPositionsToLoad;
      this.priority = priority;
      this.requestID = requestID;
      this.tickAmount = ticksUntilExpiration;
      this.ticksUntilExpiration = ticksUntilExpiration;
      this.ownerUUID = ownerUUID;
      this.dimension = dimension;
   }

   public ChunkPos[] getChunkPositionsToLoad() {
      return this.chunkPositionsToLoad;
   }

   public int getPriority() {
      return this.priority;
   }

   public long getTickAmount() {
      return this.tickAmount;
   }

   public ServerLevel getDimension() {
      return ServerLifecycleHooks.getCurrentServer().getLevel(this.dimension);
   }

   public String getRequestID() {
      return this.requestID;
   }

   public boolean isRequestID(String requestID) {
      return Objects.equals(this.requestID, requestID);
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public boolean isExpired() {
      return this.ticksUntilExpiration <= 0L;
   }

   public void decrementTicksUntilExpiration(int amountToDecrement) {
      this.ticksUntilExpiration -= (long)amountToDecrement;
   }

   public long getTicksUntilExpiration() {
      return this.ticksUntilExpiration;
   }

   private void setTicksUntilExpiration(long value) {
      this.ticksUntilExpiration = value;
   }

   public boolean isHigherPriorityThan(ChunkLoadRequest other) {
      return this.priority < other.priority;
   }

   public boolean doesContainChunk(ChunkPos chunkPos) {
      for(ChunkPos pos : this.chunkPositionsToLoad) {
         if (pos.equals(chunkPos)) {
            return true;
         }
      }

      return false;
   }

   public boolean isOwnerStillPresentInChunk() {
      if (this.ownerUUID == null) {
         return false;
      } else {
         ServerLevel level = this.getDimension();
         if (level == null) {
            return false;
         } else {
            Entity entity = level.getEntity(this.ownerUUID);
            if (entity != null && entity.isAlive()) {
               ChunkPos entityPos = new ChunkPos(entity.blockPosition());

               for(ChunkPos pos : this.chunkPositionsToLoad) {
                  if (pos.equals(entityPos)) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         }
      }
   }

   public boolean refreshIfOwnerStillPresent(long newDurationTicks) {
      if (this.isOwnerStillPresentInChunk()) {
         this.ticksUntilExpiration = newDurationTicks;
         return true;
      } else {
         return false;
      }
   }

   public void removeChunk(ChunkPos chunkPos) {
      if (this.doesContainChunk(chunkPos)) {
         ChunkPos[] newChunkPositionsToLoad = new ChunkPos[this.chunkPositionsToLoad.length - 1];
         int index = 0;

         for(ChunkPos pos : this.chunkPositionsToLoad) {
            if (!pos.equals(chunkPos)) {
               newChunkPositionsToLoad[index++] = pos;
            }
         }

         this.chunkPositionsToLoad = newChunkPositionsToLoad;
      }
   }

   public void addChunk(ChunkPos chunkPos) {
      if (!this.doesContainChunk(chunkPos)) {
         ChunkPos[] newChunkPositionsToLoad = new ChunkPos[this.chunkPositionsToLoad.length + 1];
         System.arraycopy(this.chunkPositionsToLoad, 0, newChunkPositionsToLoad, 0, this.chunkPositionsToLoad.length);
         newChunkPositionsToLoad[this.chunkPositionsToLoad.length] = chunkPos;
         this.chunkPositionsToLoad = newChunkPositionsToLoad;
      }
   }

   public void setChunkPositionsToLoad(ChunkPos[] chunkPositionsToLoad) {
      this.chunkPositionsToLoad = chunkPositionsToLoad;
   }

   public void setPriority(int priority) {
      this.priority = priority;
   }

   public CompoundTag serializeNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putString("RequestID", this.requestID);
      tag.putInt("Priority", this.priority);
      tag.putLong("TicksUntilExpiration", this.ticksUntilExpiration);
      tag.putLong("StartTickValue", this.tickAmount);
      if (this.ownerUUID != null) {
         tag.putUUID("OwnerUUID", this.ownerUUID);
      }

      tag.putString("Dimension", this.dimension.location().toString());
      ListTag chunks = new ListTag();

      for(ChunkPos pos : this.chunkPositionsToLoad) {
         CompoundTag c = new CompoundTag();
         c.putInt("X", pos.x);
         c.putInt("Z", pos.z);
         chunks.add(c);
      }

      tag.put("Chunks", chunks);
      return tag;
   }

   public static ChunkLoadRequest deserializeNBT(CompoundTag tag) {
      String requestID = tag.getString("RequestID");
      int priority = tag.getInt("Priority");
      long ticksUntilExpiration = tag.getLong("TicksUntilExpiration");
      long startTicks = tag.getLong("StartTickValue");
      UUID ownerUUID = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
      ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("Dimension")));
      ListTag chunksList = tag.getList("Chunks", 10);
      ChunkPos[] positions = new ChunkPos[chunksList.size()];

      for(int i = 0; i < chunksList.size(); ++i) {
         CompoundTag c = chunksList.getCompound(i);
         positions[i] = new ChunkPos(c.getInt("X"), c.getInt("Z"));
      }

      ChunkLoadRequest request = new ChunkLoadRequest(dimKey, positions, priority, requestID, startTicks, ownerUUID);
      request.setTicksUntilExpiration(ticksUntilExpiration);
      return request;
   }
}
