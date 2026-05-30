package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.HindenburgLightsLayer;
import com.Harbinger.Spore.Client.Models.HindieModel;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Hinderburg;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HindieRenderer extends CalamityRenderer<Hinderburg> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hindie.png");
   private static final ResourceLocation OVERCLOCKED = new ResourceLocation("spore", "textures/entity/hindie_adapted.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/hindie.png");

   public HindieRenderer(EntityRendererProvider.Context context) {
      super(context, new HindieModel(context.bakeLayer(HindieModel.LAYER_LOCATION)), 4.0F);
      this.addLayer(new HindenburgLightsLayer(this));
   }

   public ResourceLocation getTextureLocation(Hinderburg entity) {
      return entity.isAdapted() ? OVERCLOCKED : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }
}
