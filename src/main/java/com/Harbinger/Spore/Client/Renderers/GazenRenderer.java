package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.WaterCalamityCamo;
import com.Harbinger.Spore.Client.Models.GazenbrecherModel;
import com.Harbinger.Spore.Client.Special.CalamityRenderer;
import com.Harbinger.Spore.Sentities.Calamities.Gazenbrecher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GazenRenderer extends CalamityRenderer<Gazenbrecher> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/gazen.png");
   private static final ResourceLocation ADAPTED_TEXTURE = new ResourceLocation("spore", "textures/entity/burned_gazen.png");
   private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/gazen.png");

   public GazenRenderer(EntityRendererProvider.Context context) {
      super(context, new GazenbrecherModel(context.bakeLayer(GazenbrecherModel.LAYER_LOCATION)), 4.0F);
      this.addLayer(new WaterCalamityCamo(this));
   }

   public ResourceLocation getTextureLocation(Gazenbrecher entity) {
      return entity.isAdaptedToFire() ? ADAPTED_TEXTURE : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYE_TEXTURE;
   }
}
