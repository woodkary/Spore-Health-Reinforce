package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.InfectedHazmatCoat;
import com.Harbinger.Spore.Client.Models.InfectedHazmatModel;
import com.Harbinger.Spore.Client.Models.InfectedHazmatWithTank;
import com.Harbinger.Spore.Sentities.BasicInfected.InfectedHazmat;
import com.Harbinger.Spore.Sentities.Variants.HazmatVariant;
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
public class InfectedHazmatRenderer extends MobRenderer<InfectedHazmat, EntityModel<InfectedHazmat>> {
   private final EntityModel normalHazmat = this.getModel();
   private final EntityModel tankHazmat;
   private final EntityModel coatHazmat;
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(HazmatVariant.class), (p_114874_) -> {
      p_114874_.put(HazmatVariant.DEFAULT, new ResourceLocation("spore", "textures/entity/inf_hazmat.png"));
      p_114874_.put(HazmatVariant.TANK, new ResourceLocation("spore", "textures/entity/inf_hazmat.png"));
      p_114874_.put(HazmatVariant.COAT, new ResourceLocation("spore", "textures/entity/inf_hazmat_coat.png"));
   });

   public InfectedHazmatRenderer(EntityRendererProvider.Context context) {
      super(context, new InfectedHazmatModel(context.bakeLayer(InfectedHazmatModel.LAYER_LOCATION)), 0.5F);
      this.tankHazmat = new InfectedHazmatWithTank(context.bakeLayer(InfectedHazmatWithTank.LAYER_LOCATION));
      this.coatHazmat = new InfectedHazmatCoat(context.bakeLayer(InfectedHazmatCoat.LAYER_LOCATION));
   }

   public ResourceLocation getTextureLocation(InfectedHazmat hazmat) {
      return (ResourceLocation)TEXTURE.get(hazmat.getVariant());
   }

   public void render(InfectedHazmat infectedHazmat, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
      EntityModel<InfectedHazmat> entityModel;
      if (infectedHazmat.getVariant() == HazmatVariant.TANK) {
         entityModel = this.tankHazmat;
      } else if (infectedHazmat.getVariant() == HazmatVariant.COAT) {
         entityModel = this.coatHazmat;
      } else {
         entityModel = this.normalHazmat;
      }

      this.model = entityModel;
      super.render(infectedHazmat, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
   }
}
