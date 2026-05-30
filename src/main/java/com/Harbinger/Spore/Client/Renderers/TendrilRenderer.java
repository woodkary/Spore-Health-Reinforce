package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Sentities.Utility.InfectionTendril;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TendrilRenderer extends EntityRenderer<InfectionTendril> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "");

   public TendrilRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(InfectionTendril entity) {
      return TEXTURE;
   }
}
