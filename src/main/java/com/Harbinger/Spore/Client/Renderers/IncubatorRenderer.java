package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.IncubatorModel;
import com.Harbinger.Spore.Client.Special.BaseBlockEntityRenderer;
import com.Harbinger.Spore.SBlockEntities.IncubatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class IncubatorRenderer extends BaseBlockEntityRenderer<IncubatorBlockEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/block/incubator/incubator.png");
   private static final ResourceLocation GLASS = new ResourceLocation("spore", "textures/block/incubator/incubator_glass.png");
   private static final ResourceLocation BUTTONS = new ResourceLocation("spore", "textures/block/incubator/incubator_buttons.png");

   public IncubatorRenderer() {
      super(new IncubatorModel());
   }

   public ResourceLocation getTexture(IncubatorBlockEntity block) {
      return TEXTURE;
   }

   public void render(@NotNull IncubatorBlockEntity blockEntity, float partialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      super.render(blockEntity, partialTicks, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
      if (this.unRenderBlock(blockEntity)) {
         if (blockEntity.getItem(0) != ItemStack.EMPTY && blockEntity.getLevel() != null) {
            this.renderItem(pPoseStack, blockEntity.getItem(0), pBuffer, (float)blockEntity.getTicks() + partialTicks, blockEntity.getLevel(), blockEntity.getBlockPos());
         }

         this.renderGlassTransparency(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
         this.renderActiveButtons(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
      }

   }

   public void renderGlassTransparency(IncubatorBlockEntity blockEntity, PoseStack stack, MultiBufferSource bufferSource, int pPackedLight, int pPackedOverlay) {
      VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(GLASS));
      this.setModelScale(stack, blockEntity);
      this.getModel().renderToBuffer(stack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void renderActiveButtons(IncubatorBlockEntity blockEntity, PoseStack stack, MultiBufferSource bufferSource, int pPackedLight, int pPackedOverlay) {
      VertexConsumer vertexConsumer = bufferSource.getBuffer(blockEntity.isActive() ? RenderType.eyes(BUTTONS) : RenderType.entityCutout(BUTTONS));
      this.getModel().renderToBuffer(stack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void renderItem(PoseStack stack, ItemStack itemStack, MultiBufferSource source, float value, Level level, BlockPos pos) {
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      RandomSource random = RandomSource.create();
      float vibrationValue = random.nextFloat() * 0.02F;
      stack.pushPose();
      stack.translate((double)0.5F + (double)vibrationValue, (double)(0.5F + Mth.cos(value / 8.0F) / 10.0F), (double)0.5F + (double)vibrationValue);
      stack.scale(0.5F, 0.5F, 0.5F);
      stack.mulPose(Axis.YP.rotationDegrees(value));
      itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, this.getLight(level, pos), OverlayTexture.NO_OVERLAY, stack, source, level, 1);
      stack.popPose();
   }

   private int getLight(Level level, BlockPos pos) {
      int a = level.getBrightness(LightLayer.BLOCK, pos);
      int b = level.getBrightness(LightLayer.SKY, pos);
      return LightTexture.pack(a, b);
   }

   public void setModelScale(PoseStack pPoseStack, IncubatorBlockEntity block) {
      int e = block.getSide();
      this.setModelScale(pPoseStack, block, e);
   }
}
