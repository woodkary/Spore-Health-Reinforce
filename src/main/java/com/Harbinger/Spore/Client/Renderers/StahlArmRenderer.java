package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.StahlFallenArmModel;
import com.Harbinger.Spore.Sentities.FallenMultipart.StalhArm;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StahlArmRenderer extends MobRenderer<StalhArm, EntityModel<StalhArm>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/stalh.png");

   public StahlArmRenderer(EntityRendererProvider.Context context) {
      super(context, new StahlFallenArmModel(), 1.5F);
   }

   public ResourceLocation getTextureLocation(StalhArm entity) {
      return TEXTURE;
   }
}
