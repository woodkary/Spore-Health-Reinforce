package com.Harbinger.Spore.ExtremelySusThings.Package;

import com.Harbinger.Spore.Client.MusicManager.SporeMusicPlayer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SongInitializingPacket(int id, boolean val, boolean pro) {
   public SongInitializingPacket(FriendlyByteBuf buffer) {
      this(buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeInt(this.id());
      buffer.writeBoolean(this.val());
      buffer.writeBoolean(this.pro());
   }

   public static void handle(SongInitializingPacket message, Supplier context) {
      ((NetworkEvent.Context)context.get()).enqueueWork(() -> {
         if (((NetworkEvent.Context)context.get()).getDirection().getReceptionSide().isClient()) {
            SporeMusicPlayer.handlePacket(message.pro(), message.id, message.val);
            System.out.print("Song_Package_Sent");
         }

      });
      ((NetworkEvent.Context)context.get()).setPacketHandled(true);
   }
}
