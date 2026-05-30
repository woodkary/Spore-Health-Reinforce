package com.Harbinger.Spore.Client.Layers;

import com.Harbinger.Spore.Client.Models.SantaModel;
import com.Harbinger.Spore.Client.Models.SiegerModel;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.Calamities.Sieger;
import com.mojang.blaze3d.vertex.PoseStack;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SiegerHatLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Sieger> {
   private static final ResourceLocation HAT_LOCATION = new ResourceLocation("spore", "textures/entity/santa_hat.png");
   private final SantaModel model;

   public SiegerHatLayer(RenderLayerParent p_117346_, EntityModelSet set) {
      super(p_117346_);
      this.model = new SantaModel(set.bakeLayer(SantaModel.LAYER_LOCATION));
   }

   public void render(PoseStack stack, MultiBufferSource bufferSource, int p_117351_, Sieger type, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
      if ((Boolean)SConfig.SERVER.costumes.get()) {
         LocalDate localdate = LocalDate.now();
         int j = localdate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 12 || (Boolean)SConfig.SERVER.costumes_active.get()) {
            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, HAT_LOCATION, stack, bufferSource, p_117351_, type, p_117353_, p_117354_, p_117355_, p_117356_, p_117357_, p_117358_, 1.0F, 1.0F, 1.0F);
         }
      }

   }
}
