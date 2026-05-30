package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.SiegerTailModel;
import com.Harbinger.Spore.Sentities.FallenMultipart.SiegerTail;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SiegerTailRenderer extends MobRenderer<SiegerTail, EntityModel<SiegerTail>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/sieger.png");
   private static final ResourceLocation WAR = new ResourceLocation("spore", "textures/entity/war_sieger.png");

   public SiegerTailRenderer(EntityRendererProvider.Context context) {
      super(context, new SiegerTailModel(context.bakeLayer(SiegerTailModel.LAYER_LOCATION)), 1.5F);
   }

   public ResourceLocation getTextureLocation(SiegerTail entity) {
      return entity.getWar() ? WAR : TEXTURE;
   }
}
