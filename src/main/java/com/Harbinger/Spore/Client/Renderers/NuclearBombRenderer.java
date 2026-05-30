package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.NukeParts.BombFunnelModel;
import com.Harbinger.Spore.Client.Models.NukeParts.FireDiskModel;
import com.Harbinger.Spore.Client.Models.NukeParts.MushroomExplosionTop;
import com.Harbinger.Spore.Sentities.Utility.NukeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class NuclearBombRenderer extends EntityRenderer<NukeEntity> {
   private static final ResourceLocation TOP_TEXTURE = new ResourceLocation("spore", "textures/entity/bomb/top.png");
   private static final ResourceLocation FUNNEL_TEXTURE = new ResourceLocation("spore", "textures/entity/bomb/funnel.png");
   private static final ResourceLocation RING_TEXTURE = new ResourceLocation("spore", "textures/entity/bomb/fire_disk.png");
   private final BombFunnelModel funnelModel = new BombFunnelModel();
   private final FireDiskModel fireDiskModel = new FireDiskModel();
   private final MushroomExplosionTop mushroomExplosionTop = new MushroomExplosionTop();

   public NuclearBombRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(NukeEntity t) {
      return null;
   }

   public void renderTop(NukeEntity bomb, PoseStack stack, MultiBufferSource bufferSource) {
      stack.pushPose();
      stack.scale(0.95F, 1.05F, 0.95F);
      VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.eyes(TOP_TEXTURE));
      this.mushroomExplosionTop.renderToBuffer(stack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
   }

   public void renderRing(NukeEntity bomb, float ticks, PoseStack stack, MultiBufferSource bufferSource) {
      stack.pushPose();
      VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.eyes(RING_TEXTURE));
      this.fireDiskModel.renderToBuffer(stack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      this.fireDiskModel.setupAnim(bomb, 0.0F, 0.0F, ticks, 0.0F, 0.0F);
      stack.popPose();
   }

   public void renderFire(NukeEntity bomb, float ticks, PoseStack stack, MultiBufferSource bufferSource) {
      stack.pushPose();
      float uOffset = ticks * 0.01F % 1.0F;
      float vOffset = ticks * 0.01F * 2.0F % 1.0F;
      RenderType renderType = SporeRenderTypes.energySwirlStatic(FUNNEL_TEXTURE, uOffset, vOffset);
      VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
      this.funnelModel.renderToBuffer(stack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
   }

   public void renderTopFire(NukeEntity bomb, float ticks, PoseStack stack, MultiBufferSource bufferSource) {
      stack.pushPose();
      float uOffset = ticks * 0.01F % 1.0F;
      float vOffset = ticks * 0.01F * 2.0F % 1.0F;
      RenderType renderType = SporeRenderTypes.energySwirlStatic(FUNNEL_TEXTURE, -uOffset, -vOffset);
      VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
      this.mushroomExplosionTop.renderToBuffer(stack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      stack.popPose();
   }

   public void render(NukeEntity bomb, float entityYaw, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int value) {
      float ticks = (float)bomb.tickCount + partialTick;
      stack.pushPose();
      float scale = this.inflate(bomb);
      float yOffset = 1.5F * (1.0F / scale);
      stack.translate(0.0F, yOffset, 0.0F);
      stack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
      stack.translate((double)0.0F, -((double)bomb.getInitRange() / 1.2), (double)0.0F);
      stack.scale(scale, scale, scale);
      this.renderTopFire(bomb, ticks, stack, bufferSource);
      this.renderFire(bomb, ticks, stack, bufferSource);
      this.renderTop(bomb, stack, bufferSource);
      this.renderRing(bomb, ticks, stack, bufferSource);
      stack.popPose();
      super.render(bomb, entityYaw, partialTick, stack, bufferSource, value);
   }

   public float inflate(NukeEntity bomb) {
      return Math.max(bomb.getInitRange(), 1.0F);
   }
}
