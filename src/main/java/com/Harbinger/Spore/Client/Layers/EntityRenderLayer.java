package com.Harbinger.Spore.Client.Layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;

public abstract class EntityRenderLayer<T extends Entity> extends RenderLayer<T, EntityModel<T>> {
   protected EntityRenderLayer(RenderLayerParent<T, EntityModel<T>> p_117346_) {
      super(p_117346_);
   }
}
