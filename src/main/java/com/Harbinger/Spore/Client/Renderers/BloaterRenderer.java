package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BloaterModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Bloater;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloaterRenderer extends BaseInfectedRenderer<Bloater> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/bloater.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/bloater.png");

   public BloaterRenderer(EntityRendererProvider.Context context) {
      super(context, new BloaterModel(context.bakeLayer(BloaterModel.LAYER_LOCATION)), 1.0F);
   }

   public ResourceLocation getTextureLocation(Bloater entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
