package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.StalkerModel;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Stalker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class StalkerOuterLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Stalker> {
   private static final ResourceLocation CAMO = new ResourceLocation("spore", "textures/entity/stalker/stalker_layer.png");

   public StalkerOuterLayer(RenderLayerParent p_174490_) {
      super(p_174490_);
   }

   public void render(PoseStack p_116924_, MultiBufferSource p_116925_, int p_116926_, Stalker entity, float p_116928_, float p_116929_, float p_116930_, float p_116931_, float p_116932_, float p_116933_) {
      if (entity.getCamo() != 0) {
         int i = entity.getCamo();
         float r = (float)(i >> 16 & 255) / 255.0F;
         float g = (float)(i >> 8 & 255) / 255.0F;
         float b = (float)(i & 255) / 255.0F;
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.getParentModel(), CAMO, p_116924_, p_116925_, p_116926_, entity, p_116928_, p_116929_, p_116931_, p_116932_, p_116933_, p_116930_, r, g, b);
      }

   }
}
