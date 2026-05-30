package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Sentities.Experiments.Plagued;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlaguedSiringeLayer extends TranslucentLayer<Plagued> {
   private static final ResourceLocation SYRINGE = new ResourceLocation("spore", "textures/entity/plagued_syringe.png");

   public PlaguedSiringeLayer(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public ResourceLocation getTexture(Plagued type) {
      return SYRINGE;
   }
}
