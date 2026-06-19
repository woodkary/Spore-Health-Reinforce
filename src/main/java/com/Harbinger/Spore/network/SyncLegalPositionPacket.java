package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Sentities.BaseEntities.IDieWithDiscardEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SyncLegalPositionPacket {
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    public SyncLegalPositionPacket(int entityId, double x, double y, double z) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public SyncLegalPositionPacket(int entityId, Vec3 legalPosition) {
        this.entityId = entityId;
        this.x = legalPosition.x;
        this.y = legalPosition.y;
        this.z = legalPosition.z;
    }
    public SyncLegalPositionPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(this.entityId);
                if (entity instanceof IDieWithDiscardEntity e) {
                    e.setLegalPosition(x, y, z);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
