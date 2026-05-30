package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.WombModel;
import com.Harbinger.Spore.Client.Models.WombModelStageII;
import com.Harbinger.Spore.Client.Models.WombModelStageIII;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Sentities.Organoids.Womb;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomassReconfiguratorRenderer extends OrganoidMobRenderer<Womb> {
   private final EntityModel smallModel = this.getModel();
   private final EntityModel mediumModel;
   private final EntityModel largeModel;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/womb.png");
   private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation("spore", "textures/entity/womb_large.png");

   public BiomassReconfiguratorRenderer(EntityRendererProvider.Context context) {
      super(context, new WombModel(context.bakeLayer(WombModel.LAYER_LOCATION)), 0.5F);
      this.mediumModel = new WombModelStageII(context.bakeLayer(WombModelStageII.LAYER_LOCATION));
      this.largeModel = new WombModelStageIII(context.bakeLayer(WombModelStageIII.LAYER_LOCATION));
   }

   public ResourceLocation getTextureLocation(Womb reformator) {
      return reformator.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 4 ? TEXTURE_LARGE : TEXTURE;
   }

   public void render(Womb type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int value3) {
      if (type.getBiomass() > (Integer)SConfig.SERVER.reconstructor_biomass.get() / 4 && type.getBiomass() < (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         this.model = this.mediumModel;
      } else if (type.getBiomass() >= (Integer)SConfig.SERVER.reconstructor_biomass.get() / 2) {
         this.model = this.largeModel;
      } else {
         this.model = this.smallModel;
      }

      super.render(type, value1, value2, stack, bufferSource, value3);
   }
}
