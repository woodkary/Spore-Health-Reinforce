package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Layers.MepheticBottle;
import com.Harbinger.Spore.Client.Layers.SporeRenderTypes;
import com.Harbinger.Spore.Client.Models.MephiticModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Mephetic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MephticRenderer extends BaseInfectedRenderer<Mephetic> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/mephitic.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/mephitic.png");

   public MephticRenderer(EntityRendererProvider.Context context) {
      super(context, new MephiticModel(context.bakeLayer(MephiticModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new MepheticBottle(this));
      this.addLayer(new VolatileGlowingLayers(this));
      this.addLayer(new PotionLayer(this, context.getItemInHandRenderer()));
      this.addLayer(new HeldDrink(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Mephetic entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   static class VolatileGlowingLayers extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Mephetic> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/mephitic_glow.png");

      public VolatileGlowingLayers(RenderLayerParent p_117346_) {
         super(p_117346_);
      }

      public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Mephetic entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
         if (!entity.isInvisible()) {
            float alpha = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.1F);
            VertexConsumer vertexConsumer = buffer.getBuffer(SporeRenderTypes.glowingTranslucent(TEXTURE));
            ((MephiticModel)this.getParentModel()).renderToBuffer(matrixStack, vertexConsumer, packedLight, 15728640, 1.0F, 1.0F, 1.0F, alpha);
         }

      }
   }

   static class PotionLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Mephetic> {
      private final ItemInHandRenderer itemInHandRenderer;

      public PotionLayer(RenderLayerParent parent, ItemInHandRenderer itemInHandRenderer) {
         super(parent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Mephetic t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (!t.getPotions().isEmpty() && t.getPotions().size() >= 3) {
            this.renderItem(poseStack, t, multiBufferSource, i, (ItemStack)t.getPotions().get(0), -0.05F, 0.05F);
            this.renderItem(poseStack, t, multiBufferSource, i, (ItemStack)t.getPotions().get(1), 0.0F, 0.0F);
            this.renderItem(poseStack, t, multiBufferSource, i, (ItemStack)t.getPotions().get(2), 0.05F, -0.05F);
         }
      }

      private void renderItem(PoseStack poseStack, Mephetic entity, MultiBufferSource source, int light, ItemStack stack, float x, float y) {
         poseStack.pushPose();
         ((MephiticModel)this.getParentModel()).armParts.forEach((part) -> part.translateAndRotate(poseStack));
         poseStack.translate(-0.15 + (double)x, (double)(1.0F + y), (double)0.0F);
         poseStack.scale(0.5F, 0.5F, 0.5F);
         poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
         this.itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.FIXED, true, poseStack, source, light);
         poseStack.popPose();
      }
   }

   static class HeldDrink extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Mephetic> {
      private final ItemInHandRenderer itemInHandRenderer;

      public HeldDrink(RenderLayerParent parent, ItemInHandRenderer itemInHandRenderer) {
         super(parent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Mephetic t, float v, float v1, float v2, float v3, float v4, float v5) {
         this.renderItem(poseStack, t, multiBufferSource, i, t.getMainHandItem(), 0.0F);
         this.renderItem(poseStack, t, multiBufferSource, i, t.getOffhandItem(), 0.1F);
      }

      private void renderItem(PoseStack poseStack, Mephetic entity, MultiBufferSource source, int light, ItemStack stack, float offset) {
         poseStack.pushPose();
         ((MephiticModel)this.getParentModel()).OffarmParts.forEach((part) -> part.translateAndRotate(poseStack));
         poseStack.translate(0.2 + (double)offset, 1.1, (double)0.0F);
         poseStack.scale(0.5F, 0.5F, 0.5F);
         poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
         this.itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.FIXED, true, poseStack, source, light);
         poseStack.popPose();
      }
   }
}
