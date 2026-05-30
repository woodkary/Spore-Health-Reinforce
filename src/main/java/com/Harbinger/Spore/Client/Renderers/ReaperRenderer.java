package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ReaperModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Utility.Reaper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReaperRenderer extends BaseInfectedRenderer<Reaper> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/reaper.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/reaper.png");

   public ReaperRenderer(EntityRendererProvider.Context context) {
      super(context, new ReaperModel(context.bakeLayer(ReaperModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Reaper entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
