package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.asmHooks.EntityHeealuthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HealthDeltaPacket {
    int entityId;
    float delta;

    public HealthDeltaPacket(int entityId, float delta) {
        this.entityId = entityId;
        this.delta = delta;
    }
    public HealthDeltaPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.delta = buf.readFloat();
    }
    public static void encode(HealthDeltaPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.delta);
    }
    // ✅ handle：循环更新客户端实体
    public static void handle(HealthDeltaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(msg.entityId);
                if(entity instanceof LivingEntity liv) {
                    EntityHeealuthManager.INSTANCE.setHeealtthDeltaLocal(liv, msg.delta);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
