package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Spore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ResetRenderRequestHandler {
    private ResetRenderRequestHandler() {
    }

    private static final SimpleChannel RESET_RENDER_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Spore.MODID, "reset_render")).serverAcceptedVersions((version) -> true).clientAcceptedVersions((version) -> true).networkProtocolVersion(() -> "1.0").simpleChannel();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        RESET_RENDER_CHANNEL.messageBuilder(ResetRenderRequest.class, ChannelIdHandler.getChannelId(), NetworkDirection.PLAY_TO_CLIENT).consumerMainThread(ResetRenderRequest::handle).add();
        registered = true;
    }
    public static void sendToClient(ResetRenderRequest msg) {
        RESET_RENDER_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
