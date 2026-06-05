package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Protector;
import com.Harbinger.Spore.Sentities.Organoids.Proto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class SporeSavedData extends SavedData {
   public static final Map<String, ChunkLoadRequest> activeRequests = new HashMap<>();
   public static final String NAME = "spore_world_data";
   private static final String CASING_LIGHT_ALLOWED = "CasingLightAllowed";
   private static final List protectorList = new ArrayList();
   private static final List protos = new ArrayList();
   private boolean casingLightAllowed;

   public static void addProtector(Protector protector) {
      protectorList.add(protector);
   }

   public static void removeProtector(Protector protector) {
      protectorList.remove(protector);
   }

   public static List protectorList() {
      return protectorList;
   }

   public static void addProto(Proto protector) {
      protos.add(protector);
   }

   public static void removeProto(Proto protector) {
      protos.remove(protector);
   }

   public static List getHiveminds() {
      return protos;
   }

   public int getAmountOfHiveminds() {
      return protos.size();
   }

   public static SporeSavedData getDataLocation(ServerLevel level) {
      return (SporeSavedData)level.getDataStorage().get(SporeSavedData::load, "spore_world_data");
   }

   public static SporeSavedData get(ServerLevel level) {
      return (SporeSavedData)level.getDataStorage().computeIfAbsent(SporeSavedData::load, SporeSavedData::new, "spore_world_data");
   }

   public boolean isCasingLightAllowed() {
      return this.casingLightAllowed;
   }

   public void setCasingLightAllowed(boolean casingLightAllowed) {
      if (this.casingLightAllowed != casingLightAllowed) {
         this.casingLightAllowed = casingLightAllowed;
         this.setDirty();
      }
   }

   public void addRequest(ChunkLoadRequest request) {
      activeRequests.put(request.getRequestID(), request);
      this.setDirty();
   }

   public void removeRequest(String id) {
      activeRequests.remove(id);
      this.setDirty();
   }

   public Collection<ChunkLoadRequest> getRequests() {
      return activeRequests.values();
   }

   public static SporeSavedData load(CompoundTag tag) {
      SporeSavedData data = new SporeSavedData();
      if (tag.contains("ChunkRequests", 9)) {
         ListTag list = tag.getList("ChunkRequests", 10);

         for(int i = 0; i < list.size(); ++i) {
            CompoundTag entry = list.getCompound(i);
            ChunkLoadRequest request = ChunkLoadRequest.deserializeNBT(entry);
            activeRequests.put(request.getRequestID(), request);
         }
      }

      data.casingLightAllowed = tag.getBoolean(CASING_LIGHT_ALLOWED);

      return data;
   }

   public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
      ListTag listTag = new ListTag();

      for(ChunkLoadRequest request : activeRequests.values()) {
         listTag.add(request.serializeNBT());
      }

      tag.put("ChunkRequests", listTag);
      tag.putBoolean(CASING_LIGHT_ALLOWED, this.casingLightAllowed);
      return tag;
   }
}
