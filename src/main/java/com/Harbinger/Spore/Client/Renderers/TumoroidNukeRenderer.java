package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.TumoralNukeModel;
import com.Harbinger.Spore.Sentities.Utility.TumoroidNuke;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TumoroidNukeRenderer extends MobRenderer<TumoroidNuke, EntityModel<TumoroidNuke>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/tumor_bomb.png");

   public TumoroidNukeRenderer(EntityRendererProvider.Context context) {
      super(context, new TumoralNukeModel(context.bakeLayer(TumoralNukeModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(TumoroidNuke entity) {
      return TEXTURE;
   }
}
