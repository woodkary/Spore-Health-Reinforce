package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * @author karywoodOyo
 */
public class HealthPacketHandler {
    private static final SimpleChannel ENTITY_HEALTH_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "health")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ENTITY_HEALTH_CHANNEL.messageBuilder(HealthDataPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(HealthDataPacket::encode).decoder(HealthDataPacket::decode).consumerMainThread(HealthDataPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(HealthDataPacket msg) {
        if (!Thread.currentThread().getName().contains("Render")) {
            ENTITY_HEALTH_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
        }
    }
}
