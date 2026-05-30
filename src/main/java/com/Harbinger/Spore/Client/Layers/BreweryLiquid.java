package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.BraureiModel;
import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreweryLiquid extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Brauerei> {
   private static final ResourceLocation SYRINGE = new ResourceLocation("spore", "textures/entity/eyes/brewery_potion.png");

   public BreweryLiquid(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int value, Brauerei type, float v1, float v2, float v3, float v4, float v5, float v6) {
      if (!type.isInvisible() && type.getColor() != 0) {
         int i = type.getColor();
         float r = (float)(i >> 16 & 255) / 255.0F;
         float g = (float)(i >> 8 & 255) / 255.0F;
         float b = (float)(i & 255) / 255.0F;
         VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(SYRINGE));
         ((BraureiModel)this.getParentModel()).prepareMobModel(type, v1, v2, v3);
         ((BraureiModel)this.getParentModel()).setupAnim(type, v1, v2, v4, v5, v6);
         ((BraureiModel)this.getParentModel()).renderToBuffer(stack, vertexconsumer, value, LivingEntityRenderer.getOverlayCoords(type, 0.0F), r, g, b, 1.0F);
      }

   }
}
