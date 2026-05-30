package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.hohlfresserHeadModel;
import com.Harbinger.Spore.Client.Models.hohlfresserHeadModelAdapted;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HohlRenderer extends CalamityRenderer<Hohlfresser> {
   private final EntityModel Defmodel = this.getModel();
   private final EntityModel adaptedModel = new hohlfresserHeadModelAdapted();
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hohl_head.png");
   private static final ResourceLocation TEXTURE_ADAPTED = new ResourceLocation("spore", "textures/entity/hohl/hohl_head_adapted.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/hohl_head.png");

   public HohlRenderer(EntityRendererProvider.Context context) {
      super(context, new hohlfresserHeadModel(context.bakeLayer(hohlfresserHeadModel.LAYER_LOCATION)), 4.0F);
   }

   public ResourceLocation getTextureLocation(Hohlfresser entity) {
      return entity.getAdaptation() ? TEXTURE_ADAPTED : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   protected void scale(Hohlfresser livingEntity, PoseStack poseStack, float partialTickTime) {
      float val = livingEntity.getAdaptation() ? 1.75F : 1.0F;
      poseStack.scale(val, val, val);
      super.scale(livingEntity, poseStack, partialTickTime);
   }

   public void render(Hohlfresser type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = type.getAdaptation() ? this.adaptedModel : this.Defmodel;
      super.render(type, value1, value2, stack, bufferSource, light);
   }
}
