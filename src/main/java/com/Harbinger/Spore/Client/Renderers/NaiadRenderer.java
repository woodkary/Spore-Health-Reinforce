package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.NaiadModel;
import com.Harbinger.Spore.Client.Models.NaiadTritonModel;
import com.Harbinger.Spore.Client.Models.TridentNaiadCharge;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Naiad;
import com.Harbinger.Spore.Sentities.Variants.NaiadVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NaiadRenderer extends BaseInfectedRenderer<Naiad> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel triton;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/naiad.png");
   private static final ResourceLocation NAIAD_TRITON = new ResourceLocation("spore", "textures/entity/naiad_triton.png");
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/naiad.png");

   public NaiadRenderer(EntityRendererProvider.Context context) {
      super(context, new NaiadModel(context.bakeLayer(NaiadModel.LAYER_LOCATION)), 1.0F);
      this.triton = new NaiadTritonModel(context.bakeLayer(NaiadTritonModel.LAYER_LOCATION));
      this.addLayer(new NaiadChargeRenderer(this));
   }

   protected void scale(Naiad type, PoseStack poseStack, float p_115316_) {
      float value = type.getVariant() == NaiadVariants.TRITON ? 1.2F : 1.0F;
      poseStack.scale(value, value, value);
      super.scale(type, poseStack, p_115316_);
   }

   public ResourceLocation getTextureLocation(Naiad entity) {
      return entity.getVariant() == NaiadVariants.TRITON ? NAIAD_TRITON : TEXTURE;
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   public void render(Naiad type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int light) {
      this.model = type.getVariant() == NaiadVariants.TRITON ? this.triton : this.defaultModel;
      super.render(type, value1, value2, stack, bufferSource, light);
   }

   public class NaiadChargeRenderer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Naiad> {
      private final TridentNaiadCharge model = new TridentNaiadCharge();
      public static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/entity/trident_riptide.png");

      public NaiadChargeRenderer(RenderLayerParent renderer) {
         super(renderer);
      }

      public void render(PoseStack stack, MultiBufferSource multiBufferSource, int value, Naiad type, float v1, float v2, float v3, float v4, float v5, float v6) {
         if (type.isCharging() && type.getVariant() == NaiadVariants.TRITON) {
            stack.pushPose();
            stack.translate((double)0.0F, (double)0.5F, (double)0.0F);
            VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            this.model.prepareMobModel(type, v1, v2, v3);
            this.model.setupAnim(type, v1, v2, v4, v5, v6);
            this.model.renderToBuffer(stack, vertexconsumer, value, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            stack.popPose();
         }

      }
   }
}
