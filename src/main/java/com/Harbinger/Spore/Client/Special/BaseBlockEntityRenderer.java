package com.Harbinger.Spore.Client.Special;

import com.Harbinger.Spore.SBlockEntities.AnimatedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public abstract class BaseBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>, BlockEntityRendererProvider<T> {
   protected final BlockEntityModel<T> model;

   protected BaseBlockEntityRenderer(BlockEntityModel<T> model) {
      this.model = model;
   }

   public abstract ResourceLocation getTexture(T var1);

   public BlockEntityModel<T> getModel() {
      return this.model;
   }

   public void render(@NotNull T blockEntity, float partialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      if (this.unRenderBlock(blockEntity)) {
         pPoseStack.pushPose();
         float f = (float)((AnimatedEntity)blockEntity).getTicks() + partialTicks;
         VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(this.getTexture(blockEntity)));
         this.setModelScale(pPoseStack, blockEntity);
         this.model.setupAnim(blockEntity, f);
         this.model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
         pPoseStack.popPose();
      }
   }

   protected boolean unRenderBlock(BlockEntity blockEntity) {
      Entity var3 = Minecraft.getInstance().cameraEntity;
      if (var3 instanceof Player player) {
         int x = blockEntity.getBlockPos().getX();
         int y = blockEntity.getBlockPos().getY();
         int z = blockEntity.getBlockPos().getZ();
         double d0 = player.distanceToSqr((double)x, (double)y, (double)z);
         return d0 < (double)256.0F;
      } else {
         return false;
      }
   }

   public void setModelScale(PoseStack pPoseStack, T block) {
      this.setModelScale(pPoseStack, block, 2);
   }

   public void setModelScale(PoseStack pPoseStack, T block, int value) {
      pPoseStack.translate((double)0.5F, (double)1.5F, (double)0.5F);
      pPoseStack.scale(0.99F, 0.99F, 0.99F);
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
      if (value == 2) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
      }

      if (value == 3) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      }

      if (value == 4) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
      }

      if (value == 5) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      }

   }

   public BlockEntityRenderer<T> create(Context context) {
      return this;
   }
}
