package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ScavengerModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scavenger;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScavengerRenderer extends BaseInfectedRenderer<Scavenger> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/scavenger.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/scavenger.png");

   public ScavengerRenderer(EntityRendererProvider.Context context) {
      super(context, new ScavengerModel(context.bakeLayer(ScavengerModel.LAYER_LOCATION)), 1.0F);
   }

   public ResourceLocation getTextureLocation(Scavenger entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
