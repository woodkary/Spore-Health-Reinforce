package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class HealthDeltaPacketHandler {
    private static final SimpleChannel ENTITY_HEALTH_DELTA_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "healthdelta")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ENTITY_HEALTH_DELTA_CHANNEL.messageBuilder(HealthDeltaPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(HealthDeltaPacket::encode).decoder(HealthDeltaPacket::new).consumerMainThread(HealthDeltaPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(HealthDeltaPacket msg) {
        ENTITY_HEALTH_DELTA_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
