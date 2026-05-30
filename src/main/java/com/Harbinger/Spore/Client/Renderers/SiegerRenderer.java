package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SiegerArrowLayer;
import com.Harbinger.Spore.Client.Layers.SiegerHatLayer;
import com.Harbinger.Spore.Client.Models.SiegerModel;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Sieger;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SiegerRenderer extends CalamityRenderer<Sieger> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/sieger.png");
   private static final ResourceLocation WAR = new ResourceLocation("spore", "textures/entity/war_sieger.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/sieger.png");
   private static final ResourceLocation VEINS = new ResourceLocation("spore", "textures/entity/calamity_veins/sieger.png");

   public SiegerRenderer(EntityRendererProvider.Context context) {
      super(context, new SiegerModel(context.bakeLayer(SiegerModel.LAYER_LOCATION)), 4.0F);
      this.addLayer(new SiegerHatLayer(this, context.getModelSet()));
      this.addLayer(new SiegerArrowLayer(this, context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Sieger entity) {
      return entity.isAdapted() ? WAR : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
