package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.StalkerOuterLayer;
import com.Harbinger.Spore.Client.Models.StalkerModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Stalker;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StalkerRenderer extends BaseInfectedRenderer<Stalker> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/stalker/stalker.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/stalker.png");

   public StalkerRenderer(EntityRendererProvider.Context context) {
      super(context, new StalkerModel(context.bakeLayer(StalkerModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new StalkerOuterLayer(this));
   }

   public ResourceLocation getTextureLocation(Stalker entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
