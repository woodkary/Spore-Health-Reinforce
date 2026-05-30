package com.Harbinger.Spore.ExtremelySusThings.Package;

import com.Harbinger.Spore.ExtremelySusThings.ClientUtils;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record SporeGunFireSyncPacket(int playerId, int hand) {
   public SporeGunFireSyncPacket(FriendlyByteBuf buffer) {
      this(buffer.readInt(), buffer.readInt());
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeInt(this.playerId);
      buffer.writeInt(this.hand);
   }

   public static void handle(SporeGunFireSyncPacket message, Supplier context) {
      ((NetworkEvent.Context)context.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientUtils.handleClient(message)));
      ((NetworkEvent.Context)context.get()).setPacketHandled(true);
   }
}
