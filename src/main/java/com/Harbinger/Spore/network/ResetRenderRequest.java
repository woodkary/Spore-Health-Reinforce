package com.Harbinger.Spore.network;

import com.Harbinger.Spore.Core.utils.simpleRemoval.SimpleRemoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ResetRenderRequest {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.level == null) return;
            SimpleRemoveUtil.INSTANCE.resetRenderData(mc.level);
        });
        ctx.get().setPacketHandled(true);
    }
}
