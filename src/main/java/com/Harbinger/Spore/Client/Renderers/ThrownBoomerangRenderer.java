package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.TintedBufferSource;
import com.Harbinger.Spore.Sentities.Projectile.ThrownBoomerang;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownBoomerangRenderer extends EntityRenderer<ThrownBoomerang> {
   private final ItemRenderer itemRenderer;

   public ThrownBoomerangRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.itemRenderer = context.getItemRenderer();
   }

   public void render(ThrownBoomerang entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      float ageInTicks = (float)entity.tickCount + partialTicks;
      int color = entity.getColor();
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      poseStack.translate((double)0.0F, 0.15, (double)0.0F);
      poseStack.scale(2.5F, 2.5F, 2.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 20.0F));
      poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      MultiBufferSource wrappedBuffer = new TintedBufferSource(buffer, r, g, b, 1.0F);
      this.itemRenderer.renderStatic(entity.getBoomerang().copy(), ItemDisplayContext.GROUND, packedLight, 0, poseStack, wrappedBuffer, entity.level(), entity.getId());
      poseStack.popPose();
      super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
   }

   public ResourceLocation getTextureLocation(ThrownBoomerang entity) {
      return null;
   }
}
