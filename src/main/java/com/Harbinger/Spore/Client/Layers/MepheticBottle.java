package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Sentities.EvolvedInfected.Mephetic;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MepheticBottle extends TranslucentLayer<Mephetic> {
   private static final ResourceLocation SYRINGE = new ResourceLocation("spore", "textures/entity/mephitic_glass.png");

   public MepheticBottle(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public ResourceLocation getTexture(Mephetic type) {
      return SYRINGE;
   }
}
