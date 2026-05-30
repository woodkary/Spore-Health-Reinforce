package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.DelusionerEnchanterModel;
import com.Harbinger.Spore.Client.Models.DelusionerModel;
import com.Harbinger.Spore.Sentities.Organoids.Delusionare;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DelusionareRenderer extends OrganoidMobRenderer<Delusionare> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel mage;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/delusioner.png");
   private static final ResourceLocation TEXTURE_MAGE = new ResourceLocation("spore", "textures/entity/delusioner_mage.png");

   public DelusionareRenderer(EntityRendererProvider.Context context) {
      super(context, new DelusionerModel(context.bakeLayer(DelusionerModel.LAYER_LOCATION)), 1.0F);
      this.mage = new DelusionerEnchanterModel(context.bakeLayer(DelusionerEnchanterModel.LAYER_LOCATION));
   }

   public ResourceLocation getTextureLocation(Delusionare entity) {
      return entity.getTypeVariant() == 0 ? TEXTURE : TEXTURE_MAGE;
   }

   public void render(Delusionare type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int value3) {
      this.model = type.getTypeVariant() == 0 ? this.defaultModel : this.mage;
      super.render(type, value1, value2, stack, bufferSource, value3);
   }
}
