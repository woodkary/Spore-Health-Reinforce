package com.Harbinger.Spore.Client.Special;

import com.Harbinger.Spore.Client.Layers.ExperimentDormantLayer;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseExperimentRenderer<T extends Experiment> extends BaseInfectedRenderer<T> {
   public BaseExperimentRenderer(EntityRendererProvider.Context context, EntityModel<T> model, float shadow) {
      super(context, model, shadow);
      this.addLayer(new ExperimentDormantLayer<>(this));
   }

   public abstract ResourceLocation eyeLayerTexture();
}
