package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.BreweryBottle;
import com.Harbinger.Spore.Client.Layers.BreweryLiquid;
import com.Harbinger.Spore.Client.Models.BraureiModel;
import com.Harbinger.Spore.Sentities.Organoids.Brauerei;
import com.Harbinger.Spore.Sentities.Variants.BraureiVariants;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BraureiRenderer extends OrganoidMobRenderer<Brauerei> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(BraureiVariants.class), (p_114874_) -> {
      p_114874_.put(BraureiVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/brewery.png"));
      p_114874_.put(BraureiVariants.HAZARD, new ResourceLocation("spore", "textures/entity/hazard_brewery.png"));
   });

   public BraureiRenderer(EntityRendererProvider.Context context) {
      super(context, new BraureiModel(context.bakeLayer(BraureiModel.LAYER_LOCATION)), 3.0F);
      this.addLayer(new BreweryLiquid(this));
      this.addLayer(new BreweryBottle(this));
   }

   public ResourceLocation getTextureLocation(Brauerei entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }
}
