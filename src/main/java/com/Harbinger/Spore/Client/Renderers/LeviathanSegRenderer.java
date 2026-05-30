package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.WaterCalamityCamo;
import com.Harbinger.Spore.Client.Models.LeviathanMiddleSegment;
import com.Harbinger.Spore.Client.Models.LeviathanTailModel;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.FootSegLevi;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg1;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg2;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg3;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg4;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg5;
import com.Harbinger.Spore.Client.Models.KrakenTentacles.Seg6;
import com.Harbinger.Spore.Sentities.BaseEntities.LeviathanMultipart;
import com.Harbinger.Spore.Sentities.BaseEntities.IkUtil.IkLeviLeg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class LeviathanSegRenderer extends LivingEntityRenderer<LeviathanMultipart, EntityModel<LeviathanMultipart>> {
   private final EntityModel<LeviathanMultipart> mainModel = this.getModel();
   private final EntityModel<LeviathanMultipart> middleSeg = new LeviathanMiddleSegment();
   private final Seg1 tentacleSegmentModel1 = new Seg1();
   private final Seg2 tentacleSegmentModel2 = new Seg2();
   private final Seg3 tentacleSegmentModel3 = new Seg3();
   private final Seg4 tentacleSegmentModel4 = new Seg4();
   private final Seg5 tentacleSegmentModel5 = new Seg5();
   private final Seg6 tentacleSegmentModel6 = new Seg6();
   private final FootSegLevi foot = new FootSegLevi();
   private static final ResourceLocation INNARDS = new ResourceLocation("spore", "textures/entity/leviathan_insides.png");
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/leviathan_tail.png");
   private static final ResourceLocation TENTACLES = new ResourceLocation("spore", "textures/entity/kraken/kraken_t1.png");

   public LeviathanSegRenderer(EntityRendererProvider.Context context) {
      super(context, new LeviathanTailModel(), 4.0F);
      this.addLayer(new HohlColors(this));
      this.addLayer(new WaterCalamityCamo(this));
   }

   public EntityModel<Entity> getTentacleModel(int i) {
      EntityModel<Entity> var10000;
      switch (i) {
         case 1 -> var10000 = this.tentacleSegmentModel2;
         case 2 -> var10000 = this.tentacleSegmentModel3;
         case 3 -> var10000 = this.tentacleSegmentModel4;
         case 4 -> var10000 = this.tentacleSegmentModel5;
         case 5 -> var10000 = this.tentacleSegmentModel6;
         default -> var10000 = this.tentacleSegmentModel1;
      }

      return var10000;
   }

   public ResourceLocation getTextureLocation(LeviathanMultipart entity) {
      return TEXTURE;
   }

   public void render(LeviathanMultipart type, float val1, float val2, PoseStack stack, MultiBufferSource source, int light) {
      this.model = type.isTail() ? this.mainModel : this.middleSeg;
      Vec3 direction = null;
      ClientLevel level = Minecraft.getInstance().level;
      int i = type.getParentIntId();
      if (level != null && i != -1 && !type.isInvisible()) {
         Entity parent = level.getEntity(i);
         if (parent != null) {
            this.renderConnection(type, parent, stack, source, val2);
            direction = parent.getPosition(val2).subtract(type.getPosition(val2));
            direction = direction.normalize();
         }
      }

      stack.pushPose();
      if (direction != null) {
         float pitch = (float)(-Math.asin(direction.y));
         stack.mulPose(Axis.XP.rotation(pitch));
      }

      super.render(type, val1, val2, stack, source, light);
      stack.popPose();
      Vec3 entityPos = type.getPosition(val2);
      stack.pushPose();
      stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
      if (!type.isInvisible()) {
         for(IkLeviLeg leg : type.getLegs()) {
            this.renderTentacle(stack, type, light, source, leg.getEntities(), leg.getSegmentVar(), type, val2);
         }
      }

      stack.popPose();
   }

   protected void scale(LeviathanMultipart livingEntity, PoseStack poseStack, float partialTickTime) {
      float size = livingEntity.isTail() ? 1.0F : 1.3F;
      poseStack.scale(size, size, size);
      super.scale(livingEntity, poseStack, partialTickTime);
   }

   protected boolean shouldShowName(LeviathanMultipart p_115333_) {
      return false;
   }

   private void renderConnection(LeviathanMultipart parent, Entity to, PoseStack stack, MultiBufferSource buffer, float partialTick) {
      boolean adapted = parent.isAdapted();
      Vec3 start = parent.getPosition(partialTick).add((double)0.0F, (double)(parent.getBbHeight() * 0.3F), (double)0.0F);
      Vec3 end = to.getPosition(partialTick).add((double)0.0F, (double)to.getBbHeight() * (adapted ? 0.7 : (double)0.45F), (double)0.0F);
      Vec3 direction = end.subtract(start);
      float length = (float)direction.length();
      direction = direction.normalize();
      float yaw = (float)Math.atan2(direction.x, direction.z);
      float pitch = (float)(-Math.asin(direction.y));
      int color = parent.getColor();
      float r;
      float g;
      float b;
      if (color == 0) {
         r = 1.0F;
         g = 1.0F;
         b = 1.0F;
      } else {
         r = (float)(color >> 16 & 255) / 255.0F;
         g = (float)(color >> 8 & 255) / 255.0F;
         b = (float)(color & 255) / 255.0F;
      }

      stack.pushPose();
      Vec3 vec3 = parent.position().subtract(parent.position()).scale((double)-1.0F);
      stack.translate(vec3.x, vec3.y + (double)1.0F, vec3.z);
      stack.mulPose(Axis.YP.rotation(yaw));
      stack.mulPose(Axis.XP.rotation(pitch));
      float inf = 0.3F;
      float startWidth = parent.getBbWidth() * inf;
      float startHeight = parent.getBbHeight() * inf;
      float endWidth = to.getBbWidth() * inf;
      float endHeight = to.getBbHeight() * inf;
      VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(INNARDS));
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
      vertexConsumer.vertex(matrix, -hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, -hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, -hhStart, 0.0F).color(red, green, blue, alpha).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwStart, hhStart, 0.0F).color(red, green, blue, alpha).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, hhEnd, length).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
      vertexConsumer.vertex(matrix, hwEnd, -hhEnd, length).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(lightmap).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
   }

   private void renderTentacle(PoseStack stack, LeviathanMultipart type, int light, MultiBufferSource buffer, Vec3[] segments, int[] var, LivingEntity parent, float partial) {
      if (segments != null && segments.length >= 2) {
         float hurtTime = (float)parent.hurtTime - partial;
         float flashIntensity = 0.0F;
         int mutationColor = type.getColor() == 0 ? -1 : type.getColor();
         Vec3 origin = null;
         if (hurtTime > 0.0F) {
            flashIntensity = Math.min(hurtTime / 10.0F, 1.0F);
         }

         float baseR = (float)(mutationColor >> 16 & 255) / 255.0F;
         float baseG = (float)(mutationColor >> 8 & 255) / 255.0F;
         float baseB = (float)(mutationColor & 255) / 255.0F;
         float flash = flashIntensity * 0.5F;
         float g = Mth.lerp(flash, baseG, 0.2F);
         float b = Mth.lerp(flash, baseB, 0.2F);

         for(int i = 0; i < segments.length; ++i) {
            Vec3 currentPos = segments[i];
            this.renderConnection(origin, currentPos, type, light, stack, buffer, i, var[i], partial, baseR, g, b, i == segments.length - 1);
            origin = currentPos;
         }

      }
   }

   private void renderConnection(Vec3 from, Vec3 to, LeviathanMultipart parent, int light, PoseStack stack, MultiBufferSource buffer, int index, int var, float partial, float r, float g, float b, boolean last) {
      if (from != null && to != null) {
         Vec3 direction = to.subtract(from);
         float length = (float)direction.length();
         if (!(length < 1.0E-4F)) {
            direction = direction.normalize();
            float yaw = (float)Math.atan2(direction.x, direction.z);
            float pitch = (float)(-Math.asin(direction.y));
            float size = index % 2 == 0 ? 1.2F : 1.0F;
            stack.pushPose();
            stack.translate(from.x, from.y, from.z);
            stack.mulPose(Axis.YP.rotation(yaw));
            stack.mulPose(Axis.XP.rotation(pitch));
            stack.pushPose();
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TENTACLES));
            EntityModel<Entity> typeEntityModel = last ? this.foot : this.getTentacleModel(var);
            stack.mulPose(Axis.XP.rotationDegrees(90.0F));
            stack.translate(0.0F, -length / 2.0F, 0.0F);
            stack.scale(size, length * 1.05F, size);
            typeEntityModel.setupAnim(parent, 0.0F, 0.0F, (float)parent.tickCount + partial, 0.0F, 0.0F);
            typeEntityModel.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            stack.popPose();
            stack.popPose();
         }
      }
   }

   public class HohlColors extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<LeviathanMultipart> {
      public HohlColors(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int val, LeviathanMultipart type, float v, float v1, float v2, float v3, float v4, float v5) {
         if (!type.isInvisible()) {
            if (type.getColor() == 0) {
               return;
            }

            int i = type.getColor();
            float r = (float)(i >> 16 & 255) / 255.0F;
            float g = (float)(i >> 8 & 255) / 255.0F;
            float b = (float)(i & 255) / 255.0F;
            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(type)));
            this.getParentModel().renderToBuffer(poseStack, consumer, val, OverlayTexture.NO_OVERLAY, r, g, b, 0.5F);
         }

      }
   }
}
