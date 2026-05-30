package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.MoundModel;
import com.Harbinger.Spore.Sentities.Organoids.Mound;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MoundRenderer extends OrganoidMobRenderer<Mound> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/mound.png");
   private static final ResourceLocation TEXTURE_LARGE = new ResourceLocation("spore", "textures/entity/mound_large.png");
   private static final ResourceLocation TEXTURE_LINKED = new ResourceLocation("spore", "textures/entity/linked_mounds.png");
   private static final ResourceLocation TEXTURE_LARGE_LINKED = new ResourceLocation("spore", "textures/entity/mound_large_linked.png");

   public MoundRenderer(EntityRendererProvider.Context context) {
      super(context, new MoundModel(context.bakeLayer(MoundModel.LAYER_LOCATION)), 0.5F);
   }

   protected void scale(Mound mound, PoseStack poseStack, float p_115316_) {
      int scale = Math.max(mound.getAge(), 1);
      poseStack.scale((float)scale, (float)scale, (float)scale);
   }

   public ResourceLocation getTextureLocation(Mound entity) {
      if (entity.getAge() >= 3) {
         return entity.getLinked() ? TEXTURE_LARGE_LINKED : TEXTURE_LARGE;
      } else {
         return entity.getLinked() ? TEXTURE_LINKED : TEXTURE;
      }
   }
}
