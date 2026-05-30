package com.Harbinger.Spore.ExtremelySusThings.Package;

import java.util.function.Supplier;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class AdvancementGivingPackage {
   private final String advancement;
   private final int id;

   public AdvancementGivingPackage(String advancement, int id) {
      this.advancement = advancement;
      this.id = id;
   }

   public AdvancementGivingPackage(FriendlyByteBuf buffer) {
      this.advancement = buffer.readUtf();
      this.id = buffer.readInt();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeUtf(this.advancement);
      buffer.writeInt(this.id);
   }

   public static void handle(AdvancementGivingPackage message, Supplier context) {
      ((NetworkEvent.Context)context.get()).enqueueWork(() -> {
         ServerPlayer entity = ((NetworkEvent.Context)context.get()).getSender();
         if (entity != null) {
            Entity truePlayer = entity.level().getEntity(message.id);
            if (truePlayer instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)truePlayer;
               MinecraftServer server = player.server;
               Advancement advancement = server.getAdvancements().getAdvancement(new ResourceLocation(message.advancement));
               if (advancement != null) {
                  AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                  if (!progress.isDone()) {
                     for(String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                     }

                  }
               }
            }
         }
      });
      ((NetworkEvent.Context)context.get()).setPacketHandled(true);
   }
}
