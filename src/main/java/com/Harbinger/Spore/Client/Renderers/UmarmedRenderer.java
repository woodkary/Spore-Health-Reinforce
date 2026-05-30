package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.UmarmerModel;
import com.Harbinger.Spore.Sentities.Organoids.Umarmer;
import com.Harbinger.Spore.Sentities.Variants.UmarmerVariants;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UmarmedRenderer extends OrganoidMobRenderer<Umarmer> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(UmarmerVariants.class), (p_114874_) -> {
      p_114874_.put(UmarmerVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/umarmer.png"));
      p_114874_.put(UmarmerVariants.CHARRED, new ResourceLocation("spore", "textures/entity/umarmer_crispy.png"));
      p_114874_.put(UmarmerVariants.BILE, new ResourceLocation("spore", "textures/entity/umarmer_bile.png"));
      p_114874_.put(UmarmerVariants.CORROSIVE, new ResourceLocation("spore", "textures/entity/umarmer_corrosive.png"));
   });

   public UmarmedRenderer(EntityRendererProvider.Context context) {
      super(context, new UmarmerModel(context.bakeLayer(UmarmerModel.LAYER_LOCATION)), 1.2F);
   }

   public ResourceLocation getTextureLocation(Umarmer entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }
}
