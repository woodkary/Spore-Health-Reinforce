package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.SpecterModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Utility.Specter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpecterRenderer extends BaseInfectedRenderer<Specter> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/specter.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/specter.png");

   public SpecterRenderer(EntityRendererProvider.Context context) {
      super(context, new SpecterModel(context.bakeLayer(SpecterModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Specter entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Specter type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      if (!type.isInvisible()) {
         super.render(type, value1, value2, stack, bufferSource, light);
      }
   }
}
