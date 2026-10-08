package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Client.ProtoHurtFeedbackClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** One packet per admitted hit, including repeated colors and fully adapted hits. */
public record AdaptableHurtFeedbackPacket(int entityId, UUID entityUuid, AdaptableHurtColor color, int durationTicks) {
    public static final int DEFAULT_DURATION_TICKS = 10;

    public AdaptableHurtFeedbackPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUUID(), buffer.readEnum(AdaptableHurtColor.class), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeUUID(entityUuid);
        buffer.writeEnum(color);
        buffer.writeVarInt(durationTicks);
    }

    // Registered with consumerMainThread; only the client branch resolves the client handler.
    public static void handle(AdaptableHurtFeedbackPacket message, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ProtoHurtFeedbackClient.handle(message));
        context.get().setPacketHandled(true);
    }
}
