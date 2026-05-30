package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.hVindicatorModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.Hyper.Hvindicator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HyperVindicatorRenderer extends BaseInfectedRenderer<Hvindicator> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/hindicator.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/hindicator.png");

   public HyperVindicatorRenderer(EntityRendererProvider.Context context) {
      super(context, new hVindicatorModel(context.bakeLayer(hVindicatorModel.LAYER_LOCATION)), 0.5F);
      this.addLayer(new AxeLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Hvindicator entity) {
      return TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   static class AxeLayer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Hvindicator> {
      private final ItemInHandRenderer itemInHandRenderer;

      public AxeLayer(RenderLayerParent parent, ItemInHandRenderer itemInHandRenderer) {
         super(parent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Hvindicator t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (t.hasAxe()) {
            ItemStack stack = new ItemStack(Items.IRON_AXE);
            poseStack.pushPose();
            this.moveItemAround((hVindicatorModel)this.getParentModel(), poseStack);
            poseStack.translate(-0.15, 0.3, (double)-0.5F);
            poseStack.scale(1.4F, 1.4F, 1.4F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
            this.itemInHandRenderer.renderItem(t, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, true, poseStack, multiBufferSource, i);
            poseStack.popPose();
         }

      }

      void moveItemAround(hVindicatorModel model, PoseStack stack) {
         model.getHand().translateAndRotate(stack);
      }
   }
}
