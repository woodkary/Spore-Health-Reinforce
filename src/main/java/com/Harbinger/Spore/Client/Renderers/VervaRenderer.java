package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.VerdaMobLayer;
import com.Harbinger.Spore.Client.Layers.VervaMembrane;
import com.Harbinger.Spore.Client.Models.verwahrungModel;
import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VervaRenderer extends OrganoidMobRenderer<Verwa> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/verwahrung.png");

   public VervaRenderer(EntityRendererProvider.Context context) {
      super(context, new verwahrungModel(context.bakeLayer(verwahrungModel.LAYER_LOCATION)), 1.0F);
      this.addLayer(new VerdaMobLayer(this, context.getEntityRenderDispatcher()));
      this.addLayer(new VervaMembrane(this));
   }

   public ResourceLocation getTextureLocation(Verwa entity) {
      return TEXTURE;
   }
}
