package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BruteModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Brute;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BruteRenderer extends BaseInfectedRenderer<Brute> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/brute.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/brute.png");

   public BruteRenderer(EntityRendererProvider.Context context) {
      super(context, new BruteModel(context.bakeLayer(BruteModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Brute entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
