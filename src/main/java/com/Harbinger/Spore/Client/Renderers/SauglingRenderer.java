package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.SauglingModel;
import com.Harbinger.Spore.Client.Special.BaseExperimentRenderer;
import com.Harbinger.Spore.Sentities.Experiments.Saugling;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SauglingRenderer extends BaseExperimentRenderer<Saugling> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/saugling.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/saugling.png");

   public SauglingRenderer(EntityRendererProvider.Context context) {
      super(context, new SauglingModel(context.bakeLayer(SauglingModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Saugling entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Saugling type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      if (!type.isHidden()) {
         super.render(type, value1, value2, stack, bufferSource, light);
      }
   }
}
