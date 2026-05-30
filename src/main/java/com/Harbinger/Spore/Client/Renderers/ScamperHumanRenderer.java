package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.DrownedScamperModel;
import com.Harbinger.Spore.Client.Models.ScamperModel;
import com.Harbinger.Spore.Client.Models.ScamperVillagerModel;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Variants.ScamperVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScamperHumanRenderer extends MobRenderer<Scamper, EntityModel<Scamper>> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel villagerModel;
   private final EntityModel drownedModel;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(ScamperVariants.class), (p_114874_) -> {
      p_114874_.put(ScamperVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/scamper.png"));
      p_114874_.put(ScamperVariants.VILLAGER, new ResourceLocation("spore", "textures/entity/villager_scamper.png"));
      p_114874_.put(ScamperVariants.DROWNED, new ResourceLocation("spore", "textures/entity/drowned_scamper.png"));
   });

   public ScamperHumanRenderer(EntityRendererProvider.Context context) {
      super(context, new ScamperModel(context.bakeLayer(ScamperModel.LAYER_LOCATION)), 0.5F);
      this.villagerModel = new ScamperVillagerModel(context.bakeLayer(ScamperVillagerModel.LAYER_LOCATION));
      this.drownedModel = new DrownedScamperModel(context.bakeLayer(DrownedScamperModel.LAYER_LOCATION));
   }

   public ResourceLocation getTextureLocation(Scamper scamper) {
      return (ResourceLocation)TEXTURE.get(scamper.getVariant());
   }

   public void render(Scamper scamper, float value1, float value2, PoseStack stack, MultiBufferSource source, int value3) {
      if (scamper.getVariant() == ScamperVariants.VILLAGER) {
         this.model = this.villagerModel;
      } else if (scamper.getVariant() == ScamperVariants.DROWNED) {
         this.model = this.drownedModel;
      } else {
         this.model = this.defaultModel;
      }

      super.render(scamper, value1, value2, stack, source, value3);
   }
}
