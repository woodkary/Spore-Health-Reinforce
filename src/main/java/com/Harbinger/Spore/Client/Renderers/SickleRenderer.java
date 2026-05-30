package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.SickleModel;
import com.Harbinger.Spore.Sentities.Projectile.ThrownSickle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SickleRenderer extends EntityRenderer<ThrownSickle> {
   private static final ResourceLocation SICKLE_TEXTURE = new ResourceLocation("spore", "textures/entity/infected_sickle.png");
   private static final ResourceLocation SPINE_TEXTURE = new ResourceLocation("spore", "textures/entity/spine.png");
   private final SickleModel model;

   public SickleRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.model = new SickleModel(context.bakeLayer(SickleModel.LAYER_LOCATION));
   }

   public void render(ThrownSickle sickle, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
      int color = sickle.getColor();
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      poseStack.pushPose();
      poseStack.translate(0.0F, -1.0F, 0.0F);
      poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, sickle.yRotO, sickle.getYRot())));
      poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, sickle.xRotO, sickle.getXRot())));
      poseStack.scale(1.8F, 1.8F, 1.8F);
      VertexConsumer sickleConsumer = ItemRenderer.getFoilBufferDirect(bufferSource, this.model.renderType(this.getTextureLocation(sickle)), false, sickle.isFoil());
      this.model.renderToBuffer(poseStack, sickleConsumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
      poseStack.popPose();
      Entity owner = sickle.getOwner();
      if (owner != null) {
         this.renderConnection(sickle, owner, poseStack, bufferSource, partialTicks, r, g, b);
      }

      super.render(sickle, entityYaw, partialTicks, poseStack, bufferSource, light);
   }

   public ResourceLocation getTextureLocation(ThrownSickle sickle) {
      return SICKLE_TEXTURE;
   }

   private void renderConnection(ThrownSickle parent, Entity to, PoseStack stack, MultiBufferSource buffer, float partialTick, float r, float g, float b) {
      Vec3 start = parent.getPosition(partialTick).add(parent.getDeltaMovement().normalize().scale(-0.3));
      Vec3 vec3 = (new Vec3(0.2, 1.35, 0.6)).yRot(-to.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
      Vec3 end = to.getPosition(partialTick).add(vec3.x, vec3.y, vec3.z);
      Vec3 direction = end.subtract(start);
      float length = (float)direction.length();
      length = Math.max(length, 1.5F);
      direction = direction.normalize();
      float yaw = (float)Math.atan2(direction.x, direction.z);
      float pitch = (float)(-Math.asin(direction.y));
      stack.pushPose();
      stack.mulPose(Axis.YP.rotation(yaw));
      stack.mulPose(Axis.XP.rotation(pitch));
      float startWidth = 0.5F;
      float startHeight = 0.5F;
      float endWidth = 0.5F;
      float endHeight = 0.5F;
      VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, RenderType.entityTranslucent(SPINE_TEXTURE), false, parent.isFoil());
      PoseStack.Pose pose = stack.last();
      Matrix4f matrix = pose.pose();
      this.drawTaperedConnection(vertexConsumer, matrix, pose.normal(), startWidth, startHeight, endWidth, endHeight, length, OverlayTexture.NO_OVERLAY, 15728880, r, g, b, 1.0F);
      stack.popPose();
   }

   private void drawTaperedConnection(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normal, float startWidth, float startHeight, float endWidth, float endHeight, float length, int overlay, int lightmap, float red, float green, float blue, float alpha) {
      float hwStart = startWidth / 2.0F;
      float hhStart = startHeight / 2.0F;
      float hwEnd = endWidth / 2.0F;
      float hhEnd = endHeight / 2.0F;
      vertexConsumer.vertex(matrix, -hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
   }
}
