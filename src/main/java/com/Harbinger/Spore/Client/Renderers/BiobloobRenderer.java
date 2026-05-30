package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BiobloobModel;
import com.Harbinger.Spore.Client.Special.BaseExperimentRenderer;
import com.Harbinger.Spore.Sentities.Experiments.Biobloob;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiobloobRenderer extends BaseExperimentRenderer<Biobloob> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/biobloob.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/biobloob.png");

   public BiobloobRenderer(EntityRendererProvider.Context context) {
      super(context, new BiobloobModel(context.bakeLayer(BiobloobModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Biobloob entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   protected void scale(Biobloob type, PoseStack stack, float value) {
      float scale = type.getScale();
      stack.scale(scale, scale, scale);
      super.scale(type, stack, value);
   }
}
