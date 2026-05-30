package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.LaceratorVisorLayer;
import com.Harbinger.Spore.Client.Models.LaceratorModel;
import com.Harbinger.Spore.Client.Special.BaseExperimentRenderer;
import com.Harbinger.Spore.Sentities.Experiments.Lacerator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaceratorRenderer extends BaseExperimentRenderer<Lacerator> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/lacerator.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/lacerator.png");

   public LaceratorRenderer(EntityRendererProvider.Context context) {
      super(context, new LaceratorModel(context.bakeLayer(LaceratorModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new LaceratorVisorLayer(this));
   }

   public ResourceLocation getTextureLocation(Lacerator entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
