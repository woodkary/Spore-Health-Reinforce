package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class DespawnPacketHandler {
    private DespawnPacketHandler() {}
    private static final SimpleChannel DESPAWN_PACKET_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "remove")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        DESPAWN_PACKET_CHANNEL.messageBuilder(DespawnPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(DespawnPacket::encode).decoder(DespawnPacket::new).consumerMainThread(DespawnPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(DespawnPacket msg) {
        DESPAWN_PACKET_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
