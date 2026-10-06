package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Client.ProtoHurtFeedbackClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** One packet per admitted hit, including repeated colors and fully adapted hits. */
public record ProtoHurtFeedbackPacket(int entityId, UUID entityUuid, ProtoHurtColor color, int durationTicks) {
    public static final int DEFAULT_DURATION_TICKS = 10;

    public ProtoHurtFeedbackPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUUID(), buffer.readEnum(ProtoHurtColor.class), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeUUID(entityUuid);
        buffer.writeEnum(color);
        buffer.writeVarInt(durationTicks);
    }

    // Registered with consumerMainThread; only the client branch resolves the client handler.
    public static void handle(ProtoHurtFeedbackPacket message, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ProtoHurtFeedbackClient.handle(message));
        context.get().setPacketHandled(true);
    }
}
