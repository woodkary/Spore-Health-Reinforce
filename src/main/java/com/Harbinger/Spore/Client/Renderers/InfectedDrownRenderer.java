package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedDrownModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedDrowned;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedDrownRenderer extends BaseInfectedRenderer<InfectedDrowned> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inf_drowned.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/infected.png");

   public InfectedDrownRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedDrownModel(context.bakeLayer(InfectedDrownModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(InfectedDrowned entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
