package com.Harbinger.Spore.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.kary.phayriosisrebornmore.Phayriosisrebornmore;

/**
 * @author karywoodOyo
 */
public class HealthPacketHandler {
    private static final SimpleChannel ENTITY_HEALTH_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Phayriosisrebornmore.MODID, "health")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    public static void register() {
        ENTITY_HEALTH_CHANNEL.messageBuilder(HealthDataPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(HealthDataPacket::encode).decoder(HealthDataPacket::decode).consumerMainThread(HealthDataPacket::handle).add();
    }
    public static <MSG> void sendToClient(HealthDataPacket msg) {
        if (!Thread.currentThread().getName().contains("Render")) {
            ENTITY_HEALTH_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
        }
    }
}
