package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.GastgeberModel;
import com.Harbinger.Spore.Sentities.Utility.GastGeber;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GastGaverRenderer extends MobRenderer<GastGeber, EntityModel<GastGeber>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/gast.png");

   public GastGaverRenderer(EntityRendererProvider.Context context) {
      super(context, new GastgeberModel(context.bakeLayer(GastgeberModel.LAYER_LOCATION)), 0.6F);
   }

   public ResourceLocation getTextureLocation(GastGeber entity) {
      return TEXTURE;
   }
}
