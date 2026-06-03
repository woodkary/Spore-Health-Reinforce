package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class WrapperPacketHandler {
    private static final SimpleChannel WRAPPER_PACKET_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "wrapper")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        WRAPPER_PACKET_CHANNEL.messageBuilder(WrapperPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(WrapperPacket::encode).decoder(WrapperPacket::new).consumerMainThread(WrapperPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(WrapperPacket msg) {
        WRAPPER_PACKET_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
