package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.CalamityPartsHandeling;
import com.Harbinger.Spore.Sentities.Utility.CorpseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CorpseRenderer extends EntityRenderer<CorpseEntity> {
   public CalamityPartsHandeling.Part partToRender;

   public CorpseRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(CorpseEntity t) {
      return this.partToRender == null ? new ResourceLocation("") : (t.getOwnerAda() ? this.partToRender.adapted_location() : this.partToRender.location());
   }

   public void render(CorpseEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource source, int light) {
      if (this.partToRender == null || this.partToRender.id() != entity.getCorpseType()) {
         this.partToRender = CalamityPartsHandeling.getPart(entity.getCorpseType());
         if (this.partToRender == null) {
            return;
         }
      }

      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
      stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
      stack.pushPose();
      float scaleValue = entity.getInflation();
      stack.scale(scaleValue, scaleValue, scaleValue);
      stack.mulPose(Axis.XP.rotationDegrees(180.0F + this.partToRender.xRot()));
      stack.mulPose(Axis.YP.rotationDegrees(this.partToRender.yRot()));
      stack.mulPose(Axis.ZP.rotationDegrees(this.partToRender.zRot()));
      stack.translate(-this.partToRender.z(), -this.partToRender.y(), -this.partToRender.x());
      this.renderPart(entity, stack, source, light);
      stack.popPose();
      stack.popPose();
   }

   private void renderPart(CorpseEntity entity, PoseStack stack, MultiBufferSource source, int light) {
      int color = entity.getColor();
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));
      VertexConsumer translucent = source.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));

      for(ModelPart part : this.partToRender.parts()) {
         part.render(stack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         if (color != 0) {
            part.render(stack, translucent, light, OverlayTexture.NO_OVERLAY, r, g, b, 0.5F);
         }
      }

   }
}
