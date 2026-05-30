package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedWandererModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedWanderingTrader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedWandererRenderer extends BaseInfectedRenderer<InfectedWanderingTrader> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/inf_wanderer.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/in_villager.png");

   public InfectedWandererRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedWandererModel(context.bakeLayer(InfectedWandererModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(InfectedWanderingTrader entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
