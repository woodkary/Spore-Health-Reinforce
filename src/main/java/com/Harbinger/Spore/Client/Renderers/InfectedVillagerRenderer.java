package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedVillagerModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedVillager;
import com.Harbinger.Spore.Sentities.Variants.InfVillagerSkins;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InfectedVillagerRenderer extends BaseInfectedRenderer<InfectedVillager> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(InfVillagerSkins.class), (p_114874_) -> {
      p_114874_.put(InfVillagerSkins.DEFAULT, new ResourceLocation("spore", "textures/entity/villager/in_villager.png"));
      p_114874_.put(InfVillagerSkins.DESERT, new ResourceLocation("spore", "textures/entity/villager/inf_villager_desert.png"));
      p_114874_.put(InfVillagerSkins.JUNGLE, new ResourceLocation("spore", "textures/entity/villager/inf_villager_jungle.png"));
      p_114874_.put(InfVillagerSkins.SAVANNA, new ResourceLocation("spore", "textures/entity/villager/inf_villager_savanna.png"));
      p_114874_.put(InfVillagerSkins.SWAMP, new ResourceLocation("spore", "textures/entity/villager/inf_villager_swamp.png"));
      p_114874_.put(InfVillagerSkins.TAIGA, new ResourceLocation("spore", "textures/entity/villager/inf_villager_taiga.png"));
      p_114874_.put(InfVillagerSkins.TUNDRA, new ResourceLocation("spore", "textures/entity/villager/inf_villager_tundra.png"));
   });
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/in_villager.png");

   public InfectedVillagerRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedVillagerModel(context.bakeLayer(InfectedVillagerModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(InfectedVillager entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }
}
