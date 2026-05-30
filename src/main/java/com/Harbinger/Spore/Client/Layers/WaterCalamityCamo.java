package com.Harbinger.Spore.Client.Layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluids;

public class WaterCalamityCamo<T extends LivingEntity> extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<T> {
   public WaterCalamityCamo(RenderLayerParent<T, net.minecraft.client.model.EntityModel<T>> p_117346_) {
      super(p_117346_);
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int value, T type, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
      Entity camera = Minecraft.getInstance().getCameraEntity();
      if (camera != null) {
         if (!camera.isEyeInFluidType(Fluids.WATER.getFluidType()) && type.isEyeInFluidType(Fluids.WATER.getFluidType())) {
            int color = ((Biome)type.level().getBiome(type.getOnPos()).value()).getWaterColor();
            if (!type.isInvisible()) {
               float r = (float)(color >> 16 & 255) / 255.0F;
               float g = (float)(color >> 8 & 255) / 255.0F;
               float b = (float)(color & 255) / 255.0F;
               VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(type)));
               this.getParentModel().renderToBuffer(stack, consumer, value, OverlayTexture.NO_OVERLAY, r, g, b, 0.5F);
            }
         }

      }
   }
}
