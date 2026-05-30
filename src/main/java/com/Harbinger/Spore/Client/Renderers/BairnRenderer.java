package com.Harbinger.Spore.Client.Renderers;

import net.minecraft.client.model.EntityModel;

import com.Harbinger.Spore.Client.Layers.BairnEyeLayer;
import com.Harbinger.Spore.Client.Models.BairnModel;
import com.Harbinger.Spore.Sentities.BasicInfected.Bairn;
import com.Harbinger.Spore.Sentities.Variants.BairnSkins;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BairnRenderer extends MobRenderer<Bairn, EntityModel<Bairn>> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(BairnSkins.class), (p_114874_) -> {
      p_114874_.put(BairnSkins.ZOMBIE, new ResourceLocation("spore", "textures/entity/bairn/bairn_human.png"));
      p_114874_.put(BairnSkins.VILLAGER, new ResourceLocation("spore", "textures/entity/bairn/bairn_villager.png"));
      p_114874_.put(BairnSkins.DROWNED, new ResourceLocation("spore", "textures/entity/bairn/bairn_drowned.png"));
      p_114874_.put(BairnSkins.HUSK, new ResourceLocation("spore", "textures/entity/bairn/bairn_husk.png"));
      p_114874_.put(BairnSkins.ZOMBIE_VILLAGER, new ResourceLocation("spore", "textures/entity/bairn/bairn_zombie_villager.png"));
   });

   public BairnRenderer(EntityRendererProvider.Context context) {
      super(context, new BairnModel(context.bakeLayer(BairnModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new BairnEyeLayer(this));
   }

   public ResourceLocation getTextureLocation(Bairn hazmat) {
      return (ResourceLocation)TEXTURE.get(hazmat.getVariant());
   }
}
