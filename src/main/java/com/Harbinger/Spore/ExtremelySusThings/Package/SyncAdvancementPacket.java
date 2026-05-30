package com.Harbinger.Spore.ExtremelySusThings.Package;

import com.Harbinger.Spore.ExtremelySusThings.ClientAdvancementTracker;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncAdvancementPacket {
   private final String advancementId;
   private final boolean hasAdvancement;

   public SyncAdvancementPacket(String advancementId, boolean hasAdvancement) {
      this.advancementId = advancementId;
      this.hasAdvancement = hasAdvancement;
   }

   public SyncAdvancementPacket(FriendlyByteBuf buffer) {
      this.advancementId = buffer.readUtf();
      this.hasAdvancement = buffer.readBoolean();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeUtf(this.advancementId);
      buffer.writeBoolean(this.hasAdvancement);
   }

   public static void handle(SyncAdvancementPacket message, Supplier context) {
      ((NetworkEvent.Context)context.get()).enqueueWork(() -> {
         if (((NetworkEvent.Context)context.get()).getDirection().getReceptionSide().isClient()) {
            ClientAdvancementTracker.setAdvancement(message.advancementId, message.hasAdvancement);
         }

      });
      ((NetworkEvent.Context)context.get()).setPacketHandled(true);
   }
}
