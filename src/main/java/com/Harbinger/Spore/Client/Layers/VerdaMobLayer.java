package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.verwahrungModel;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class VerdaMobLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Verwa> {
   private final EntityRenderDispatcher entityRenderer;

   public VerdaMobLayer(RenderLayerParent context, EntityRenderDispatcher entityRenderer) {
      super(context);
      this.entityRenderer = entityRenderer;
   }

   public void render(PoseStack stack, MultiBufferSource source, int value3, Verwa type, float p_117353_, float value2, float asf, float p_117356_, float p_117357_, float p_117358_) {
      Entity entity = type.getStoredEntity();
      if (!type.isBurrowing() && entity != null) {
         stack.pushPose();
         stack.mulPose(Axis.YP.rotationDegrees(-type.yBodyRot));
         stack.scale(0.9F, 0.9F, 0.9F);
         EntityRenderer var13 = this.entityRenderer.getRenderer(entity);
         if (var13 instanceof MobRenderer) {
            MobRenderer renderer = (MobRenderer)var13;
            EntityModel model = renderer.getModel();
            ResourceLocation texture = renderer.getTextureLocation(entity);
            VertexConsumer consumer = source.getBuffer(RenderType.entityCutoutNoCull(texture));
            model.prepareMobModel(entity, 0.0F, 0.0F, value2);
            model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            model.renderToBuffer(stack, consumer, value3, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         }

         stack.popPose();
      }

   }
}
