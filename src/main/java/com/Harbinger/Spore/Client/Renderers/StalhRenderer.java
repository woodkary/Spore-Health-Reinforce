package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.StahlmorderModel;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Stahlmorder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StalhRenderer extends CalamityRenderer<Stahlmorder> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/stalh.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/stalh.png");

   public StalhRenderer(EntityRendererProvider.Context context) {
      super(context, new StahlmorderModel(), 4.0F);
      this.addLayer(new StalhSwordLight(this));
   }

   public ResourceLocation getTextureLocation(Stahlmorder entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }

   public static class StalhSwordLight extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Stahlmorder> {
      private static final ResourceLocation BLADE = new ResourceLocation("spore", "textures/entity/eyes/stalh_blade.png");

      public StalhSwordLight(RenderLayerParent renderer) {
         super(renderer);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Stahlmorder t, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!t.isInvisible()) {
            float alpha = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.01F);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SporeRenderTypes.glowingTranslucent(BLADE));
            ((StahlmorderModel)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
         }
      }
   }
}
