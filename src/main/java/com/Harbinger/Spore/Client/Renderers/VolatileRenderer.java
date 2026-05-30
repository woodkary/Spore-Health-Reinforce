package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.VolatileModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Volatile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VolatileRenderer extends BaseInfectedRenderer<Volatile> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/volatile.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/volatile.png");

   public VolatileRenderer(EntityRendererProvider.Context context) {
      super(context, new VolatileModel(context.bakeLayer(VolatileModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new VolatileGlowingLayers(this));
   }

   public ResourceLocation getTextureLocation(Volatile entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   static class VolatileGlowingLayers extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Volatile> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/volatile_pulsation.png");

      public VolatileGlowingLayers(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Volatile entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!entity.isInvisible()) {
            float alpha = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);
            VertexConsumer vertexConsumer = buffer.getBuffer(SporeRenderTypes.glowingTranslucent(TEXTURE));
            ((VolatileModel)this.getParentModel()).renderToBuffer(matrixStack, vertexConsumer, packedLight, 15728640, 1.0F, 1.0F, 1.0F, alpha);
         }

      }
   }
}
