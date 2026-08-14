package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class UnmodifiableDataPacketHandler {
    private static final SimpleChannel ENTITY_DATA_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "entity_data")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        ENTITY_DATA_CHANNEL.messageBuilder(UnmodifiableDataPacket.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).encoder(UnmodifiableDataPacket::encode).decoder(UnmodifiableDataPacket::new).consumerMainThread(UnmodifiableDataPacket::handle).add();
        registered = true;
    }
    public static void sendToClient(UnmodifiableDataPacket msg) {
        ENTITY_DATA_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
