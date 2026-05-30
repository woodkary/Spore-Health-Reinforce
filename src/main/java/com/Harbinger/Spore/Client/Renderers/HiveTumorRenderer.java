package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.HiveTumorMembraneLayer;
import com.Harbinger.Spore.Client.Models.HivetumorModel;
import com.Harbinger.Spore.Sentities.Organoids.HiveTumor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HiveTumorRenderer extends OrganoidMobRenderer<HiveTumor> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hivetumor.png");

   public HiveTumorRenderer(EntityRendererProvider.Context context) {
      super(context, new HivetumorModel(context.bakeLayer(HivetumorModel.LAYER_LOCATION)), 1.5F);
      this.addLayer(new HiveTumorMembraneLayer(this));
   }

   protected boolean isShaking(HiveTumor type) {
      return super.isShaking(type) || type.isScared();
   }

   public ResourceLocation getTextureLocation(HiveTumor proto) {
      return TEXTURE;
   }
}
