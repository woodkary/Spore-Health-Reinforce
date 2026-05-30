package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.KnightModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Knight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnightRenderer extends BaseInfectedRenderer<Knight> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/knight.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/knight.png");

   public KnightRenderer(EntityRendererProvider.Context context) {
      super(context, new KnightModel(context.bakeLayer(KnightModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Knight entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
