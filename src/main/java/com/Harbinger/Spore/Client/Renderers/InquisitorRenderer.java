package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InquisitorModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Hyper.Inquisitor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InquisitorRenderer extends BaseInfectedRenderer<Inquisitor> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inquisitor.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/inquisitor.png");

   public InquisitorRenderer(EntityRendererProvider.Context context) {
      super(context, new InquisitorModel(context.bakeLayer(InquisitorModel.LAYER_LOCATION)), 0.7F);
      this.addLayer(new InquisitorBlood(this));
   }

   public ResourceLocation getTextureLocation(Inquisitor entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }

   static class InquisitorBlood extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Inquisitor> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inquisitor_blood.png");

      public InquisitorBlood(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Inquisitor entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!entity.isInvisible() && entity.getBonusDamage() > 10) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            ((InquisitorModel)this.getParentModel()).renderToBuffer(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         }

      }
   }
}
