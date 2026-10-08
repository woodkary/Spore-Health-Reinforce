package com.Harbinger.Spore.Client;

import com.Harbinger.Spore.Sentities.Organoids.Proto;
import com.Harbinger.Spore.network.AdaptableHurtFeedbackPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ProtoHurtFeedbackClient {
    private ProtoHurtFeedbackClient() {
    }

    public static void handle(AdaptableHurtFeedbackPacket message) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(message.entityId()) instanceof Proto proto
                && proto.getUUID().equals(message.entityUuid())) {
            proto.applyClientHurtFeedback(message.color(), message.durationTicks());
        }
    }
}
