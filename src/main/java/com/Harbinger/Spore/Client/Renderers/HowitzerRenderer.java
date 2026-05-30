package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.HowitzerEmissiveLayer;
import com.Harbinger.Spore.Client.Layers.HowitzerLightsLayer;
import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.HowitzerModel;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Howitzer;
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
public class HowitzerRenderer extends CalamityRenderer<Howitzer> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/howitzer.png");
   private static final ResourceLocation RADIATION = new ResourceLocation("spore", "textures/entity/nuclear_howitzer.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/howitzer_eyes.png");

   public HowitzerRenderer(EntityRendererProvider.Context context) {
      super(context, new HowitzerModel(), 4.0F);
      this.addLayer(new HowitzerNeonGreenLayer(this));
      this.addLayer(new HowitzerEmissiveLayer(this));
      this.addLayer(new HowitzerLightsLayer(this));
   }

   public ResourceLocation getTextureLocation(Howitzer entity) {
      return entity.isRadioactive() ? RADIATION : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }

   static class HowitzerNeonGreenLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Howitzer> {
      public HowitzerNeonGreenLayer(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Howitzer t, float v, float v1, float v2, float ageInTicks, float v4, float v5) {
         int ticksBoom = t.getSelfDetonation();
         if (!t.isInvisible() && t.isRadioactive() && ticksBoom > 0) {
            float omega = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);
            float alpha = 0.2F + omega / (float)(30 - ticksBoom);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SporeRenderTypes.glowingTranslucent(HowitzerRenderer.RADIATION));
            ((HowitzerModel)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, i, 15728640, 0.0F, 1.0F, 0.0F, alpha);
         }

      }
   }
}
