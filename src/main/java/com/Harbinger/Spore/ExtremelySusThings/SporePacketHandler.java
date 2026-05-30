package com.Harbinger.Spore.ExtremelySusThings;

import com.Harbinger.Spore.ExtremelySusThings.Package.AdvancementGivingPackage;
import com.Harbinger.Spore.ExtremelySusThings.Package.OpenGraftingScreenPacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.OpenSurgeryScreenPacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.RequestAdvancementPacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.SongInitializingPacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.SporeGunFirePacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.SporeGunFireSyncPacket;
import com.Harbinger.Spore.ExtremelySusThings.Package.SyncAdvancementPacket;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class SporePacketHandler {
   private static final String PROTOCOL_VERSION = "1";
   public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("spore", "main"), () -> "1", "1"::equals, "1"::equals);
   private static final AtomicInteger packetId = new AtomicInteger(0);

   public static void registerPackets() {
      INSTANCE.messageBuilder(RequestAdvancementPacket.class, packetId.getAndIncrement()).encoder(RequestAdvancementPacket::encode).decoder(RequestAdvancementPacket::new).consumerMainThread(RequestAdvancementPacket::handle).add();
      INSTANCE.messageBuilder(SporeGunFirePacket.class, packetId.getAndIncrement()).encoder(SporeGunFirePacket::encode).decoder(SporeGunFirePacket::new).consumerMainThread(SporeGunFirePacket::handle).add();
      INSTANCE.messageBuilder(SyncAdvancementPacket.class, packetId.getAndIncrement()).encoder(SyncAdvancementPacket::encode).decoder(SyncAdvancementPacket::new).consumerMainThread(SyncAdvancementPacket::handle).add();
      INSTANCE.messageBuilder(AdvancementGivingPackage.class, packetId.getAndIncrement()).encoder(AdvancementGivingPackage::encode).decoder(AdvancementGivingPackage::new).consumerMainThread(AdvancementGivingPackage::handle).add();
      INSTANCE.messageBuilder(OpenGraftingScreenPacket.class, packetId.getAndIncrement()).encoder(OpenGraftingScreenPacket::encode).decoder(OpenGraftingScreenPacket::new).consumerMainThread(OpenGraftingScreenPacket::handle).add();
      INSTANCE.messageBuilder(OpenSurgeryScreenPacket.class, packetId.getAndIncrement()).encoder(OpenSurgeryScreenPacket::encode).decoder(OpenSurgeryScreenPacket::new).consumerMainThread(OpenSurgeryScreenPacket::handle).add();
      INSTANCE.messageBuilder(SporeGunFireSyncPacket.class, packetId.getAndIncrement()).encoder(SporeGunFireSyncPacket::encode).decoder(SporeGunFireSyncPacket::new).consumerMainThread(SporeGunFireSyncPacket::handle).add();
      INSTANCE.messageBuilder(SongInitializingPacket.class, packetId.getAndIncrement()).encoder(SongInitializingPacket::encode).decoder(SongInitializingPacket::new).consumerMainThread(SongInitializingPacket::handle).add();
   }

   public static void sendToServer(Object packet) {
      INSTANCE.sendToServer(packet);
   }

   public static void sendToClient(Object packet, ServerPlayer player) {
      INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
   }
}
