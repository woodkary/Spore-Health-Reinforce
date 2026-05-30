package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedHuman;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedHumanRenderer extends BaseInfectedRenderer<InfectedHuman> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/infected.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/infected.png");

   public InfectedHumanRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedModel(context.bakeLayer(InfectedModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(InfectedHuman entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
