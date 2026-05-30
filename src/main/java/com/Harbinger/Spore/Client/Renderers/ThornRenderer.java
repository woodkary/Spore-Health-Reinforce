package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.ThornModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Thorn;
import com.Harbinger.Spore.Sentities.Variants.ThornVariants;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThornRenderer extends BaseInfectedRenderer<Thorn> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(ThornVariants.class), (p_114874_) -> {
      p_114874_.put(ThornVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/vervathorn.png"));
      p_114874_.put(ThornVariants.TOXIC, new ResourceLocation("spore", "textures/entity/toxic_vervathorn.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/vervathorn.png");

   public ThornRenderer(EntityRendererProvider.Context context) {
      super(context, new ThornModel(context.bakeLayer(ThornModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Thorn entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
