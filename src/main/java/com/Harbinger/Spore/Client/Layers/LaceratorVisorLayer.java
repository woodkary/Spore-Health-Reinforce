package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Sentities.Experiments.Lacerator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaceratorVisorLayer extends TranslucentLayer<Lacerator> {
   private static final ResourceLocation VISOR = new ResourceLocation("spore", "textures/entity/eyes/lacerator_visor.png");

   public LaceratorVisorLayer(RenderLayerParent p_117346_) {
      super(p_117346_);
   }

   public ResourceLocation getTexture(Lacerator type) {
      return VISOR;
   }
}
