package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SyncLegalPositionPacketHandler {
    private SyncLegalPositionPacketHandler() {}
    private static final SimpleChannel DESPAWN_PACKET_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "legal_position")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        DESPAWN_PACKET_CHANNEL.messageBuilder(SyncLegalPositionPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(SyncLegalPositionPacket::encode).decoder(SyncLegalPositionPacket::new).consumerMainThread(SyncLegalPositionPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(SyncLegalPositionPacket msg) {
        DESPAWN_PACKET_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
