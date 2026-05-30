package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.GrieferModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Griefer;
import com.Harbinger.Spore.Sentities.Variants.GrieferVariants;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrieferRenderer extends BaseInfectedRenderer<Griefer> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(GrieferVariants.class), (p_114874_) -> {
      p_114874_.put(GrieferVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/griefer.png"));
      p_114874_.put(GrieferVariants.TOXIC, new ResourceLocation("spore", "textures/entity/griefer_toxic.png"));
      p_114874_.put(GrieferVariants.RADIOACTIVE, new ResourceLocation("spore", "textures/entity/griefer_radioactive.png"));
      p_114874_.put(GrieferVariants.BILE, new ResourceLocation("spore", "textures/entity/griefer_bile.png"));
      p_114874_.put(GrieferVariants.NAPALM, new ResourceLocation("spore", "textures/entity/griefer_napalm.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/griefer.png");

   public GrieferRenderer(EntityRendererProvider.Context context) {
      super(context, new GrieferModel(context.bakeLayer(GrieferModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Griefer entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
