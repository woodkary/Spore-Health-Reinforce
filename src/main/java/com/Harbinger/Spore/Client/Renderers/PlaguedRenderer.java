package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.PlaguedSiringeLayer;
import com.Harbinger.Spore.Client.Models.PlaguedModel;
import com.Harbinger.Spore.Client.Special.BaseExperimentRenderer;
import com.Harbinger.Spore.Sentities.Experiments.Plagued;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlaguedRenderer extends BaseExperimentRenderer<Plagued> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/plagued.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/plagued.png");

   public PlaguedRenderer(EntityRendererProvider.Context context) {
      super(context, new PlaguedModel(context.bakeLayer(PlaguedModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new PlaguedSiringeLayer(this));
   }

   public ResourceLocation getTextureLocation(Plagued entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   protected boolean isShaking(Plagued type) {
      return true;
   }
}
