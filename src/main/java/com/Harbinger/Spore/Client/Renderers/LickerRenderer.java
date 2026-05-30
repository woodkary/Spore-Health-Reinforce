package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Models.LickerModel;
import com.Harbinger.Spore.Sentities.FallenMultipart.Licker;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LickerRenderer extends MobRenderer<Licker, EntityModel<Licker>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/gazen.png");
   private static final ResourceLocation BURNED = new ResourceLocation("spore", "textures/entity/burned_gazen.png");

   public LickerRenderer(EntityRendererProvider.Context context) {
      super(context, new LickerModel(context.bakeLayer(LickerModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Licker entity) {
      return entity.getBurned() ? BURNED : TEXTURE;
   }
}
