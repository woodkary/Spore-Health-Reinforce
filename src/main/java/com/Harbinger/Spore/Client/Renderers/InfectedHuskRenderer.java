package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedHuskModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedHusk;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedHuskRenderer extends BaseInfectedRenderer<InfectedHusk> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inf_husk.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/inf_husk.png");

   public InfectedHuskRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedHuskModel(context.bakeLayer(InfectedHuskModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(InfectedHusk entity) {
      return TEXTURE;
   }

   protected void scale(InfectedHusk p_115314_, PoseStack poseStack, float p_115316_) {
      double size = 1.2;
      poseStack.scale((float)size, (float)size, (float)size);
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
