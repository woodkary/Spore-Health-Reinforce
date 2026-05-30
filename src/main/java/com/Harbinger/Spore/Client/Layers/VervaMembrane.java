package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VervaMembrane extends TranslucentLayer<Verwa> {
   private static final ResourceLocation SYRINGE = new ResourceLocation("spore", "textures/entity/eyes/verwa_membrane.png");

   public VervaMembrane(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public ResourceLocation getTexture(Verwa type) {
      return SYRINGE;
   }
}
