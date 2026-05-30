package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.JagdhundModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Jagdhund;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JagdhundRenderer extends BaseInfectedRenderer<Jagdhund> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/jagdhund.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/jagdhund.png");

   public JagdhundRenderer(EntityRendererProvider.Context context) {
      super(context, new JagdhundModel(context.bakeLayer(JagdhundModel.LAYER_LOCATION)), 0.5F);
   }

   public ResourceLocation getTextureLocation(Jagdhund entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Jagdhund type, float p_115456_, float p_115457_, PoseStack stack, MultiBufferSource bufferSource, int p_115460_) {
      this.shadowRadius = type.isUnderground() ? 0.0F : 0.5F;
      if (type.isBurrowing() || type.isEmerging()) {
         float a = type.getBbHeight() * 2.0F;
         float b = 0.0F;
         if (type.isBurrowing()) {
            b = 0.0F - a / (float)type.getBorrow_tick() * (float)type.getBorrow();
         } else if (type.isEmerging()) {
            b = -0.5F - a + a / (float)type.getEmerge_tick() * (float)type.getEmerge();
         }

         stack.translate((double)0.0F, (double)b, (double)0.0F);
      }

      if (!type.isUnderground() || type.isEmerging() || type.isBurrowing()) {
         super.render(type, p_115456_, p_115457_, stack, bufferSource, p_115460_);
      }

   }

   protected boolean isShaking(Jagdhund type) {
      return super.isShaking(type) || type.isBurrowing() || type.isEmerging();
   }
}
