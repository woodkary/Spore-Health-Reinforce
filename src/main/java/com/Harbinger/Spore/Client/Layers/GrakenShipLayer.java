package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.GrakensenkerModel;
import com.Harbinger.Spore.Client.Models.GrakensenkerShipModel;
import com.Harbinger.Spore.Sentities.Calamities.Grakensenker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GrakenShipLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Grakensenker> {
   private static final ResourceLocation SHIP = new ResourceLocation("spore", "textures/entity/graken_ship.png");
   private static final ResourceLocation SHIP_EYE = new ResourceLocation("spore", "textures/entity/eyes/graken_ship.png");
   private final GrakensenkerShipModel model = new GrakensenkerShipModel();

   public GrakenShipLayer(RenderLayerParent layerParent) {
      super(layerParent);
   }

   public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Grakensenker t, float v, float v1, float v2, float v3, float v4, float v5) {
      if (t.getAdaptation()) {
         ((GrakensenkerModel)this.getParentModel()).copyPropertiesTo(this.model);
         this.model.prepareMobModel(t, v, v1, v5);
         this.model.setupAnim(t, v, v1, v2 + (float)t.tickCount, v3, v4);
         renderColoredCutoutModel(this.model, SHIP, poseStack, multiBufferSource, i, t, 1.0F, 1.0F, 1.0F);
         VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.eyes(SHIP_EYE));
         this.model.renderToBuffer(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
