package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.VigilModel;
import com.Harbinger.Spore.Client.Models.VigilSignModel;
import com.Harbinger.Spore.Client.Models.ringerVigilModel;
import com.Harbinger.Spore.Sentities.Organoids.Vigil;
import com.Harbinger.Spore.Sentities.Variants.VigilVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
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
public class VigilRenderer extends OrganoidMobRenderer<Vigil> {
   private final EntityModel defaultModel = this.getModel();
   private final EntityModel alterModel;
   private final EntityModel ringerModel;
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore", "textures/entity/vigil.png");
   private static final ResourceLocation STALKER = new ResourceLocation("spore", "textures/entity/vigil_stalker.png");
   private static final ResourceLocation RINGER = new ResourceLocation("spore", "textures/entity/ringer_vigil.png");

   public VigilRenderer(EntityRendererProvider.Context context) {
      super(context, new VigilModel(context.bakeLayer(VigilModel.LAYER_LOCATION)), 1.0F);
      this.alterModel = new VigilSignModel(context.bakeLayer(VigilSignModel.LAYER_LOCATION));
      this.ringerModel = new ringerVigilModel(context.bakeLayer(ringerVigilModel.LAYER_LOCATION));
      this.addLayer(new SignModel(this, context.getItemInHandRenderer()));
   }

   public void render(Vigil type, float value1, float value2, PoseStack stack, MultiBufferSource bufferSource, int value3) {
      this.model = type.getVariant() == VigilVariants.TROLL ? this.alterModel : (type.getVariant() == VigilVariants.RINGER ? this.ringerModel : this.defaultModel);
      super.render(type, value1, value2, stack, bufferSource, value3);
   }

   public ResourceLocation getTextureLocation(Vigil entity) {
      return entity.isStalker() ? STALKER : (entity.getVariant() == VigilVariants.RINGER ? RINGER : TEXTURE);
   }

   protected void scale(Vigil type, PoseStack stack, float value) {
      if (type.isStalker()) {
         stack.scale(1.2F, 1.2F, 1.2F);
      }

      super.scale(type, stack, value);
   }

   private static class SignModel extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Vigil> {
      private final ItemInHandRenderer itemInHandRenderer;

      public SignModel(RenderLayerParent renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
         super(renderLayerParent);
         this.itemInHandRenderer = itemInHandRenderer;
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Vigil t, float v, float v1, float v2, float v3, float v4, float v5) {
         if (t.getVariant() == VigilVariants.TROLL) {
            EntityModel var12 = this.getParentModel();
            if (var12 instanceof VigilSignModel) {
               VigilSignModel signModel = (VigilSignModel)var12;
               ItemStack stack = new ItemStack(Items.OAK_SIGN);
               poseStack.pushPose();

               for(ModelPart part : signModel.getArms()) {
                  part.translateAndRotate(poseStack);
               }

               poseStack.translate(0.3F, 0.1F, -0.4F);
               poseStack.scale(2.25F, 2.25F, 2.25F);
               poseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
               poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
               this.itemInHandRenderer.renderItem(t, stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, i);
               poseStack.popPose();
            }
         }

      }
   }
}
