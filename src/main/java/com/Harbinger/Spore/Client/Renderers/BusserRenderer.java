package com.Harbinger.Spore.Client.Renderers;

import com.Harbinger.Spore.Client.Models.BusserModel;
import com.Harbinger.Spore.Client.Models.ExplodingBusserModel;
import com.Harbinger.Spore.Client.Models.RangedBusserModel;
import com.Harbinger.Spore.Client.Models.TransporterPhayresModel;
import com.Harbinger.Spore.Client.Special.BaseInfectedRenderer;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Busser;
import com.Harbinger.Spore.Sentities.Variants.BusserVariants;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BusserRenderer extends BaseInfectedRenderer<Busser> {
   private final EntityModel normalBusser = this.getModel();
   private final EntityModel explodingBusser;
   private final EntityModel toxic_busser;
   private final EntityModel carrier_busser;
   private static final ResourceLocation EYES_TEXTURE = new ResourceLocation("spore", "textures/entity/eyes/busser.png");
   public static final Map TEXTURE = (Map)Util.make(Maps.newEnumMap(BusserVariants.class), (p_114874_) -> {
      p_114874_.put(BusserVariants.DEFAULT, new ResourceLocation("spore", "textures/entity/busser.png"));
      p_114874_.put(BusserVariants.ENHANCED, new ResourceLocation("spore", "textures/entity/busser_aggressive.png"));
      p_114874_.put(BusserVariants.BOMBER, new ResourceLocation("spore", "textures/entity/busserbomber.png"));
      p_114874_.put(BusserVariants.TOXIC, new ResourceLocation("spore", "textures/entity/toxic_busser.png"));
      p_114874_.put(BusserVariants.TRANSPORTER, new ResourceLocation("spore", "textures/entity/transported_bussy.png"));
   });

   public BusserRenderer(EntityRendererProvider.Context context) {
      super(context, new BusserModel(context.bakeLayer(BusserModel.LAYER_LOCATION)), 0.5F);
      this.explodingBusser = new ExplodingBusserModel(context.bakeLayer(ExplodingBusserModel.LAYER_LOCATION));
      this.toxic_busser = new RangedBusserModel(context.bakeLayer(RangedBusserModel.LAYER_LOCATION));
      this.carrier_busser = new TransporterPhayresModel(context.bakeLayer(TransporterPhayresModel.LAYER_LOCATION));
      this.addLayer(new BusserBlockRenderer(this));
   }

   public ResourceLocation getTextureLocation(Busser entity) {
      return (ResourceLocation)TEXTURE.get(entity.getVariant());
   }

   public ResourceLocation eyeLayerTexture() {
      return EYES_TEXTURE;
   }

   protected void scale(Busser type, PoseStack stack, float value) {
      if (type.getVariant() == BusserVariants.ENHANCED) {
         float size = 1.4F;
         stack.scale(size, size, size);
      }

      super.scale(type, stack, value);
   }

   public void render(Busser busser, float p_115456_, float p_115457_, PoseStack stack, MultiBufferSource bufferSource, int p_115460_) {
      EntityModel<Busser> entityModel = this.normalBusser;
      if (busser.getVariant() == BusserVariants.BOMBER) {
         entityModel = this.explodingBusser;
      }

      if (busser.getVariant() == BusserVariants.TOXIC) {
         entityModel = this.toxic_busser;
      }

      if (busser.getVariant() == BusserVariants.TRANSPORTER) {
         entityModel = this.carrier_busser;
      }

      this.model = entityModel;
      super.render(busser, p_115456_, p_115457_, stack, bufferSource, p_115460_);
   }

   public static class BusserBlockRenderer extends com.Harbinger.Spore.Client.Layers.EntityRenderLayer<Busser> {
      private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

      public BusserBlockRenderer(RenderLayerParent layerParent) {
         super(layerParent);
      }

      private int getLight(Level level, BlockPos pos) {
         int a = level.getBrightness(LightLayer.BLOCK, pos);
         int b = level.getBrightness(LightLayer.SKY, pos);
         return LightTexture.pack(a, b);
      }

      public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int value, Busser t, float v, float v1, float v2, float v3, float v4, float v5) {
         BlockState state = t.getCarriedBlock();
         if (!t.isInvisible()) {
            EntityModel var13 = this.getParentModel();
            if (var13 instanceof TransporterPhayresModel) {
               TransporterPhayresModel transporterPhayresModel = (TransporterPhayresModel)var13;
               if (state != null) {
                  ItemStack itemStack = new ItemStack(state.getBlock().asItem());
                  poseStack.pushPose();

                  for(ModelPart part : transporterPhayresModel.tail) {
                     part.translateAndRotate(poseStack);
                  }

                  if (!itemStack.equals(ItemStack.EMPTY)) {
                     this.renderItem(poseStack, itemStack, multiBufferSource, (float)value, t.level(), t.blockPosition());
                  }

                  poseStack.popPose();
                  return;
               }
            }
         }

      }

      public void renderItem(PoseStack stack, ItemStack itemStack, MultiBufferSource source, float value, Level level, BlockPos pos) {
         stack.pushPose();
         stack.scale(1.25F, 1.25F, 1.25F);
         stack.translate((double)0.0F, (double)0.5F, (double)0.0F);
         stack.mulPose(Axis.YP.rotationDegrees(value));
         this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, this.getLight(level, pos), OverlayTexture.NO_OVERLAY, stack, source, level, 1);
         stack.popPose();
      }
   }
}
