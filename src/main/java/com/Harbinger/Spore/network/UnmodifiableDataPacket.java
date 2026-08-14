package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.utils.HeasdalthUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class UnmodifiableDataPacket {
    private final int entityId;

    public UnmodifiableDataPacket(int entityId) {
        this.entityId = entityId;
    }
    public UnmodifiableDataPacket(FriendlyByteBuf buf){
        this.entityId = buf.readInt();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.level == null){
                return;
            }
            Entity entity = mc.level.getEntity(entityId);
            if(entity == null){
                return;
            }
            HeasdalthUtil.INSTANCE.createUnmodifiableEntityDataLocal(entity);
        });
        ctx.get().setPacketHandled(true);
    }
}
