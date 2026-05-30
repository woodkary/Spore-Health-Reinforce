package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.DetasheHyperClaw;
import com.Harbinger.Spore.Sentities.Utility.HyperClaw;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HyperClawRenderer extends MobRenderer<HyperClaw, EntityModel<HyperClaw>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hyper_evoker.png");

   public HyperClawRenderer(EntityRendererProvider.Context context) {
      super(context, new DetasheHyperClaw(context.bakeLayer(DetasheHyperClaw.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(HyperClaw entity) {
      return TEXTURE;
   }
}
