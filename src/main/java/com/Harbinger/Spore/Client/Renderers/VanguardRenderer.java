package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.VanguardArrowLayerModel;
import com.Harbinger.Spore.Client.Models.VanguardModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Utility.Vanguard;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VanguardRenderer extends BaseInfectedRenderer<Vanguard> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/vanguard.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/vanguard.png");

   public VanguardRenderer(EntityRendererProvider.Context context) {
      super(context, new VanguardModel(context.bakeLayer(VanguardModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new VanguardCrossbowHold(this, context.getItemInHandRenderer()));
      this.addLayer(new VanguardArrowLayer(this));
   }

   public ResourceLocation getTextureLocation(Vanguard entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   private static class VanguardCrossbowHold extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Vanguard> {
      private final ItemInHandRenderer itemInHandRenderer;
      private static final ItemStack itemStack;
      private static final ItemStack FLINT;
      private static final ItemStack HORN;

      public VanguardCrossbowHold(RenderLayerParent renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
         super(renderLayerParent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Vanguard t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (!t.isInvisible()) {
            ItemStack stack = t.getItemInHand(InteractionHand.MAIN_HAND);
            poseStack.pushPose();

            for(ModelPart part : ((VanguardModel)this.getParentModel()).partList) {
               part.translateAndRotate(poseStack);
            }

            poseStack.translate(-0.15F, 0.4F, 0.1F);
            poseStack.scale(1.25F, 1.25F, 1.25F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            this.itemInHandRenderer.renderItem(t, stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
            ItemStack leftStack = t.getItemInHand(InteractionHand.OFF_HAND);
            poseStack.pushPose();

            for(ModelPart part : ((VanguardModel)this.getParentModel()).partList) {
               part.translateAndRotate(poseStack);
            }

            poseStack.translate(0.05F, 0.55F, 0.1F);
            poseStack.scale(1.25F, 1.25F, 1.25F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            this.itemInHandRenderer.renderItem(t, leftStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
            poseStack.pushPose();

            for(ModelPart part : ((VanguardModel)this.getParentModel()).pouchPartList) {
               part.translateAndRotate(poseStack);
            }

            poseStack.translate(-0.2, 0.35, 0.55);
            poseStack.scale(1.25F, 1.25F, 1.25F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            this.itemInHandRenderer.renderItem(t, itemStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
            poseStack.pushPose();

            for(ModelPart part : ((VanguardModel)this.getParentModel()).torsoPartList) {
               part.translateAndRotate(poseStack);
            }

            poseStack.scale(1.25F, 1.25F, 1.25F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.pushPose();
            poseStack.translate((double)0.0F, -0.1, (double)0.25F);
            this.itemInHandRenderer.renderItem(t, FLINT, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate((double)0.0F, -0.1, -0.35);
            this.itemInHandRenderer.renderItem(t, HORN, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
            poseStack.popPose();
         }
      }

      static {
         itemStack = new ItemStack(Items.FIREWORK_ROCKET);
         FLINT = new ItemStack(Items.FLINT_AND_STEEL);
         HORN = new ItemStack(Items.GOAT_HORN);
      }
   }

   private static class VanguardArrowLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Vanguard> {
      private final VanguardArrowLayerModel model = new VanguardArrowLayerModel();
      private static final ResourceLocation ARROW_LOCATION = new ResourceLocation("minecraft:textures/entity/projectiles/arrow.png");

      public VanguardArrowLayer(RenderLayerParent renderer) {
         super(renderer);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Vanguard t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (!t.isInvisible()) {
            VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(ARROW_LOCATION));
            this.model.renderToBuffer(poseStack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(t, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}
