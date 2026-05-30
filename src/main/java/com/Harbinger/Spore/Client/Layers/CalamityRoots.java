package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.RootsModel;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CalamityRoots<T extends Calamity> extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<T> {
   private static final ResourceLocation LOCATION = new ResourceLocation("spore", "textures/entity/roots.png");
   private final RootsModel model;

   public CalamityRoots(RenderLayerParent<T, net.minecraft.client.model.EntityModel<T>> p_117346_, EntityModelSet set) {
      super(p_117346_);
      this.model = new RootsModel(set.bakeLayer(RootsModel.LAYER_LOCATION));
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int value, T type, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
      if (type.isRooted()) {
         stack.pushPose();
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LOCATION));
         this.model.renderToBuffer(stack, consumer, value, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         stack.popPose();
      }

   }
}
