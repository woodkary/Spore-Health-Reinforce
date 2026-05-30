package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BrotkatzeModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Hyper.Brot;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrotkatzeRenderer extends BaseInfectedRenderer<Brot> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/brotkatze.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/brotkatze.png");

   public BrotkatzeRenderer(EntityRendererProvider.Context context) {
      super(context, new BrotkatzeModel(context.bakeLayer(BrotkatzeModel.LAYER_LOCATION)), 1.0F);
   }

   public ResourceLocation getTextureLocation(Brot entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
