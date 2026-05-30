package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedVindicatorModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.InfectedVendicator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedVindicatorRenderer extends BaseInfectedRenderer<InfectedVendicator> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inf_vindicator.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/inf_vindicator.png");

   public InfectedVindicatorRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedVindicatorModel(context.bakeLayer(InfectedVindicatorModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(InfectedVendicator entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
