package com.Harbinger.Spore.Client;

import com.Harbinger.Spore.Sentities.BaseEntities.DamageAdaptableEntity;
import com.Harbinger.Spore.network.AdaptableHurtFeedbackPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class AdaptableHurtFeedbackClient {
    private AdaptableHurtFeedbackClient() {
    }

    public static void handle(AdaptableHurtFeedbackPacket message) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(message.entityId()) instanceof DamageAdaptableEntity adaptable
                && adaptable.getAdaptableUUID().equals(message.entityUuid())) {
            adaptable.applyClientHurtFeedback(message.color(), message.durationTicks());
        }
    }
}
