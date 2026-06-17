package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class DespawnPacket {
    private final int entityId;
    private final Entity.RemovalReason reason;
    public DespawnPacket(int entityId, Entity.RemovalReason reason) {
        this.entityId = entityId;
        this.reason = reason;
    }
    public DespawnPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.reason = buf.readEnum(Entity.RemovalReason.class);
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeEnum(this.reason);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(this.entityId);
                if (entity != null) {
                    SimpleRemoveUtil.INSTANCE.removeLocal(entity, reason);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
