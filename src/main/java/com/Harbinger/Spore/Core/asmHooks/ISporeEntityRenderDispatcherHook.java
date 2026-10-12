package com.Harbinger.Spore.Core.asmHooks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

public interface ISporeEntityRenderDispatcherHook {
    void render(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z,
                float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight);
}
