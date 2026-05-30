package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg1Model;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg2Model;
import com.Harbinger.Spore.Client.Models.HohlfresserSeg3Model;
import com.Harbinger.Spore.Client.Models.adaptedwormsegment1;
import com.Harbinger.Spore.Client.Models.adaptedwormsegment2;
import com.Harbinger.Spore.Client.Models.adaptedwormsegment3;
import com.Harbinger.Spore.Client.Models.adaptedwormtail;
import com.Harbinger.Spore.Client.Models.hohlfresserTailModel;
import com.Harbinger.Spore.Sentities.BaseEntities.HohlMultipart;
import com.Harbinger.Spore.Sentities.Calamities.Hohlfresser;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.Util;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class HohlSegRenderer extends LivingEntityRenderer<HohlMultipart, EntityModel<HohlMultipart>> {
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(HohlMultipart.SegmentVariants.class), (p_114874_) -> {
      p_114874_.put(HohlMultipart.SegmentVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/hohl/hohl_seg1.png"));
      p_114874_.put(HohlMultipart.SegmentVariants.MELEE, new ResourceLocation("spore", "textures/entity/hohl/hohl_seg2.png"));
      p_114874_.put(HohlMultipart.SegmentVariants.ORGAN, new ResourceLocation("spore", "textures/entity/hohl/hohl_seg3.png"));
   });
   public static final Map ADA_TEXTURE = (Map)Util.make(Maps.newEnumMap(HohlMultipart.SegmentVariants.class), (p_114874_) -> {
      p_114874_.put(HohlMultipart.SegmentVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/hohl/adaptedwormsegment1.png"));
      p_114874_.put(HohlMultipart.SegmentVariants.MELEE, new ResourceLocation("spore", "textures/entity/hohl/adaptedwormsegment2.png"));
      p_114874_.put(HohlMultipart.SegmentVariants.ORGAN, new ResourceLocation("spore", "textures/entity/hohl/adaptedwormsegment3.png"));
   });
   private static final ResourceLocation INNARDS = new ResourceLocation("spore", "textures/entity/worm_innards.png");
   private static final ResourceLocation TAIL = new ResourceLocation("spore", "textures/entity/hohl/hohl_seg1.png");
   private static final ResourceLocation ADA_TAIL = new ResourceLocation("spore", "textures/entity/hohl/adaptedwormtail.png");
   private final EntityModel mainSegment = this.getModel();
   private final EntityModel adamainSegment;
   private final EntityModel meleeSegment;
   private final EntityModel adameleeSegment;
   private final EntityModel organSegment;
   private final EntityModel adaorganSegment;
   private final EntityModel tailModel;
   private final EntityModel adatailModel;

   public HohlSegRenderer(EntityRendererProvider.Context context) {
      super(context, new HohlfresserSeg1Model(context.bakeLayer(HohlfresserSeg1Model.LAYER_LOCATION)), 4.0F);
      this.meleeSegment = new HohlfresserSeg2Model(context.bakeLayer(HohlfresserSeg2Model.LAYER_LOCATION));
      this.organSegment = new HohlfresserSeg3Model(context.bakeLayer(HohlfresserSeg3Model.LAYER_LOCATION));
      this.tailModel = new hohlfresserTailModel(context.bakeLayer(hohlfresserTailModel.LAYER_LOCATION));
      this.adamainSegment = new adaptedwormsegment1();
      this.adameleeSegment = new adaptedwormsegment2();
      this.adaorganSegment = new adaptedwormsegment3();
      this.adatailModel = new adaptedwormtail();
      this.addLayer(new HohlColors(this));
      this.addLayer(new HohlEmmisive(this));
   }

   public ResourceLocation getTextureLocation(HohlMultipart entity) {
      return entity.isTail() ? (entity.isAdapted() ? ADA_TAIL : TAIL) : (entity.isAdapted() ? (ResourceLocation)ADA_TEXTURE.get(entity.getSegmentVariant()) : (ResourceLocation)TEXTURE.get(entity.getSegmentVariant()));
   }

   protected void scale(HohlMultipart type, PoseStack stack, float p_115316_) {
      float size = type.getSize();
      stack.scale(size, size, size);
      super.scale(type, stack, p_115316_);
   }

   public EntityModel getSegmentModel(HohlMultipart type) {
      switch (type.getSegmentVariant()) {
         case MELEE -> {
            return type.isAdapted() ? this.adameleeSegment : this.meleeSegment;
         }
         case ORGAN -> {
            return type.isAdapted() ? this.adaorganSegment : this.organSegment;
         }
         default -> {
            return type.isAdapted() ? this.adamainSegment : this.mainSegment;
         }
      }
   }

   public void render(HohlMultipart type, float val1, float val2, PoseStack stack, MultiBufferSource source, int light) {
      this.model = type.isTail() ? (type.isAdapted() ? this.adatailModel : this.tailModel) : this.getSegmentModel(type);
      super.render(type, val1, val2, stack, source, light);
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
   }

   protected boolean shouldShowName(HohlMultipart p_115333_) {
      return false;
   }

   private void renderConnection(HohlMultipart parent, Entity to, PoseStack stack, MultiBufferSource buffer, float partialTick) {
      boolean adapted = parent.isAdapted();
      float var10000;
      if (to instanceof HohlMultipart hohlMultipart) {
         var10000 = hohlMultipart.getSize();
      } else {
         var10000 = to instanceof Hohlfresser ? 1.2F : 0.0F;
      }

      float i = var10000;
      Vec3 start = parent.getPosition(partialTick).add((double)0.0F, (double)parent.getBbHeight() * (adapted ? 0.6 : (double)0.3F) * (double)parent.getSize(), (double)0.0F);
      Vec3 end = to.getPosition(partialTick).add((double)0.0F, (double)to.getBbHeight() * (adapted ? 0.7 : (double)0.45F) * (double)i, (double)0.0F);
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
      stack.translate(vec3.x, vec3.y + (double)(adapted ? 2 : 1), vec3.z);
      stack.mulPose(Axis.YP.rotation(yaw));
      stack.mulPose(Axis.XP.rotation(pitch));
      float inf = adapted ? 0.45F : 0.6F;
      float startWidth = parent.getBbWidth() * inf * parent.getSize();
      float startHeight = parent.getBbHeight() * inf * parent.getSize();
      float endWidth = to.getBbWidth() * inf * i;
      float endHeight = to.getBbHeight() * inf * i;
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

   public class HohlColors extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<HohlMultipart> {
      public HohlColors(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int val, HohlMultipart type, float v, float v1, float v2, float v3, float v4, float v5) {
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

   static class HohlEmmisive extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<HohlMultipart> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hohl/hohl_bile.png");

      public HohlEmmisive(RenderLayerParent renderLayerParent) {
         super(renderLayerParent);
      }

      public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, HohlMultipart entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!entity.isInvisible() && entity.getSegmentVariant() == HohlMultipart.SegmentVariants.ORGAN && !entity.isTail()) {
            float alpha = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);
            VertexConsumer vertexConsumer = buffer.getBuffer(SporeRenderTypes.glowingTranslucent(TEXTURE));
            this.getParentModel().renderToBuffer(matrixStack, vertexConsumer, packedLight, 15728640, 1.0F, 1.0F, 1.0F, alpha);
         }

      }
   }
}
