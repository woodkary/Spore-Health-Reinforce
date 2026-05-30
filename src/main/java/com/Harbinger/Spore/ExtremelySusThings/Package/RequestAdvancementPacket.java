package com.Harbinger.Spore.ExtremelySusThings.Package;

import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import java.util.function.Supplier;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class RequestAdvancementPacket {
   private final String advancementId;
   private final int id;

   public RequestAdvancementPacket(String advancementId, int id) {
      this.advancementId = advancementId;
      this.id = id;
   }

   public RequestAdvancementPacket(FriendlyByteBuf buffer) {
      this.advancementId = buffer.readUtf();
      this.id = buffer.readInt();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeUtf(this.advancementId);
      buffer.writeInt(this.id);
   }

   public static void handle(RequestAdvancementPacket message, Supplier context) {
      ((NetworkEvent.Context)context.get()).enqueueWork(() -> {
         NetworkEvent.Context ctx = (NetworkEvent.Context)context.get();
         ServerPlayer player = ctx.getSender();
         if (player != null) {
            Entity truePlayer = player.level().getEntity(message.id);
            if (truePlayer instanceof ServerPlayer) {
               ServerPlayer playerValue = (ServerPlayer)truePlayer;
               MinecraftServer server = playerValue.server;
               Advancement advancement = server.getAdvancements().getAdvancement(new ResourceLocation(message.advancementId));
               if (advancement == null) {
                  return;
               }

               boolean hasAdvancement = playerValue.getAdvancements().getOrStartProgress(advancement).isDone();
               SporePacketHandler.sendToClient(new SyncAdvancementPacket(message.advancementId, hasAdvancement), playerValue);
            }

         }
      });
      ((NetworkEvent.Context)context.get()).setPacketHandled(true);
   }
}
