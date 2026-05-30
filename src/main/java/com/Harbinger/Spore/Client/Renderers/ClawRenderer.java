package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.InfEvoClawModel;
import com.Harbinger.Spore.Sentities.Utility.InfEvoClaw;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClawRenderer extends MobRenderer<InfEvoClaw, EntityModel<InfEvoClaw>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/claw.png");

   public ClawRenderer(EntityRendererProvider.Context context) {
      super(context, new InfEvoClawModel(context.bakeLayer(InfEvoClawModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(InfEvoClaw entity) {
      return TEXTURE;
   }
}
