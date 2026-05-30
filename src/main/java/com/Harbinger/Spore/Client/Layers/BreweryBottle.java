package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreweryBottle extends TranslucentLayer<Brauerei> {
   private static final ResourceLocation SYRINGE = new ResourceLocation("spore", "textures/entity/eyes/brewery_glass.png");

   public BreweryBottle(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public ResourceLocation getTexture(Brauerei type) {
      return SYRINGE;
   }
}
